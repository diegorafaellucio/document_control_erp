package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.InserirArquivoProcessosVO;
import net.wasys.getdoc.domain.vo.SiaAtualizaDocumentoVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static net.wasys.util.DummyUtils.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class FuncoesService {

	@Autowired private DocumentoService documentoService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private ProcessoService processoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private RelatorioGeralEtapaService relatorioGeralEtapaService;
	@Autowired private SiaService siaService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ParametroService parametroService;
	@Autowired private MigrarProcessoService migrarProcessoService;
	@Autowired private ImportacaoProcessoService importacaoProcessoService;
	@Autowired private LogImportacaoService logImportacaoService;
	@Autowired private CampoService campoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private BacalhauService bacalhauService;
	@Autowired private ImagemService imagemService;

	@Transactional
	public void atualizarRelatorioGeral(String processosString) throws Exception {
		if(StringUtils.isBlank(processosString)) {
			return;
		}

		List<Long> processoIdList = new ArrayList<>();

		String[] processosArray = processosString.split(",");
		for (String processoIdString : processosArray) {
			processoIdString = processoIdString.trim();
			Long processoId = Long.parseLong(processoIdString);
			processoIdList.add(processoId);
		}

		relatorioGeralService.atualizarRelatorioGeral(processoIdList);
	}

	@Transactional
	public void tipificarDocumento(Long documentoId, MultiValueMap<Long, Long> docsIds) {
		List<Long> imagensIds = docsIds.get(documentoId);
		Documento documento = documentoService.get(documentoId);
		systraceThread("------------------------------------------------------------------------");
		systraceThread("Tipificando Documentos do Processo : " + documento.getProcesso().getId());
		systraceThread("Tipificando Documento: " + documentoId);
		systraceThread("------------------------------------------------------------------------");
		documentoService.tipificar2(documento, imagensIds);
	}

    public void notificarDocumentoSia(String numInscricao, String numCandidato, String numMatricula, Long codOrigemDocumento, Usuario usuario) {

		SiaAtualizaDocumentoVO vo = new SiaAtualizaDocumentoVO();
		vo.setNumeroInscricao(numInscricao);
		vo.setNumeroCandidato(numCandidato);
		String codOrigemStr = codOrigemDocumento != null ? codOrigemDocumento.toString() : null;
		vo.setCodOrigem(codOrigemStr);
		ConsultaExterna consultaExterna = siaService.notificarDocumento(vo);

		gravarLogNotificacaoSia(numInscricao, numCandidato, codOrigemDocumento, consultaExterna, usuario);
	}

	private void gravarLogNotificacaoSia(String numInscricao, String numCandidato, Long codOrigemDocumento, ConsultaExterna consultaExterna, Usuario usuario) {
		String resultado = consultaExterna.getResultado();

		ProcessoFiltro filtro1  = new ProcessoFiltro();
		filtro1.setNumCandidatoInscricao(numInscricao);
		filtro1.setCamposFiltro(CampoMap.CampoEnum.NUM_INSCRICAO, Arrays.asList(numInscricao));
		filtro1.setCamposFiltro(CampoMap.CampoEnum.NUM_CANDIDATO, Arrays.asList(numCandidato));
		List<Processo> processos = processoService.findByFiltro(filtro1, null, null);
		Processo processo = !processos.isEmpty() ? processos.get(0) : null;

		if(processo != null) {
			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setCodsOrigem(Arrays.asList(codOrigemDocumento));
			filtro.setProcesso(processo);
			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
			Documento documento = !documentos.isEmpty() ? documentos.get(0) : null;
			if(documento != null) {
				documentoLogService.criaLog(documento, usuario, AcaoDocumento.NOTIFICACAO_SIA, resultado);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void inserirArquivoNosDocumentos(InserirArquivoProcessosVO vo, Usuario usuarioLogado) {

		systraceThread("Iniciando execução");

		if (!isParametrosInserirArquivosDocumentosValidos(vo)) {
			throw new RuntimeException("ProcessoIds, tiposDocumentos e arquivo para inserção são obrigatórios.");
		}

		String processoIdsStr = vo.getProcessoIds();
		String[] split = processoIdsStr.split(",");
		List<Long> processoIds = Arrays.stream(split).map(id -> Long.valueOf(id.trim())).collect(Collectors.toList());
		List<Processo> processos = processoService.findByIds(processoIds);

		systraceThread("Atualizando (" + processoIds.size() + ") processos.");

		for (Processo processo : processos) {

			try {
				inserirArquivo(vo, processo, usuarioLogado);
			}
			catch (Exception e) {
				String exceptionMessage = getExceptionMessage(e);
				systraceThread(exceptionMessage, LogLevel.ERROR);
				e.printStackTrace();
			}
		}

		systraceThread("Finalizando execução");
	}

	private void inserirArquivo(InserirArquivoProcessosVO vo, Processo processo, Usuario usuarioLogado) {

		systraceThread("Atualizando processo=" + processo);

		String tiposDocumentosSelecionadosStr = vo.getTiposDocumentosIds();
		String[] split = tiposDocumentosSelecionadosStr.split(",");
		List<Long> tiposDocumentosSelecionadosIds = Arrays.stream(split).map(id -> Long.valueOf(id.trim())).collect(Collectors.toList());

		List<TipoDocumento> tiposDocumentosSelecionados = tipoDocumentoService.findByIds(tiposDocumentosSelecionadosIds);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		List<TipoDocumento> tiposDocumentosDoTipoProcesso = tiposDocumentosSelecionados.stream().filter(td -> td.getTipoProcesso().equals(tipoProcesso)).collect(Collectors.toList());

		if (isEmpty(tiposDocumentosDoTipoProcesso)) {
			throw new RuntimeException("Não foi encontrado documento para atualização. processo=" + processo + ", tiposDocumentosSelecionados=" + tiposDocumentosSelecionados);
		}

		if (tiposDocumentosDoTipoProcesso.size() > 1) {
			throw new RuntimeException("Encontrado mais de um tipo de documento no mesmo tipo processo para atualização. Selecione apenas o tipo de documento correto a ser atualizado. processo=" + processo
					+ ", tiposDocumentosSelecionados=" + tiposDocumentosSelecionados);
		}

		TipoDocumento tipoDocumentoParaAtualizar = tiposDocumentosDoTipoProcesso.get(0);

		File arquivo = vo.getArquivo();
		boolean aprovarDocumento = vo.isAprovarDocumento();
		processoService.inserirArquivoNoDocumento(processo, tipoDocumentoParaAtualizar, arquivo, usuarioLogado, aprovarDocumento);
	}

	private boolean isParametrosInserirArquivosDocumentosValidos(InserirArquivoProcessosVO vo) {

		String processoIds = vo.getProcessoIds();
		String tiposDocumentosSelecionados = vo.getTiposDocumentosIds();
		File arquivo = vo.getArquivo();

		return !isBlank(processoIds) && !isBlank(tiposDocumentosSelecionados) && arquivo != null;
	}

	@Transactional(rollbackFor=Exception.class)
	public void reprocessarRelatorioDeEtapa() {

		HorasUteisCalculator huc = processoService.buildHUC();
		String dateStringJson = parametroService.getValor(ParametroService.P.ULTIMA_DATA_RELATORIO_GERAL_ETAPA);
		Map<?, ?> map = DummyUtils.jsonStringToMap(dateStringJson);
		String dataInicioString = (String) map.get("inicio");
		String dataFimString = (String) map.get("fim");

		Date dataInicio = DummyUtils.parseDateTime(dataInicioString);
		Date dataFim = DummyUtils.parseDateTime2(dataFimString);

		boolean contiuar = false;
		List<Long> ids = processoLogService.findProcessoIdToAjustarEtapa(dataInicio, dataFim);

		do {

			List<Long> list = DummyUtils.removeItens(ids, 500);

			for (Long processoId : list) {

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {

					RelatorioGeral relatorioGeral = relatorioGeralService.getByProcesso(processoId);
					if(relatorioGeral != null) {
						relatorioGeralEtapaService.criaRelatorioGeral(relatorioGeral, huc);
					}
				});
				tw.runNewThread();
			}

			contiuar = (ids != null && !ids.isEmpty());
			DummyUtils.sleep(10);
		}
		while (contiuar);
	}

	@Transactional(rollbackFor=Exception.class)
	public void ajustarLogComEtapa() {

		List<Long> situacoesComEtapaDeMudanca = Arrays.asList(351L, 268L, 283L);
		Date dataMudanca = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(dataMudanca);
		c.set(2020, 4, 12, 16, 30, 00);
		dataMudanca = c.getTime();

		String dateStringJson = parametroService.getValor(ParametroService.P.ULTIMA_DATA_RELATORIO_GERAL_ETAPA);
		Map<?, ?> map = DummyUtils.jsonStringToMap(dateStringJson);
		String dataInicioString = (String) map.get("inicio");
		String dataFimString = (String) map.get("fim");

		Date dataInicio = DummyUtils.parseDateTime(dataInicioString);
		Date dataFim = DummyUtils.parseDateTime2(dataFimString);
		List<ProcessoLog> logs = processoLogService.findLogToAjustarEtapa(dataInicio, dataFim);

		boolean contiuar = false;
		do {

			List<ProcessoLog> list = DummyUtils.removeItens(logs, 500);

			for (ProcessoLog log : list) {
				Situacao situacao = log.getSituacao();
				Etapa etapa = situacao.getEtapa();
				if(etapa != null) {
					Date finalDataMudanca = dataMudanca;

					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {

						Long processoLogId = log.getId();
						ProcessoLog log1 = processoLogService.get(processoLogId);

						BigDecimal horasPrazo = etapa.getHorasPrazo();
						Long situacaoId = situacao.getId();
						HorasUteisCalculator huc = processoService.buildHUC(situacaoId);
						Date data = log1.getData();
						Date prazoLimiteEtapa = processoService.calculaPrazoLimiteEtapa(situacao, data);
						if (data.before(finalDataMudanca) && situacoesComEtapaDeMudanca.contains(situacaoId)) {
							horasPrazo = new BigDecimal(36);
							prazoLimiteEtapa = huc.addHoras(data, horasPrazo);
						}

						log1.setEtapa(etapa);
						log1.setPrazoLimiteEtapa(prazoLimiteEtapa);
						log1.setHorasPrazoEtapa(horasPrazo);
						processoLogService.saveOrUpdate(log1);

					});
					tw.runNewThread();
				}
			}

			contiuar = (logs != null && !logs.isEmpty());
			DummyUtils.sleep(10);
		}
		while (contiuar);
	}

	@Transactional(rollbackFor=Exception.class)
	public void ajustarLogEPrazoLimiteSituacao() {

		Date dataInicio = DummyUtils.parseDate("10/02/2021");
		Date dataFim = DummyUtils.parseDate("12/02/2021");
		List<ProcessoLog> logs = processoLogService.findLogToAjustarEtapaEPrazoLimite(dataInicio, dataFim);

		boolean contiuar = false;
		do {
			List<ProcessoLog> list = DummyUtils.removeItens(logs, 500);

			for (ProcessoLog log : list) {
				Situacao situacao = log.getSituacao();

				Etapa etapa = situacao.getEtapa();
				if(etapa != null) {

					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {

						processoService.ajustePrazoLimiteSituacao(log, situacao);


					});
					tw.runNewThread();
				}
			}

			contiuar = (logs != null && !logs.isEmpty());
			DummyUtils.sleep(10);
		}
		while (contiuar);
	}

	public void alterarSituacaoEmMassa(String processosString, Situacao novaSituacao, Usuario usuario, String observacao) {

		if (StringUtils.isBlank(processosString) || novaSituacao == null) {
			return;
		}

		String[] processosArray = processosString.split(",");
		int length = processosArray.length;
		int count = 1;
		List<Long> processosIdsNaoAtualizados = new ArrayList<>();
		systraceThread("Processando alteracao em massa de situacao qtd: " + length + " processos...");

		for (String processoIdString : processosArray) {

			systraceThread("Processo " + processoIdString + " situacao " + novaSituacao.getNome() + " " + count + " de " + length);

			processoIdString = processoIdString.trim();
			String finalProcessoIdString = processoIdString;

			Long processoId = Long.parseLong(finalProcessoIdString);
			Processo processo = processoService.get(processoId);

			TipoProcesso tipoProcessoSituacao = novaSituacao.getTipoProcesso();
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			if (tipoProcessoSituacao.equals(tipoProcesso)) {
				try {
					processoService.concluirEmMassa(singletonList(processo), usuario, novaSituacao, observacao);
				}
				catch (Exception e) {
					processosIdsNaoAtualizados.add(processoId);
					e.printStackTrace();
				}
			}
			else {
				systraceThread("Processo não é do mesmo tipo_processo da situação. processo.tipo_processo=" + tipoProcesso + ", situacao.tipo_processo=" + tipoProcessoSituacao);
				processosIdsNaoAtualizados.add(processoId);
			}

			count++;
		}

		systraceThread("Finalizado. Processos não atualizados=" + processosIdsNaoAtualizados);
	}

	public void migrarTipoDeProcesso(List<Long> processoIds, TipoProcesso tipoProcessoDestino, boolean excluirProcessosAposMigracao, Usuario usuarioLogado, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) {

		List<Processo> processos = processoService.findByIds(processoIds);
		List<Processo> processosIncorretos = processos.stream().filter(p -> p.getTipoProcesso().equals(tipoProcessoDestino)).collect(Collectors.toList());
		if (!processosIncorretos.isEmpty()) {
			throw new IllegalArgumentException("Já existem processos do tipo de processo destino. tipoProcessoDestino=" + tipoProcessoDestino + ", processos=" + processosIncorretos);
		}

		systraceThread("Iniciando migração dos processos=" + processos);

		for (Processo processo : processos) {

			systraceThread("Migrando processo=" + processo + " para o tipoProcesso=" + tipoProcessoDestino);
			try {
				migrarProcessoService.migrarTipoDeProcesso(processo, tipoProcessoDestino, excluirProcessosAposMigracao, usuarioLogado, tipoDocumentoEquivalenteParaTipoDocumento);
			}
			catch (Exception e) {
				systraceThread("Não foi possível migrar o processo=" + processo, LogLevel.ERROR);
				e.printStackTrace();
			}
		}

		systraceThread("Migração finalizada.");
	}

	@Transactional
	public void marcarParaNotificarDocumentosSia(Date dataInicioAprovacao, Date dataFimAprovacao, Usuario usuarioLogado) {

		dataInicioAprovacao = DateUtils.truncate(dataInicioAprovacao, Calendar.DAY_OF_MONTH);

		if (dataFimAprovacao != null) {
			dataFimAprovacao = DummyUtils.truncateFinalDia(dataFimAprovacao);
		}

		systraceThread("Iniciando marcação para notificação dos documentos no SIA, aprovados entre=" + dataInicioAprovacao + " e " + dataFimAprovacao + ", usuario=" + usuarioLogado);

		List<Documento> documentosParaNotificar = documentoService.findNotificadosSia(dataInicioAprovacao, dataFimAprovacao);

		systraceThread("Quantide de documentos a serem atualizados=" + documentosParaNotificar.size());

		for (Documento documento : documentosParaNotificar) {
			documentoService.atualizarNotificadoSia(documento.getId(), false);
		}

		systraceThread("Fim da marcação de documentos.");
	}

	public void reimportacaoEmMassa(TipoProcesso tipoProcesso, Usuario usuario, int maxImportacao, int inicioImportacao) throws Exception {

		LogImportacaoFiltro filtro = new LogImportacaoFiltro();
		filtro.setTipoProcesso(tipoProcesso);
		filtro.setTipoImportacao(TipoImportacao.PROCESSO);
		filtro.setStatus(StatusImportacao.SUCESSO);
		filtro.setOrdem(SortOrder.DESCENDING);
		filtro.setCampoOrdem("id");

		List<LogImportacao> logsImportacao = logImportacaoService.findByFiltro(filtro, inicioImportacao, maxImportacao);
		systraceThread("Iniciando importação em massa. Quantidade: " + logsImportacao.size());

		for (LogImportacao logImportacao : logsImportacao) {
			String pathArquivo = logImportacao.getPathArquivo();
			File file = new File(pathArquivo);
			String fileName = file.getName();

			boolean exists = file.exists();
			if (!exists) {
				systraceThread("Arquivo inexistente. Nome Arquivo: " + fileName + ". Path: " + pathArquivo);
				continue;
			}

			try {
				importacaoProcessoService.iniciarProcessamentoDoArquivo(file, usuario, fileName, true);
			}
			catch (Exception e) {
				e.printStackTrace();
				String stackTrace = DummyUtils.getStackTrace(e);
				String currentMethodName = DummyUtils.getCurrentMethodName();
				systraceThread(currentMethodName + "EXCEPTION: " + stackTrace + ". Nome Arquivo: " + fileName + ". Path: " + pathArquivo, LogLevel.ERROR);
			}

			DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
		}

	}

	public void criarCamposEmMassa(Usuario usuarioLogado, TipoProcesso tipoProcesso, TipoCampoGrupo tipoGrupo, TipoCampo novoTipoCampo, String processosIds) {
		ProcessoFiltro processoFiltro = new ProcessoFiltro();

		if(StringUtils.isNotBlank(processosIds)) {
			List<Long> list = new ArrayList<>();
			String[] processoIdsArray = processosIds.split(",");
			for (String processoIdStr : processoIdsArray) {
				Long processoId = Long.valueOf(processoIdStr.trim());
				list.add(processoId);
			}
			processoFiltro.setProcessoIdList(list);
		}

		processoFiltro.setTiposProcesso(Arrays.asList(tipoProcesso));
		List<Processo> processos = processoService.findByFiltro(processoFiltro, null, null);
		systraceThread("Iniciando criação de campos em massa. Quantidade: " + processos.size());

		campoService.criarCamposEmMassaAndReordenado(processos, usuarioLogado, tipoGrupo, novoTipoCampo);
	}

	public void vincularEmMassa(Usuario usuario, File file) throws Exception {

		Map<Long, String> map = criarMap(file);
		if(map.isEmpty()) {
			return;
		}

		int qtdeThreads = 10;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(qtdeThreads);
		ExecutorTimeoutUtil etu = new ExecutorTimeoutUtil(executor);

		long threadTimeout = 10000;
		etu.configurar(threadTimeout, 10, map.size());

		Map<Long, TransactionWrapper> twMap = new HashMap<>();
		for (Long processoId : map.keySet()) {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				DummyUtils.mudarNomeThread("pooll-vinculo-massa");

				Processo processo = processoService.get(processoId);
				if (processo != null) {
					String numCandidato = map.get(processoId);
					systraceThread("Vinculando processo: " + processoId + ". Ao número de candidato: " + numCandidato);
					AlunoFiltro alunoFiltro = new AlunoFiltro();
					alunoFiltro.setNumCandidato(numCandidato);
					processoService.atualizarProcessoComSiaFiesProuni(processoId, usuario, null, alunoFiltro, null);
				}
				else {
					systraceThread("processo não encontrado. ID: " + processoId);
				}
			});
			etu.submit(tw, processoId);
			twMap.put(processoId, tw);
			DummyUtils.sleep(150);
		}

		etu.esperarTerminarFuturesOuCancelar();

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Long, TransactionWrapper> entry : twMap.entrySet()) {
			Long processoId = entry.getKey();
			TransactionWrapper tw = entry.getValue();
			Exception exception = tw.getException();
			if(exception != null) {
				String exceptionMessage = getExceptionMessage(exception);
				sb.append("\nErro ao processar processo ").append(processoId).append(": ").append(exceptionMessage);
			}
		}

		List<Object> timeouts = etu.getReferenciaThreadsTimeouts();
		if(!timeouts.isEmpty()) {
			sb.append("\nOcorreram ").append(timeouts.size()).append(" timeouts: ").append(timeouts);
		}

		if(sb.length() > 0) {
			systraceThread(sb);
			throw new MessageKeyException("erroInesperado.error", sb.toString());
		}
	}


	private Map<Long, String> criarMap(File file) throws IOException {

		BufferedReader br = getBufferedReader(file);

		CSVRecord record = null;
		Map<Long, String> map = new LinkedHashMap<>();
		try {
			CSVParser csvp = new CSVParser(br, CSVFormat.EXCEL.withDelimiter(';'));
			List<CSVRecord> records = csvp.getRecords();
			for (CSVRecord record2 : records) {

				if(record == null) {
					record = record2;
					//pula o header
					continue;
				}
				record = record2;

				String processoIdStr = record.get(0);
				Long processoId = new Long(processoIdStr);
				String numeroCandidato = record.get(1);

				map.put(processoId, numeroCandidato);
			}
		}
		catch (Exception e) {
			if(record != null) {
				String exceptionMessage = getExceptionMessage(e);
				long recordNumber = record.getRecordNumber();
				throw new RuntimeException("Erro ao processar a linha " + recordNumber + ": " + exceptionMessage, e);
			}
			else {
				throw e;
			}
		}
		finally {
			br.close();
		}

		return map;
	}

	public void reImportar(Usuario usuario, File file) throws Exception {

		String fileName = file.getName();
		try {
			importacaoProcessoService.iniciarProcessamentoDoArquivo(file, usuario, fileName, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			String stackTrace = DummyUtils.getStackTrace(e);
			String currentMethodName = DummyUtils.getCurrentMethodName();
			systraceThread(currentMethodName + "EXCEPTION: " + stackTrace + ". Nome Arquivo: " + fileName , LogLevel.ERROR);
		}
	}

	private BufferedReader getBufferedReader(File file) throws FileNotFoundException {
		String name = file.getName();
		String extensao = DummyUtils.getExtensao(name);
		extensao = StringUtils.isNotBlank(extensao) ? extensao.toLowerCase() : null;
		Charset charset = "csv".equals(extensao) ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8;
		FileInputStream fis = new FileInputStream(file);
		return new BufferedReader(new InputStreamReader(fis, charset));
	}

	public void processarTodosProcessoParaRelatorioGeral() {

		String processoIdStr = parametroService.getValor(ParametroService.P.ULTIMO_PROCESSO_ID_PROCESSADO_RG);
		Long processoIdStart = StringUtils.isNotBlank(processoIdStr) ? Long.parseLong(processoIdStr) : 0L;

		DummyUtils.systraceThread("#rreg# processoIniciao: " + processoIdStart);
		List<Long> processosIds = processoService.findProcessosIds(processoIdStart, 100000);

		List<Future> futures = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(15);
		try {
			int count = 0;
			int processados = 0;
			for (Long processoId : processosIds) {

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {

					mudarNomeThread("thdfunc-reprocessar-relatorio-geral");

					StopWatch stopWatchProcesso = new StopWatch();
					stopWatchProcesso.start();
					try {
						atualizarRelatorioGeral(processoId.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				Future<?> future = executorService.submit(tw);
				futures.add(future);
				sleep(100); //sem isso o hibernate se perde nas sessions
				count++;
				if (count == 1000) {
					processados = processados + count;
					DummyUtils.systraceThread("#rreg# Fim do processamento de 1000 registros, processados: " + processados + " de: " + processosIds.size() + " ultimo processo id: " + processoId);
					parametroService.setValor(ParametroService.P.ULTIMO_PROCESSO_ID_PROCESSADO_RG, processoId.toString());
					count = 0;
					DummyUtils.systraceThread("#rreg# ultimo processo: " + processoId);
				}
				processoIdStart = processoId;
			}

			parametroService.setValor(ParametroService.P.ULTIMO_PROCESSO_ID_PROCESSADO_RG, processoIdStart.toString());

			long timeout = System.currentTimeMillis() + 1000 * 60 * 60 * 12;
			checkTimeout(futures, timeout);
		}
		finally {
			executorService.shutdown();
		}
	}

	public void rodarBacalhauByImagem(List<Long> imagemIds) {
		bacalhauService.executarBacalhauGeralByImagens(imagemIds);
	}

	public void rodarBacalhauRecuperarImagensPerdidas() {
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		logAcesso.setServletPath("thd-recuperarImagensPerdidasFuncao");
		bacalhauService.recuperarImagensPerdidas();
	}

	public void validarImagensInexistentes() {
		imagemService.validarImagensInexistentes();
	}
}