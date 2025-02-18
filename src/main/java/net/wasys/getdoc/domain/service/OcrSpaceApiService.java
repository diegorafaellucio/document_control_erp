package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsOcrSpaceVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;


@Service
public class OcrSpaceApiService {

	@Autowired private CampoService campoService;
	@Autowired private ImagemService imagemService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private ParametroService parametroService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ExceptionService exceptionService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ConsultaExternaService consultaExternaService;

	public void tratarImagens() {

		List<Long> imagensIds = imagemService.findIdsToFullText();

		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<Future> futures = new ArrayList<>();
		Map<Imagem, TransactionWrapper> workers = new LinkedHashMap<>();

		Timer timer = new Timer();
		try {
			AtomicInteger ok = new AtomicInteger(0);
			AtomicInteger erro = new AtomicInteger(0);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Thread thread = Thread.currentThread();
					int identityHashCode = System.identityHashCode(thread);
					thread.setName("thdtmr-fulltext-" + identityHashCode);
					int okInt = ok.get();
					int erroInt = erro.get();
					int processados = okInt + erroInt;
					systraceThread( DummyUtils.formatTime(new Date()) + " processadas " + processados + " de " + imagensIds.size() + " linhas. Ok: " + okInt + ", erros: " + erroInt + ".");
				}
			}, 0, 10000);

			for (Long imagemId : imagensIds) {

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					try {
						Imagem imagem = imagemService.get(imagemId);

						workers.put(imagem, tw);

						Thread thread = Thread.currentThread();
						int identityHashCode = System.identityHashCode(thread);
						thread.setName("thdtw-poolOcr-" + imagemId + "-" + identityHashCode);

						int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						boolean dev = DummyUtils.isDev();
						if (hora <= 6 && hora >= 20 && !dev) {
							return;
						}

						String fullText = getOcrSpaceFullText(imagemId);
						fullText = fullText != null ? fullText : "";
						OcrSpaceApiService ocrSpaceApiService = applicationContext.getBean(OcrSpaceApiService.class);
						ocrSpaceApiService.salvarImagem(imagem, fullText);

						ok.incrementAndGet();
					}
					catch (Exception e) {
						erro.incrementAndGet();
						throw e;
					}
				});
				Future<?> future = executor.submit(tw);
				futures.add(future);
				DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
			}

			long timeout = System.currentTimeMillis() + 20 * 60 * 1000;
			DummyUtils.checkTimeout(futures, timeout);
		}
		finally {
			timer.cancel();
			executor.shutdown();
		}

		StringBuilder exceptionsMessages = new StringBuilder();
		for (Imagem imagem : workers.keySet()) {
			TransactionWrapper tw = workers.get(imagem);
			Exception exception = tw.getException();
			if(exception == null) {
				continue;
			}

			String message = exceptionService.getMessage(exception);
			Long imagemId = imagem.getId();
			exceptionsMessages.append("ImagemId: ").append(imagemId).append(". ").append(message).append("\n");
			String stackTrace = ExceptionUtils.getStackTrace(exception);
			exceptionsMessages.append(stackTrace);
			exceptionsMessages.append("\n\n");
		}

		if(exceptionsMessages.length() > 0) {
			emailSmtpService.enviarEmailException("Erros excecutando conversão de imagem no OCRSpace: ", exceptionsMessages.toString(), "");
			throw new RuntimeException(exceptionsMessages.toString());
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarImagem(Imagem imagem, String fullText) {
		Long imagemId = imagem.getId();
		imagemMetaService.updateFullText(imagemId, fullText);
		imagem.setAguardandoFulltext(false);
		imagemService.saveOrUpdate(imagem);
	}

	public String getOcrSpaceFullText(Long imagemId) throws Exception {

		StopWatch sw = new StopWatch();
		sw.start();
		ConsultaExterna consultaExterna = new ConsultaExterna();
		String stackTrace = null;
		String mensagem = null;
		try {
			Map<String, String> parametrosMap = new LinkedHashMap<>();
			parametrosMap.put("imagemId", imagemId.toString());
			String parametrosJson = DummyUtils.objectToJson(parametrosMap);
			consultaExterna.setParametros(parametrosJson);

			ConfiguracoesWsOcrSpaceVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_OCRSPACE, ConfiguracoesWsOcrSpaceVO.class);
			String endPoint = configuracoesVO.getEndpoint();
			String apiKey = configuracoesVO.getApikey();
			String idioma = configuracoesVO.getIdioma();

			if (StringUtils.isBlank(idioma)) {
				idioma = "por";
			}

			if (StringUtils.isBlank(endPoint)) {
				throw new RuntimeException("O endpoint do OCRSpace é necessário.");
			}

			if (StringUtils.isBlank(apiKey)) {
				throw new RuntimeException("A apiKey do OCRSpace é necessária.");
			}

			Imagem imagem = imagemService.get(imagemId);
			String extensao = imagem.getExtensao();
			String fileName = imagemId + "." + extensao;
			File file = imagemService.getFile(imagem);
			if(!file.exists()){
				throw new RuntimeException("Arquivo da imagem não encontrado.");
			}

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost(endPoint);

			post.addHeader("apikey", apiKey);
			post.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addTextBody("language", idioma);
			builder.addTextBody("scale", "true");
			builder.addTextBody("detectOrientation", "true");
			builder.addTextBody("FileType", ".Auto");
			builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, fileName);

			HttpEntity body = builder.build();
			post.setEntity(body);

			CloseableHttpResponse response = client.execute(post);
			HttpEntity bodyEntity = response.getEntity();
			String fullText = EntityUtils.toString(bodyEntity, StandardCharsets.UTF_8);
			fullText = parseResults(fullText);
			return fullText;
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar Rest WebService: %s", mensagem);
			systraceThread(mensagem, LogLevel.ERROR);
			e.printStackTrace();

			stackTrace = DummyUtils.getStackTrace(e);

			throw e;
		}
		finally {
			consultaExterna.setTipo(TipoConsultaExterna.OCR_SPACE);
			consultaExterna.setStatus(StringUtils.isBlank(stackTrace) ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO);
			consultaExterna.setResultado(null);
			consultaExterna.setStackTrace(stackTrace);
			consultaExterna.setMensagem(mensagem);
			consultaExterna.setTempoExecucao(sw.getTime());

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				consultaExternaService.saveOrUpdate(consultaExterna);
			});
			tw.runNewThread();
		}
	}

	private String parseResults(String jsonObject) {
		String retorno = null;
		JSONObject object = new JSONObject(jsonObject);
		JSONArray parsedResults = object.getJSONArray("ParsedResults");
		JSONObject parsedResultsObject = parsedResults.getJSONObject(0);
		String texto = parsedResultsObject.getString("ParsedText");
		if(texto != null && !texto.isEmpty()) {
			retorno = texto;
		}
		return retorno;
	}


	private BigDecimal percentualDeAcerto(Long imagemId) {
		Double percent = null;

		String fullText = imagemMetaService.getFullText(imagemId);

		Imagem imagem = imagemService.get(imagemId);

		Processo processo = imagem.getDocumento().getProcesso();

		List<Campo> campos =  campoService.findByProcessoTipo(processo.getId(), TipoEntradaCampo.TEXTO.name());

		List<String> palavrasEsperadas = new ArrayList<>();

		Aluno aluno = processo.getAluno();
		palavrasEsperadas.add(aluno.getNome());
		palavrasEsperadas.add(aluno.getCpf());
		palavrasEsperadas.add(aluno.getIdentidade());
		palavrasEsperadas.add(aluno.getUfIdentidade().toString());
		palavrasEsperadas.add(aluno.getOrgaoEmissor());
		palavrasEsperadas.add(DummyUtils.format(aluno.getDataEmissao(), "dd/MM/yyyy"));
		palavrasEsperadas.add(aluno.getMae());

		for (Campo campo : campos) {
			palavrasEsperadas.add(campo.getValor());
		}

		int palavrasCount = 0;
		int palavrasOk = 0;
		for (String palavra : palavrasEsperadas) {
			if(StringUtils.isNotBlank(palavra)) {

				palavrasCount++;

				palavra = StringUtils.trim(palavra);
				palavra = StringUtils.upperCase(palavra);
				palavra = DummyUtils.substituirCaracteresEspeciais(palavra);

				boolean matches = fullText.matches(".*[^\\w]"  + palavra + "[^\\w].*");
				if(matches) {
					//DummyUtils.systraceThread("Encontrou " + palavra);
					palavrasOk++;
				}
			}
		}

		percent = (double) palavrasOk / (double) palavrasCount * 100;

		return BigDecimal.valueOf(percent);
	}
}
