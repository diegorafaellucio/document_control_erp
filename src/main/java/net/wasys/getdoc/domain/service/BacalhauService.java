package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoErroBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoFalhaBacalhau;
import net.wasys.getdoc.domain.repository.BacalhauRepository;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.rest.jackson.ObjectMapper;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class BacalhauService {

	@Autowired protected ApplicationContext applicationContext;
	@Autowired private BacalhauRepository bacalhauRepository;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ResourceService resourceService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ParametroService parametroService;
	@Autowired private ImagemService imagemService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	@Autowired private EmailRecebidoAnexoService emailRecebidoAnexoService;
	@Autowired private BacalhauImagemPerdidaService bacalhauImagemPerdidaService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private BacalhauReversoRelatorioService bacalhauReversoRelatorioService;

	public Bacalhau get(Long id) {
		return bacalhauRepository.get(id);
	}

	public int countByFiltro(BacalhauFiltro filtro) {
		return bacalhauRepository.countByFiltro(filtro);
	}

	public List<Bacalhau> findByFiltro(BacalhauFiltro filtro, int first, int pageSize) {
		return bacalhauRepository.findByFiltro(filtro, first, pageSize);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(Bacalhau bacalhau) throws MessageKeyException {
		try {
			bacalhauRepository.saveOrUpdate(bacalhau);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public void executarBacalhau(TipoExecucaoBacalhau tipo, Map<String, String> emails, Date dataInicio, Date dataFim, String prefixo) {

		try {

			File relatorio = getRelatorioFile(prefixo);
			BufferedWriter bw = getBufferedWriter(relatorio);

			final BacalhauVO bacalhauVO = new BacalhauVO();
			bacalhauVO.setDataInicio(dataInicio);
			bacalhauVO.setDataFim(dataFim);
			bacalhauVO.setTipoExecucao(tipo);

			Calendar cal = Calendar.getInstance();
			if (dataInicio.equals(dataFim) || dataInicio.after(dataFim)) {
				this.gerarRelatorio(dataInicio, dataInicio, bacalhauVO, bw);
			} else {
				do {
					tipo = TipoExecucaoBacalhau.PERIODO;
					this.gerarRelatorio(dataInicio, dataInicio, bacalhauVO, bw);
					cal.setTime(dataInicio);
					cal.add(Calendar.DATE, 1);
					dataInicio = cal.getTime();
				} while (dataInicio.before(dataFim) || dataInicio.equals(dataFim));
			}

			finalizarArquivoBacalhau(tipo, emails, prefixo, relatorio, bw, bacalhauVO);

		} catch (Exception e) {

			e.printStackTrace();
			emailSmtpService.enviarEmailException("BacalhauService.gerarRelatorio()", e);

			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	private void finalizarArquivoBacalhau(TipoExecucaoBacalhau tipo, Map<String, String> emails, String prefixo, File relatorio, BufferedWriter bw, BacalhauVO bacalhauVO) throws IOException {
		bw.flush();
		bw.close();
		bacalhauVO.setRelatorio(relatorio);

		final Bacalhau bacalhau = new Bacalhau();
		bacalhau.setData(bacalhauVO.getDataInicio());
		bacalhau.setTipoExecucao(tipo.toString());
		bacalhau.setTotalArquivos(bacalhauVO.getTotalArquivos());
		bacalhau.setTotalErros(bacalhauVO.getFerradas());

		List<Long> arquivosErros = new ArrayList<>();

		for(BacalhauArquivoVO temp : bacalhauVO.getArquivosErros()){
			arquivosErros.add(temp.getRegistroId());
		}

		bacalhau.setArquivosErro(objectToJson(arquivosErros));

		this.saveOrUpdate(bacalhau);

		String mode = DummyUtils.getMode();
		if (!"dev".equals(mode) && !tipo.equals(TipoExecucaoBacalhau.PERIODO)) {
			emailSmtpService.enviarBacalhau(bacalhauVO, emails, prefixo);
		}
	}

	private File getRelatorioFile(String prefixo) {
		String bacalhauDirStr = resourceService.getValue(ResourceService.BACALHAU_PATH);
		File bacalhauDir = new File(bacalhauDirStr);
		if (!bacalhauDir.exists()) {
			bacalhauDir.mkdir();
		}

		return new File(bacalhauDir, prefixo + "-relatorio.csv");
	}

	private BufferedWriter getBufferedWriter(File relatorio) throws IOException {

		String bacalhauDirStr = resourceService.getValue(ResourceService.BACALHAU_PATH);
		File bacalhauDir = new File(bacalhauDirStr);
		if (!bacalhauDir.exists()) {
			bacalhauDir.mkdir();
		}

		FileWriter fw = new FileWriter(relatorio);
		return new BufferedWriter(fw);
	}

	public BacalhauVO gerarRelatorio(Date dataInicio, Date dataFim, BacalhauVO bacalhauVO, BufferedWriter bw) throws IOException {

		dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
		Calendar dataFimC = Calendar.getInstance();
		dataFimC.setTime(dataFim);
		dataFimC.add(Calendar.DAY_OF_MONTH, 1);
		dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
		dataFimC.add(Calendar.MILLISECOND, -1);
		dataFim = dataFimC.getTime();

		verificarDocumentos(bw, bacalhauVO, dataInicio, dataFim);

		verificarAnexos(bw, bacalhauVO, dataInicio, dataFim);

		verificarAnexosEmails(bw, bacalhauVO, dataInicio, dataFim);

		return bacalhauVO;
	}

	private void verificarAnexosEmails(BufferedWriter bw, BacalhauVO bacalhauVO, Date dataInicio, Date dataFim) throws IOException {

		List<Long> ids = emailRecebidoAnexoService.findIdsByDataDigitalizacao(dataInicio, dataFim);

		systraceThread(String.valueOf(ids.size()));

		do {

			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 100 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<EmailRecebidoAnexo> anexos = emailRecebidoAnexoService.findByIds(ids2);
			for (final EmailRecebidoAnexo anexo : anexos) {

				BacalhauArquivoVO vo = new BacalhauArquivoEmailRecebidoAnexoVO(anexo) {
					@Override
					public String criaCaminho() {
						return emailRecebidoAnexoService.gerarPath(anexo);
					}
				};

				verificarArquivo(bw, bacalhauVO, vo);
			}

			bw.flush();
			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while (!ids.isEmpty());
	}

	private void verificarAnexos(BufferedWriter bw, BacalhauVO bacalhauVO, Date dataInicio, Date dataFim) throws IOException {

		List<Long> ids = processoLogAnexoService.findIdsByDataDigitalizacao(dataInicio, dataFim);

		systraceThread(String.valueOf(ids.size()));

		do {

			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 100 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<ProcessoLogAnexo> anexos = processoLogAnexoService.findByIds(ids2);
			for (final ProcessoLogAnexo anexo : anexos) {

				BacalhauArquivoVO vo = new BacalhauArquivoAnexoVO(anexo) {
					@Override
					public String criaCaminho() {
						return processoLogAnexoService.gerarPath(anexo);
					}
				};

				verificarArquivo(bw, bacalhauVO, vo);
			}

			bw.flush();
			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while (!ids.isEmpty());
	}

	private void verificarDocumentos(BufferedWriter bw, BacalhauVO bacalhauVO, Date dataInicio, Date dataFim) throws IOException {

		List<Long> ids = imagemService.findIdsByDataDigitalizacao(dataInicio, dataFim);

		systraceThread(String.valueOf(ids.size()));

		do {
			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 100 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<Imagem> imagens = imagemService.findByIds(ids2);
			for (final Imagem imagem : imagens) {

				BacalhauArquivoVO vo = new BacalhauArquivoImagemVO(imagem) {
					@Override
					public String criaCaminho() {
						return imagemService.gerarCaminho(imagem);
					}
				};

				imagemService.atualizarInfoImagem(imagem);

				verificarArquivo(bw, bacalhauVO, vo);
			}

			bw.flush();
			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while (!ids.isEmpty());
	}

	private void verificarArquivo(BufferedWriter bw, BacalhauVO bacalhauVO, BacalhauArquivoVO vo) throws IOException {

		String hashChecksum = vo.getHashChecksum();
		String caminho = vo.getCaminho();
		String caminhoOk = caminho;

		bacalhauVO.addTotalArquivos();

		boolean existsOnStorage = new File(caminho).exists();
		if (StringUtils.isBlank(caminho) || !existsOnStorage) {

			String pathCache = getPathCache(caminho);
			boolean existsOnCache = new File(pathCache).exists();

			gravarInformacoesIniciaisDoArquivo(bw, vo);

			TipoFalhaBacalhau tipoFalhaBacalhau = !existsOnStorage ? TipoFalhaBacalhau.NAO_ENCONTRADO : TipoFalhaBacalhau.ENCONTRADO;
			salvarFalha(bw, caminho, tipoFalhaBacalhau, false);

			tipoFalhaBacalhau = !existsOnCache ? TipoFalhaBacalhau.NAO_ENCONTRADO : TipoFalhaBacalhau.ENCONTRADO;
			salvarFalha(bw, pathCache, tipoFalhaBacalhau, true);

			String exceptionMessage = tentarRecuperaImagem(vo);

			if(StringUtils.isNotBlank(exceptionMessage)) {
				Long registroId = vo.getRegistroId();
				Imagem imagem = imagemService.get(registroId);

				TipoExecucaoBacalhau tipoExecucao = bacalhauVO.getTipoExecucao();
				salvarDadosImagemFerrada(vo, imagem, tipoExecucao, TipoErroBacalhau.INEXISTENTE);
			}

			gravarInformacoesImagemRecuperada(bw, exceptionMessage);

			bacalhauVO.addFerrada(vo);
			caminhoOk = null;
		}

		if (caminhoOk != null && StringUtils.isNotBlank(hashChecksum)) {

			String hashChecksum2 = DummyUtils.getHashChecksum(new File(caminhoOk));
			if (!hashChecksum.equals(hashChecksum2)) {

				salvarFalha(bw, caminho, TipoFalhaBacalhau.HASH_INVALIDO, false);
				bacalhauVO.addFerrada(vo);
			}
		}
	}

	private void gravarInformacoesImagemRecuperada(BufferedWriter bw, String exceptionMessage) throws IOException {
		bw.write(StringUtils.isBlank(exceptionMessage) ? "Sim" : exceptionMessage);
		bw.write("\n");
	}

	private String tentarRecuperaImagem(BacalhauArquivoVO vo) {

		String caminho = vo.getCaminho();
		if(StringUtils.isBlank(caminho)) {
			caminho = vo.criaCaminho();
		}

		String pathCache = getPathCache(caminho);
		boolean existsOnCache = new File(pathCache).exists();

		String exceptionMessage = "";
		try {
			boolean existsOnStorage = new File(caminho).exists();
			boolean cacheToStorage = existsOnCache && !existsOnStorage;

			if(cacheToStorage) {
				File fileCache = new File(pathCache);
				DummyUtils.copyFile(fileCache, new File(caminho));
			}
			else {
				exceptionMessage = "Arquivo não encontrado no Cache";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			exceptionMessage = DummyUtils.getExceptionMessage(e);
		}

		return exceptionMessage;
	}

	private void gravarInformacoesIniciaisDoArquivo(BufferedWriter bw, BacalhauArquivoVO vo) throws IOException {

		TipoArquivoBacalhau tipoArquivoBacalhau = vo.getTipoArquivo();
		Long registroId = vo.getRegistroId();
		Date dataRegistro = vo.getDataRegistro();

		bw.write(tipoArquivoBacalhau.name());
		bw.write(";");
		bw.write(registroId.toString());
		bw.write(";");
		bw.write(dataRegistro != null ? DummyUtils.formatDateTime(dataRegistro) : "");
		bw.write(";");
	}

	private String getPathCache(String caminho) {

		String cachePath = resourceService.getValue(ResourceService.CACHE_PATH);
		String[] pathSplit = caminho.split("imagens");

		int length = pathSplit.length;
		String imagem = pathSplit[length -1];
		String imgfiles = cachePath.concat("imgfiles").concat(imagem);

		return imgfiles;
	}

	private void salvarFalha(BufferedWriter bw, String caminho, TipoFalhaBacalhau tipoFalha, boolean pularColuna) throws IOException {

		if(pularColuna) bw.write(";");

		bw.write(caminho != null ? caminho : "");
		bw.write(";");
		bw.write(tipoFalha.name());
		bw.write(";");
	}

	public String objectToJson(Object obj) {
		if (obj == null) {
			return null;
		}
		ObjectMapper om = new ObjectMapper();
		String json = om.writeValueAsString(obj);
		return json;
	}

	public void executarBacalhauGeralByImagens(List<Long> imagemIds) {

		String agendamentoBacalhauGeral = parametroService.getValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL);
		JSONObject agendamentoBacalhauGeralJson = StringUtils.isBlank(agendamentoBacalhauGeral) ? agendarBacalhauGeral() : new JSONObject(agendamentoBacalhauGeral);
		Long ultimaImagemProcessadaId = agendamentoBacalhauGeralJson.getLong(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID);

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();

		AtomicInteger imagensOk = new AtomicInteger();
		AtomicInteger imagensPerdidas = new AtomicInteger();
		AtomicInteger imagensCorrompidas = new AtomicInteger();
		ExecutorService executor = Executors.newFixedThreadPool(10);

		DummyUtils.addParameter(logAcesso, "imagensParaVerificar", imagemIds.size());
		try {
			do {
				List<Long> list2 = DummyUtils.removeItens(imagemIds, 200);
				deletarRegistros(list2);

				List<Future> futures = new ArrayList<>();
				List<Imagem> imagemList = imagemService.findByIds(list2);
				for (Imagem imagem : imagemList) {
					ultimaImagemProcessadaId = imagem.getId();
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						TipoErroBacalhau tipoErro = verificarArquivoGeral(imagem);
						if(tipoErro == null) {
							imagensOk.incrementAndGet();
						} else if(TipoErroBacalhau.INEXISTENTE.equals(tipoErro)) {
							imagensPerdidas.incrementAndGet();
						} else if(TipoErroBacalhau.HASH_INVALIDO.equals(tipoErro)) {
							imagensCorrompidas.incrementAndGet();
						}
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(14);
				}
			} while (!imagemIds.isEmpty());
		} finally {
			executor.shutdown();
		}

		DummyUtils.addParameter(logAcesso, "imagensOk", imagensOk.get());
		DummyUtils.addParameter(logAcesso, "imagensPerdidas", imagensPerdidas.get());
		DummyUtils.addParameter(logAcesso, "imagensCorrompidas", imagensCorrompidas.get());

		agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID, ultimaImagemProcessadaId);
		agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.AGENDAMENTO_FINALIZADO, "false");
		parametroService.setValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL, agendamentoBacalhauGeralJson.toString());
	}

	public void executarBacalhauGeral() {

		String agendamentoBacalhauGeral = parametroService.getValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL);
		JSONObject agendamentoBacalhauGeralJson = StringUtils.isBlank(agendamentoBacalhauGeral) ? agendarBacalhauGeral() : new JSONObject(agendamentoBacalhauGeral);
		Long ultimaImagemProcessadaId = agendamentoBacalhauGeralJson.getLong(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID);

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(logAcesso, "ultimaImagemProcessadaInicioId", ultimaImagemProcessadaId);

		int qtdRegistros = 1000;
		List<Long> ids = imagemService.findToVerificarGeral(ultimaImagemProcessadaId, qtdRegistros);

		DummyUtils.addParameter(logAcesso, "imagensParaVerificar", ids.size());

		if(ids == null || ids.isEmpty()) return;

		AtomicInteger imagensOk = new AtomicInteger();
		AtomicInteger imagensPerdidas = new AtomicInteger();
		AtomicInteger imagensCorrompidas = new AtomicInteger();
		boolean processamentoFinalizado = false;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			int horaDia = 0;
			do {
				List<Long> list2 = DummyUtils.removeItens(ids, 200);

				deletarRegistros(list2);

				List<Future> futures = new ArrayList<>();
				List<Imagem> imagemList = imagemService.findByIds(list2);
				for (Imagem imagem : imagemList) {
					ultimaImagemProcessadaId = imagem.getId();
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						TipoErroBacalhau tipoErro = verificarArquivoGeral(imagem);
						if(tipoErro == null) {
							imagensOk.incrementAndGet();
						} else if(TipoErroBacalhau.INEXISTENTE.equals(tipoErro)) {
							imagensPerdidas.incrementAndGet();
						} else if(TipoErroBacalhau.HASH_INVALIDO.equals(tipoErro)) {
							imagensCorrompidas.incrementAndGet();
						}
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(14);
				}

				long timeout = System.currentTimeMillis() + 5 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);

				if(ids.isEmpty()) {
					ids = imagemService.findToVerificarGeral(ultimaImagemProcessadaId, qtdRegistros);
					if(ids.isEmpty()) {
						processamentoFinalizado = true;
					}

					recuperarImagensPerdidas();

					DummyUtils.sleep(200);
				}

				agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID, ultimaImagemProcessadaId);
				agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.AGENDAMENTO_FINALIZADO, processamentoFinalizado);
				parametroService.setValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL, agendamentoBacalhauGeralJson.toString());
				DummyUtils.addParameter(logAcesso, "ultimaImagemProcessadaFimId", ultimaImagemProcessadaId);
				DummyUtils.addParameter(logAcesso, "processamentoFinalizado", processamentoFinalizado);

				horaDia = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			}
			while(!ids.isEmpty() && horaDia <= 7);
		}
		finally {
			executor.shutdown();
		}

		DummyUtils.addParameter(logAcesso, "imagensOk", imagensOk.get());
		DummyUtils.addParameter(logAcesso, "imagensPerdidas", imagensPerdidas.get());
		DummyUtils.addParameter(logAcesso, "imagensCorrompidas", imagensCorrompidas.get());
	}

	private void deletarRegistros(List<Long> list2) {

		List<Long> bifIds = bacalhauImagemPerdidaService.findByImagemIdList(list2);
		boolean deletando = bifIds != null && !bifIds.isEmpty();

		if(!deletando) return;

		for (Long bifId : bifIds) {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				bacalhauImagemPerdidaService.deletarRegistroById(bifId);
			});
			tw.runNewThread();
		}
	}

	private TipoErroBacalhau verificarArquivoGeral(Imagem imagem) {

		BacalhauArquivoVO vo = new BacalhauArquivoImagemVO(imagem) {
			@Override
			public String criaCaminho() {
				return imagemService.gerarCaminho(imagem);
			}
		};

		String caminho = vo.getCaminho();
		File file = new File(caminho);
		boolean exists = file.exists();

		if(!exists) {
			salvarDadosImagemFerrada(vo, imagem, TipoExecucaoBacalhau.AGENDADO, TipoErroBacalhau.INEXISTENTE);
			return TipoErroBacalhau.INEXISTENTE;
		}
		else {
			String hashChecksum = imagem.getHashChecksum();
			String hash2 = DummyUtils.getHashChecksum(file);
			if(StringUtils.isNotBlank(hashChecksum) && !hashChecksum.equals(hash2)) {
				salvarDadosImagemFerrada(vo, imagem, TipoExecucaoBacalhau.AGENDADO, TipoErroBacalhau.HASH_INVALIDO);
				return TipoErroBacalhau.HASH_INVALIDO;
			}

			Boolean existente = imagem.isExistente();
			if(!existente) {
				imagem.setExistente(true);
				imagemService.saveOrUpdate(imagem);
			}
		}

		return null;
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarDadosImagemFerrada(BacalhauArquivoVO vo, Imagem imagem, TipoExecucaoBacalhau tipoExecucaoBacalhau, TipoErroBacalhau tipoErroBacalhau) {

		Long registroId = vo.getRegistroId();
		deletarRegistros(Arrays.asList(registroId));

		TipoArquivoBacalhau tipoArquivo = vo.getTipoArquivo();

		BacalhauImagemPerdida bacalhauImagemPerdida = new BacalhauImagemPerdida();
		bacalhauImagemPerdida.setImagem(imagem);
		bacalhauImagemPerdida.setTipoArquivo(tipoArquivo);
		bacalhauImagemPerdida.setTipoExecucao(tipoExecucaoBacalhau);
		bacalhauImagemPerdida.setData(new Date());
		bacalhauImagemPerdida.setTipoErro(tipoErroBacalhau);

		bacalhauImagemPerdidaService.saveOrUpdate(bacalhauImagemPerdida);
	}

	@Transactional(rollbackFor = Exception.class)
	public JSONObject agendarBacalhauGeral() {

		Bacalhau bacalhau = new Bacalhau();
		bacalhau.setData(new Date());
		bacalhau.setTipoExecucao(TipoExecucaoBacalhau.AGENDADO.name());
		bacalhau.setTotalArquivos(0);
		bacalhau.setTotalErros(0);

		this.saveOrUpdate(bacalhau);

		String agendamentoBacalhauGeral = parametroService.getValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL);
		Long ultimaImagemProcessadaId = imagemService.getFirstRegistro();

		JSONObject agendamentoBacalhauGeralJson = StringUtils.isNotBlank(agendamentoBacalhauGeral) ? new JSONObject(agendamentoBacalhauGeral) :  new JSONObject();
		agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID, ultimaImagemProcessadaId);
		agendamentoBacalhauGeralJson.put(BacalhauImagemPerdida.AGENDAMENTO_FINALIZADO, false);

		parametroService.setValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL, agendamentoBacalhauGeralJson.toString());

		return agendamentoBacalhauGeralJson;
	}

	public void recuperarImagensPerdidas() {

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		AtomicInteger imagensRecuperadas = new AtomicInteger();

		BacalhauFiltro filtro = new BacalhauFiltro();
		filtro.setAhRecuperar(true);
		int count = bacalhauImagemPerdidaService.countByFiltro(filtro);
		if(count == 0) {
			DummyUtils.addParameter(logAcesso, "imagensRecuperadas", imagensRecuperadas.get());
			return;
		}

		List<BacalhauImagemPerdida> imagensPerdidas = bacalhauImagemPerdidaService.findByFiltro(filtro, null, null);

		do {
			List<BacalhauImagemPerdida> list = DummyUtils.removeItens(imagensPerdidas, 100);

			for (BacalhauImagemPerdida bacalhauImagemPerdida : list) {
				Imagem imagem = bacalhauImagemPerdida.getImagem();

				BacalhauArquivoVO vo = new BacalhauArquivoImagemVO(imagem) {
					@Override
					public String criaCaminho() {
						return imagemService.gerarCaminho(imagem);
					}
				};

				String erro = tentarRecuperaImagem(vo);

				String caminho = vo.getCaminho();
				if(StringUtils.isBlank(caminho)) {
					caminho = vo.criaCaminho();
				}
				String pathCache = getPathCache(caminho);
				boolean imagemRecuperada = StringUtils.isBlank(erro) ? true : false;

				bacalhauImagemPerdida.setCaminhoCache(pathCache);
				bacalhauImagemPerdida.setRecuperadaDoCache(imagemRecuperada);
				bacalhauImagemPerdida.setErro(erro);

				bacalhauImagemPerdidaService.saveOrUpdate(bacalhauImagemPerdida);

				if(imagemRecuperada) {
					imagensRecuperadas.incrementAndGet();
					imagem.setExistente(true);
					imagemService.saveOrUpdate(imagem);
				}
			}

			DummyUtils.sleep(1000);

		}
		while (!imagensPerdidas.isEmpty());

		DummyUtils.addParameter(logAcesso, "imagensRecuperadas", imagensRecuperadas.get());
	}

	public Map<String, Double> carregarPercentualStatusBacalhauGeral(Map<String, Integer> statusBacalhauGeralMap) {

		Integer checked = statusBacalhauGeralMap.get("checked");
		Integer faltam = statusBacalhauGeralMap.get("faltam");
		Integer totalImagensPerdidas = statusBacalhauGeralMap.get("imagensPerdidas");
		Integer totalImagensRecuperadas = statusBacalhauGeralMap.get("imagensRecuperadas");
		Integer imagensNaoRecuperadas = statusBacalhauGeralMap.get("imagensNaoRecuperadas");
		Integer totalImagens = (faltam + checked);

		double faltamPercent = totalImagens > 0 ? ((double) faltam / totalImagens) * 100 : 0;
		double finalizouPercent = totalImagens > 0 ? ((double) checked / totalImagens) * 100 : 0;
		double imagensPerdidasPercent = totalImagens > 0 ? ((double) totalImagensPerdidas / totalImagens) * 100 : 0;
		double imagensRecuperadasPercent = totalImagensPerdidas > 0 ? ((double) totalImagensRecuperadas / totalImagensPerdidas) * 100 : 0;
		double imagensNaoRecuperadasPercent = totalImagens > 0 ? ((double) imagensNaoRecuperadas / totalImagens * 100) : 0;
		double imagensNaoRecuperadas2Percent = totalImagensPerdidas > 0 ? ((double) imagensNaoRecuperadas / totalImagensPerdidas * 100) : 0;

		BigDecimal faltamPercentBd = BigDecimal.valueOf(faltamPercent);
		faltamPercentBd = faltamPercentBd.setScale(2, RoundingMode.HALF_UP);
		faltamPercent = faltamPercentBd.doubleValue();

		BigDecimal finalizouPercentBd = BigDecimal.valueOf(finalizouPercent);
		finalizouPercentBd = finalizouPercentBd.setScale(2, RoundingMode.HALF_UP);
		finalizouPercent = finalizouPercentBd.doubleValue();

		BigDecimal fimagensPerdidasPercentBd = BigDecimal.valueOf(imagensPerdidasPercent);
		fimagensPerdidasPercentBd = fimagensPerdidasPercentBd.setScale(5, RoundingMode.HALF_UP);
		imagensPerdidasPercent = fimagensPerdidasPercentBd.doubleValue();

		BigDecimal imagensRecuperadasPercentBd = BigDecimal.valueOf(imagensRecuperadasPercent);
		imagensRecuperadasPercentBd = imagensRecuperadasPercentBd.setScale(2, RoundingMode.HALF_UP);
		imagensRecuperadasPercent = imagensRecuperadasPercentBd.doubleValue();

		BigDecimal imagensNaoRecuperadasBd = BigDecimal.valueOf(imagensNaoRecuperadasPercent);
		imagensNaoRecuperadasBd = imagensNaoRecuperadasBd.setScale(5, RoundingMode.HALF_UP);
		imagensNaoRecuperadasPercent = imagensNaoRecuperadasBd.doubleValue();

		BigDecimal imagensNaoRecuperadas2PercentBd = BigDecimal.valueOf(imagensNaoRecuperadas2Percent);
		imagensNaoRecuperadas2PercentBd = imagensNaoRecuperadas2PercentBd.setScale(2, RoundingMode.HALF_UP);
		imagensNaoRecuperadas2Percent = imagensNaoRecuperadas2PercentBd.doubleValue();

		Map<String, Double> statusBacalhauGeralPercent = new LinkedHashMap<>();
		statusBacalhauGeralPercent.put("faltamPercent", faltamPercent);
		statusBacalhauGeralPercent.put("finalizouPercent", finalizouPercent);
		statusBacalhauGeralPercent.put("imagensPerdidasPercent", imagensPerdidasPercent);
		statusBacalhauGeralPercent.put("imagensRecuperadasPercent", imagensRecuperadasPercent);
		statusBacalhauGeralPercent.put("imagensNaoRecuperadasPercent", imagensNaoRecuperadasPercent);
		statusBacalhauGeralPercent.put("imagensNaoRecuperadas2Percent", imagensNaoRecuperadas2Percent);

		return statusBacalhauGeralPercent;
	}

	public void executarBacalhauReverso() throws Exception {

		BacalhauReversoVO vo = new BacalhauReversoVO();
		vo.count = new AtomicInteger();
		vo.dataExecucao = new Date();
		vo.anoContinuacao = null;
		vo.mesContinuacao = null;
		vo.diaContinuacao = null;
		vo.agrupadorContinuacao = null;

		String parametrosJob = parametroService.getValor(ParametroService.P.BACALHAU_REVERSO);
		if(StringUtils.isNotBlank(parametrosJob)) {
			String[] split = parametrosJob.split(";");
			vo.dataExecucao = DummyUtils.parseDateTime3(split[0]);
			vo.anoContinuacao = split[1];
			vo.mesContinuacao = split[2];
			vo.diaContinuacao = split[3];
			vo.agrupadorContinuacao = split[4];
		}

		String imagensPath = resourceService.getValue(ResourceService.IMAGENS_PATH);
		File imagensDir = new File(imagensPath);

		Timer timer = new Timer();

		try {
			LogAcesso log = LogAcessoFilter.getLogAcesso();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					logAcessoService.saveOrUpdateNewSession(log);
				}
			}, 5000, 5000);

			File[] anosDirs = imagensDir.listFiles();
			if(anosDirs != null) {
				List<File> anosDirsList = Arrays.asList(anosDirs);
				Collections.sort(anosDirsList, BacalhauService::comparaArquivosPorNome);
				for (File anoDir : anosDirsList) {
					if(!isContinuarAno(vo, anoDir)) {
						continue;
					}
					vo.anoDir = anoDir;
					executarBacalhauReversoAno(vo);
				}
			}

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				parametroService.setValor(ParametroService.P.BACALHAU_REVERSO, null);
			});
			tw.runNewThread();
			tw.throwException();
		}
		finally {
			timer.cancel();
		}
	}

	private void executarBacalhauReversoAno(BacalhauReversoVO vo) throws Exception {

		if(vo.anoDir.isDirectory()) {
			vo.builder = new BacalhauReversoRelatorioService.BacalhauReversoRelatorioBuilder();

			LogAcesso log = LogAcessoFilter.getLogAcesso();
			DummyUtils.addParameter(log, "anoDir", vo.anoDir.getName());

			File[] mesesDirs = vo.anoDir.listFiles();
			if(mesesDirs != null) {
				List<File> mesesDirsList = Arrays.asList(mesesDirs);
				Collections.sort(mesesDirsList, BacalhauService::comparaArquivosPorNome);
				for (File mesDir : mesesDirsList) {
					if(!isCcontinuarMes(vo, mesDir)) {
						continue;
					}
					vo.builder.addDir(vo.anoDir.getName() + "/" + mesDir.getName());

					vo.mesDir = mesDir;
					executarBacalhauReversoMes(vo);
				}
			}

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				bacalhauReversoRelatorioService.registrar(vo.dataExecucao, vo.builder);
			});
			tw.runNewThread();
			tw.throwException();
		}
	}

	private void executarBacalhauReversoMes(BacalhauReversoVO vo) throws Exception {

		if(vo.mesDir.isDirectory()) {
			LogAcesso log = LogAcessoFilter.getLogAcesso();
			DummyUtils.addParameter(log, "mesDir", vo.mesDir.getName());

			File[] diasDirs = vo.mesDir.listFiles();
			if(diasDirs != null) {
				List<File> diasDirsList = Arrays.asList(diasDirs);
				Collections.sort(diasDirsList, BacalhauService::comparaArquivosPorNome);
				for (File diaDir : diasDirsList) {
					if (!isCcontinuarDia(vo, diaDir)) {
						continue;
					}

					vo.diaDir = diaDir;
					executarBacalhauReversoDia(vo);
				}
			}
		}
	}

	private void executarBacalhauReversoDia(BacalhauReversoVO vo) throws Exception {

		if(vo.diaDir.isDirectory()) {
			LogAcesso log = LogAcessoFilter.getLogAcesso();
			DummyUtils.addParameter(log, "diaDir", vo.diaDir.getName());

			File[] agrupadoresDirs = vo.diaDir.listFiles();
			if(agrupadoresDirs != null) {
				List<File> agrupadoresDirsList = Arrays.asList(agrupadoresDirs);
				Collections.sort(agrupadoresDirsList, BacalhauService::comparaArquivosPorNome);
				for (File agrupadorDir : agrupadoresDirsList) {
					if(!isCcontinuarAgrupador(vo, agrupadorDir)) {
						continue;
					}
					vo.agrupadorDir = agrupadorDir;
					executarBacalhauReversoAgrupador(vo);

					registrarContinuacao(vo.dataExecucao, agrupadorDir);
				}
			}
		}
	}

	private void executarBacalhauReversoAgrupador(BacalhauReversoVO vo) throws IOException {

		LogAcesso log = LogAcessoFilter.getLogAcesso();

		if(vo.agrupadorDir.isDirectory()) {
			DummyUtils.addParameter(log, "agrupadorDir", vo.agrupadorDir.getName());

			File[] imagensFiles = vo.agrupadorDir.listFiles();
			if(imagensFiles != null) {
				List<File> imagensFilesList = Arrays.asList(imagensFiles);
				Collections.sort(imagensFilesList, BacalhauService::comparaArquivosPorNome);
				for (File imagemFile : imagensFilesList) {
					Integer result = executarBacalhauReversoImagem(vo.builder, imagemFile);
					if(result != null) {
						if(result.intValue() == 1) {
							vo.count.incrementAndGet();
						} else {
							vo.countInexistente.incrementAndGet();
						}
					}
				}
			}
		}

		DummyUtils.addParameter(log, "arquivosOk", vo.count.get());
		DummyUtils.addParameter(log, "arquivosInexistentes", vo.countInexistente.get());
	}

	private Integer executarBacalhauReversoImagem(BacalhauReversoRelatorioService.BacalhauReversoRelatorioBuilder builder, File imagemFile) throws IOException {

		if(imagemFile.isFile() && imagemFile.exists()) {

			try {
				String fileName = imagemFile.getName();
				int indexUnderline = fileName.indexOf("_");
				int indexDot = fileName.indexOf(".");
				String imagemIdStr = fileName.substring(indexUnderline + 1, indexDot);
				Long imagemId = StringUtils.isNumeric(imagemIdStr) ? new Long(imagemIdStr) : null;

				boolean exists = imagemId != null ? imagemService.exists(imagemId) : false;
				if(exists) {
					bacalhauReversoRelatorioService.registrarEstatisticas(builder, imagemFile, true);
					return 1;
				}
				else {
					bacalhauReversoRelatorioService.registrarEstatisticas(builder, imagemFile, false);

					String imagemDirPath = resourceService.getValue(ResourceService.IMAGENS_PATH);
					String destDirPath = resourceService.getValue(ResourceService.BACALHAU_REVERSO_PATH);

					String imagemPath = imagemFile.getAbsolutePath();
					String imagemDestPath = imagemPath.replace(imagemDirPath, destDirPath);

					File destFile = new File(imagemDestPath);
					if(destFile.exists()) {
						destFile.delete();
					}
					FileUtils.moveFile(imagemFile, destFile);
					return 0;
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Erro ao analisar " + imagemFile.getAbsolutePath(), e);
			}
		}

		return null;
	}

	private boolean isCcontinuarAgrupador(BacalhauReversoVO vo, File agrupadorDir) {

		String diaPath = agrupadorDir.getParent();
		File diaDir = new File(diaPath);

		String mesPath = diaDir.getParent();
		File mesDir = new File(mesPath);

		String anoPath = mesDir.getParent();
		File anoDir = new File(anoPath);
		String ano = anoDir.getName();
		if(!ano.equals(vo.anoContinuacao)) {
			return true;
		}

		String mes = mesDir.getName();
		if(!mes.equals(vo.mesContinuacao)) {
			return true;
		}

		String dia = diaDir.getName();
		if(!dia.equals(vo.diaContinuacao)) {
			return true;
		}

		int agrupadorContinuacao = Integer.parseInt(vo.agrupadorContinuacao);
		String agrupadorAtualStr = agrupadorDir.getName();
		int agrupadorAtual = Integer.parseInt(agrupadorAtualStr);
		return agrupadorAtual >= agrupadorContinuacao;
	}

	private boolean isCcontinuarDia(BacalhauReversoVO vo, File diaDir) {

		String mesPath = diaDir.getParent();
		File mesDir = new File(mesPath);

		String anoPath = mesDir.getParent();
		File anoDir = new File(anoPath);
		String ano = anoDir.getName();
		if(!ano.equals(vo.anoContinuacao)) {
			return true;
		}

		String mes = mesDir.getName();
		if(!mes.equals(vo.mesContinuacao)) {
			return true;
		}

		int diaContinuacao = Integer.parseInt(vo.diaContinuacao);
		String diaAtualStr = diaDir.getName();
		int diaAtual = Integer.parseInt(diaAtualStr);
		return diaAtual >= diaContinuacao;
	}

	private boolean isCcontinuarMes(BacalhauReversoVO vo, File mesDir) {

		String anoPath = mesDir.getParent();
		File anoDir = new File(anoPath);
		String ano = anoDir.getName();
		if(!ano.equals(vo.anoContinuacao)) {
			return true;
		}

		int mesContinuacao = Integer.parseInt(vo.mesContinuacao);
		String mesAtualStr = mesDir.getName();
		int mesAtual = Integer.parseInt(mesAtualStr);
		return mesAtual >= mesContinuacao;
	}

	private boolean isContinuarAno(BacalhauReversoVO vo, File atualDir) {
		if(StringUtils.isBlank(vo.anoContinuacao)) {
			return true;
		}
		int continuacao2 = Integer.parseInt(vo.anoContinuacao);
		String atual = atualDir.getName();
		int atualInt = Integer.parseInt(atual);
		return atualInt >= continuacao2;
	}

	private void registrarContinuacao(Date dataExecucao, File agrupadorDir) throws Exception {

		String agrupador = agrupadorDir.getName();
		File diaDir = new File(agrupadorDir.getParent());
		String dia = diaDir.getName();
		File mesDir = new File(diaDir.getParent());
		String mes = mesDir.getName();
		File anoDir = new File(mesDir.getParent());
		String ano = anoDir.getName();

		StringBuilder sb = new StringBuilder();
		String dataExecucaoStr = DummyUtils.formatDateTime3(dataExecucao);
		sb.append(dataExecucaoStr).append(";");
		sb.append(ano).append(";");
		sb.append(mes).append(";");
		sb.append(dia).append(";");
		sb.append(agrupador).append(";");

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			parametroService.setValor(ParametroService.P.BACALHAU_REVERSO, sb.toString());
		});
		tw.runNewThread();
		tw.throwException();
	}

	private static int comparaArquivosPorNome(File arquivo1, File arquivo2) {
		String nome1 = arquivo1 == null ? null : arquivo1.getName();
		String nome2 = arquivo2 == null ? null : arquivo2.getName();
		return nome1.compareTo(nome2);
	}

	private static class BacalhauReversoVO {

		private Date dataExecucao;
		private String anoContinuacao;
		private String mesContinuacao;
		private String diaContinuacao;
		private String agrupadorContinuacao;
		private BacalhauReversoRelatorioService.BacalhauReversoRelatorioBuilder builder;
		public File anoDir;
		public File mesDir;
		public File diaDir;
		private File agrupadorDir;
		private AtomicInteger count = new AtomicInteger();
		private AtomicInteger countInexistente = new AtomicInteger();
	}
}
