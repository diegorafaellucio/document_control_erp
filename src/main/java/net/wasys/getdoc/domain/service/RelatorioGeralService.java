package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.RelatorioGeralRepository;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.GrupoSuperiorVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.TempoStatusVO;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro.Tipo;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.ExcelCsvWriter;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.wasys.util.DummyUtils.getCampoProcesso;
import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RelatorioGeralService {

	private final Pattern PATTERN_NUMERO_ENTRE_PARENTESIS = Pattern.compile("(\\d+)");
	private final String SUFIX_DESC_CAMPO_BASE_INTERNA = " Desc.";

	@Autowired private RelatorioGeralRepository relatorioGeralRepository;
	@Autowired private RelatorioGeralSolicitacaoService relatorioGeralSolicitacaoService;
	@Autowired private RelatorioGeralSituacaoService relatorioGeralSituacaoService;
	@Autowired private RelatorioGeralEtapaService relatorioGeralEtapaService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ParametroService parametroService;
	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private MessageService messageService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private RelatorioIsencaoDisciplinasService relatorioIsencaoDisciplinasService;
	@Autowired private AlunoService alunoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ApplicationContext applicationContext;

	public RelatorioGeral get(Long id) {
		return relatorioGeralRepository.get(id);
	}

	public RelatorioGeral getByProcessoAndCriaCasoNaoExista(Long processoId) {
		RelatorioGeral rg = getByProcesso(processoId);
		if(rg == null) {
			Processo processo = processoService.get(processoId);
			rg = criaRelatorioGeral(processo, new HashMap<>(), null);
		}
		return rg;
	}

	public RelatorioGeral getByProcesso(Long processoId) {
		return relatorioGeralRepository.getByProcesso(processoId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RelatorioGeral rg) throws MessageKeyException {
		relatorioGeralRepository.saveOrUpdate(rg);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarCampos(List<Long> processosIds) throws Exception {

		Map<Long, Map<String, RegistroValorVO>> baseInternaMap = getBasesInternasMap();
		int total = processosIds.size();

		do {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				List<Long> processosIds2 = new ArrayList<>();
				for (int i = 0; i < 1000 && !processosIds.isEmpty(); i++) {
					Long id = processosIds.remove(0);
					processosIds2.add(id);
				}

				systraceThread("buscando ids... restam " + processosIds.size());
				List<Processo> processos = processoService.findByIds(processosIds2);

				systraceThread("buscando rgs... restam " + processosIds.size());
				Map<Long, RelatorioGeral> mapRelatorio = findByProcessosIds(processosIds2);

				for (int i = 0; i < processos.size(); i++) {

					Processo processo = processos.get(i);
					processos.set(i, null);

					Long processoId = processo.getId();
					RelatorioGeral rg = mapRelatorio.get(processoId);
					if(rg == null) {
						continue;
					}

					String camposJson = getCamposDinamicosJson(baseInternaMap, processoId);
					rg.setCamposDinamicos(camposJson);

					saveOrUpdateWithoutFlush(rg);

					processosIds2.remove(processoId);
					if(i % 100 == 0 && i != 0) {
						systraceThread((processosIds.size() + processosIds2.size()) + " de " + total);
					}
				}
			});
			tw.runNewThread();
			tw.throwException();
		}
		while(!processosIds.isEmpty());
	}

	@Transactional(rollbackFor=Exception.class)
	public Date atualizar(Date data0) throws Exception {

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();

		Date data = data0;
		if(data == null) {
			data = DummyUtils.parseDate("01/01/2019");
		} else {
			data = DateUtils.addMinutes(data, -30);
		}

		Date dataInicio = data;
		Date dataFim = new Date();

		DummyUtils.addParameter(logAcesso, "dataInicio", DummyUtils.formatDateTime2(dataInicio));

		long horas = (dataFim.getTime() - dataInicio.getTime()) / 3600000;
		if(horas > 2) {
			dataFim = DateUtils.addMinutes(data0, 90);
		}

		systraceThread("[data] atualizando de " + DummyUtils.formatDateTime(dataInicio) + " a " + DummyUtils.formatDateTime(dataFim));

		List<Object[]> processosIds = processoService.findToRelatorioGeral(dataInicio, dataFim, null);
		int total = processosIds.size();

		if(total > 0) {

			Map<Long, Map<String, RegistroValorVO>> baseInternaMap = getBasesInternasMap();

			systraceThread("[gerando] hucs das situações");
			List<Situacao> ativas = situacaoService.findByFiltro(new SituacaoFiltro(), null, null);
			Map<Long, HorasUteisCalculator> hucBySituacoes = processoService.buildHUCBySituacoes(ativas);

			do {
				List<Long> processosIds2 = new ArrayList<>();
				Date dataUltimoProcesso = null;
				for (int i = 0; i < 200 && !processosIds.isEmpty(); i++) {
					Object[] processo = processosIds.remove(0);
					Long id = (Long) processo[0];
					dataUltimoProcesso = (Date) processo[1];
					processosIds2.add(id);
				}
				Date dataUltimoProcesso2 = dataUltimoProcesso;

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {

					DummyUtils.mudarNomeThread("thd-real-geral-atualizar");

					systraceThread("buscando solicitacoes... restam " + processosIds.size());
					Map<Long, RelatorioGeralSolicitacao> mapRelatorioSolicitacao = relatorioGeralSolicitacaoService.findByProcessosIds(processosIds2);

					systraceThread("buscando situacoes... restam " + processosIds.size());
					Map<Date, RelatorioGeralSituacao> mapRelatorioSituacao = relatorioGeralSituacaoService.findByProcessosIdsMap(processosIds2);

					systraceThread("buscando etapas... restam " + processosIds.size());

					systraceThread("buscando ids... restam " + processosIds.size());
					List<Processo> processos = processoService.findByIds(processosIds2);

					systraceThread("buscando rgs... restam " + processosIds.size());
					Map<Long, RelatorioGeral> mapRelatorio = findByProcessosIds(processosIds2);

					Map<Processo, RelatorioGeral> mapRelatorio2 = new HashMap<>();

					systraceThread("criando rgs... restam " + processosIds.size());
					for (int i = 0; i < processos.size(); i++) {

						Processo processo = processos.get(i);
						processos.set(i, null);
						RelatorioGeral rg = criaRelatorioGeral(processo, mapRelatorio, baseInternaMap);

						saveOrUpdateWithoutFlush(rg);

						mapRelatorio2.put(processo, rg);

						Long processoId = processo.getId();
						processosIds2.remove(processoId);
						if(i % 20 == 0 && i != 0) {
							systraceThread("RelatorioGeral - faltam " + (processosIds.size() + processosIds2.size()) + " de " + total);
						}
					}

					Set<Processo> keySet = mapRelatorio2.keySet();
					List<Processo> keyList = new ArrayList<>(keySet);

					for (int i = 0; i < keyList.size(); i++) {

						Processo processo = keyList.get(i);
						Situacao situacao = processo.getSituacao();
						Long situacaoId = situacao.getId();
						HorasUteisCalculator huc = hucBySituacoes.get(situacaoId);
						RelatorioGeral rg = mapRelatorio2.get(processo);

						relatorioGeralSolicitacaoService.criaRelatorioGeral(processo, rg, mapRelatorioSolicitacao, huc);
						relatorioGeralSituacaoService.criaRelatorioGeral(processo, rg, mapRelatorioSituacao, huc);
						relatorioGeralEtapaService.criaRelatorioGeral(rg, huc);

						Long processoId = processo.getId();
						processosIds2.remove(processoId);
						if (i % 100 == 0 && i != 0) {
							systraceThread("RelatorioGeralSolicitacao RelatorioGeralSituacao RelatorioGeralEtapa - " + (processosIds.size() + processosIds2.size()) + " de " + total);
						}
					}

					int i = 0;

					if(dataUltimoProcesso2.after(data0)) {
						systraceThread("[data] atualizado: " + DummyUtils.formatDateTime2(dataUltimoProcesso2) + ". Faltam " + (processosIds.size() + processosIds2.size()) + " de " + total);
						i++;
						DummyUtils.addParameter(logAcesso, "setDataUltimoProcesso1-" + i, DummyUtils.formatDateTime2(dataUltimoProcesso2));
						parametroService.setValorDate(P.ULTIMA_DATA_RELATORIO_GERAL, dataUltimoProcesso2);
					}
				});
				tw.runNewThread();
				tw.throwException();
			}
			while(!processosIds.isEmpty());
		}

		Date dataFim2 = dataFim;
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			DummyUtils.mudarNomeThread("thd-real-geral-set-ultima-data");
			DummyUtils.addParameter(logAcesso, "setDataFim-2", DummyUtils.formatDateTime2(dataFim2));
			parametroService.setValorDate(P.ULTIMA_DATA_RELATORIO_GERAL, dataFim2);
		});
		tw.runNewThread();
		tw.throwException();

		systraceThread("[data] atualizado de " + DummyUtils.formatDateTime(dataInicio) + " a " + DummyUtils.formatDateTime(dataFim2) + ". " + total + " processos. fim ");
		return dataFim2;
	}

	public Map<Long, Map<String, RegistroValorVO>> getBasesInternasMap() {
		Map<Long, Map<String, RegistroValorVO>> baseInternaMap = new HashMap<>();
		List<Long> basesInternasIds = tipoCampoService.findBasesInternas();
		for (Long baseInternaId : basesInternasIds) {
			BaseRegistroFiltro filtro = new BaseRegistroFiltro();
			filtro.setBaseInterna(new BaseInterna(baseInternaId));
			int limit = 100000;//limit para não estourar a memória...
			List<RegistroValorVO> list = baseRegistroService.findByFiltro(filtro, 0, limit);
			Map<String, RegistroValorVO> map = baseInternaMap.get(baseInternaId);
			map = map != null ? map : new HashMap<>();
			baseInternaMap.put(baseInternaId, map);
			for (RegistroValorVO vo : list) {
				BaseRegistro baseRegistro = vo.getBaseRegistro();
				String chaveUnicidade = baseRegistro.getChaveUnicidade();
				map.put(chaveUnicidade, vo);
			}
		}
		return baseInternaMap;
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdateWithoutFlush(RelatorioGeral relatorioGeral) {
		relatorioGeralRepository.saveOrUpdateWithoutFlush(relatorioGeral);
	}

	private Map<Long, RelatorioGeral> findByProcessosIds(List<Long> processosIds2) {

		List<RelatorioGeral> list = relatorioGeralRepository.findByProcessosIds(processosIds2);
		Map<Long, RelatorioGeral> map = new HashMap<Long, RelatorioGeral>();

		for (RelatorioGeral rg : list) {

			Long processoId = rg.getProcessoId();
			map.put(processoId, rg);
		}

		return map;
	}

	public RelatorioGeral criaRelatorioGeral(Processo processo, Map<Long, RelatorioGeral> mapRelatorio, Map<Long, Map<String, RegistroValorVO>> baseInternaMap) {

		Long processoId = processo.getId();

		processo = processoService.get(processoId);

		Situacao situacao = processo.getSituacao();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		if (situacao == null) {
			try {
				processoService.criarPrimeiraSituacao(null, null, processo, tipoProcessoId);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		RelatorioGeral rg = mapRelatorio.get(processoId);
		rg = rg != null ? rg : new RelatorioGeral();

		rg.setProcessoId(processoId);

		Date dataCriacao = processo.getDataCriacao();
		rg.setDataCriacao(dataCriacao);

		Date dataEnvioAnalise = processo.getDataEnvioAnalise();
		rg.setDataEnvioAnalise(dataEnvioAnalise);

		Date dataEmAnalise = processoService.getLastDataStatus(processo, null, StatusProcesso.EM_ANALISE);
		rg.setDataEmAnalise(dataEmAnalise);

		Date dataEmAcompanhamento = processoService.getFirstDataStatus(processo, null, StatusProcesso.EM_ACOMPANHAMENTO);
		rg.setDataEmAcompanhamento(dataEmAcompanhamento);

		Integer nivelPrioridade = processo.getNivelPrioridade();
		rg.setNivelPrioridade(nivelPrioridade);

		if (processo.getAluno() != null && processo.getAluno().getId() != null) {
			Long alunoId = processo.getAluno().getId();
			Aluno aluno = alunoService.get(alunoId);
			if(aluno != null) {
				String cpfCnpj = aluno.getCpf();
				cpfCnpj = DummyUtils.getCpfCnpj(cpfCnpj);
				rg.setCpfCnpj(cpfCnpj);

				String nome = aluno.getNome();
				rg.setNome(nome);

				String nomeSocial = aluno.getNomeSocial();
				rg.setNomeSocial(nomeSocial);

				String passaporte = aluno.getPassaporte();
				rg.setPassaporte(passaporte);

				String identidade = aluno.getIdentidade();
				rg.setIdentidade(identidade);

				Estado identidadeUf = aluno.getUfIdentidade();
				rg.setIdentidadeUf(identidadeUf);

				String orgaoEmissor = aluno.getOrgaoEmissor();
				rg.setOrgaoEmissor(orgaoEmissor);

				Date dataEmissao = aluno.getDataEmissao();
				rg.setDataEmissao(dataEmissao);

				String nomeMae = aluno.getMae();
				rg.setNomeMae(nomeMae);

				String nomePai = aluno.getPai();
				rg.setNomePai(nomePai);
			}
		}

		if (processo.getAnalista() != null && processo.getAnalista().getId() != null) {
			Long usuarioId = processo.getAnalista().getId();
			Usuario analista = usuarioService.get(usuarioId);
			rg.setAnalista(analista);
		}

		Date dataFinalizacao = processo.getDataFinalizacao();
		rg.setDataFinalizacao(dataFinalizacao);

		StatusProcesso status = processo.getStatus();
		rg.setStatus(status);

		BigDecimal horasPrazoAnalise = processo.getHorasPrazoAnalise();
		rg.setHorasPrazoAnalise(horasPrazoAnalise);

		StatusPrazo statusPrazoSituacao = processoService.getStatusPrazoSituacao(processo);
		rg.setStatusPrazoSituacao(statusPrazoSituacao);

		Date prazoLimiteAnalise = processo.getPrazoLimiteAnalise();
		if(prazoLimiteAnalise == null && dataEmAnalise != null){
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = processoService.buildHUC(situacaoId);
			BigDecimal horasPrazo = tipoProcesso.getHorasPrazo();
			if(Situacao.ISENCAO_DISCIPLINAS_IDS.contains(situacaoId)) {
				horasPrazo = situacao.getHorasPrazo();
			}
			prazoLimiteAnalise = huc.addHoras(dataEmAnalise, horasPrazo);
			if(Situacao.ISENCAO_DISCIPLINAS_IDS.contains(situacaoId)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(prazoLimiteAnalise.getTime());
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				prazoLimiteAnalise = cal.getTime();
			}
			processo.setPrazoLimiteAnalise(prazoLimiteAnalise);
		}
		rg.setPrazoLimiteAnalise(prazoLimiteAnalise);

		Date prazoLimiteEmAcompanhamento = processo.getPrazoLimiteEmAcompanhamento();
		rg.setPrazoLimiteEmAcompanhamento(prazoLimiteEmAcompanhamento);

		ProcessoLog lastLog = processoLogService.findLastLogByProcesso(processoId, null);
		if (lastLog != null) {
			rg.setDataUltimaTratativa(lastLog.getData());
		}

		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		if((dataFinalizacaoAnalise == null || (dataEmAnalise != null && dataFinalizacaoAnalise.after(dataEmAnalise))) && (StatusProcesso.EM_ACOMPANHAMENTO.equals(status) || StatusProcesso.CONCLUIDO.equals(status) || StatusProcesso.CANCELADO.equals(status) || StatusProcesso.PENDENTE.equals(status))) {
			dataFinalizacaoAnalise = processoService.getDataFinalizacaoAnalise(processo);
			processo.setDataFinalizacaoAnalise(dataFinalizacaoAnalise);
		}
		rg.setDataFinalizacaoAnalise(dataFinalizacaoAnalise);

		Date dataFinalizacaoEmAcompanhamento = null;
		if(dataEmAcompanhamento != null && (StatusProcesso.EM_ANALISE.equals(status) || StatusProcesso.CONCLUIDO.equals(status) || StatusProcesso.CANCELADO.equals(status) || StatusProcesso.PENDENTE.equals(status))) {
			dataFinalizacaoEmAcompanhamento = processoService.getDataFinalizacaoEmAcompanhamento(processoId, dataEmAcompanhamento);
		}
		rg.setDataFinalizacaoEmAcompanhamento(dataFinalizacaoEmAcompanhamento);

		rg.setSituacao(situacao);

		if (processo.getAutor() != null && processo.getAutor().getId() != null) {
			Long autorId = processo.getAutor().getId();
			Usuario autor = usuarioService.get(autorId);
			rg.setAutor(autor);
		}

		if (tipoProcesso != null && tipoProcessoId != null) {
			rg.setTipoProcesso(tipoProcesso);
		}

		TempoStatusVO tempoRascunhoVO = processoService.getTempoRascunho(processo);
		BigDecimal tempoRascunho = tempoRascunhoVO.getTempoTotal();
		rg.setTempoRascunho(tempoRascunho);

		TempoStatusVO tempoAguardandoAnaliseVO = processoService.getTempoAguardandoAnalise(processo);
		BigDecimal tempoAguardandoAnalise = tempoAguardandoAnaliseVO.getTempoTotal();
		int vezesAguardandoAnalise = tempoAguardandoAnaliseVO.getVezes();
		rg.setTempoAguardandoAnalise(tempoAguardandoAnalise);
		rg.setVezesAguardandoAnalise(vezesAguardandoAnalise);

		TempoStatusVO tempoPendenteVO = processoService.getTempoPendente(processo);
		BigDecimal tempoPendente = tempoPendenteVO.getTempoTotal();
		int vezesPendente = tempoPendenteVO.getVezes();
		rg.setTempoPendente(tempoPendente);
		rg.setVezesPendente(vezesPendente);

		TempoStatusVO tempoEmAnaliseVO = processoService.getTempoEmAnalise(processo);
		BigDecimal tempoEmAnalise = tempoEmAnaliseVO.getTempoTotal();
		int vezesEmAnalise = tempoEmAnaliseVO.getVezes();
		rg.setTempoEmAnalise(tempoEmAnalise);
		rg.setVezesEmAnalise(vezesEmAnalise);

		TempoStatusVO tempoEmAcompanhamentoVO = processoService.getTempoEmAcompanhamento(processo);
		BigDecimal tempoEmAcompanhamento = tempoEmAcompanhamentoVO.getTempoTotal();
		int vezesEmAcompanhamento = tempoEmAcompanhamentoVO.getVezes();
		rg.setTempoEmAcompanhamento(tempoEmAcompanhamento);
		rg.setVezesEmAcompanhamento(vezesEmAcompanhamento);

		BigDecimal tempoAteFinalizacao = processoService.getTempoAteFinalizacao(processo);
		rg.setTempoAteFinalizacao(tempoAteFinalizacao);

		BigDecimal tempoAteFinalizacaoAnalise = processoService.getTempoAteFinalizacaoAnalise(processo);
		rg.setTempoAteFinalizacaoAnalise(tempoAteFinalizacaoAnalise);

		BigDecimal tempoSlaCriacao = processoService.getTempoSlaCriacao(processo);
		rg.setTempoSlaCriacao(tempoSlaCriacao);

		BigDecimal tempoSlaTratativa = processoService.getTempoSlaTratativa(processo);
		rg.setTempoSlaTratativa(tempoSlaTratativa);

		BigDecimal horasExpediente = rg.getHorasExpediente();
		if(horasExpediente == null) {
			String[] expedienteArray = parametroService.getExpediente();
			Expediente expediente = new Expediente(expedienteArray);
			horasExpediente = expediente.getHoras();
			rg.setHorasExpediente(horasExpediente);
		}

		String camposJson = getCamposDinamicosJson(baseInternaMap, processoId);
		rg.setCamposDinamicos(camposJson);

		Origem origem = processo.getOrigem();
		rg.setOrigem(origem);

		Map<Irregularidade, Integer> countIrregularidadesMap = pendenciaService.countIrregularidadesByProcesso(processoId);
		List<Map<String, Object>> countIrregularidadesList = new ArrayList<>();
		for (Map.Entry<Irregularidade, Integer> entry : countIrregularidadesMap.entrySet()) {
			Irregularidade irregularidade = entry.getKey();
			Integer count = entry.getValue();
			Map<String, Object> irregularidadeMap = new LinkedHashMap<>();
			irregularidadeMap.put("id", irregularidade.getId());
			irregularidadeMap.put("nome", irregularidade.getNome());
			irregularidadeMap.put("count", count);
			countIrregularidadesList.add(irregularidadeMap);
		}
		String irregularidadesJson = DummyUtils.objectToJson(countIrregularidadesList);
		rg.setIrregularidades(irregularidadesJson);

		relatorioGeralRepository.saveOrUpdate(rg);

		return rg;
	}

	public String getCamposDinamicosJson(Map<Long, Map<String, RegistroValorVO>> baseInternaMap, Long processoId) {

		List<CampoGrupo> grupos = campoGrupoService.findByProcesso(processoId);
		List<GrupoSuperiorVO> grupoSuperiorVOS = processoService.getGrupoSuperiorVOS(new HashSet<>(grupos));

		Map<String, Map<String, String>> gruposCamposMap = new LinkedHashMap<>();
		for (GrupoSuperiorVO grupoVO : grupoSuperiorVOS) {

			CampoGrupo grupoSuperior = grupoVO.getGrupoSuperior();
			List<CampoGrupo> subgrupos = grupoVO.getGruposFilhos();

			if (CollectionUtils.isEmpty(subgrupos)) {
				preencherMapCampoGrupo(gruposCamposMap, null, grupoSuperior, baseInternaMap);
			}
			for (CampoGrupo grupo : subgrupos) {
				preencherMapCampoGrupo(gruposCamposMap, grupoSuperior, grupo, baseInternaMap);
			}
		}

		return DummyUtils.objectToJson(gruposCamposMap);
	}

	private void preencherMapCampoGrupo(Map<String, Map<String, String>> gruposCamposMap, CampoGrupo grupoSuperior, CampoGrupo grupo, Map<Long, Map<String, RegistroValorVO>> baseInternaMap) {

		String grupoNome = grupo.getNome();
		Map<String, String> camposMap = gruposCamposMap.get(grupoNome);
		camposMap = camposMap != null ? camposMap : new LinkedHashMap<>();
		gruposCamposMap.put(grupoNome, camposMap);

		Set<Campo> campos = grupo.getCampos();
		for (Campo campo : campos) {

			if (campo != null) {

				String campoNome = campo.getNome();
				String campoValor = campo.getValor();

				TipoEntradaCampo tipo = campo.getTipo();
				if (tipo.equals(TipoEntradaCampo.COMBO_BOX_ID)) {
					BaseInterna baseInterna = campo.getBaseInterna();

					RegistroValorVO registroValorVO = null;
					if (baseInternaMap != null) {
						Long baseInternaId = baseInterna.getId();
						Map<String, RegistroValorVO> map2 = baseInternaMap.get(baseInternaId);
						registroValorVO = map2.get(campoValor);
					}

					if (registroValorVO == null) {
						BaseRegistroFiltro baseRegistroFiltro = new BaseRegistroFiltro();
						baseRegistroFiltro.setBaseInterna(baseInterna);
						baseRegistroFiltro.setChaveUnicidade(campoValor);
						List<RegistroValorVO> list = baseRegistroService.findByFiltro(baseRegistroFiltro, null, null);
						for (RegistroValorVO vo : list) {
							registroValorVO = vo;
							break;
						}
					}

					String descricao = registroValorVO != null ? registroValorVO.getLabel() : "";
					camposMap.put(campoNome, campoValor);
					camposMap.put(campoNome + SUFIX_DESC_CAMPO_BASE_INTERNA, descricao);
				}
				else {
					camposMap.put(campoNome, campoValor);
				}

				if (grupoSuperior != null) {
					camposMap.put("GRUPO SUPERIOR", grupoSuperior.getNome());

					int numero = 0;

					Matcher matcher = PATTERN_NUMERO_ENTRE_PARENTESIS.matcher(grupoNome);
					if (matcher.find()) {
						numero = Integer.valueOf(matcher.group());
					}

					camposMap.put("INDEX", String.valueOf(numero));
				}
			}
		}
	}

	public List<RelatorioGeral> findByFiltro(RelatorioGeralFiltro filtro, Integer first, Integer pageSize) {
		return relatorioGeralRepository.findByFiltro(filtro, first, pageSize);
	}

	public int countByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralRepository.countByFiltro(filtro);
	}

	private LinkedMultiValueMap<String, String> renderHeaderRow(RelatorioGeralFiltro filtro, ExcelCsvWriter ecw, boolean carregarCamposDinamicos) {

		Tipo tipo = filtro.getTipo();
		if(Tipo.ETAPAS.equals(tipo)) {
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.ETAPA.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.FASE_ETAPA.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.INICIO_ETAPA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.FIM_ETAPA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE_ETAPA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TOTAL_ETAPA.getNome(), 3000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SLA_ATENDIDO_ETAPA.getNome(), 3000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.TRATATIVAS_NAO_CONTADAS_ETAPA.getNome(), 5500);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.USUARIOS.getNome(), 9500);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.PROCESSO_LOG_ID.getNome(), 3000);
		}
		else if(Tipo.SOLICITACOES.equals(tipo)) {
			//Solicitação ID, Área, Analista, Ação Tomada, Data da Solicitação, hh:mm, Prazo da Solicitação, hh:mm, Data Fin. Solicitação, hh:mm, Status
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SOLICITACAO_ID.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.AREA.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.ACAO_TOMADA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_SOLICITACAO.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_SOLICITACAO.getNome(), 3000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_SOLICITACAO.getNome(), 5500);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.STATUS.getNome(), 5500);
		}
		else if(Tipo.SITUACOES.equals(tipo)) {
			//Situação ID, Situação, Situação Anterior, Tempo Total, Data, hh:mm, Data Fim, hh:mm
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO_ID.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.STATUS_SITUACAO.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO_ANTERIOR.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TOTAL.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA.getNome(), 5000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM.getNome(), 3000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TRATATIVA.getNome(), 3000);
		}

		ecw.escrever(RelatorioGeralCampos.ColunasEnum.RELATORIO_GERAL_ID.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PROCESSO_ID.getNome(), 3000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.MOTIVO_DA_REQUISICAO.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.NOME.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.NOME_SOCIAL.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.CPF.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PASSAPORTE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.IDENTIDADE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.ORGAO_EMISSOR.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_EMISSAO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.MAE.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PAI.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_DE_CRIACAO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_DE_ENVIO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_EM_ANALISE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE_H.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_ANALISE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_ACOMPANHAMENTO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ACOMPANHAMENTO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_EM_ACOMPANHAMENTO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_ULTIMA_TRATATIVA.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FINALIZACAO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO.getNome(), 8000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.STATUS.getNome(), 5000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.PRIORIDADE.getNome(), 5000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_LOGIN.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_PERFIL.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA.getNome(), 6000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA_LOGIN.getNome());
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_RASCUNHO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_AGUARD_AN.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_AGUARD_AN.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_PENDENTE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_PENDENTE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_EM_ACOMPANHAMENTO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_EM_ACOMPANHAMENTO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_EM_ANALISE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_EM_ANALISE.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_ATE_FIN_AN.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_ATE_FIN.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_CRIACAO.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_CRIACAO_DIAS.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_TRATATIVA.getNome(), 4000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_TRATATIVA_DIAS.getNome(), 4000);

		LinkedMultiValueMap<String, String> ordenados = null;
		if(carregarCamposDinamicos) {
			ordenados = tipoCampoService.findListByGrupoAndNome(null);
			for (String grupo : ordenados.keySet()) {
				List<String> campos = ordenados.get(grupo);
				for (String campo : campos) {
					ecw.escrever(grupo + " - " + campo, 8000);
					if(RelatorioGeralCampos.CAMPOS_COD.contains(campo.toUpperCase())){
						ecw.escrever(grupo + " - " + "Cod. " + campo, 4000);
					}
					if(CampoMap.CampoEnum.CAMPUS.getNome().equals(campo.toUpperCase())){
						ecw.escrever(grupo + " - " + "Polo Parceiro", 4000);
					}
				}
			}
		}
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.ACAO_FINANCIAMENTOS.getNome(), 12000);
		ecw.escrever(RelatorioGeralCampos.ColunasEnum.EVIDENCIA.getNome(), 16000);

		if (Tipo.ETAPAS.equals(tipo)) {
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO_DE.getNome(), 6000);
			ecw.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO_PARA.getNome(), 6000);
		}

		return ordenados;
	}

	public File render(RelatorioGeralFiltro filtro) {

		RelatorioGeralFiltro.Tipo tipo = filtro.getTipo();
		RelatorioGeralFiltro.TipoArquivo tipoArquivo = filtro.getTipoArquivo();
		systraceThread(tipo + " " + tipoArquivo);
		tipoArquivo = tipoArquivo != null ? tipoArquivo : RelatorioGeralFiltro.TipoArquivo.XLSX;
		try {
			String extensao = tipoArquivo.name().toLowerCase();
			File file = File.createTempFile("relatorio-geral-", "." + extensao);
			DummyUtils.deleteOnExitFile(file);

			ExcelCsvWriter ecw = new ExcelCsvWriter();

			if(RelatorioGeralFiltro.TipoArquivo.XLSX.equals(tipoArquivo)) {
				ExcelWriter ew = new ExcelWriter();
				ew.criarArquivo(extensao);
				Workbook workbook = ew.getWorkbook();
				ExcelFormat ef = new ExcelFormat(workbook);
				ew.setExcelFormat(ef);
				ew.criaPlanilha(tipo.getPlanilha());
				ecw.setWriter(ew);
			}
			else if(RelatorioGeralFiltro.TipoArquivo.CSV.equals(tipoArquivo)) {
				String absolutePath = file.getAbsolutePath();
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(absolutePath));
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
						.withDelimiter(';')
						.withAllowMissingColumnNames()
						.withTrim()
				);
				ecw.setWriter(csvPrinter);
			}

			ecw.criaLinha(0);

			LinkedMultiValueMap<String, String> headerDinamicoOrdenado;
			if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipo)){
				headerDinamicoOrdenado = renderHeaderRow(filtro, ecw, true);
			}
			else {
				headerDinamicoOrdenado = relatorioIsencaoDisciplinasService.renderCabecalho(ecw);
			}

			if (Tipo.PROCESSOS.equals(tipo)) {
				renderRowsProcessos(ecw, filtro, headerDinamicoOrdenado);
			}
			else if (Tipo.SOLICITACOES.equals(tipo)) {
				renderRowsSolicitacoes(ecw, filtro, headerDinamicoOrdenado);
			}
			else if (Tipo.ISENCAO_DISCIPLINAS.equals(tipo)){
				renderRowsIsencaoDisciplinas(ecw, filtro, headerDinamicoOrdenado);
			}
			else if (Tipo.SITUACOES.equals(tipo)) {
				renderRowsSituacoes(ecw, filtro, headerDinamicoOrdenado);
			}
			else if (Tipo.ETAPAS.equals(tipo)) {
				renderRowsEtapas(ecw, filtro, headerDinamicoOrdenado);
			}

			ecw.close(file);

			return file;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRowsEtapas(ExcelCsvWriter ecw, RelatorioGeralFiltro filtro, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		final List<Long> ids = relatorioGeralEtapaService.findIdsByFiltro(filtro);
		int total = ids.size();
		if(total > GetdocConstants.EXCEL_MAX_ROWS) {
			throw new MessageKeyException("excelLimiteDeLinhas.error", total);
		}

		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			List<RelatorioGeralEtapa> list = relatorioGeralEtapaService.findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				RelatorioGeralEtapa rge = list.get(i);
				RelatorioGeral rg = rge.getRelatorioGeral();

				ecw.criaLinha(rowNum++);
				Tipo tipo = filtro.getTipo();
				renderRowEtapa(ecw, rge);
				renderRow(ecw, rg, true, headerDinamicoOrdenado, tipo);

				Situacao situacaoDe = rge.getSituacaoDe();
				String situacaoDeNome = situacaoDe != null ? situacaoDe.getNome() : "";
				ecw.escrever(situacaoDeNome);

				Situacao situacaoPara = rge.getSituacaoPara();
				String situacaoParaNome = situacaoPara != null ? situacaoPara.getNome() : "";
				ecw.escrever(situacaoParaNome);

				DummyUtils.sleep(1);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
				}
			}

			long fim = System.currentTimeMillis();
			systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderRowEtapa(ExcelCsvWriter ecw, RelatorioGeralEtapa rge) {

		Etapa etapa = rge.getEtapa();
		ecw.escrever(etapa.getNome());

		FaseEtapa fase = etapa.getFase();
		String faseStr = messageService.getValue("FaseEtapa." + fase.name() + ".label");
		ecw.escrever(faseStr);

		Date dataInicio = rge.getDataInicio();
		String dataInicioStr = dataInicio != null ? DummyUtils.formatDate(dataInicio) : "";
		String horaInicioStr = dataInicio != null ? DummyUtils.formatTime2(dataInicio) : "";
		ecw.escrever(dataInicioStr);
		ecw.escrever(horaInicioStr);

		Date dataFim = rge.getDataFim();
		String dataFimStr = dataFim != null ? DummyUtils.formatDate(dataFim) : "";
		String horaFimStr = dataFim != null ? DummyUtils.formatTime2(dataFim) : "";
		ecw.escrever(dataFimStr);
		ecw.escrever(horaFimStr);

		Date prazoLimite = rge.getPrazoLimite();
		String dataLimiteStr = prazoLimite != null ? DummyUtils.formatDate(prazoLimite) : "";
		String horaLimiteStr = prazoLimite != null ? DummyUtils.formatTime(prazoLimite) : "";
		ecw.escrever(dataLimiteStr);
		ecw.escrever(horaLimiteStr);

		BigDecimal tempo = rge.getTempo();
		String tempoStr = getTempoStr(tempo, null);
		ecw.escrever(tempoStr);

		String atendeuPrazo = "";
		Boolean prazoOk = rge.getPrazoOk();
		if(prazoLimite != null && prazoLimite.before(new Date()) || dataFimStr != null) {
			atendeuPrazo = prazoOk ? "Sim" : "Não";
		}
		ecw.escrever(atendeuPrazo);

		int tratativasNaoContada = rge.getTratativasNaoContada();
		ecw.escrever(tratativasNaoContada);

		String usuariosNomes = relatorioGeralEtapaService.getNomesUsuarios(rge);
		ecw.escrever(usuariosNomes);

		Long processoLogInicialId = rge.getProcessoLogInicialId();
		ecw.escrever(processoLogInicialId);
	}

	private String getTempoStr(BigDecimal tempo, Tipo tipoRelatorio) {
		String tempoStr = tempo != null ? tempo.toString() : "";

		if(tempo != null) {
			HorasUteisCalculator huc = processoService.buildHUC();
			BigDecimal jornadaExpediente= huc.getJornadaExpediente();
			int result = tempo.compareTo(jornadaExpediente);

			if(result == 1 && Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
				Long tempoLong = tempo.longValue();
				Long jornadaExpedienteLong = jornadaExpediente.longValue();
				Long result2 = (tempoLong / jornadaExpedienteLong);
				tempoStr = Long.toString(result2);
				return tempoStr;
			}
			else {
				int index = tempoStr.indexOf(".");
				Double minutos = Double.parseDouble(tempoStr.substring(index));
				Integer horas = Integer.parseInt(tempoStr.substring(0, index));
				String tamHoras = horas.toString();
				Double tempoCorrigido = minutos * 60;
				Integer minArredondado = Math.toIntExact(Math.round(tempoCorrigido));
				String tamMinutos = minArredondado.toString();

				int lenght = tamHoras.length();
				if (lenght < 2) {
					tamHoras = "0" + tamHoras;
				}
				int lenght2 = tamMinutos.length();
				if (lenght2 < 2) {
					tamMinutos = "0" + tamMinutos;
				}
				tempoStr = (tamHoras + ":" + tamMinutos);
			}
		}
		return tempoStr;
	}

	private void renderRowsProcessos(ExcelCsvWriter ecw, RelatorioGeralFiltro filtro, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		final List<Long> ids = relatorioGeralRepository.fingIdsByFiltro(filtro);
		int total = ids.size();
		if(total > GetdocConstants.EXCEL_MAX_ROWS) {
			throw new MessageKeyException("excelLimiteDeLinhas.error", total);
		}

		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			List<RelatorioGeral> list = relatorioGeralRepository.findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				RelatorioGeral rg = list.get(i);

				ecw.criaLinha(rowNum++);
				Tipo tipo = filtro.getTipo();
				renderRow(ecw, rg, true, headerDinamicoOrdenado, tipo);

				DummyUtils.sleep(1);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderRowsSolicitacoes(ExcelCsvWriter ecw, RelatorioGeralFiltro filtro, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		final List<Long> ids = relatorioGeralSolicitacaoService.findIdsByFiltro(filtro);
		int total = ids.size();
		if(total > GetdocConstants.EXCEL_MAX_ROWS) {
			throw new MessageKeyException("excelLimiteDeLinhas.error", total);
		}

		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			List<RelatorioGeralSolicitacao> list = relatorioGeralSolicitacaoService.findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				RelatorioGeralSolicitacao rgs = list.get(i);
				RelatorioGeral relatorioGeral = rgs.getRelatorioGeral();

				ecw.criaLinha(rowNum++);

				renderRowSolicitacao(ecw, rgs);
				Tipo tipo = filtro.getTipo();
				renderRow(ecw, relatorioGeral, false, headerDinamicoOrdenado, tipo);

				DummyUtils.sleep(1);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderRowsSituacoes(ExcelCsvWriter ecw, RelatorioGeralFiltro filtro, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		final List<Long> ids = relatorioGeralSituacaoService.findIdsByFiltro(filtro);
		int total = ids.size();
		if(total > GetdocConstants.EXCEL_MAX_ROWS) {
			throw new MessageKeyException("excelLimiteDeLinhas.error", total);
		}

		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			List<RelatorioGeralSituacao> list = relatorioGeralSituacaoService.findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				RelatorioGeralSituacao rgs = list.get(i);
				RelatorioGeral relatorioGeral = rgs.getRelatorioGeral();

				ecw.criaLinha(rowNum++);
				Tipo tipo = filtro.getTipo();
				renderRowSituacao(ecw, rgs, tipo, null);
				renderRow(ecw, relatorioGeral, true, headerDinamicoOrdenado, tipo);

				DummyUtils.sleep(1);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderRowsIsencaoDisciplinas(ExcelCsvWriter ecw, RelatorioGeralFiltro filtro, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		final List<Long> ids = relatorioGeralSituacaoService.findIdsByFiltro(filtro);
		int total = ids.size();
		if(total > GetdocConstants.EXCEL_MAX_ROWS) {
			throw new MessageKeyException("excelLimiteDeLinhas.error", total);
		}

		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		List<RelatorioGeralSituacao> listFinal = new ArrayList<>();
		int rowNum = 1;
		Integer count = 1;
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			List<RelatorioGeralSituacao> list = relatorioGeralSituacaoService.findByIds(ids2);
			listFinal.addAll(list);
		}
		while(!ids.isEmpty());

		if (listFinal != null && !listFinal.isEmpty()) {
			listFinal = listFinal.stream().sorted((p1, p2) -> p1.getData().compareTo(p2.getData())).collect(Collectors.toList());
			listFinal = listFinal.stream().sorted((p1, p2) -> p1.getProcessoId().compareTo(p2.getProcessoId())).collect(Collectors.toList());
		}

		Long idProcesso = 0l;
		String statusAnterior = null;
		for (int i = 0; i < listFinal.size(); i++) {

			RelatorioGeralSituacao rgs = listFinal.get(i);
			RelatorioGeral relatorioGeral = rgs.getRelatorioGeral();
			Situacao situacaoAnterior = rgs.getSituacaoAnterior();

			Situacao situacao = rgs.getSituacao();
			StatusProcesso status = situacao.getStatus();
			String statusNome = status.name();

			if (i == 0) {
				idProcesso = rgs.getProcessoId();
				statusAnterior = statusNome;
			} else {
				if (idProcesso.equals(rgs.getProcessoId())) {
					if (!statusAnterior.equals(statusNome) && !statusNome.equals(StatusProcesso.CONCLUIDO.name())) {
						++count;
					} else if (statusAnterior.equals(statusNome) && statusNome.equals(StatusProcesso.CONCLUIDO.name())) {
						++count;
					}
				} else {
					count = 1;
				}
				statusAnterior = statusNome;
				idProcesso = rgs.getProcessoId();
			}

			ecw.criaLinha(rowNum++);
			Tipo tipo = filtro.getTipo();
			renderRow(ecw, relatorioGeral, true, headerDinamicoOrdenado, tipo);

			renderRowSituacao(ecw, rgs, tipo, count);

			DummyUtils.sleep(1);

			if(rowNum % 100 == 0) {
				long fim = System.currentTimeMillis();
				systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
			}
		}

		Session session = sessionFactory.getCurrentSession();
		session.clear();
	}

	public void renderRow(ExcelCsvWriter ecw, RelatorioGeral rg, boolean carregarCamposDinamicos, LinkedMultiValueMap<String, String> headerDinamicoOrdenado, Tipo tipoRelatorio) {

		Long id = rg.getId();
		ecw.escrever(id);

		Long processoId = rg.getProcessoId();
		ecw.escrever(processoId);

		TipoProcesso tipoProcesso = rg.getTipoProcesso();
		String tipoProcessoNome = tipoProcesso == null ? "": tipoProcesso.getNome();
		ecw.escrever(tipoProcessoNome);

		String nome = rg.getNome();
		ecw.escrever(nome);

		String nomeSocial = rg.getNomeSocial();
		ecw.escrever(nomeSocial);

		String cpf = rg.getCpfCnpj();
		ecw.escrever(cpf);

		String passaporte = rg.getPassaporte();
		ecw.escrever(passaporte);

		String identidade = rg.getIdentidade();
		ecw.escrever(identidade);

		String orgaoEmissor = rg.getOrgaoEmissor();
		ecw.escrever(orgaoEmissor);

		Date dataEmissao = rg.getDataEmissao();
		String dataEmissaoStr = DummyUtils.formatDate(dataEmissao);
		ecw.escrever(dataEmissaoStr);

		String mae = rg.getNomeMae();
		ecw.escrever(mae);

		String pai = rg.getNomePai();
		ecw.escrever(pai);

		Date dataCriacao = rg.getDataCriacao();
		String dataCriacaoStr = DummyUtils.formatDate(dataCriacao);
		ecw.escrever(dataCriacaoStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaCriacaoStr = DummyUtils.formatTime(dataCriacao);
			ecw.escrever(horaCriacaoStr);
		}

		Date dataEnvioAnalise = rg.getDataEnvioAnalise();
		String dataEnvioAnaliseStr = dataEnvioAnalise != null ? DummyUtils.formatDate(dataEnvioAnalise) : "";
		ecw.escrever(dataEnvioAnaliseStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaEnvioAnaliseStr = dataEnvioAnalise != null ? DummyUtils.formatTime(dataEnvioAnalise) : "";
			ecw.escrever(horaEnvioAnaliseStr);
		}

		Date dataEmAnalise = rg.getDataEmAnalise();
		String dataEmAnaliseStr = dataEmAnalise != null ? DummyUtils.formatDate(dataEmAnalise) : "";
		ecw.escrever(dataEmAnaliseStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaEmAnaliseStr = dataEmAnalise != null ? DummyUtils.formatTime(dataEmAnalise) : "";
			ecw.escrever(horaEmAnaliseStr);
		}

		BigDecimal horasPrazoAnalise = rg.getHorasPrazoAnalise();
		ecw.escrever(horasPrazoAnalise);

		Date prazoLimiteAnalise = rg.getPrazoLimiteAnalise();
		String prazoLimiteAnaliseStr = prazoLimiteAnalise != null ? DummyUtils.formatDate(prazoLimiteAnalise) : "";
		ecw.escrever(prazoLimiteAnaliseStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaLimiteAnaliseStr = prazoLimiteAnalise != null ? DummyUtils.formatTime(prazoLimiteAnalise) : "";
			ecw.escrever(horaLimiteAnaliseStr);
		}

		Date dataFinalizacaoAnalise = rg.getDataFinalizacaoAnalise();
		String dataFinalizacaoAnaliseStr = dataFinalizacaoAnalise != null ? DummyUtils.formatDate(dataFinalizacaoAnalise) : "";
		ecw.escrever(dataFinalizacaoAnaliseStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaFinalizacaoAnaliseStr = dataFinalizacaoAnalise != null ? DummyUtils.formatTime(dataFinalizacaoAnalise) : "";
			ecw.escrever(horaFinalizacaoAnaliseStr);
		}

		Date dataEmAcompanhamento = rg.getDataEmAcompanhamento();
		String dataEmAcompanhamentoStr = dataEmAcompanhamento != null ? DummyUtils.formatDate(dataEmAcompanhamento) : "";
		ecw.escrever(dataEmAcompanhamentoStr);
		String horaEmAcompanhamentoStr = dataEmAcompanhamento != null ? DummyUtils.formatTime(dataEmAcompanhamento) : "";
		ecw.escrever(horaEmAcompanhamentoStr);

		Date prazoLimiteEmAcompanhamento = rg.getPrazoLimiteEmAcompanhamento();
		String prazoLimiteEmAcompanhamentoStr = prazoLimiteEmAcompanhamento != null ? DummyUtils.formatDate(prazoLimiteEmAcompanhamento) : "";
		ecw.escrever(prazoLimiteEmAcompanhamentoStr);
		String horaLimiteEmAcompanhamentoStr = prazoLimiteEmAcompanhamento != null ? DummyUtils.formatTime(prazoLimiteEmAcompanhamento) : "";
		ecw.escrever(horaLimiteEmAcompanhamentoStr);

		Date dataFinalizacaoEmAcompanhamento = rg.getDataFinalizacaoEmAcompanhamento();
		String dataFinalizacaoEmAcompanhamentoStr = dataFinalizacaoEmAcompanhamento != null ? DummyUtils.formatDate(dataFinalizacaoEmAcompanhamento) : "";
		ecw.escrever(dataFinalizacaoEmAcompanhamentoStr);
		String horaFinalizacaoEmAcompanhamentoStr = dataFinalizacaoEmAcompanhamento != null ? DummyUtils.formatTime(dataFinalizacaoEmAcompanhamento) : "";
		ecw.escrever(horaFinalizacaoEmAcompanhamentoStr);

		Date dataUltimaTratativa = rg.getDataUltimaTratativa();
		if (dataUltimaTratativa == null) {
			ProcessoLog lastLog = processoLogService.findLastLogByProcesso(processoId, null);
			dataUltimaTratativa = lastLog.getData();
			rg.setDataUltimaTratativa(dataUltimaTratativa);
		}
		String dataUltimaTratativaStr = dataUltimaTratativa != null ? DummyUtils.formatDate(dataUltimaTratativa) : "";
		ecw.escrever(dataUltimaTratativaStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaUltimaTratativaStr = dataUltimaTratativa != null ? DummyUtils.formatTime(dataUltimaTratativa) : "";
			ecw.escrever(horaUltimaTratativaStr);
		}

		Date dataFinalizacao = rg.getDataFinalizacao();
		String dataFinalizacaoStr = dataFinalizacao != null ? DummyUtils.formatDate(dataFinalizacao) : "";
		ecw.escrever(dataFinalizacaoStr);
		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String horaFinalizacaoStr = dataFinalizacao != null ? DummyUtils.formatTime(dataFinalizacao) : "";
			ecw.escrever(horaFinalizacaoStr);
		}

		Situacao situacao = rg.getSituacao();
		String situacaoNome = situacao != null ? situacao.getNome() : "";
		ecw.escrever(situacaoNome);

		StatusProcesso status = rg.getStatus();
		String statusStr = messageService.getValue("StatusProcesso." + status.name() + ".label");
		ecw.escrever(statusStr);

		Integer nivelPrioridade = rg.getNivelPrioridade();
		ecw.escrever(nivelPrioridade);

		Usuario autor = rg.getAutor();
		String autorNome = autor != null ? autor.getNome() : "";
		ecw.escrever(autorNome);

		String autorLogin = autor != null ? autor.getLogin() : "";
		ecw.escrever(autorLogin);

		String autorRole = autor != null ? messageService.getValue("RoleGD." + autor.getRoleGD() + ".label") : "";
		ecw.escrever(autorRole);

		Usuario analista = rg.getAnalista();
		String analistaNome = analista != null ? analista.getNome() : "";
		ecw.escrever(analistaNome);

		String analistaLogin = analista != null ? analista.getLogin() : "";
		ecw.escrever(analistaLogin);

		BigDecimal tempoRascunho = rg.getTempoRascunho();
		ecw.escrever(tempoRascunho);

		Integer vezesAguardandoAnalise = rg.getVezesAguardandoAnalise();
		BigDecimal tempoAguardandoAnalise = rg.getTempoAguardandoAnalise();
		ecw.escrever(tempoAguardandoAnalise);

		Integer vezesAguardandoAnaliseStr = vezesAguardandoAnalise > 0 ? vezesAguardandoAnalise : null;
		ecw.escrever(vezesAguardandoAnaliseStr);

		Integer vezesPendente = rg.getVezesPendente();
		BigDecimal tempoPendente = rg.getTempoPendente();
		ecw.escrever(tempoPendente);

		String vezesPendenteStr = vezesPendente > 0 ? vezesPendente.toString() : null;
		ecw.escrever(vezesPendenteStr);

		Integer vezesEmAcompanhamento = rg.getVezesEmAcompanhamento();
		BigDecimal tempoEmAcompanhamento = rg.getTempoEmAcompanhamento();
		ecw.escrever(tempoEmAcompanhamento);

		String vezesEmAcompanhamentoStr = vezesEmAcompanhamento > 0 ? vezesEmAcompanhamento.toString() : null;
		ecw.escrever(vezesEmAcompanhamentoStr);

		Integer vezesEmAnalise = rg.getVezesEmAnalise();
		BigDecimal tempoEmAnalise = rg.getTempoEmAnalise();
		ecw.escrever(tempoEmAnalise);

		String vezesEmAnaliseStr = vezesEmAnalise > 0 ? vezesEmAnalise.toString() : null;
		ecw.escrever(vezesEmAnaliseStr);

		BigDecimal tempoAteFinalizacaoAnalise = rg.getTempoAteFinalizacaoAnalise();
		ecw.escrever(tempoAteFinalizacaoAnalise);

		BigDecimal tempoAteFinalizacao = rg.getTempoAteFinalizacao();
		ecw.escrever(tempoAteFinalizacao);

		BigDecimal tempoSlaCriacao = rg.getTempoSlaCriacao();
		ecw.escrever(tempoSlaCriacao);

		BigDecimal horasExpediente = rg.getHorasExpediente();

		if (tempoSlaCriacao != null && horasExpediente != null) {
			BigDecimal dias = tempoSlaCriacao.divideToIntegralValue(horasExpediente);
			ecw.escrever(dias);
		}
		else {
			ecw.escrever("");
		}

		BigDecimal tempoSlaTratativa = rg.getTempoSlaTratativa();
		ecw.escrever(tempoSlaTratativa);
		if (tempoSlaTratativa != null && horasExpediente != null) {
			BigDecimal dias = tempoSlaTratativa.divideToIntegralValue(horasExpediente);
			ecw.escrever(dias);
		}
		else {
			ecw.escrever("");
		}

		if (carregarCamposDinamicos) {
			String camposDinamicos = rg.getCamposDinamicos();
			if(headerDinamicoOrdenado != null && camposDinamicos != null) {
				Map<String, Map<String, String>> mapCamposDinamicos = (Map<String, Map<String, String>>) DummyUtils.jsonStringToMap(camposDinamicos);
				for (String grupo : headerDinamicoOrdenado.keySet()) {
					List<String> campos = headerDinamicoOrdenado.get(grupo);
					for (String nomeCampo : campos) {

						nomeCampo = StringUtils.upperCase(nomeCampo);
						Map<String, String> camposValores = mapCamposDinamicos.get(grupo);

						if (camposValores == null) {
							camposValores = buscarCamposValoresEmSubgrupos(mapCamposDinamicos, grupo, nomeCampo, camposValores);
						}

						String valor = camposValores != null ? camposValores.get(nomeCampo) : "";
						String campoDesc = nomeCampo + SUFIX_DESC_CAMPO_BASE_INTERNA;
						String valor2 = DummyUtils.limparCharsChaveUnicidade(valor);

						if(camposValores != null && camposValores.containsKey(campoDesc)) {
							valor = camposValores.get(campoDesc);
						}

						ecw.escrever(valor);

						if(RelatorioGeralCampos.CAMPOS_COD.contains(nomeCampo)){
							ecw.escrever(valor2);
						}

						if(CampoMap.CampoEnum.CAMPUS.getNome().equals(nomeCampo)){
							List<RegistroValorVO> valoresByBaseInternaAndChaveUnicidade = baseRegistroService.findValoresByBaseInternaAndChaveUnicidade(BaseInterna.CAMPUS_ID, valor2);
							if(!valoresByBaseInternaAndChaveUnicidade.isEmpty()) {
								RegistroValorVO registroValorVO = valoresByBaseInternaAndChaveUnicidade.get(0);
								Map<String, BaseRegistroValor> mapColunaRegistroValor = registroValorVO.getMapColunaRegistroValor();
								BaseRegistroValor baseRegistroValor = mapColunaRegistroValor.get(TipoCampo.POLO_PARCEIRO);
								String nome1 = baseRegistroValor != null ? baseRegistroValor.getValor() : "";
								ecw.escrever(nome1);
							}
							else{
								ecw.escrever("");
							}
						}
					}
				}
			}
		}

		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			String acaoFinanciamentos = situacao.getAcaoFinanciamentos();
			ecw.escrever(acaoFinanciamentos);
		}

		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {
			ProcessoLogFiltro filtro1 = new ProcessoLogFiltro();
			filtro1.setProcessoId(processoId);
			filtro1.setAcaoList(Arrays.asList(AcaoProcesso.REGISTRO_EVIDENCIA));
			filtro1.setOrdenar("pl.data", SortOrder.DESCENDING);
			filtro1.setTipoEvidencia(Arrays.asList(new Long(TipoEvidencia.EVIDENCIA_PROUNI)));
			List<ProcessoLog> result1 = processoLogService.findByFiltro(filtro1, 0, 1);
			ProcessoLog logRegistroEvidencia = result1.isEmpty() ? null : result1.get(0);
			String registroEvidencia = logRegistroEvidencia != null ? logRegistroEvidencia.getObservacaoCurta2(500) : null;
			ecw.escrever(registroEvidencia);
		}
	}

	private Map<String, String> buscarCamposValoresEmSubgrupos(Map<String, Map<String, String>> mapCamposDinamicos, String grupo, String nomeCampo, Map<String, String> camposValores) {

		Set<String> nomesGruposDinamicos = mapCamposDinamicos.keySet();
		for (String nomeGrupoDinamico : nomesGruposDinamicos) {

			if (nomeGrupoDinamico.contains(grupo)) {

				Map<String, String> camposValoresSubgrupo = mapCamposDinamicos.get(nomeGrupoDinamico);

				if (camposValoresSubgrupo != null && camposValoresSubgrupo.containsKey("GRUPO SUPERIOR")) {

					for (String nomeCampoSubgrupo : camposValoresSubgrupo.keySet()) {

						if (nomeCampoSubgrupo.equalsIgnoreCase(nomeCampo)) {

							String index = camposValoresSubgrupo.get("INDEX");

							String valorCampoSubgrupo = camposValoresSubgrupo.get(nomeCampoSubgrupo);

							if(CampoMap.CampoEnum.IES_DE_ORIGEM.getNome().equals(nomeCampo)){
								valorCampoSubgrupo = baseRegistroService.getLabel(BaseInterna.IES_DE_ORIGEM_ISENCAO, valorCampoSubgrupo);
							}

							if(CampoMap.CampoEnum.CURSO_DE_ORIGEM.getNome().equals(nomeCampo)){
								valorCampoSubgrupo = baseRegistroService.getLabel(BaseInterna.CURSO_DE_ORIGEM_ISENCAO, valorCampoSubgrupo);
							}

							valorCampoSubgrupo = "(" + index + ") " + valorCampoSubgrupo;

							camposValores = camposValores == null ? new HashMap<>() : camposValores;
							String valorConcatenadoSubgrupos = camposValores.get(nomeCampoSubgrupo);
							valorConcatenadoSubgrupos = valorConcatenadoSubgrupos == null ? valorCampoSubgrupo : valorConcatenadoSubgrupos + "\n" + valorCampoSubgrupo;

							camposValores.put(nomeCampoSubgrupo, valorConcatenadoSubgrupos);
						}
					}
				}
			}
		}
		return camposValores;
	}

	private void renderRowSolicitacao(ExcelCsvWriter ecw, RelatorioGeralSolicitacao rs) {

		Long solicitacaoId = rs.getSolicitacaoId();
		ecw.escrever(solicitacaoId);

		Area area = rs.getArea();
		String areaDescricao = area.getDescricao();
		ecw.escrever(areaDescricao);

		Usuario analistaSolicitante = rs.getAnalistaSolicitante();
		String analistaSolicitanteNome = analistaSolicitante != null ? analistaSolicitante.getNome() : "";
		ecw.escrever(analistaSolicitanteNome);

		AcaoProcesso acao = rs.getAcao();
		String acaoStr = messageService.getValue("AcaoProcesso." + acao + ".label");
		ecw.escrever(acaoStr);

		Date dataSolicitacao = rs.getDataSolicitacao();
		String dataSolicitacaoStr = dataSolicitacao != null ? DummyUtils.formatDate(dataSolicitacao) : "";
		ecw.escrever(dataSolicitacaoStr);
		String horaSolicitacaoStr = dataSolicitacao != null ? DummyUtils.formatTime(dataSolicitacao) : "";
		ecw.escrever(horaSolicitacaoStr);

		Date prazoSolicitacao = rs.getPrazoLimite();
		String prazoSolicitacaoStr = prazoSolicitacao != null ? DummyUtils.formatDate(prazoSolicitacao) : "";
		ecw.escrever(prazoSolicitacaoStr);
		String horaPrazoSolicitacaoStr = prazoSolicitacao != null ? DummyUtils.formatTime(prazoSolicitacao) : "";
		ecw.escrever(horaPrazoSolicitacaoStr);

		Date finalizacaoSolicitacao = rs.getDataFinalizacao();
		String finalizacaoSolicitacaoStr = finalizacaoSolicitacao != null ? DummyUtils.formatDate(finalizacaoSolicitacao) : "";
		ecw.escrever(finalizacaoSolicitacaoStr);
		String horaFinalizacaoSolicitacaoStr = finalizacaoSolicitacao != null ? DummyUtils.formatTime(finalizacaoSolicitacao) : "";
		ecw.escrever(horaFinalizacaoSolicitacaoStr);

		StatusSolicitacao status = rs.getStatus();
		String statusStr = messageService.getValue("StatusSolicitacao." + status + ".label");
		ecw.escrever(statusStr);
	}

	private void renderRowSituacao(ExcelCsvWriter ecw, RelatorioGeralSituacao rs, Tipo tipoRelatorio, Integer contadorProcesso) {

		if(!Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)){
			Long id = rs.getId();
			ecw.escrever(id);
		}

		Situacao situacao = rs.getSituacao();
		StatusProcesso status = situacao.getStatus();
		String statusNome = status.name();
		ecw.escrever(statusNome);

		String situacaoNome = situacao.getNome();
		ecw.escrever(situacaoNome);

		Situacao situacaoAnterior = rs.getSituacaoAnterior();
		if (situacaoAnterior != null) {
			ecw.escrever(situacaoAnterior.getNome());
		}
		else {
			ecw.escrever("");
		}

		ecw.escrever(rs.getTempo());

		Date data = rs.getData();
		String dataStr = data != null ? DummyUtils.formatDate(data) : "";
		ecw.escrever(dataStr);
		String horaStr = data != null ? DummyUtils.formatTime(data) : "";
		ecw.escrever(horaStr);

		data = rs.getDataFim();
		dataStr = data != null ? DummyUtils.formatDate(data) : "";
		ecw.escrever(dataStr);
		horaStr = data != null ? DummyUtils.formatTime(data) : "";
		ecw.escrever(horaStr);

		Long calc = rs.getTempoTratativa();
		BigDecimal tempoTrativa = DummyUtils.formatarMilisegundosParaHoras(calc);
		ecw.escrever(tempoTrativa);

		if(Tipo.ISENCAO_DISCIPLINAS.equals(tipoRelatorio)) {

			Etapa etapa = situacao.getEtapa();
			boolean etapaFinal = etapa.getEtapaFinal();
			RelatorioGeral relatorioGeral = rs.getRelatorioGeral();

			Long processoId = rs.getProcessoId();
			Long relatorioGeralId = relatorioGeral.getId();
			Date dataInicial = rs.getData();

			RelatorioGeralFiltro relatorioGeralFiltro = new RelatorioGeralFiltro();
			relatorioGeralFiltro.setRelatorioGeralId(relatorioGeralId);
			relatorioGeralFiltro.setDataInicio(dataInicial);
			relatorioGeralFiltro.setDataFim(data);

			if(etapaFinal) {
				Etapa etapaAnterior = situacaoAnterior.getEtapa();
				relatorioGeralFiltro.setEtapa(etapaAnterior);
			} else {
				relatorioGeralFiltro.setEtapa(etapa);
			}

			List<RelatorioGeralEtapa> rgeList = relatorioGeralEtapaService.findByFiltro(relatorioGeralFiltro, null, null);

			RelatorioGeralEtapa  relatorioGeralEtapa = new RelatorioGeralEtapa();

			if(rgeList.size() > 1) {
				for(RelatorioGeralEtapa rge: rgeList) {
					Date rgeDataInicio = rge.getDataInicio();
					if(dataInicial.equals(rgeDataInicio) || etapaFinal) {
						relatorioGeralEtapa = rge;
						break;
					}
				}
			}
			else if(rgeList.size() == 1) {
				relatorioGeralEtapa = rgeList.get(0);
			}

			if (!rgeList.isEmpty()) {

				Date dataInicio = relatorioGeralEtapa.getDataInicio();
				String dataInicioStr = dataInicio != null ? DummyUtils.formatDate(dataInicio) : "";
				String horaInicioStr = dataInicio != null ? DummyUtils.formatTime2(dataInicio) : "";
				ecw.escrever(dataInicioStr);
				ecw.escrever(horaInicioStr);

				Date dataFim = relatorioGeralEtapa.getDataFim();
				String dataFimStr = dataFim != null ? DummyUtils.formatDate(dataFim) : "";
				String horaFimStr = dataFim != null ? DummyUtils.formatTime2(dataFim) : "";
				ecw.escrever(dataFimStr);
				ecw.escrever(horaFimStr);

				Date prazoLimite = relatorioGeralEtapa.getPrazoLimite();
				String dataLimiteStr = prazoLimite != null ? DummyUtils.formatDate(prazoLimite) : "";
				String horaLimiteStr = prazoLimite != null ? DummyUtils.formatTime(prazoLimite) : "";
				ecw.escrever(dataLimiteStr);
				ecw.escrever(horaLimiteStr);

				BigDecimal tempo = relatorioGeralEtapa.getTempo();
				String tempoStr = getTempoStr(tempo, tipoRelatorio);
				ecw.escrever(tempoStr);
			} else {
				ecw.escrever("");
				ecw.escrever("");
				ecw.escrever("");
				ecw.escrever("");
				ecw.escrever("");
				ecw.escrever("");
				ecw.escrever("");
			}

			ecw.escrever(processoId + "" + contadorProcesso);


			data = rs.getData();

			Usuario	analistaLog = processoLogService.getAnalistaByProcessoIdAndData(processoId, data);

			if(analistaLog == null){
				ecw.escrever("");
				ecw.escrever("");
			} else {
				ecw.escrever(analistaLog.getNome());
				Subperfil subperfilAtivo = analistaLog.getSubperfilAtivo();
				if(subperfilAtivo != null) {
					String subperfilAtivoDescricao = subperfilAtivo.getDescricao();
					ecw.escrever(subperfilAtivoDescricao);
				} else {
					ecw.escrever("");
				}
			}
		}
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralRepository.findIdsByFiltro(filtro);
	}

	public List<RelatorioGeral> findByIds(List<Long> ids) {
		return relatorioGeralRepository.findByIds(ids);
	}

	@Transactional(rollbackFor = Exception.class)
	public Date atualizarDataUltimaAtualizacaoProcessos(Date inicio) throws Exception {

		if (inicio == null) {
			inicio = DummyUtils.parseDate("12/12/2020");
		}

		Date fim = getDataFimExecucaoAtualizacaoDatasProcesso(inicio);

		systraceThread("Iniciando atualização com inicio=" + DummyUtils.formatDateTime(inicio) + " e fim=" + DummyUtils.formatDateTime(fim));

		Map<Long, Date> maxDataProcessoLogParaProcessoId = processoLogService.findMapToAtualizarDataUltimaAtualizacao(inicio, fim);

		int qntProcessos = maxDataProcessoLogParaProcessoId.values().size();
		systraceThread("Qnt processos encontrados=" + qntProcessos);

		int i = 0;
		for (Map.Entry<Long, Date> entry : maxDataProcessoLogParaProcessoId.entrySet()) {

			Long processoId = entry.getKey();
			Date maxDataProcessoLog = entry.getValue();

			if (i % 1000 == 0 && i != 0) {
				systraceThread("Processou (" + i + ") de (" + qntProcessos + ")");
			}

			processoService.atualizarDataUltimaAtualizacao(processoId, maxDataProcessoLog);

			i++;
		}

		LogAcesso la = LogAcessoFilter.getLogAcesso();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			parametroService.setValorDate(P.ULTIMA_DATA_ATUALIZACAO_DATAS_PROCESSO, fim);
			DummyUtils.addParameter(la, "ultimaDataAtualizacaoDatasProcessoFim", DummyUtils.formatDateTime2(fim));
		});
		tw.runNewThread();
		tw.throwException();

		systraceThread("[data] atualizado de " + DummyUtils.formatDateTime(inicio) + " a " + DummyUtils.formatDateTime(fim) + ". " + qntProcessos + " processos. fim ");
		return fim;
	}

	private Date getDataFimExecucaoAtualizacaoDatasProcesso(Date dataInicio) {

		Date agora = new Date();
		long intervaloEmHoras = MILLISECONDS.toHours(agora.getTime() - dataInicio.getTime());

		Date dataFim;
		if (intervaloEmHoras > 2) {
			dataFim = DateUtils.addMinutes(dataInicio, 90);
		}
		else {
			dataFim = agora;
		}

		return dataFim;
	}

	@Transactional
	public void atualizarRelatorioGeral(List<Long> processosList) {

		if(CollectionUtils.isEmpty(processosList)) {
			return;
		}

		int length = processosList.size();
		int count = 1;
		systraceThread("Processando relatorio geral qtd: " + length + " processos...");
		for (Long processoId : processosList) {

			systraceThread("Processo " + processoId + " " + count + " de " + length);

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				Processo processo = processoService.get(processoId);
				Map<Long, RelatorioGeral> mapRelatorio = new HashMap<>();
				RelatorioGeral relatorioGeral = getByProcessoAndCriaCasoNaoExista(processoId);
				mapRelatorio.put(processoId, relatorioGeral);

				RelatorioGeral rg = criaRelatorioGeral(processo, mapRelatorio, null);
				saveOrUpdate(rg);

				List<Long> processosIds2 = Arrays.asList(processoId);
				Map<Long, RelatorioGeralSolicitacao> mapRelatorioSolicitacao = relatorioGeralSolicitacaoService.findByProcessosIds(processosIds2);
				Map<Date, RelatorioGeralSituacao> mapRelatorioSituacao = relatorioGeralSituacaoService.findByProcessosIdsMap(processosIds2);

				Situacao situacao = processo.getSituacao();
				Long situacaoId = situacao.getId();
				HorasUteisCalculator huc = processoService.buildHUC(situacaoId);

				relatorioGeralSolicitacaoService.criaRelatorioGeral(processo, rg, mapRelatorioSolicitacao, huc);
				relatorioGeralSituacaoService.criaRelatorioGeral(processo, rg, mapRelatorioSituacao, huc);
				relatorioGeralEtapaService.criaRelatorioGeral(rg, huc);
			});
			tw.runNewThread();
			count++;
		}
	}

	public List<Processo> verificaCampoFiltroByProcesso(RelatorioGeralFiltro filtro){

		ProcessoFiltro filtroProcesso = new ProcessoFiltro();
		List <CampoDinamicoVO> camposFiltro =  filtro.getCamposFiltro();

		filtroProcesso.setCamposFiltro(camposFiltro);

		int count = processoService.countByFiltro(filtroProcesso);

		List<Processo> processosByCampoFiltro = processoService.findByFiltro(filtroProcesso, null, null);

		return processosByCampoFiltro;
	}

	public void comparaProcessosCriaCasoNaoExista (List<Processo> listProcesso, RelatorioGeralFiltro filtro){

		List <RelatorioGeral> listRelatorioGeral = findByFiltro(filtro, null, null);
		boolean existeNaListaProcesso = false;

		for(Processo p : listProcesso){
			Long processoId = p.getId();
			for(RelatorioGeral rg : listRelatorioGeral){
				Long rgProcessoId = rg.getProcessoId();
				if(processoId.equals(rgProcessoId)){
					existeNaListaProcesso = true;
					break;
				}
			}
			if(existeNaListaProcesso == false){
				getByProcessoAndCriaCasoNaoExista(processoId);
			}
		}
	}

	public Integer retornaContador(RelatorioGeralSituacao rgs, Integer count){

		Situacao situacaoAtaul = rgs.getSituacao();
		Situacao situacaoAnterior = rgs.getSituacaoAnterior();
		Etapa etapa = situacaoAtaul.getEtapa();
		boolean etapaFinal = etapa.getEtapaFinal();

		if(etapaFinal){
			count++;
		}

		if(situacaoAnterior == null){
			count = 1;
		}

		return count;
	}
}
