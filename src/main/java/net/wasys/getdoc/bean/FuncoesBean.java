package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.InserirArquivoProcessosVO;
import net.wasys.getdoc.domain.vo.ResultadoDarknet;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.job.EnvioSisFiesProuniConcluidoParaAnaliseJob;
import net.wasys.util.DummyUtils;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.Hibernate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@ViewScoped
public class FuncoesBean extends AbstractBean {

	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private ImagemService imagemService;
	@Autowired private ResourceService resourceService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private TipificacaoDarknetService tipificacaoDarknetService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;
	@Autowired private FuncoesService funcoesService;
	@Autowired private DocumentoService documentoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private TipificacaoFluxoAprovacaoService tipificacaoFluxoAprovacaoService;
	@Autowired private OcrFluxoAprovacaoService ocrFluxoAprovacaoService;
	@Autowired private FluxoAprovacaoAnaliseService fluxoAprovacaoAnaliseService;
	@Autowired private EnvioSisFiesProuniConcluidoParaAnaliseJob envioSisFiesProuniConcluidoParaAnaliseJob;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private UsuarioService usuarioService;

	private int maxAmostragem;
	private Long processoId;
	private Long processoId1;
	private Long tipoProcessoId;
	private Date dataInicioRelatorioGeral;
	private Date dataFimRelatorioGeral;
	private Date dataInicioDigitalizacao;
	private Date dataFimDigitalizacao;
	private String url;
	private String header;
	private String body;
	private String proxy;
	private String method;
	private String release;
	private File fileAnexo;
	private List<ResultadoDarknet> resultadosDarknet;
	private Map<ModeloDocumento, List<TipoDocumento>> documentosCorrespondentesDarknet;
	private String email;
	private String tipoEnvioEmail = "";
	private String processoIds;
	private String numInscricao;
	private String numCandidato;
	private String numMatricula;
	private Long codOrigemDocumento;
	private String processosString;
	private Long processoIdTipificacao;
	private InserirArquivoProcessosVO inserirArquivoProcessosVO = new InserirArquivoProcessosVO();
	private String fileNameIncluir;
	protected TipoProcesso tipoProcesso;
	private List<Situacao> situacaoList = new ArrayList<>();
	private List<TipoProcesso> tiposProcessos;
	private Situacao novaSituacao;
	private String jsonEquivalenciasTiposDeDocumentos;
	private boolean excluirProcessosAposMigracao;
	protected TipoProcesso tipoProcessoDestinoMigracao;
	private String processosStringMigracao;
	private String imagensIdsStringBacalhau;
	private Date dataInicioNotificaSia;
	private Date dataFimNotificaSia;
	private LogLevel logLevel;
	private List<LogLevel> logLevels;
	private List<TipoProcesso> tipoProcessosImportacao;
	private TipoProcesso tipoProcessoImportacao;
	private int maxImportacao;
	private int inicioImportacao;
	private TipoCampoGrupo tipoGrupo;
	private List<TipoCampoGrupo> tipoGrupos;
	private List<TipoCampo> tipoCampoList;
	private TipoCampo tipoCampo;
	private File fileExcelVincularEmMassa;
	private String fileNameVincularEmMassa;
	private File fileExcelReimportacao;
	private String fileNameReimportacao;

	private String etapaFluxo;
	private List<String> etapasFluxo;
	private String processosAnalistaString;
	private Usuario novoAnalista;
	private List<Usuario> analistas;

	protected void initBean() {

		logLevel = DummyUtils.getLogLevel();
		logLevels = Arrays.asList(LogLevel.values());

		etapaFluxo = "";
		etapasFluxo = Arrays.asList("Tipificação", "OCR", "Análise");

		Usuario usuario = getUsuarioLogado();
		RoleGD role = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(role);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);
		tipoProcessosImportacao = Arrays.asList(tipoProcessoService.get(TipoProcesso.SIS_PROUNI), tipoProcessoService.get(TipoProcesso.SIS_FIES));

		situacaoList.clear();

		UsuarioFiltro usuarioFiltro = new UsuarioFiltro();
		usuarioFiltro.setStatus(StatusUsuario.ATIVO);
		usuarioFiltro.setRole(RoleGD.GD_ANALISTA);
		this.analistas = usuarioService.findByFiltro(usuarioFiltro);
	}

	public void amostragemImagens() {

		systraceThread("iniciando "+ maxAmostragem + " amostragem de Imagens" + DummyUtils.formatDateTime(new Date()));

		try {
			String amostragemPath = resourceService.getValue(ResourceService.AMOSTRAGEM_IMAGENS_PATH);
			List<TipoDocumento> tiposDocumento = tipoDocumentoService.findByTipoProcesso(null, null, null);

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				for (TipoDocumento tipoDocumento : tiposDocumento) {

					String tipoDocumentoNome = tipoDocumento.getNome();
					tipoDocumentoNome = DummyUtils.substituirCaracteresEspeciais(tipoDocumentoNome);
					tipoDocumentoNome = tipoDocumentoNome.replace(" ", "_");
					File amostragemDocumentoPath = new File(amostragemPath + File.separator + tipoDocumentoNome + File.separator + "images");
					if (!amostragemDocumentoPath.exists()) {
						amostragemDocumentoPath.mkdir();
					}
					File amostragemDocumentoAnnotarionsPath = new File(amostragemDocumentoPath.getParent(), "annotarions");
					if (!amostragemDocumentoAnnotarionsPath.exists()) {
						amostragemDocumentoAnnotarionsPath.mkdir();
					}

					int i = 0;
					List<Imagem> imagens = imagemService.findByTipoDocumentoToAmostragem(tipoDocumento, maxAmostragem);
					for (Imagem imagem : imagens) {

						File imagemFile = imagemService.getFile(imagem);
						String imagemFileName = imagemFile.getName();
						File destFile = new File(amostragemDocumentoPath, imagemFileName);

						FileUtils.copyFile(imagemFile, destFile);

						systraceThread(tipoDocumentoNome + " copiando " + imagemFile.getAbsolutePath() + " para " + destFile.getAbsolutePath() + " " + ++i + " de " + imagens.size());
					}
				}
			});
			tw.runNewThread();
			tw.throwException();

			systraceThread("finalizado amostragem Imagens " + DummyUtils.formatDateTime(new Date()));
		}
		catch (Exception e) {
			systraceThread("Erro ao executar amostragem Imagens "+ DummyUtils.formatDateTime(new Date()), LogLevel.ERROR);
			e.printStackTrace();
		}
	}

	public int getMaxAmostragem() {
		return maxAmostragem;
	}

	public void setMaxAmostragem(int maxAmostragem) {
		this.maxAmostragem = maxAmostragem;
	}

	public void checagemHttp() {

		try {
			List<BasicNameValuePair> parameters = new LinkedList<>();
			Map<String, String> mapHeader =  new HashMap<>();

			String[] headers = header.split(Pattern.quote(";"));
			if (StringUtils.isNotBlank(header)){
				for (String headerMap : headers){
					String[] headerValues = headerMap.split(":");
					mapHeader.put(headerValues[0].trim(),headerValues[1].trim());
				}
			}

			String bodyRaw = "";
			if (StringUtils.isNotBlank(body) && DummyUtils.isJson(body)){
				bodyRaw = body;
			}
			else {
				String[] params = body.split(Pattern.quote(";"));
				Map<String, String> mapBody = new HashMap<>();
				if(StringUtils.isNotBlank(body)){
					for (String bodyMap : params){
						String[] bodyValues =  bodyMap.split(":");
						mapBody.put(bodyValues[0].trim(), bodyValues[1].trim());
					}
				}

				for (ConcurrentHashMap.Entry<String, String> entry : mapBody.entrySet()){
					parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
			}

			RestClient restClient = new RestClient(url);
			restClient.setRepeatTimes(1);
			restClient.setHeaders(mapHeader);

			if (StringUtils.isNotBlank(proxy)){
				List<String> proxys = Arrays.asList(proxy.split(";"));
				String proxyHost = proxys.get(0).trim();
				Integer proxyPort = null;
				String proxyUser = null;
				String proxyPassword = null;
				if(proxys.size() > 1) {
					String proxyPortStr = proxys.get(1).trim();
					proxyPort = new Integer(proxyPortStr);
					if(proxys.size() > 2) {
						proxyUser = proxys.get(2).trim();
						proxyPassword = proxys.get(3).trim();
					}
				}

				ProxyManager proxyManager = new ProxyManager(proxyHost, proxyPort, proxyUser, proxyPassword);
				restClient.setProxyManager(proxyManager);
				systraceThread("proxyHost: " + proxyHost + " proxyPort: " + proxyPort + " proxyUser: " + proxyUser + " proxyPassword: " + proxyPassword);
			}

			if("GET".equals(method)) {
				this.release = restClient.execute(String.class);
			} else if("POST".equals(method) && StringUtils.isNotBlank(bodyRaw)) {
				this.release = restClient.execute(bodyRaw, String.class);
			} else {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
				this.release = restClient.execute(entity, String.class);
			}
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void atualizaCamposDinamicosRelatorioGeral() {

		try {
			List<Object[]> processos = processoService.findToRelatorioGeral(dataInicioRelatorioGeral, dataFimRelatorioGeral, tipoProcessoId);
			List<Long> processosIds = new ArrayList<>();
			for(Object[] processo : processos) {
				processosIds.add((Long)processo[0]);
			}

			relatorioGeralService.atualizarCampos(processosIds);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Atualizando Campos Dinâmicos Realtório Geral");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void atualizarRelatorioGeral() {

		try {
			funcoesService.atualizarRelatorioGeral(processosString);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Relatório Geral executado para o processo " +  processoId1);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void uploadAnexo(FileUploadEvent event) {

		try {
			UploadedFile uploadedFile = event.getFile();
			this.fileAnexo = DummyUtils.getFile("anexo-darknet-", uploadedFile);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void uploadAnexoIncluir(FileUploadEvent event) {

		try {
			UploadedFile uploadedFile = event.getFile();

			fileNameIncluir = uploadedFile.getFileName();
			inserirArquivoProcessosVO.setArquivo(DummyUtils.getFile("anexo-incluir-", uploadedFile));

			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void ajustarLogComEtapa() {

		try {
			funcoesService.ajustarLogComEtapa();

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void ajustarLogEPrazoLimiteSituacao() {

		try {
			funcoesService.ajustarLogEPrazoLimiteSituacao();

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void reprocessarRelatorioDeEtapa() {

		try {
			funcoesService.reprocessarRelatorioDeEtapa();

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void testDarknet() {

		try {
			resultadosDarknet = tipificacaoDarknetService.getTipificacaoWS(null, fileAnexo);

			if(resultadosDarknet.isEmpty()) {
				addFaceMessage(FacesMessage.SEVERITY_ERROR, "Documento não reconhecido.", null);
			}

			systraceThread(String.valueOf(resultadosDarknet));

			documentosCorrespondentesDarknet = new LinkedHashMap();
			for (ResultadoDarknet rd : resultadosDarknet) {
				String label = rd.getLabel();
				List<ModeloDocumento> modelosDocumentos = modeloDocumentoService.findByLabelDarknet(label);
				for (ModeloDocumento modeloDocumento : modelosDocumentos) {
					Long modeloDocumentoId = modeloDocumento.getId();
					List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByModeloDocumento(modeloDocumentoId);
					documentosCorrespondentesDarknet.put(modeloDocumento, tiposDocumentos);
				}
			}
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

    public void testeEnvioEmail() {

        try {
			Usuario usuarioLogado = getUsuarioLogado();

			if ("pendente".equals(tipoEnvioEmail)) {
                emailEnviadoService.enviarNotificacaoPendencia(processoId, email, usuarioLogado, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_PENDENCIA);
            }
            else if ("aprovada".equals(tipoEnvioEmail)) {
                emailEnviadoService.enviarNotificacaoAprovacao(processoId, email, usuarioLogado);
            }

            addFaceMessage(FacesMessage.SEVERITY_INFO, "Email enviado", null);
        }
        catch (Exception e) {
			addMessageError(e);
		}
    }

	public void atualizarProcessos() {

		Usuario usuarioLogado = getUsuarioLogado();

		String[] split = processoIds.split(",");
		List<Long> processoIds = new ArrayList<>();
		for(String processoId : split){
			Long processoIdLong = new Long(processoId.trim());
			processoIds.add(processoIdLong);
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				Processo processo = processoService.get(processoIdLong);
				String numCandidato = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_CANDIDATO);
				String numInscrito = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_INSCRICAO);
				AlunoFiltro filtro = new AlunoFiltro();
				filtro.setNumInscricao(numInscrito);
				filtro.setNumCandidato(numCandidato);
				consultaCandidatoService.criaOuAtualizaProcessoSia(usuarioLogado, filtro);

			});
			tw.runNewThread();
		}

		addFaceMessage(FacesMessage.SEVERITY_INFO, "Processos Atualizados!", null);
	}

	public void enviarParaAnalise(){

		String[] split = processoIds.split(",");
		List<Long> processoIds = new ArrayList<>();
		for(String processoId : split){
			Long processoIdLong = new Long(processoId.trim());
			processoIds.add(processoIdLong);
		}

		List<Processo> processos = processoService.findByIds(processoIds);

		processos.forEach(processo -> {
			try {
				processoService.enviarParaAnalise(processo, null);
			} catch (Exception e) {
				addMessageError(e);
			}
		});

		addFaceMessage(FacesMessage.SEVERITY_INFO, "Processos Atualizados!", null);
	}

	public void notificarDocumentoSia() {

		try {

			Usuario usuarioLogado = getUsuarioLogado();

			funcoesService.notificarDocumentoSia(numInscricao, numCandidato, numMatricula, codOrigemDocumento, usuarioLogado);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Documento notificado", null);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void executeTipificacao() {

		try {
			Processo processo = processoService.get(processoIdTipificacao);
			if(processo == null) {
				throw new RuntimeException("Não existe processo para este id: " + processoIdTipificacao);
			}

			MultiValueMap<Long, Long> docsIds = documentoService.findIdsToTipificacao2ByProcesso(processo);

			List<Long> ids = new ArrayList<>(docsIds.keySet());

			systraceThread("tipificando... " + ids.size() + ", " + ids);

			for (Long documentoId : ids) {
				List<Long> imagensIds = docsIds.get(documentoId);
				Documento documento = documentoService.get(documentoId);
				documentoService.tipificar2(documento, imagensIds);
			}

			addFaceMessage(FacesMessage.SEVERITY_INFO, ids.isEmpty() ? "Não foi encontrado documento para tipificacao" : " Tipificação finalizada.");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void executeTipificacaoPortalGraduacao() {
		long inicio = System.currentTimeMillis();

		int batchSize = 50;

		ExecutorService executor = Executors.newFixedThreadPool(5);
		try {

			DocumentoFiltro filtro = new DocumentoFiltro();

			filtro.setDataDigitalizacaoInicio(dataInicioDigitalizacao);
			filtro.setDataDigitalizacaoFim(dataFimDigitalizacao);

			MultiValueMap<Long, Long> docsIds = documentoService.findIdsToTipificacao3(filtro);
			List<Long> todosOsIds = new ArrayList<>(docsIds.keySet());

			if(todosOsIds.isEmpty()) {
				return;
			}

			systraceThread("Tipificando documentos obtidos ao executar a função de tipificação por intervalo de data com origem Portal Graduação: " + todosOsIds.size() );

			int batchAmount = (int) todosOsIds.size()/batchSize;

			systraceThread("Tipificando " + batchAmount + "lotes!" );
			int batchCount = 0;

			do {

				batchCount++;

				systraceThread("Tipificando lote:" + batchCount + "/" + batchAmount );


				List<Long> idsProcessar = DummyUtils.removeItens(todosOsIds, batchSize);
				if(idsProcessar.isEmpty()) break;

				List<Future> futures = new ArrayList<>();

				int docCount = 0;
				for (Long documentoId : idsProcessar) {

					docCount++;

					systraceThread("Tipificando documento :" + docCount + "/" +idsProcessar.size() + " do lote " + batchCount + "/" + batchAmount);

					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						funcoesService.tipificarDocumento(documentoId, docsIds);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);


			}	while(!todosOsIds.isEmpty());

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("job finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
			addMessageError(e);
		}
		finally {
			executor.shutdown();
		}
	}

	public void inserirArquivoNosDocumentos() {

		systraceThread("Iniciou execução");

		try {

			Usuario usuarioLogado = getUsuarioLogado();

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				funcoesService.inserirArquivoNosDocumentos(inserirArquivoProcessosVO, usuarioLogado);
			});
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void atualizarSituacaoEmMassa() {

		try {

			Usuario usuarioLogado = getUsuarioLogado();

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> funcoesService.alterarSituacaoEmMassa(processosString, novaSituacao, usuarioLogado, "Alteração Em Massa"));
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void reimportarEmMassa() {

		try {

			Usuario usuarioLogado = getUsuarioLogado();
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> funcoesService.reimportacaoEmMassa(tipoProcessoImportacao, usuarioLogado, maxImportacao, inicioImportacao));
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		}
		catch (Exception e) {
			addMessageError(e);
		} finally {
			systraceThread("FIM DA IMPORTACAO EM MASSA");
		}
	}

	public void selecionaTipoProcesso(ValueChangeEvent event) {

		Object newValue = event.getNewValue();
		if (newValue instanceof TipoProcesso) {
			tipoProcesso = (TipoProcesso) newValue;
			Long tipoProcessoId = tipoProcesso.getId();
			situacaoList = situacaoService.findByTipoProcesso(tipoProcessoId);
			situacaoList.forEach(s -> Hibernate.initialize(s.getTipoProcesso()));
		}
	}

	public void migrarTipoDeProcesso() {

		systraceThread("Iniciou execução");

		try {

			Usuario usuarioLogado = getUsuarioLogado();

			Map<String, String> tipoProcessoEquivalenteParaTipoProcesso = (Map<String, String>) DummyUtils.jsonStringToMap(jsonEquivalenciasTiposDeDocumentos);

			List<Long> processoIds = Arrays.stream(processosStringMigracao.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> funcoesService.migrarTipoDeProcesso(processoIds, tipoProcessoDestinoMigracao, excluirProcessosAposMigracao, usuarioLogado, tipoProcessoEquivalenteParaTipoProcesso));
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void redundanciaNotificacaoDocumentoSia() {

		systraceThread("Iniciou execução");

		try {

			Usuario usuarioLogado = getUsuarioLogado();

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				funcoesService.marcarParaNotificarDocumentosSia(dataInicioNotificaSia, dataFimNotificaSia, usuarioLogado);
			});
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void criarCamposEmMassa() {

		systraceThread("Iniciou execução");

		try {
			Usuario usuarioLogado = getUsuarioLogado();

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				funcoesService.criarCamposEmMassa(usuarioLogado, tipoProcesso, tipoGrupo, tipoCampo, processosString);
			});
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rodarBacalhauByImagem() {

		if(StringUtils.isBlank(imagensIdsStringBacalhau)) {
			return;
		}

		Timer timer = new Timer();
		try {
			List<Long> imagensIds = Arrays.stream(imagensIdsStringBacalhau.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());

			LogAcesso log = LogAcessoFilter.getLogAcesso();
			log.setServletPath("thd-rodarBacalhauByImagem");

			final LogAcesso logAcesso = log;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						logAcessoService.saveOrUpdateNewSession(logAcesso);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 5000, 5000);
			TransactionWrapper tw = new TransactionWrapper(applicationContext);

			tw.setRunnable(() -> {
					funcoesService.rodarBacalhauByImagem(imagensIds);
			});
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rodarBacalhauRecuperarImagensPerdidas() {
		try {
			funcoesService.rodarBacalhauRecuperarImagensPerdidas();
		} catch (Exception e) {
			e.printStackTrace();
		}

		addFaceMessage(FacesMessage.SEVERITY_INFO, "Job finalizado. Acompanhe os logs do sistema.");
	}

	public String getImagensIdsStringBacalhau() {
		return imagensIdsStringBacalhau;
	}

	public void setImagensIdsStringBacalhau(String imagensIdsStringBacalhau) {
		this.imagensIdsStringBacalhau = imagensIdsStringBacalhau;
	}

	public void findTipoGrupos() {
		Long tipoProcessoId = tipoProcesso.getId();
		tipoGrupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
	}

	public void findTipoCampos() {
		tipoCampoList = tipoCampoService.findByTipoCampoGrupo(tipoGrupo, false);
		int size = tipoCampoList.size();
	}

	public List<TipoCampoGrupo> getTipoGrupos() {
		return tipoGrupos;
	}

	public void setTipoGrupos(List<TipoCampoGrupo> tipoGrupos) {
		this.tipoGrupos = tipoGrupos;
	}

	public TipoCampoGrupo getTipoGrupo() {
		return tipoGrupo;
	}

	public void setTipoGrupo(TipoCampoGrupo tipoGrupo) {
		this.tipoGrupo = tipoGrupo;
	}

	public List<TipoCampo> getTipoCampoList() {
		return tipoCampoList;
	}

	public void setTipoCampoList(List<TipoCampo> tipoCampoList) {
		this.tipoCampoList = tipoCampoList;
	}

	public TipoCampo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(TipoCampo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}

	public void alterarLevelLog() {
		DummyUtils.setLogLevel(logLevel);
		addMessage("logLevelAlterado.sucesso");
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public Date getDataInicioRelatorioGeral() {
		return dataInicioRelatorioGeral;
	}

	public void setDataInicioRelatorioGeral(Date dataInicioRelatorioGeral) {
		this.dataInicioRelatorioGeral = dataInicioRelatorioGeral;
	}

	public TipoDocumentoService getTipoDocumentoService() {
		return tipoDocumentoService;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public Date getDataFimRelatorioGeral() {
		return dataFimRelatorioGeral;
	}

	public void setDataFimRelatorioGeral(Date dataFimRelatorioGeral) {
		this.dataFimRelatorioGeral = dataFimRelatorioGeral;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public File getFileAnexo() {
		return fileAnexo;
	}

	public List<ResultadoDarknet> getResultadosDarknet() {
		return resultadosDarknet;
	}

	public Map<ModeloDocumento, List<TipoDocumento>> getDocumentosCorrespondentesDarknet() {
		return documentosCorrespondentesDarknet;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTipoEnvioEmail() {
		return tipoEnvioEmail;
	}

	public void setTipoEnvioEmail(String tipoEnvioEmail) {
		this.tipoEnvioEmail = tipoEnvioEmail;
	}

	public String getProcessoIds() {
		return processoIds;
	}

	public void setProcessoIds(String processoIds) {
		this.processoIds = processoIds;
	}

	public String getNumInscricao() {
		return numInscricao;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
	}

	public String getNumCandidato() {
		return numCandidato;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}

	public String getNumMatricula() {
		return numMatricula;
	}

	public void setNumMatricula(String numMatricula) {
		this.numMatricula = numMatricula;
	}

	public Long getCodOrigemDocumento() {
		return codOrigemDocumento;
	}

	public void setCodOrigemDocumento(Long codOrigemDocumento) {
		this.codOrigemDocumento = codOrigemDocumento;
	}

	public Long getProcessoId1() {
		return processoId1;
	}

	public void setProcessoId1(Long processoId1) {
		this.processoId1 = processoId1;
	}

	public String getProcessosString() {
		return processosString;
	}

	public void setProcessosString(String processosString) {
		this.processosString = processosString;
	}

	public Long getProcessoIdTipificacao() {
		return processoIdTipificacao;
	}

	public void setProcessoIdTipificacao(Long processoIdTipificacao) {
		this.processoIdTipificacao = processoIdTipificacao;
	}

	public Date getDataInicioDigitalizacao() {
		return dataInicioDigitalizacao;
	}

	public void setDataInicioDigitalizacao(Date dataInicioDigitalizacao) {
		this.dataInicioDigitalizacao = dataInicioDigitalizacao;
	}

	public Date getDataFimDigitalizacao() {
		return dataFimDigitalizacao;
	}

	public void setDataFimDigitalizacao(Date dataFimDigitalizacao) {
		this.dataFimDigitalizacao = dataFimDigitalizacao;
	}

	public InserirArquivoProcessosVO getInserirArquivoProcessosVO() {
		return inserirArquivoProcessosVO;
	}

	public void setInserirArquivoProcessosVO(InserirArquivoProcessosVO inserirArquivoProcessosVO) {
		this.inserirArquivoProcessosVO = inserirArquivoProcessosVO;
	}

	public String getFileNameIncluir() {
		return fileNameIncluir;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

	public List<Situacao> getSituacaoList() {
		return situacaoList;
	}

	public void setSituacaoList(List<Situacao> situacaoList) {
		this.situacaoList = situacaoList;
	}

	public Situacao getNovaSituacao() {
		return novaSituacao;
	}

	public void setNovaSituacao(Situacao novaSituacao) {
		this.novaSituacao = novaSituacao;
	}

	public String getJsonEquivalenciasTiposDeDocumentos() {
		return jsonEquivalenciasTiposDeDocumentos;
	}

	public void setJsonEquivalenciasTiposDeDocumentos(String jsonEquivalenciasTiposDeDocumentos) {
		this.jsonEquivalenciasTiposDeDocumentos = jsonEquivalenciasTiposDeDocumentos;
	}

	public boolean isExcluirProcessosAposMigracao() {
		return excluirProcessosAposMigracao;
	}

	public void setExcluirProcessosAposMigracao(boolean excluirProcessosAposMigracao) {
		this.excluirProcessosAposMigracao = excluirProcessosAposMigracao;
	}

	public TipoProcesso getTipoProcessoDestinoMigracao() {
		return tipoProcessoDestinoMigracao;
	}

	public void setTipoProcessoDestinoMigracao(TipoProcesso tipoProcessoDestinoMigracao) {
		this.tipoProcessoDestinoMigracao = tipoProcessoDestinoMigracao;
	}

	public String getProcessosStringMigracao() {
		return processosStringMigracao;
	}

	public void setProcessosStringMigracao(String processosStringMigracao) {
		this.processosStringMigracao = processosStringMigracao;
	}

	public Date getDataInicioNotificaSia() {
		return dataInicioNotificaSia;
	}

	public void setDataInicioNotificaSia(Date dataInicioNotificaSia) {
		this.dataInicioNotificaSia = dataInicioNotificaSia;
	}

	public Date getDataFimNotificaSia() {
		return dataFimNotificaSia;
	}

	public void setDataFimNotificaSia(Date dataFimNotificaSia) {
		this.dataFimNotificaSia = dataFimNotificaSia;
	}

	public List<LogLevel> getLogLevels() {
		return logLevels;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	/*public static void main(String[] args) {

		ProcessadorArquivo processador = ProcessadorArquivoFactory.getProcessador(new File("C:\\Users\\Felipe\\Documents\\hashs-v2.csv"), null);
		List<LinhaVO> linhas = processador.processar();
		for (LinhaVO linha : linhas) {
			Map<String, String> colunaValor = linha.getColunaValor();
			String documentoId = colunaValor.get("documento_id");
			String hashOutroMesmoTipo = colunaValor.get("hash_outro_mesmo_tipo");
			String hashs = colunaValor.get("hashs");

			Set<String> hashsOutros = new HashSet<>(Arrays.asList(hashOutroMesmoTipo.split(",")));
			boolean contem = true;
			for (String hash : hashs.split(",")) {
				if(!hashsOutros.contains(hash)) {
					contem = false;
					break;
				}
			}

			System.out.println(documentoId + ";" + hashOutroMesmoTipo + ";" + hashs + ";" + contem);
		}
	}*/

	public List<TipoProcesso> getTipoProcessosImportacao() {
		return tipoProcessosImportacao;
	}

	public void setTipoProcessosImportacao(List<TipoProcesso> tipoProcessosImportacao) {
		this.tipoProcessosImportacao = tipoProcessosImportacao;
	}

	public TipoProcesso getTipoProcessoImportacao() {
		return tipoProcessoImportacao;
	}

	public void setTipoProcessoImportacao(TipoProcesso tipoProcessoImportacao) {
		this.tipoProcessoImportacao = tipoProcessoImportacao;
	}

	public String getEtapaFluxo() {
		return etapaFluxo;
	}

	public void setEtapaFluxo(String etapaFluxo) {
		this.etapaFluxo = etapaFluxo;
	}

	public List<String> getEtapasFluxo() {
		return etapasFluxo;
	}

	public void setEtapasFluxo(List<String> etapasFluxo) {
		this.etapasFluxo = etapasFluxo;
	}

	public int getMaxImportacao() {
		return maxImportacao;
	}

	public void setMaxImportacao(int maxImportacao) {
		this.maxImportacao = maxImportacao;
	}

	public int getInicioImportacao() {
		return inicioImportacao;
	}

	public void setInicioImportacao(int inicioImportacao) {
		this.inicioImportacao = inicioImportacao;
	}

	public void reprocessarFluxoAprovacao() {
		long inicio = System.currentTimeMillis();
		systraceThread("Iniciando Reprocessamento Fluxo Aprovação " + etapaFluxo);

		long THREAD_TIMEOUT = 2 * 60000;
		int QTD_THREADS = 2;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(QTD_THREADS);
		try {

			List<Long> processoIds = Arrays.stream(processosString.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
			int totalProcessos = processoIds.size();

			long timeoutTotal = (totalProcessos * THREAD_TIMEOUT) / QTD_THREADS;
			ExecutorTimeoutUtil executorTimeout = new ExecutorTimeoutUtil(executor, THREAD_TIMEOUT, timeoutTotal, QTD_THREADS);

			for (Long processoId : processoIds) {
				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					Processo processo = processoService.get(processoId);
					if (etapaFluxo.equalsIgnoreCase("Tipificação")) {
						tipificacaoFluxoAprovacaoService.tipificarDocumentosByProcesso(processo, true);
						return;
					}
					if (etapaFluxo.equalsIgnoreCase("OCR")) {
						ocrFluxoAprovacaoService.realizarOcrByProcesso(processo, true);
						return;
					}
					if (etapaFluxo.equalsIgnoreCase("Análise")) {
						fluxoAprovacaoAnaliseService.validarResultadoOCR(processo, true);
						return;
					}
				});
				executorTimeout.submit(tw);
				DummyUtils.sleep(152);
			}
			executorTimeout.esperarTerminarFuturesOuCancelar();
			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("Finalizado Reprocessamento Fluxo Aprovação " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
			addMessageError(e);
		}
		finally {
			executor.shutdown();
		}
	}

	public void executarJobSisProuni() {
		long inicio = System.currentTimeMillis();
		systraceThread("Iniciando JobSisProuni");

		try {
			envioSisFiesProuniConcluidoParaAnaliseJob.execute();
			systraceThread("Finalizado execução JobSisProuni");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void vincularEmMassa() {

		systraceThread("Iniciou execução");

		try {
			Usuario usuario = getUsuarioLogado();
			funcoesService.vincularEmMassa(usuario, fileExcelVincularEmMassa);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Sucesso.");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void uploadVincularEmMassa(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileNameVincularEmMassa = fileName;
			this.fileExcelVincularEmMassa = File.createTempFile(fileName, "." + extensao);
			DummyUtils.deleteOnExitFile(this.fileExcelVincularEmMassa);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, fileExcelVincularEmMassa);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public String getFileNameVincularEmMassa() {
		return fileNameVincularEmMassa;
	}

	public void setFileNameVincularEmMassa(String fileNameVincularEmMassa) {
		this.fileNameVincularEmMassa = fileNameVincularEmMassa;
	}

	public File getFileExcelVincularEmMassa() {
		return fileExcelVincularEmMassa;
	}

	public void setFileExcelVincularEmMassa(File fileExcelVincularEmMassa) {
		this.fileExcelVincularEmMassa = fileExcelVincularEmMassa;
	}

	public void uploadReimportacao(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileNameReimportacao = fileName;
			this.fileExcelReimportacao = File.createTempFile(fileName, "." + extensao);
			DummyUtils.deleteOnExitFile(this.fileExcelReimportacao);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, fileExcelReimportacao);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void reImportacao() {

		systraceThread("Iniciou execução");

		try {
			Usuario usuario = getUsuarioLogado();
			funcoesService.reImportar(usuario, fileExcelReimportacao);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Sucesso.");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public File getFileExcelReimportacao() {
		return fileExcelReimportacao;
	}

	public void setFileExcelReimportacao(File fileExcelReimportacao) {
		this.fileExcelReimportacao = fileExcelReimportacao;
	}

	public String getFileNameReimportacao() {
		return fileNameReimportacao;
	}

	public void setFileNameReimportacao(String fileNameReimportacao) {
		this.fileNameReimportacao = fileNameReimportacao;
	}

	public void validarImagensInexistentes() {
		funcoesService.validarImagensInexistentes();
	}

	public void atualizarAnalistaEmMassa() {
		try {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			Usuario usuario = getUsuarioLogado();
			List<Long> procesosIdList = new ArrayList<>();

			String[] processos = processosAnalistaString.split(",");
			for (String processo : processos) {
				procesosIdList.add(Long.parseLong(processo.trim()));
			}

			tw.setRunnable(() -> {
				DummyUtils.mudarNomeThread("thdfunc-atualizar-analista-em-massa");
				processoService.trocarAnalistas(procesosIdList, novoAnalista, usuario);
			});
			tw.startThread();

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job iniciado. Acompanhe os logs do sistema.");
		} catch (Exception e) {
			addMessageError(e);
		}

	}

	public String getProcessosAnalistaString() {
		return processosAnalistaString;
	}

	public void setProcessosAnalistaString(String processosAnalistaString) {
		this.processosAnalistaString = processosAnalistaString;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public void setAnalistas(List<Usuario> analistas) {
		this.analistas = analistas;
	}

	public Usuario getNovoAnalista() {
		return novoAnalista;
	}

	public void setNovoAnalista(Usuario novoAnalista) {
		this.novoAnalista = novoAnalista;
	}
}
