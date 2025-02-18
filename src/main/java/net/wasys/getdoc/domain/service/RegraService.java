package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.RegraRepository;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RegraService {

	private final Pattern NUM_VERSAO_REGEXP = Pattern.compile(".* \\((\\d)\\)$");
	private final Pattern ACESSO_PARAMETRO_JSON_REGEX = Pattern.compile("\\[('|\")(.*?)('|\")\\]");
	private final Pattern PATTERN_VAR = Pattern.compile("\\w+(\\[((('|\")[^'\"]*('|\"))|(\\d+))\\])+");

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ProcessoRegraLogService processoRegraLogService;
	@Autowired private FontesExternasService fontesExternasService;
	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private RegraLinhaService regraLinhaService;
	@Autowired private RegraRepository regraRepository;
	@Autowired private ProcessoService processoService;
	@Autowired private FuncaoJsService funcaoJsService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private DocumentoService documentoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ImagemService imagemService;
	@Autowired private RegraRoleService regraRoleService;
	@Autowired private RegraSubperfilService regraSubperfilService;
	@Autowired private CampoService campoService;
	@Autowired private SubRegraService subRegraService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private PendenciaService pendenciaService;

	public Regra get(Long id) {
		return regraRepository.get(id);
	}

	public Map<String, Set<OrigemValorVO>> buscarDependencias(Regra regra) {

		Map<String, Set<OrigemValorVO>> dependencias = new HashMap<>();
		Long regraId = regra.getId();
		RegraLinha raiz = regraLinhaService.getRaiz(regraId);
		if(raiz != null) {
			Set<SubRegra> subRegras = raiz.getSubRegras();
			SubRegra subRegraRaiz = subRegras.iterator().next();

			buscarDependencias(dependencias, subRegraRaiz);
		}

		return dependencias;
	}

	@Transactional(rollbackFor=Exception.class)
	public Regra duplicar(Regra regra, Usuario usuario) throws MessageKeyException {
		return duplicar(regra, usuario, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public Regra duplicar(Regra regra, Usuario usuario, TipoProcesso tipoProcesso) throws MessageKeyException {

		regra = regraRepository.get(regra.getId());
		List<TipoProcesso> tiposProcessos = new ArrayList<>();
		Situacao situacao = null;

		if(tipoProcesso != null) {
			tiposProcessos.add(tipoProcesso);
		}
		else {
			situacao = regra.getSituacao();
			Set<RegraTipoProcesso> rtps = regra.getTiposProcessos();
			for (RegraTipoProcesso rtp : rtps) {
				TipoProcesso tipoProcesso2 = rtp.getTipoProcesso();
				tiposProcessos.add(tipoProcesso2);
			}
		}

		String descricao = regra.getDescricao();
		String nome = regra.getNome();
		Date inicioVigencia = regra.getInicioVigencia();
		Date fimVigencia = regra.getFimVigencia();
		TipoExecucaoRegra tipoExecucao = regra.getTipoExecucao();
		String nome2;
		int num = 1;
		Matcher matcher = NUM_VERSAO_REGEXP.matcher(nome);
		if(matcher.find()) {
			String group = matcher.group(1);
			num = Integer.parseInt(group);
			nome = nome.substring(0, nome.length() - 4);
		}
		num++;
		boolean exists;
		do {
			nome2 = nome + " (" + num++ + ")";
			exists = regraRepository.existsByNome(nome2);
		}
		while(exists);

		Regra regra2 = new Regra();
		regra2.setAtiva(false);
		regra2.setNome(nome2);
		regra2.setDescricao(descricao);
		regra2.setInicioVigencia(inicioVigencia);
		regra2.setFimVigencia(fimVigencia);
		regra2.setSituacao(situacao);
		regra2.setTipoExecucao(tipoExecucao);

		saveOrUpdate(regra2, usuario, tiposProcessos, null);

		regraLinhaService.duplicar(regra, regra2, usuario);

		return regra2;
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Regra regra, Usuario usuario, List<TipoProcesso> tiposProcessos, List<Subperfil> subperfilList) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(regra);

		regra.setDataAlteracao(new Date());

		Date fimVigencia = regra.getFimVigencia();
		if(fimVigencia != null) {
			fimVigencia = DateUtils.addDays(fimVigencia, 1);
			fimVigencia = DateUtils.addSeconds(fimVigencia, -1);
			regra.setFimVigencia(fimVigencia);
		}

		try {
			if(tiposProcessos != null) {
				Set<RegraTipoProcesso> rtps = regra.getTiposProcessos();
				Set<Long> antigos = new HashSet<>();
				for(RegraTipoProcesso rtp: rtps) {
					TipoProcesso tipoProcesso = rtp.getTipoProcesso();
					Long tipoProcessoId = tipoProcesso.getId();
					antigos.add(tipoProcessoId);
				}
				for(TipoProcesso tp: tiposProcessos) {
					RegraTipoProcesso rtp = new RegraTipoProcesso();
					Long tpId = tp.getId();
					if (antigos.contains(tpId)) {
						antigos.remove(tpId);
						continue;
					}
					rtp.setTipoProcesso(tp);
					rtp.setRegra(regra);
					rtps.add(rtp);
				}
				for (RegraTipoProcesso rtp : new ArrayList<>(rtps)) {
					TipoProcesso tp = rtp.getTipoProcesso();
					Long rtpId = tp.getId();
					if (antigos.contains(rtpId)) {
						rtps.remove(rtp);
					}
				}
			}

			regraRepository.saveOrUpdate(regra);

			Long regraId = regra.getId();
			List<RegraRole> salvos = regraRoleService.findByRegra(regraId);
			if (salvos != null) {
				List<RegraRole> regraRoles = regra.getRegraRoles();
				salvos.removeAll(regraRoles);
				salvos.forEach(s -> {
					Long id = s.getId();
					regraRoleService.delete(id);
				});
			}

			if(subperfilList != null) {
				List<RegraSubperfil> regraSubperfilList2 = new ArrayList<>();
				List<RegraSubperfil> regraSubperfilList = regraSubperfilService.findByRegra(regraId);
				for(RegraSubperfil regraSubperfil : regraSubperfilList) {
					Subperfil subperfil = regraSubperfil.getSubperfil();

					boolean existe = subperfilList.contains(subperfil);
					if(!existe) {
						Long regraSubperfilId = regraSubperfil.getId();
						regraSubperfilService.delete(regraSubperfilId);
					}
					else {
						regraSubperfilList2.add(regraSubperfil);
					}
				}

				for(Subperfil subperfil : subperfilList) {
					boolean existe = false;
					for(RegraSubperfil regraSubperfil : regraSubperfilList2) {
						Subperfil subperfil1 = regraSubperfil.getSubperfil();
						if(subperfil1.equals(subperfil)) {
							existe = true;
							continue;
						}
					}

					if(!existe) {
						RegraSubperfil regraSubperfil = new RegraSubperfil();
						regraSubperfil.setRegra(regra);
						regraSubperfil.setSubperfil(subperfil);
						regraSubperfilService.saveOrUpdate(regraSubperfil);
					}
				}
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(regra, usuario, tipoAlteracao);
	}

	public boolean existsByFiltro(RegraFiltro filtro) {
		return regraRepository.existsByFiltro(filtro);
	}

	public int countByFiltro(RegraFiltro filtro) {
		return regraRepository.countByFiltro(filtro);
	}

	public List<Regra> findByFiltro(RegraFiltro filtro, Integer inicio, Integer max) {
		return regraRepository.findByFiltro(filtro, inicio, max);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long regraId, Usuario usuario) throws MessageKeyException {

		Regra regra = get(regraId);

		logAlteracaoService.registrarAlteracao(regra, usuario, TipoAlteracao.EXCLUSAO);

		try {
			regraRepository.deleteById(regraId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Regra> findAtivosDiferenteDeId(Long regraId) {
		return regraRepository.findAtivosDiferenteDeId(regraId);
	}

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public ProcessoRegra executarRegra(ProcessoRegra pr, Usuario usuario, boolean reprocessar) {
		return executarRegra(pr, usuario, null, reprocessar);
	}

	@Transactional(rollbackFor=Exception.class)
	public ProcessoRegra executarRegraPropagation(ProcessoRegra pr, Usuario usuario, Map<CampoGrupo, List<Campo>> camposMap, boolean reprocessar) {
		regraRepository.deatach(pr);
		return executarRegra(pr, usuario, camposMap, reprocessar);
	}

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public ProcessoRegra executarRegra(ProcessoRegra pr, Usuario usuario, Map<CampoGrupo, List<Campo>> camposMap, boolean reprocessar) {

		systraceThread(pr.getProcesso().getId() + " " + pr.getRegra().getNome());

		Date inicio = new Date();
		Processo processo = pr.getProcesso();
		Regra regra = pr.getRegra();
		Long regraId = regra.getId();

		RegraLinha raiz = regraLinhaService.getRaiz(regraId);
		if(raiz != null) {
			Set<SubRegra> subRegras = raiz.getSubRegras();

			pr.setDataExecucao(inicio);
			if(!subRegras.iterator().hasNext()) {
				throw new MessageKeyException("regra.execucao.semSubRegra.error", regra.getNome());
			}

			SubRegra subRegraRaiz = subRegras.iterator().next();

			RegraEngineVO vo;
			try {
				vo = buildRegraEngineVO(processo, pr, camposMap, reprocessar);
				vo.setUsuario(usuario);
				vo.setProcessoRegra(pr);
				Long processoRegraId = pr.getId();
				if (reprocessar && processoRegraId != null) {
					processoRegraService.merge(pr);
				} else {
					processoRegraService.saveOrUpdate(pr);
				}
			}
			catch (Exception e) {
				Long processoId = processo.getId();
				String regraNome = regra.getNome();
				String exceptionMessage = DummyUtils.getExceptionMessage(e);
				String stackTrace = DummyUtils.getStackTrace(e);
				systraceThread("Erro ao iniciar processamento da regra #" + regraId + " " + regraNome + ". Processo: " + processoId + ": " + exceptionMessage, LogLevel.ERROR);
				e.printStackTrace();
				pr.setMensagem(exceptionMessage);
				pr.setStackTrace(stackTrace);
				pr.setStatus(StatusProcessoRegra.ERRO);
				processoRegraService.saveOrUpdate(pr);
				return pr;
			}

			executarSubRegra(vo, subRegraRaiz, usuario);

			List<ProcessoRegraLog> logs = pr.getProcessoRegraLogs();
			for (ProcessoRegraLog log : logs) {
				processoRegraLogService.saveOrUpdate(log);
			}

			pr.setTempo(System.currentTimeMillis() - inicio.getTime());
			processoRegraService.saveOrUpdate(pr);
		}

		return pr;
	}

	public ProcessoRegra testarRegra(Regra regra, Map<String, Set<OrigemValorVO>> deps, Usuario usuario) {

		ProcessoRegra pr = null;
		Date inicio = new Date();
		Long regraId = regra.getId();
		RegraLinha raiz = regraLinhaService.getRaiz(regraId);
		if(raiz != null) {
			Set<SubRegra> subRegras = raiz.getSubRegras();
			SubRegra subRegraRaiz = subRegras.iterator().next();

			RegraEngineVO vo = buildRegraEngineVOTeste(deps);
			pr = new ProcessoRegra();
			pr.setRegra(regra);
			Date dataAlteracao = regra.getDataAlteracao();
			pr.setDataRegra(dataAlteracao);
			pr.setDataExecucao(inicio);
			vo.setProcessoRegra(pr);

			executarSubRegra(vo, subRegraRaiz, usuario);

			pr.setTempo(System.currentTimeMillis() - inicio.getTime());
		}

		return pr;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<FonteVO> buscarBasesInternasDisponiveis(SubRegra subRegra) {

		List<FonteVO> basesInternasDisponiveis = new ArrayList<>();
		Set<SubRegra> subRegrasPai = subRegra.getSubRegrasPai();
		for (SubRegra subRegraPai : subRegrasPai) {
			buscarBasesInternasUtilizadas(basesInternasDisponiveis, subRegraPai);
		}

		return basesInternasDisponiveis;
	}

	private void buscarBasesInternasUtilizadas(List<FonteVO> basesInternasDisponiveis, SubRegra subRegra) {

		if(subRegra.isTipoBaseInterna()) {
			FonteVO fonteVO = new FonteVO();
			fonteVO.setBaseInterna(subRegra.getBaseInterna());
			fonteVO.setNome(subRegra.getVarConsulta());
			basesInternasDisponiveis.add(fonteVO);
		}

		RegraLinha linha = subRegra.getLinha();
		RegraLinha linhaPai = linha.getLinhaPai();
		if(linhaPai != null) {
			RegraLinha regraLinhaPai = regraLinhaService.get(linhaPai.getId());
			Set<SubRegra> subRegrasPai = regraLinhaPai.getSubRegras();
			for (SubRegra subRegraPai : subRegrasPai) {
				buscarBasesInternasUtilizadas(basesInternasDisponiveis, subRegraPai);
			}
		}
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<FonteVO> buscarConsultasExternasDisponiveis(SubRegra subRegra) {

		List<FonteVO> fontesExternasDisponiveis = new ArrayList<>();
		Set<SubRegra> subRegrasPai = subRegra.getSubRegrasPai();
		for (SubRegra subRegraPai : subRegrasPai) {
			buscarConsultasExternasUtilizadas(fontesExternasDisponiveis, subRegraPai);
		}

		return fontesExternasDisponiveis;
	}

	private void buscarConsultasExternasUtilizadas(List<FonteVO> fontesExternasDisponiveis, SubRegra subRegra) {

		if(subRegra.isTipoConsultaExterna()) {

			TipoConsultaExterna consultaExterna = subRegra.getConsultaExterna();
			FonteExterna fonteExterna = fontesExternasService.findByNome(consultaExterna);

			FonteVO fonteVO = new FonteVO();
			fonteVO.setFonteExterna(fonteExterna);
			fonteVO.setNome(subRegra.getVarConsulta());
			fontesExternasDisponiveis.add(fonteVO);
		}

		RegraLinha linha = subRegra.getLinha();
		RegraLinha linhaPai = linha.getLinhaPai();
		if(linhaPai != null) {
			RegraLinha regraLinhaPai = regraLinhaService.get(linhaPai.getId());
			Set<SubRegra> subRegrasPai = regraLinhaPai.getSubRegras();
			for (SubRegra subRegraPai : subRegrasPai) {
				buscarConsultasExternasUtilizadas(fontesExternasDisponiveis, subRegraPai);
			}
		}
	}

	public List<Long> findTiposProcessoByRegraId(Long regraId) {
		return regraRepository.findTiposProcessoByRegraId(regraId);
	}

	private String montarVariavelJson(String fonte, Set<OrigemValorVO> origemValorVOs) {

		JSONObject json = new JSONObject();
		for (OrigemValorVO origemValor : origemValorVOs) {

			String origem = origemValor.getOrigem();
			String valor = origemValor.getValor();
			if(StringUtils.isNotBlank(valor)) {
				Matcher matcher = ACESSO_PARAMETRO_JSON_REGEX.matcher(origem);

				List<String> hierarquiaObjs = new ArrayList<>();
				while(matcher.find()) {
					String campo = matcher.group(2);
					hierarquiaObjs.add(campo);
				}

				JSONObject last = json;
				for (int i = 0; i < hierarquiaObjs.size(); i++) {
					String objStr = hierarquiaObjs.get(i);
					boolean isNull = last.isNull(objStr);
					Object obj = isNull ? null : last.get(objStr);
					if(obj != null && !(obj instanceof JSONObject))
						continue;
					JSONObject jsonObj = (JSONObject) obj;
					if(i == hierarquiaObjs.size() - 1) {
						last.put(objStr, valor);
					}
					else {
						jsonObj = jsonObj != null ? jsonObj : new JSONObject();
						last.put(objStr, jsonObj);
						last = jsonObj;
					}
				}
			}
		}

		String jsonString = json.toString();
		if(!"{}".equals(jsonString)) {
			jsonString = DummyUtils.stringToJson(jsonString);
			String variavelJs = "var " + fonte + " = " + jsonString + ";\n";
			return variavelJs;
		}

		return "";
	}

	private void buscarDependencias(Map<String, Set<OrigemValorVO>> dependencias, SubRegra subRegra) {

		TipoSubRegra tipo = subRegra.getTipo();
		if(TipoSubRegra.BASE_INTERNA.equals(tipo) || TipoSubRegra.CONSULTA_EXTERNA.equals(tipo)) {

			List<DeparaParam> deparaParams = subRegra.getDeparaParams();
			for (DeparaParam deparaParam : deparaParams) {

				String origem = deparaParam.getOrigem();
				String fonte = deparaParam.getNomeFonte();
				fonte = DummyUtils.formatarNomeVariavel(fonte);

				Set<OrigemValorVO> origemValorVOs = dependencias.get(fonte);
				origemValorVOs = origemValorVOs != null ? origemValorVOs : new LinkedHashSet<>();
				dependencias.put(fonte, origemValorVOs);

				OrigemValorVO origemValor = new OrigemValorVO();
				Long id = deparaParam.getSubRegra().getId();
				origemValor.addSubRegraId(String.valueOf(id));
				origemValor.setOrigem(origem);

				origemValorVOs.add(origemValor);
			}
		}
		else if(TipoSubRegra.CONDICAO.equals(tipo)) {

			String condicionalJs = subRegra.getCondicionalJs();
			Set<String> vars = getVarsCondicao(condicionalJs);

			buscaDependenciasCondicao(dependencias, subRegra, vars);
		}
		else if(TipoSubRegra.FIM.equals(tipo)) {

			String observacao = subRegra.getObservacao();

			if(observacao != null) {
				Set<String> vars = getVarsCondicao(observacao);

				buscaDependenciasCondicao(dependencias, subRegra, vars);
			}
		}

		RegraLinha linha = subRegra.getLinha();
		RegraLinha filha = linha.getFilha();
		if(filha != null) {
			Set<SubRegra> subRegras = filha.getSubRegras();
			for (SubRegra filho : subRegras) {
				buscarDependencias(dependencias, filho);
			}
		}
	}

	private void buscaDependenciasCondicao(Map<String, Set<OrigemValorVO>> dependencias, SubRegra subRegra, Set<String> vars) {

		for (String var : vars) {

			String origem = var.substring(var.indexOf('['), var.length());
			String fonte = var.substring(0, var.indexOf('['));

			Set<OrigemValorVO> origemValorVOs = dependencias.get(fonte);
			origemValorVOs = origemValorVOs != null ? origemValorVOs : new LinkedHashSet<>();
			dependencias.put(fonte, origemValorVOs);

			OrigemValorVO origemValor = new OrigemValorVO();
			origemValor.setOrigem(origem);
			origemValorVOs.add(origemValor);

			Long id = subRegra.getId();
			origemValor.addSubRegraId(String.valueOf(id));
		}
	}

	private Set<String> getVarsCondicao(String condicionalJs) {

		Set<String> vars = new TreeSet<>();

		if(StringUtils.isNotBlank(condicionalJs)) {
			Matcher matcher = PATTERN_VAR.matcher(condicionalJs);

			while(matcher.find()) {
				String group = matcher.group();
				vars.add(group);
			}
		}

		return vars;
	}

	public Set<String> getTodasVarsCondicao(String condicionalJs) {

		Set<String> vars = new LinkedHashSet<>();

		Matcher matcher1 = PATTERN_VAR.matcher(condicionalJs);
		while(matcher1.find()) {
			String group = matcher1.group(0);
			vars.add(group);
		}

		Pattern pattern2 = Pattern.compile("\\([^\\(\\)]+\\)");
		Matcher matcher2 = pattern2.matcher(condicionalJs);
		if(matcher2.find()) {
			String group = matcher2.group(0);
			group = group.replace("(", "");
			group = group.replace(")", "");
			String[] split = group.split(",");
			for (String aux : split) {
				aux = StringUtils.trim(aux);
				if(aux.matches("^[a-zA-Z]+[0-9]*$")) {
					vars.add(aux);
				}
			}
		}

		return vars;
	}

	private RegraEngineVO buildRegraEngineVO(Processo processo, ProcessoRegra pr, Map<CampoGrupo, List<Campo>> camposMap, boolean isReprocessar) {
		Long id = processo.getId();
		Processo processo2 = processoService.get(id);
		processo = processo2 != null ? processo2 : processo;
		RegraEngineVO vo = new RegraEngineVO();

		vo.setReprocessar(isReprocessar);

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine scriptEngine = factory.getEngineByName("JavaScript");
		vo.setScriptEngine(scriptEngine);

		ObjectMapper objectMapper = new ObjectMapper();
		vo.setObjectMapper(objectMapper);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		String nomeProcesso = tipoProcesso.getNome();
		nomeProcesso = DummyUtils.formatarNomeVariavel(nomeProcesso);

		StringBuilder js = new StringBuilder();

		String processoMapJson = getProcessoJson(vo, processo, camposMap);
		processoMapJson = DummyUtils.stringToJson(processoMapJson);
		js.append("var " + nomeProcesso + " = ").append(processoMapJson).append(";\n");

		String regrasDaSituacaoJson = getRegrasDaSituacaoJson(objectMapper, processo, pr);
		regrasDaSituacaoJson = DummyUtils.stringToJson(regrasDaSituacaoJson);
		if(StringUtils.isNotBlank(regrasDaSituacaoJson)) {
			js.append("var RegrasDaSituacao = ").append(regrasDaSituacaoJson).append(";\n");
		}

		vo.setJs(js);
		return vo;
	}

	private String getRegrasDaSituacaoJson(ObjectMapper om, Processo processo, ProcessoRegra pr) {

		Map<String, Object> regrasDaSituacaoMap = new HashMap<>();

		Regra regra = pr.getRegra();
		Integer decisaoFluxo = regra.getDecisaoFluxo();
		if(decisaoFluxo == null) {
			return null;
		}

		Long processoId = processo.getId();
		Situacao situacao = regra.getSituacao();
		Long situacaoId = situacao.getId();

		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setProcessoId(processoId);
		filtro.setSituacaoId(situacaoId);
		filtro.setDecisaoFluxo(false);
		List<ProcessoRegra> prs = processoRegraService.findLasts(filtro);

		boolean temRegraVerde = false;
		boolean temRegraAmarela = false;
		boolean temRegraVermelho = false;
		boolean temRegraErro = false;
		boolean temRegraPendente = false;
		for (ProcessoRegra pr1 : prs) {
			StatusProcessoRegra status = pr1.getStatus();
			FarolRegra farol = pr1.getFarol();
			String mensagem = pr1.getMensagem();
			Regra regra1 = pr1.getRegra();
			String regra1Nome = regra1.getNome();
			Long subRegraFinalId = pr1.getSubRegraFinalId();
			Map<String, Object> regraMap = new HashMap<>();
			regraMap.put("status", status.name());
			regraMap.put("farol", farol != null ? farol.name() : "");
			regraMap.put("mensagem", mensagem);
			regraMap.put("subRegraId", subRegraFinalId);
			regrasDaSituacaoMap.put(regra1Nome, regraMap);
			temRegraVerde |= FarolRegra.VERDE.equals(farol);
			temRegraAmarela |= FarolRegra.AMARELO.equals(farol);
			temRegraVermelho |= FarolRegra.VERMELHO.equals(farol);
			temRegraErro |= StatusProcessoRegra.ERRO.equals(status);
			temRegraPendente |= StatusProcessoRegra.PENDENTE.equals(status);
		}
		regrasDaSituacaoMap.put("temRegraVerde", temRegraVerde);
		regrasDaSituacaoMap.put("temRegraAmarela", temRegraAmarela);
		regrasDaSituacaoMap.put("temRegraVermelho", temRegraVermelho);
		regrasDaSituacaoMap.put("temRegraErro", temRegraErro);
		regrasDaSituacaoMap.put("temRegraPendente", temRegraPendente);

		String regrasDaSituacaoJson = om.writeValueAsString(regrasDaSituacaoMap);
		return regrasDaSituacaoJson;
	}

	private RegraEngineVO buildRegraEngineVOTeste(Map<String, Set<OrigemValorVO>> deps) {
		RegraEngineVO vo = new RegraEngineVO();

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine scriptEngine = factory.getEngineByName("JavaScript");
		vo.setScriptEngine(scriptEngine);

		ObjectMapper objectMapper = new ObjectMapper();
		vo.setObjectMapper(objectMapper);

		StringBuilder js = new StringBuilder();

		for (String fonte : deps.keySet()) {
			Set<OrigemValorVO> origemValorVOs = deps.get(fonte);
			String var = montarVariavelJson(fonte, origemValorVOs);
			js.append(var);
		}

		vo.setJs(js);

		return vo;
	}

	private void executarSubRegra(RegraEngineVO vo, SubRegra sb, Usuario usuario) {

		ProcessoRegraLog log = new ProcessoRegraLog();
		ProcessoRegra pr = vo.getProcessoRegra();
		Processo processo = pr.getProcesso();
		Long processoId = processo != null ? processo.getId() : null;
		Regra regra = pr.getRegra();

		try {
			copyToLog(vo, sb, log, pr);

			Boolean result = null;
			TipoSubRegra tipo = sb.getTipo();
			if(TipoSubRegra.BASE_INTERNA.equals(tipo)) {
				executarBaseInterna(vo, sb, log);
				pr.addProcessoRegraLog(log);
			}
			else if(TipoSubRegra.CONDICAO.equals(tipo)) {
				executarCondicao(vo, sb, log);
				pr.addProcessoRegraLog(log);
				result = Boolean.valueOf(log.getResult());
			}
			else if(TipoSubRegra.CONSULTA_EXTERNA.equals(tipo)) {
				executarConsultaExterna(vo, sb, log);
				pr.addProcessoRegraLog(log);
			}
			else if(TipoSubRegra.CHAMADA_REGRA.equals(tipo)) {
				pr.addProcessoRegraLog(log);
			}
			else if(TipoSubRegra.INATIVA.equals(tipo)) {
				pr.addProcessoRegraLog(log);
			}
			else if(TipoSubRegra.FIM.equals(tipo)) {
				pr.addProcessoRegraLog(log);
				executarFim(vo, sb);
			}

			executarAcoes(sb, processo, usuario);

			StatusProcessoRegra status = log.getStatus();

			Long subRegraId = sb.getId();
			TipoConsultaExterna consultaExterna = sb.getConsultaExterna();
			if(StatusProcessoRegra.ERRO.equals(status)) {
				String observacao = log.getObservacao();
				pr.setMensagem(observacao);
				pr.setStatus(StatusProcessoRegra.ERRO);
				pr.setSubRegraFinalId(subRegraId);
			}
			else if(TipoSubRegra.CONSULTA_EXTERNA.equals(tipo) && TipoConsultaExterna.BRSCAN.equals(consultaExterna)){
				pr.setStatus(StatusProcessoRegra.PROCESSANDO);
				pr.setSubRegraFinalId(subRegraId);
				return;
			}
			else if(TipoSubRegra.CONDICAO.equals(tipo)) {
				executarProximaSubRegra(vo, sb, result, usuario);
			}
			else if(TipoSubRegra.CHAMADA_REGRA.equals(tipo)) {
				executarChamadaRegra(vo, sb);
			}
			else if(!TipoSubRegra.FIM.equals(tipo) && !StatusProcessoRegra.PENDENTE.equals(status)) {
				executarProximaSubRegra(vo, sb, usuario);
			}
		}
		catch (Exception e) {

			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			Long regraId = regra.getId();
			String regraNome = regra.getNome();
			systraceThread("Erro ao processar regra #" + regraId + " " + regraNome + ". Processo: " + processoId + ": " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();

			String stackTrace = DummyUtils.getStackTrace(e);
			log.setStackTrace(stackTrace);
			log.setStatus(StatusProcessoRegra.ERRO);

			pr.setMensagem(exceptionMessage);
			pr.setStackTrace(stackTrace);
			pr.setStatus(StatusProcessoRegra.ERRO);
			Long subRegraId = sb.getId();
			pr.setSubRegraFinalId(subRegraId);

			pr.addProcessoRegraLog(log);
		}
	}

	private void executarAcoes(SubRegra subRegra, Processo processo, Usuario usuario) {
		if(processo == null) {
			return;
		}
		List<SubRegraAcao> subRegraAcoes = subRegra.getSubRegraAcoes();
		for(SubRegraAcao sra: subRegraAcoes){
			SubRegraAcoes acao = sra.getAcao();
			if(SubRegraAcoes.ALTERAR_STATUS_DOCUMENTO.equals(acao)){
				alterarStatusDocumento(sra, processo, usuario);
			}else if(SubRegraAcoes.ALTERAR_OBRIGATORIEDADE_DOCUMENTO.equals(acao)){
				alterarObrigatoriedadeDocumento(sra, processo, usuario);
			}else if(SubRegraAcoes.ADICIONAR_GRUPO_NO_PROCESSO.equals(acao)){
				adicionarGrupoNoProcesso(sra, processo);
			}else if(SubRegraAcoes.ALTERAR_VALOR_CAMPO.equals(acao)){
				alterarValorCampo(sra, processo);
			}else if(SubRegraAcoes.ALTERAR_OBRIGATORIEDADE_CAMPO.equals(acao)) {
				alterarObrigatoriedadeCampos(sra, processo);
			}
		}
	}
	private void alterarObrigatoriedadeCampos(SubRegraAcao sra, Processo processo) {
		Long processoId = processo.getId();
		Boolean obrigatoriedadeCampo = sra.getObrigatoriedadeCampo();
		List<Long> tipoCampoIdList = sra.getTipoCampoIdList();
		for (Long id : tipoCampoIdList) {
			List<Campo> list = campoService.findByProcessoTipoCampos(processoId, Arrays.asList(id));
			for (Campo campo : list) {
				campo.setObrigatorio(obrigatoriedadeCampo);
				campoService.saveOrUpdate(campo);
			}
		}
	}

	private void alterarValorCampo(SubRegraAcao sra, Processo processo) {

		final Long processoId = processo.getId();

		TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
		tw2.setRunnable(() -> {

			TipoCampo tipoCampo = sra.getTipoCampo();
			String novoValorCampo = sra.getNovoValorCampo();

			List<CampoAbstract> campos = new ArrayList<>();

			Processo processo1 = processoService.get(processoId);

			for (CampoGrupo gruposCampo : processo1.getGruposCampos()) {
				for (Campo campo : gruposCampo.getCampos()) {
					if (campo.getTipoCampoId().equals(tipoCampo.getId())) {

						campo.setValor(novoValorCampo);

						campoService.saveOrUpdate(campo);

						//campos.add(campo);
						//Processo processo1 = processoService.get(processoId);
						//processoService.atualizarProcesso(processo1, null, campos);

						return;
					}
				}
			}
		});
		tw2.runNewThread();
	}

	private void alterarStatusDocumento(SubRegraAcao sra, Processo processo, Usuario usuario) {

		Boolean todosDocumentosAprovados = sra.getTodosDocumentosAprovados();
		StatusDocumento novoStatus = sra.getStatusDocumento();
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		if(todosDocumentosAprovados){
			filtro.setStatusDocumentoList(Arrays.asList(StatusDocumento.DIGITALIZADO));
			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
			for(Documento d: documentos){
				alterarStatus(usuario, sra, novoStatus, d);
			}
		}
		else {
			List<Long> tipoDocumentosIds = sra.getTipoDocumentosIds();
			filtro.setTipoDocumentoIdList(tipoDocumentosIds);
			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
			for(Documento documento : documentos) {
				alterarStatus(usuario, sra, novoStatus, documento);
			}
		}
	}

	private void alterarStatus(Usuario usuario, SubRegraAcao sra, StatusDocumento novoStatus, Documento d) {

		StatusDocumento statusInicial = d.getStatus();
		if (!statusInicial.equals(novoStatus)) {

			if (StatusDocumento.EXCLUIDO.equals(novoStatus)) {
				documentoService.excluirDocumento(null, d, null);
			} else {
				verificaFluxoAlteracaoDocumento(d, novoStatus);
				//FIXME não chamar saveOrUpdate aqui, e sim documentoService.atualizarStatus(status)
				documentoService.saveOrUpdate(d);
			}

			SubRegra subRegra = sra.getSubRegra();
			RegraLinha linha = subRegra.getLinha();
			Regra regra = linha.getRegra();
			String regraNome = regra.getNome();
			String observacao = "Alterado de " + statusInicial + " para " + novoStatus + ". Regra: " + regraNome;
			documentoLogService.criaLog(d, usuario, AcaoDocumento.ALTERACAO_STATUS, observacao);
		}
	}

	private void alterarObrigatoriedadeDocumento(SubRegraAcao sra, Processo processo, Usuario usuario) {

		Boolean todosDocumentosAprovados = sra.getTodosDocumentosAprovados();
		Boolean obrigatoriedadeDocumento = sra.getObrigatoriedadeDocumento();
		SubRegra subRegra = sra.getSubRegra();
		RegraLinha linha = subRegra.getLinha();
		Regra regra = linha.getRegra();
		List<Long> tipoDocumentosIds = sra.getTipoDocumentosIds();
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
		for(Documento d: documentos){
			TipoDocumento tipoDocumento = d.getTipoDocumento();
			if(tipoDocumento == null){
				continue;
			}
			Long tipoDocumentoId = tipoDocumento.getId();
			StatusDocumento status = d.getStatus();
			Integer versaoAtual = d.getVersaoAtual();
			boolean obrigatoriedadeInicial = d.getObrigatorio();

			if(todosDocumentosAprovados || tipoDocumentosIds.contains(tipoDocumentoId)){
				if(StatusDocumento.EXCLUIDO.equals(status) && obrigatoriedadeDocumento) {
					d.setStatus(versaoAtual > 0 ? StatusDocumento.DIGITALIZADO : StatusDocumento.INCLUIDO);
				}
				else if(StatusDocumento.INCLUIDO.equals(status) && !obrigatoriedadeDocumento){
					d.setStatus(StatusDocumento.EXCLUIDO);
				}
				d.setObrigatorio(obrigatoriedadeDocumento);
				if(!new Boolean(obrigatoriedadeInicial).equals(obrigatoriedadeDocumento)){
					String regraNome = regra.getNome();
					String observacao = "Alterado de " + obrigatoriedadeInicial + " para " + obrigatoriedadeDocumento + ". Regra: " + regraNome;
					documentoLogService.criaLog(d, usuario, AcaoDocumento.ALTERACAO_OBRIGATORIEDADE, observacao);
				}

				//FIXME não chamar saveOrUpdate aqui, e sim documentoService.atualizarObrigatoriedade(obrigatorio, status)
				documentoService.saveOrUpdate(d);
			}
		}
	}

	private void adicionarGrupoNoProcesso(SubRegraAcao sra, Processo processo) {

		List<Long> tipoGruposIds = sra.getTipoGruposIds();
		for (Long tipoGrupoId : tipoGruposIds) {

			Long processoId = processo.getId();
			boolean exist = campoGrupoService.existByProcesso(processoId, tipoGrupoId);

			if(!exist) {
				TipoCampoGrupo tipoGrupo = tipoCampoGrupoService.get(tipoGrupoId);

				CampoGrupo grupo = campoGrupoService.criaGrupo(processo, tipoGrupo);
				campoGrupoService.saveOrUpdate(grupo);

				List<TipoCampo> listCampos = tipoCampoService.findByTipoCampoGrupo(tipoGrupo, false);
				for (TipoCampo tipoCampo : listCampos) {
					Campo campo = campoService.criaCampo(grupo, tipoCampo);
					campoService.saveOrUpdate(campo);
				}
			}
		}
	}

	private void verificaFluxoAlteracaoDocumento (Documento documento, StatusDocumento novoStatus){
		StatusDocumento status = documento.getStatus();
		if(StatusDocumento.EXCLUIDO.equals(status) && StatusDocumento.INCLUIDO.equals(novoStatus)) {
			documento.setStatus(novoStatus);
		}else if(!StatusDocumento.EXCLUIDO.equals(status) && !StatusDocumento.INCLUIDO.equals(novoStatus)) {
			documento.setStatus(novoStatus);
		}
	}

	private void executarFim(RegraEngineVO vo, SubRegra sb) throws ScriptException {

		ProcessoRegra pr = vo.getProcessoRegra();

		Situacao situacaoDestino = sb.getSituacaoDestino();
		Long situacaoDestinoId = situacaoDestino != null ? situacaoDestino.getId() : null;
		situacaoDestino = situacaoDestinoId != null ? situacaoService.get(situacaoDestinoId) : null;
		FarolRegra farol = sb.getFarol();
		pr.setFarol(farol);
		pr.setStatus(StatusProcessoRegra.OK);
		pr.setSituacaoDestino(situacaoDestino);

		String observacao = sb.getObservacao();
		StringBuilder js = vo.getJs();
		ScriptEngine engine = vo.getScriptEngine();

		StringBuilder js1 = new StringBuilder();
		js1.append(js);

		Set<String> vars = getVarsCondicao(observacao);
		for (String var : vars) {
			Object eval = engine.eval(js1.toString() + " " + var);
			observacao = observacao.replace(var, String.valueOf(eval));
		}
		pr.setMensagem(observacao);

		Long subRegraId = sb.getId();
		pr.setSubRegraFinalId(subRegraId);
	}

	private void executarChamadaRegra(RegraEngineVO vo, SubRegra sb) throws ScriptException {

		executarFim(vo, sb);

		Regra regra = sb.getRegraFilha();
		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegraAtual = vo.getProcessoRegra();
		Processo processo = processoRegraAtual.getProcesso();
		ProcessoRegra processoRegra = new ProcessoRegra();
		processoRegra.setRegra(regra);
		processoRegra.setProcesso(processo);
		executarRegra(processoRegra, usuario, true);
	}

	private void executarConsultaExterna(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log) throws Exception {

		ConsultaExterna consultaExterna = null;
		Map<String, String> mapParam = montarDeparaParams(vo, sb, log);
		ObjectMapper om = vo.getObjectMapper();
		String params = om.writeValueAsString(mapParam);
		log.setParams(params);

		TipoConsultaExterna tipoConsultaExterna = sb.getConsultaExterna();
		if(TipoConsultaExterna.DECODE.equals(tipoConsultaExterna)) {
			consultaExterna = consultarDecode(vo, log, mapParam);
		}
		else if(TipoConsultaExterna.DETRAN_ARN.equals(tipoConsultaExterna)) {
			consultaExterna = consultarDetraArn(vo, log, mapParam);
		}
		else if(TipoConsultaExterna.LEILAO.equals(tipoConsultaExterna)) {
			consultaExterna = consultarLeilaoInforcar(vo, log, mapParam);
		}
		else if(TipoConsultaExterna.CRIVO.equals(tipoConsultaExterna)) {
			consultaExterna = consultarCrivo(vo, log, mapParam, sb);
		}
		else if(TipoConsultaExterna.CREDILINK.equals(tipoConsultaExterna)) {
			consultaExterna = consultarCredilink(vo, log, mapParam, sb);
		}
		/*else if(TipoConsultaExterna.NFE_INTERESSE.equals(tipoConsultaExterna)) {
			consultaExterna = consultarNfeInteresse(vo, log, mapParam, sb);
		}*/
		else if(TipoConsultaExterna.DATA_VALID.equals(tipoConsultaExterna)) {
			consultaExterna = consultarDataValid(vo, log, mapParam, sb);
		}
		else if(TipoConsultaExterna.DATA_VALID_BIOMETRIA.equals(tipoConsultaExterna)) {
			consultaExterna = consultarDataValidBiometria(vo, log, mapParam, sb);
		}
//		else if(TipoConsultaExterna.RENAVAM_INDICADORES_CHASSI.equals(tipoConsultaExterna)) {
//			consultaExterna = consultarRenavamIndicadoresChassi(vo, log, mapParam);
//		}
		else if(TipoConsultaExterna.BRSCAN.equals(tipoConsultaExterna)) {
			consultaExterna = consultarBrScan(vo, log, mapParam);
		}

		if(consultaExterna == null) {
			return;
		}

		StatusConsultaExterna status = consultaExterna.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			log.setStatus(StatusProcessoRegra.ERRO);
			String stackTrace = consultaExterna.getStackTrace();
			log.setStackTrace(stackTrace);
			String mensagem = consultaExterna.getMensagem();
			log.setObservacao(mensagem);
		}
		else {
			String resultado = consultaExterna.getResultado();
			gravarConsulta(vo, sb, log, resultado);
		}
	}

	private ConsultaExterna consultarBrScan(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam) {
		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String cpfCnpj = mapParam.get(BrScanRequestVO.CPF);
		String documento = mapParam.get(BrScanRequestVO.DOCUMENTO);
		String selfie = mapParam.get(BrScanRequestVO.SELFIE);

		ConsultaExterna consultaExterna = fontesExternasService.executarBrScan(cpfCnpj, documento, selfie, processo, usuario);
		String resultado = consultaExterna.getResultado();
		JSONObject json = resultado != null ? new JSONObject(resultado) : null;
		if(json != null && json.has("type")){
			String type = (String) json.get("type");
			if(StringUtils.isNotBlank(type) && type.equals("error")){
				consultaExterna.setStatus(StatusConsultaExterna.ERRO);
				String description = (String) json.get("description");
				consultaExterna.setMensagem(description);
			}
		}
		return consultaExterna;
	}

	private ConsultaExterna consultarCredilink(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam, SubRegra sb) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String cpfCnpj = mapParam.get(CredilinkRequestVO.CAMPO_CPF_CNPJ);
		String nome = mapParam.get(CredilinkRequestVO.CAMPO_NOME);
		String telefone = mapParam.get(CredilinkRequestVO.CAMPO_TELEFONE);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoCredilink(cpfCnpj, nome, telefone, processo, usuario);
		} else {
			ce = fontesExternasService.executarCredilink(cpfCnpj, nome, telefone, processo, usuario);
		}

		return ce;
	}

	/*private ConsultaExterna consultarNfeInteresse(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam, SubRegra sb) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String chaveNfe = mapParam.get(NfeInteresseRequestVO.CHAVE_NFE);
		String ufAutor = mapParam.get(NfeInteresseRequestVO.UF_AUTOR);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoNfeInteresse(chaveNfe, ufAutor, processo, usuario);
		} else {
			ce = fontesExternasService.executarNfeInteresse(chaveNfe, ufAutor, processo, usuario);
		}

		return ce;
	}*/

	private ConsultaExterna consultarDataValid(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam, SubRegra sb) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String cpf = mapParam.get(DataValidRequestVO.CPF);
		String dataValidadeCnhStr = mapParam.get(DataValidRequestVO.DATA_VALIDADE_CNH);
		Date dataValidadeCnh = DummyUtils.parse(dataValidadeCnhStr, "dd/MM/yyyy");
		String nome = mapParam.get(DataValidRequestVO.NOME);
		String dataNascimentoStr= mapParam.get(DataValidRequestVO.DATA_NASCIMENTO);
		Date dataNascimento = DummyUtils.parse(dataNascimentoStr, "dd/MM/yyyy");
		String nomeMaeFinanciado = mapParam.get(DataValidRequestVO.NOME_MAE);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoDataValid(cpf, dataValidadeCnh, nome, dataNascimento, nomeMaeFinanciado, processo, usuario);
		} else {
			ce = fontesExternasService.executarDataValid(cpf, dataValidadeCnh, nome, dataNascimento, nomeMaeFinanciado, processo, usuario);
		}

		return ce;
	}

	private ConsultaExterna consultarDataValidBiometria(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam, SubRegra sb) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String documentoIdStr = mapParam.get(DataValidBiometriaRequestVO.FOTO);
		Long documentoId = new Long(documentoIdStr);
		Imagem primeiraImagem = imagemService.getPrimeiraImagem(documentoId);
		File foto = imagemService.getFile(primeiraImagem);
		String cpf = mapParam.get(DataValidBiometriaRequestVO.CPF);
		String dataValidadeCnhStr = mapParam.get(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH);
		Date dataValidadeCnh = DummyUtils.parse(dataValidadeCnhStr, "dd/MM/yyyy");
		String nome = mapParam.get(DataValidBiometriaRequestVO.NOME);
		String dataNascimentoStr = mapParam.get(DataValidBiometriaRequestVO.DATA_NASCIMENTO);
		Date dataNascimento = DummyUtils.parse(dataNascimentoStr, "dd/MM/yyyy");
		String nomeMae = mapParam.get(DataValidBiometriaRequestVO.NOME_MAE);

		ConsultaExterna ce;

		ce = fontesExternasService.executarDataValidBiometria(foto, cpf, dataValidadeCnh, nome, dataNascimento, nomeMae, processo, usuario);

		return ce;
	}

	private ConsultaExterna consultarDecode(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		//String placa = mapParam.get(DecodeInfoCarRequestVO.PLACA);
		String chassi = mapParam.get(DecodeInfoCarRequestVO.CHASSI);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoDecodeInfocar(/*placa, */chassi, processo, usuario);
		} else {
			ce = fontesExternasService.executarDecodeInforcar(/*placa, */chassi, processo, usuario);
		}

		return ce;
	}

	private ConsultaExterna consultarDetraArn(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String placa = mapParam.get(DetranArnRequestVO.PLACA);
		String chassi = mapParam.get(DetranArnRequestVO.CHASSI);
		String uf = mapParam.get(DetranArnRequestVO.UF);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoDetranArn(placa, chassi, uf, processo, usuario);
		} else {
			ce = fontesExternasService.executarDetranArn(placa, chassi, uf, processo, usuario);
		}

		return ce;
	}

//	private ConsultaExterna consultarRenavamIndicadoresChassi(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam) {
//
//		Usuario usuario = vo.getUsuario();
//		ProcessoRegra processoRegra = log.getProcessoRegra();
//		Processo processo = processoRegra.getProcesso();
//		String chassi = mapParam.get(RenavamIndicadoresChassiRequestVO.CHASSI);
//
//		ConsultaExterna ce;
//		if (vo.isReprocessar()) {
//			ce = fontesExternasService.executarNovoRenavamIndicadoresChassi(chassi, processo, usuario);
//		} else {
//			ce = fontesExternasService.executarRenavamIndicadoresChassi(chassi, processo, usuario);
//		}
//
//		return ce;
//	}

	private ConsultaExterna consultarLeilaoInforcar(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();
		String placa = mapParam.get(LeilaoInfoCarRequestVO.PLACA);

		ConsultaExterna ce;
		if (vo.isReprocessar()) {
			ce = fontesExternasService.executarNovoLeilaoInfocar(placa, processo, usuario);
		} else {
			ce = fontesExternasService.executarLeilaoInfocar(placa, processo, usuario);
		}

		return ce;
	}

	private ConsultaExterna consultarCrivo(RegraEngineVO vo, ProcessoRegraLog log, Map<String, String> mapParam, SubRegra sb) {

		Usuario usuario = vo.getUsuario();
		ProcessoRegra processoRegra = log.getProcessoRegra();
		Processo processo = processoRegra.getProcesso();

		String politica = sb.getSubConsultaExterna();

		ConsultaExterna ce = fontesExternasService.executarCrivo(mapParam, politica, processo, usuario);
		return ce;
	}

	private void executarCondicao(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log) throws ScriptException {

		String condicionalJs = sb.getCondicionalJs();

		StringBuilder params = buscarParams(vo, condicionalJs);
		log.setParams(params.toString());

		StringBuilder js = vo.getJs();
		ScriptEngine engine = vo.getScriptEngine();

		StringBuilder js2 = new StringBuilder();
		adicionarFuncoesUtilizadas(js2, condicionalJs);
		js2.append(js);
		js2.append(condicionalJs).append(";");

		log.setJs(js2.toString());

		Object eval = engine.eval(js2.toString());
		log.setResult(String.valueOf(eval));
	}

	private void adicionarFuncoesUtilizadas(StringBuilder sb, String condicionalJs) {

		List<FuncaoJs> funcoes = funcaoJsService.findAll();

		for (FuncaoJs funcao: funcoes) {
			String nomeFuncao = funcao.getNome();
			String nome = nomeFuncao.substring(0, nomeFuncao.indexOf('('));
			if (condicionalJs.contains(nome)) {
				sb.append(funcao.getFuncao() + "\n");
			}
		}
	}

	private StringBuilder buscarParams(RegraEngineVO vo, String condicionalJs) {

		StringBuilder js = vo.getJs();
		ScriptEngine engine = vo.getScriptEngine();

		StringBuilder js1 = new StringBuilder();
		js1.append(js);

		Set<String> vars = getVarsCondicao(condicionalJs);
		StringBuilder params = new StringBuilder();
		params.append("{");
		for (String var : vars) {

			Object eval = "";
			try {
				eval = engine.eval(js1.toString() + " " + var);
			}
			catch (Exception e) {
				String exceptionMessage = DummyUtils.getExceptionMessage(e);
				ProcessoRegra pr = vo.getProcessoRegra();
				Processo processo = pr.getProcesso();
				Long processoId = processo != null ? processo.getId() : null;
				Regra regra = pr.getRegra();
				Long regraId = regra.getId();
				String regraNome = regra.getNome();
				systraceThread("Erro ao buscar valor da variável " + var + ". Regra: #" + regraId + " " + regraNome + ". Processo: " + processoId + ": " + exceptionMessage, LogLevel.ERROR);
			}
			params.append("\"").append(var).append("\"").append(": \"").append(eval).append("\",");
		}
		params.append("}");

		return params;
	}

	private void executarBaseInterna(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log) throws Exception {

		BaseInterna baseInterna = sb.getBaseInterna();
		Map<String, String> mapParam = montarDeparaParams(vo, sb, log);
		ObjectMapper om = vo.getObjectMapper();
		String params = om.writeValueAsString(mapParam);
		log.setParams(params);
		String chaveUnicidade = baseRegistroService.montarChaveUnicidadeSemCoringa(baseInterna, mapParam);

		String resultadoJson = consultarBaseInterna(vo, baseInterna, chaveUnicidade);
		gravarConsulta(vo, sb, log, resultadoJson);
	}

	private void gravarConsulta(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log, String resultado) throws Exception {

		resultado = StringUtils.isNotBlank(resultado) ? resultado  : "{}";
		resultado = DummyUtils.stringToJson(resultado);
		log.setResult(resultado);

		String varConsulta = sb.getVarConsulta();
		StringBuilder varSb = new StringBuilder();
		varSb.append("var ").append(varConsulta).append(" = ").append(resultado).append(";\n");

		// manter resultado sempre no começo do js, para que o valor possa ser sobrescrito no teste de regra
		StringBuilder js = vo.getJs();
		varSb.append(js);
		vo.setJs(varSb);

		TipoConsultaExterna consultaExterna = sb.getConsultaExterna();
		ProcessoRegra pr = log.getProcessoRegra();
		Processo processo = pr.getProcesso();
		if(consultaExterna != null) {
			List<DeparaRetorno> depara = sb.getDeparaRetornos();
			List<TipoCampo> campos = new ArrayList<>();

			Map<CampoGrupo, List<Campo>> camposMap = vo.getCamposMap();
			if(camposMap == null) {
				return;
			}
			List<Campo> campoList = new ArrayList<>();
			for (List<Campo> campoList2 : camposMap.values()) {
				campoList.addAll(campoList2);
			}

			for (DeparaRetorno deparaRetorno : depara) {
				Boolean sobrescreverValor = deparaRetorno.getSobrescreverValor();
				TipoCampo tipoCampo = deparaRetorno.getTipoCampo();
				String origem = deparaRetorno.getOrigem();

				StringBuilder script2 = new StringBuilder();
				script2.append(varSb);
				//testar
				adicionarFuncoesUtilizadas(script2, origem);
				script2.append(origem);

				ScriptEngine engine = vo.getScriptEngine();

				if(sobrescreverValor != null && !sobrescreverValor){
					TipoCampoGrupo grupo = tipoCampo.getGrupo();
					String grupoNome = grupo.getNome();
					String tipoCampoNome = tipoCampo.getNome();
					Campo campo = DummyUtils.getCampoProcesso(campoList, grupoNome, tipoCampoNome);
					String valorAtual = campo != null ? campo.getValor() : null;
					if(StringUtils.isNotBlank(valorAtual)) {
						continue;
					}
				}

				try {
					Object eval = engine.eval(script2.toString());
					Object valor = eval;
					String valorStr = valor != null ? String.valueOf(valor) : "";

					tipoCampo.setValor(valorStr);
					campos.add(tipoCampo);
				}
				catch (Exception e) {
					String exceptionMessage = DummyUtils.getExceptionMessage(e);
					systraceThread("Erro ao gravar retorno: " + exceptionMessage, LogLevel.ERROR);
					e.printStackTrace();
				}
			}

			//faz antes do deatach
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			String varNomeProcesso = tipoProcesso.getNome();

			if(processo != null) {

				regraRepository.deatach(processo);
				Usuario usuario = vo.getUsuario();
				if(usuario != null) {
					regraRepository.deatach(usuario);
				}

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					Processo processo2 = processoService.get(processo.getId());
					ProcessoService processoService = applicationContext.getBean(ProcessoService.class);
					processoService.atualizarProcesso(processo2, usuario, campos, false, false);
				});
				tw.runNewThread();
				tw.throwException();
			}

			varNomeProcesso = DummyUtils.formatarNomeVariavel(varNomeProcesso);
			for (TipoCampo campo : campos) {
				TipoCampoGrupo grupo = campo.getGrupo();
				String grupoNome = grupo.getNome();
				String campoNome = campo.getNome();
				String valor = campo.getValor();

				String varGrupo = varNomeProcesso + "['" + grupoNome + "']";
				String varCampo = varGrupo + "['" + campoNome + "']";
				varSb.append("\n").append(varGrupo).append(" = ").append(varGrupo).append(" ? ").append(varGrupo).append(" : {};");
				varSb.append("\n").append(varCampo).append(" = ").append(varCampo).append(" ? ").append(varCampo).append(" : {};");
				varSb.append("\n").append(varCampo).append(" = \"").append(valor).append("\";");
			}
			vo.setJs(varSb);
		}
	}

	private void executarProximaSubRegra(RegraEngineVO vo, SubRegra sb, Usuario usuario) {

		RegraLinha filha = getLinhaFilha(sb);
		Set<SubRegra> subRegras = filha.getSubRegras();
		SubRegra filho = subRegras.iterator().next();
		executarSubRegra(vo, filho, usuario);
	}

	private void executarProximaSubRegra(RegraEngineVO vo, SubRegra sb, Boolean result, Usuario usuario) {

		RegraLinha filha = getLinhaFilha(sb);
		Set<SubRegra> subRegras = filha.getSubRegras();
		for (SubRegra filho : subRegras) {
			Boolean filhoSim = filho.getFilhoSim();
			if(filhoSim.equals(result)) {
				executarSubRegra(vo, filho, usuario);
				return;
			}
		}
	}

	private RegraLinha getLinhaFilha(SubRegra sb) {

		RegraLinha linha = sb.getLinha();
		RegraLinha filha = linha.getFilha();

		if(filha == null) {
			throw new RuntimeException("Sub-Regra filha não encontrada. Uma execução de regra correta deve sempre terminar com um Sub-Regra do tipo \"FIM\"");
		}

		return filha;
	}


	private String consultarBaseInterna(RegraEngineVO vo, BaseInterna baseInterna, String chaveUnicidade) {

		List<RegistroValorVO> list = baseRegistroService.findValoresByBaseInternaAndChaveUnicidade(baseInterna.getId(), chaveUnicidade);

		Map<String, String> mapBase = new HashMap<>();
		RegistroValorVO registroValorVO = list.isEmpty() ? null : list.get(0);
		if(registroValorVO != null) {
			Map<String, BaseRegistroValor> mapColunaRegistroValor = registroValorVO.getMapColunaRegistroValor();
			Set<String> keySet = mapColunaRegistroValor.keySet();
			for (String coluna : keySet) {
				BaseRegistroValor valor = mapColunaRegistroValor.get(coluna);
				String valorStr = valor.getValor();
				mapBase.put(coluna, valorStr);
			}
		}

		ObjectMapper om = vo.getObjectMapper();
		String baseMapJson = om.writeValueAsString(mapBase);
		return baseMapJson;
	}

	private Map<String, String> montarDeparaParams(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log) throws ScriptException {

		StringBuilder js = vo.getJs();
		ScriptEngine engine = vo.getScriptEngine();

		Map<String, String> colunaValor = new HashMap<>();
		List<DeparaParam> deparaParams = sb.getDeparaParams();
		for (DeparaParam deparaParam : deparaParams) {

			String fonte = deparaParam.getNomeFonte();
			fonte = DummyUtils.formatarNomeVariavel(fonte);
			String origem1 = deparaParam.getOrigem();
			String destino = deparaParam.getDestino();

			String valorOrigem = "";
			if(StringUtils.isNotBlank(fonte) && StringUtils.isNotBlank(origem1)) {

				String origem = fonte + origem1;

				StringBuilder js2 = new StringBuilder();
				js2.append("function execSubregraFunc() { \n");
				js2.append(js);
				js2.append("return ").append(origem).append(";\n");
				js2.append("}\n");
				js2.append("execSubregraFunc();");

				log.setJs(js2.toString());
				Object eval = engine.eval(js2.toString());
				valorOrigem = (String) eval;
			}

			colunaValor.put(destino, valorOrigem);
		}

		return colunaValor;
	}

	private String getProcessoJson(RegraEngineVO vo, Processo processo, Map<CampoGrupo, List<Campo>> camposMap) {

		ObjectMapper om = vo.getObjectMapper();
		Map<String, Object> processoMap = new HashMap<>();

		Long processoId = processo.getId();
		Long tipoProcessoId = processo.getTipoProcesso().getId();
		if(camposMap == null) {
			camposMap = campoService.findMapByProcesso(null, processoId, false);
		}
		vo.setCamposMap(camposMap);

		for (CampoGrupo grupo : camposMap.keySet()) {
			String grupoNome = grupo.getNome();
			List<Campo> campos = camposMap.get(grupo);
			for (Campo campo : campos) {

				String campoNome = campo.getNome();
				String campoValor = campo.getValor();
				TipoEntradaCampo tipo = campo.getTipo();
				regraRepository.deatach(campo);
				regraRepository.deatach(grupo);

				Map<String, Object> map = (Map<String, Object>) processoMap.get(grupoNome);
				map = map != null ? map : new HashMap<>();
				processoMap.put(grupoNome, map);

				map.put(campoNome, campoValor);

				if(StringUtils.isNotBlank(campoValor)) {
					if(TipoEntradaCampo.MOEDA.equals(tipo)) {
						BigDecimal valor = DummyUtils.stringToCurrency(campoValor);
						campoValor = valor != null ? valor.toString() : null;
						map.put(campoNome, campoValor);//substitui o valor pelo formatado
					}
					BaseInterna baseInterna = campo.getBaseInterna();
					if(baseInterna != null) {
						BaseRegistroFiltro filtro = new BaseRegistroFiltro();
						filtro.setChaveUnicidade(campoValor);
						filtro.setBaseInterna(baseInterna);
						List<RegistroValorVO> registros = baseRegistroService.findByFiltro(filtro, 0, 1);
						if(!registros.isEmpty()) {

							Map<String, Object> map2 = new LinkedHashMap<>();
							Map<String, BaseRegistroValor> registroMap = registros.get(0).getMapColunaRegistroValor();
							for (String coluna : registroMap.keySet()) {
								BaseRegistroValor registroValor = registroMap.get(coluna);
								String valor = registroValor.getValor();
								map2.put(coluna, valor);
							}

							map.put(campoNome, map2);//substitui o valor pelo map com os valores do registro
						}
					}
				}
			}
		}
		//carregar dados dos campos Grupos Editavel ou obrigatorio
		Map<String, Object> dadosCamposGrupos = getDadosCamposGrupos(camposMap);
		processoMap.put(TipoCampo.DADOS_CAMPOS, dadosCamposGrupos);

		//carregar dados dos documentos
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> listDoc = documentoService.findByFiltro(filtro, null, null);
		List<TipoDocumento> tipoDocs = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
		if(listDoc != null){

			List<Documento> docs = listDoc.stream().filter(d -> !Arrays.asList(StatusDocumento.EXCLUIDO).contains(d.getStatus())).collect(Collectors.toList());

			if(docs != null){
				Map<String, Map<String, String>> documentoJsonMap = getDocumentoJsonMap(om, docs, tipoDocs);
				processoMap.put("DOCUMENTOS", documentoJsonMap);
			}
		}

		Situacao situacao = processo.getSituacao();
		String situacaoAtual = situacao.getNome();
		String situacaoAnteriorNome = "";

		ProcessoLog processoLogAnterior = processoLogService.findSituacaoAnterior(processo, situacao,null);
		if(processoLogAnterior != null){
			Situacao situacaoAnterior = processoLogAnterior.getSituacaoAnterior();
			situacaoAnteriorNome = situacaoAnterior != null ? situacaoAnterior.getNome() : "";
		}

		Map<String, Object> outrosDadosMap = new LinkedHashMap<>();
		List<Long> processosId = processoService.findProcessosIdsRelacionados(processo);
		Map<StatusDocumento, Long> map = documentoService.countByStatusMap(processosId);
		Long quantidadeDocumentosObrigatorios = documentoService.getQuantidadeObrigatorio(processosId);
		Integer[] qtdes = documentoService.getQuantidadesObrigatoriosEquivalencias(processosId);
		Integer quantidadeDocumentoObrigatoriosNaoDigitalizados = qtdes[0];
		Integer quantidadeDocumentoObrigatoriosPendentes = qtdes[1];
		DocumentoFiltro documentoFiltro = new DocumentoFiltro();
		documentoFiltro.setStatusDifetenteDeList(Arrays.asList(StatusDocumento.EXCLUIDO, StatusDocumento.APROVADO));
		documentoFiltro.setDiferenteDeOutros(true);
		documentoFiltro.setProcesso(processo);
		int naoAprovados = documentoService.countByFiltro(documentoFiltro);

		Long digitalizados = map.get(StatusDocumento.DIGITALIZADO);
		digitalizados = digitalizados != null ? digitalizados : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS DIGITALIZADOS", digitalizados);
		Long excluidos = map.get(StatusDocumento.EXCLUIDO);
		excluidos = excluidos != null ? excluidos : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS EXCLUÍDOS", excluidos);
		Long incluidos = map.get(StatusDocumento.INCLUIDO);
		incluidos = incluidos != null ? incluidos : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS INCLUÍDOS", incluidos);
		Long pendentes = map.get(StatusDocumento.PENDENTE);
		pendentes = pendentes != null ? pendentes : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS PENDENTES", pendentes);
		Long aprovados = map.get(StatusDocumento.APROVADO);
		aprovados = aprovados != null ? aprovados : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS APROVADOS", aprovados);
		outrosDadosMap.put("QTDE DOCUMENTOS NAO APROVADOS <> OUTROS", naoAprovados);
		Long processados = map.get(StatusDocumento.PROCESSANDO);
		processados = processados != null ? processados : 0;
		outrosDadosMap.put("QTDE DOCUMENTOS PROCESSANDO", processados);
		outrosDadosMap.put("QTDE DOCUMENTOS OBRIGATORIOS", quantidadeDocumentosObrigatorios);
		outrosDadosMap.put("QTDE DOCUMENTOS OBRIGATORIOS NÃO DIGITALIZADOS", quantidadeDocumentoObrigatoriosNaoDigitalizados);
		outrosDadosMap.put("QTDE DOCUMENTOS OBRIGATÓRIOS PENDENTES", quantidadeDocumentoObrigatoriosPendentes);
		outrosDadosMap.put("SITUAÇÃO ATUAL", situacaoAtual);
		outrosDadosMap.put("SITUAÇÃO ANTERIOR", situacaoAnteriorNome);

		Date dataCriacao = processo.getDataCriacao();
		outrosDadosMap.put("DATA CRIACAO", dataCriacao);

		//TODO tem que verificar se a regra usa o relatórioGeral, se não usa nem deveria carregar
		/*RelatorioGeral relatorioGeral = relatorioGeralService.getByProcesso(processoId);
		Date dataEnvioAnalise = relatorioGeral.getDataEnvioAnalise();
		Date dataFinalizacao = relatorioGeral.getDataFinalizacao();
		Integer vezesPendente = relatorioGeral.getVezesPendente();
		Integer vezesAguardandoAnalise = relatorioGeral.getVezesAguardandoAnalise();
		Integer vezesEmAnalise = relatorioGeral.getVezesEmAnalise();
		Boolean existPendencia = processoLogService.existPendenciaByProcesso(processoId);
		outrosDadosMap.put("DATA ENVIO ANALISE", dataEnvioAnalise);
		outrosDadosMap.put("DATA DE FINALIZAÇÃO", dataFinalizacao);
		outrosDadosMap.put("VEZES PENDENTE", vezesPendente);
		outrosDadosMap.put("VEZES EM ANALISE", vezesEmAnalise);
		outrosDadosMap.put("VEZES AGUARDANDO ANALISE", vezesAguardandoAnalise);
		outrosDadosMap.put("PASSOU POR PENDENCIA", existPendencia);*/

		processoMap.put("OUTROS DADOS", outrosDadosMap);

		String processoMapJson = om.writeValueAsString(processoMap);
		return processoMapJson;
	}

	private Map<String, Object> getDadosCamposGrupos(Map<CampoGrupo, List<Campo>> camposMap) {
		Map<String, Object> dadosCamposGrupos = new LinkedHashMap<>();

		for (CampoGrupo grupo : camposMap.keySet()) {
			String grupoNome = grupo.getNome();
			List<Campo> campos = camposMap.get(grupo);
			Map<String, Object> dadosCampos = new LinkedHashMap<>();
			dadosCamposGrupos.put(grupoNome, dadosCampos);
			for (Campo campo : campos) {
				String campoNome = campo.getNome();

				Map<String, Object> dados = new LinkedHashMap<>();
				Boolean editavel = campo.getEditavel();
				boolean obrigatorio = campo.getObrigatorio();

				dados.put(TipoCampo.EDITAVEL, editavel);
				dados.put(TipoCampo.OBRIGATORIO, obrigatorio);
				dadosCampos.put(campoNome, dados);

			}
		}
		return dadosCamposGrupos;
	}

	private Map<String, Map<String, String>> getDocumentoJsonMap(ObjectMapper om, List<Documento> docs, List<TipoDocumento> tipoDocs) {

		Map<String, Map<String, String>> objectMap = new HashMap<>();

		for(Documento documento : docs){

			for(Iterator<TipoDocumento> iter = tipoDocs.iterator(); iter.hasNext();){
				TipoDocumento tp = iter.next();
				if(tp.getNome().equalsIgnoreCase(documento.getNome())){
					iter.remove();
					break;
				}
			}

			BigDecimal porcentagemRF = documento.getSimilaridadeFacial();
			Long documentoId = documento.getId();
			String docNome = documento.getNome();
			StatusFacial statusFacial = documento.getStatusFacial();
			boolean obrigatorio = documento.getObrigatorio();
			StatusDocumento status = documento.getStatus();
			Date dataDigitalizacao = documento.getDataDigitalizacao();
			Integer versaoAtual = documento.getVersaoAtual();
			StatusOcr statusOcr = documento.getStatusOcr();
			Long irregularidadeId = pendenciaService.getLastIrregularidade(documentoId);

			Map<String, String> valuesMap = new HashMap<>();
			valuesMap.put(Documento.ID, String.valueOf(documentoId));
			valuesMap.put(Documento.OBRIGATORIO, String.valueOf(obrigatorio));
			valuesMap.put(Documento.STATUS, String.valueOf(status));
			valuesMap.put(Documento.DATA_DIGITALIZACAO, String.valueOf(dataDigitalizacao));
			valuesMap.put(Documento.VERSAO_ATUAL, String.valueOf(versaoAtual));
			valuesMap.put(Documento.STATUS_ORC, String.valueOf(statusOcr));
			valuesMap.put(Documento.IRREGULARIDADE_ID, String.valueOf(irregularidadeId));
			if(documento.getReconhecimentoFacial()){
				valuesMap.put(Documento.PORCENTAGEM_RECONHECIMENTO_FACIAL, porcentagemRF != null ? porcentagemRF.toString() : "");
				valuesMap.put(Documento.STATUS_RECONHECIMENTO_FACIAL, statusFacial != null ? statusFacial.toString() : "");
			}

			objectMap.put(docNome, valuesMap);
		}

		for(TipoDocumento tp : tipoDocs){

			String tpNome = tp.getNome();
			if(!objectMap.containsKey(tpNome)) {

				Map<String, String> valuesMap = new HashMap<>();
				valuesMap.put(Documento.OBRIGATORIO, "");
				valuesMap.put(Documento.STATUS, "");
				valuesMap.put(Documento.DATA_DIGITALIZACAO, "");
				valuesMap.put(Documento.VERSAO_ATUAL, "");
				valuesMap.put(Documento.STATUS_ORC, "");
				valuesMap.put(Documento.IRREGULARIDADE_ID, "");
				if(tp.getReconhecimentoFacial()){
					valuesMap.put(Documento.PORCENTAGEM_RECONHECIMENTO_FACIAL, "");
					valuesMap.put(Documento.STATUS_RECONHECIMENTO_FACIAL, "");
				}

				objectMap.put(tpNome, valuesMap);
			}
		}

		return objectMap;
	}

	private void copyToLog(RegraEngineVO vo, SubRegra sb, ProcessoRegraLog log, ProcessoRegra pr) {

		StringBuilder js = vo.getJs();
		TipoSubRegra tipo = sb.getTipo();
		BaseInterna baseInterna = sb.getBaseInterna();
		String condicionalJs = sb.getCondicionalJs();
		FarolRegra farol = sb.getFarol();
		Boolean filhoSim = sb.getFilhoSim();
		TipoConsultaExterna consultaExterna = sb.getConsultaExterna();
		String observacao = sb.getObservacao();
		Regra regraFilha = sb.getRegraFilha();
		Long subRegraId = sb.getId();
		String varConsulta = sb.getVarConsulta();
		Situacao situacaoDestino = sb.getSituacaoDestino();

		log.setProcessoRegra(pr);
		log.setSubRegraId(subRegraId);
		log.setData(new Date());
		log.setTipo(tipo);
		log.setBaseInterna(baseInterna);
		log.setCondicionalJs(condicionalJs);
		log.setFarol(farol);
		log.setFilhoSim(filhoSim);
		log.setConsultaExterna(consultaExterna);
		log.setObservacao(observacao);
		log.setRegraFilha(regraFilha);
		log.setVarConsulta(varConsulta);
		log.setJs(js.toString());
		log.setStatus(StatusProcessoRegra.OK);
		log.setSituacaoDestino(situacaoDestino);
	}

	public void continueExecutarRegra(ProcessoRegra pr, Usuario usuario) {

		Processo processo = pr.getProcesso();
		Regra regra = pr.getRegra();
		Long subRegraFinalId = pr.getSubRegraFinalId();
		SubRegra sb = subRegraService.get(subRegraFinalId);
		Long processoId = processo.getId();
		Map<CampoGrupo, List<Campo>> camposMap = campoService.findMapByProcesso(null, processoId, false);

		RegraEngineVO vo = new RegraEngineVO();
		try {
			vo = buildRegraEngineVO(processo, pr, camposMap, false);
			StringBuilder voJs = vo.getJs();

			boolean isTipoConsultaExterna = sb.isTipoConsultaExterna();
			StringBuilder js = new StringBuilder();
			if(isTipoConsultaExterna){
				ConsultaExternaFiltro filtro = new ConsultaExternaFiltro();
				filtro.setProcessoId(processoId);

				TipoConsultaExterna tipoConsultaExterna = sb.getConsultaExterna();
				filtro.setTipo(tipoConsultaExterna);
				ConsultaExterna consultaExterna = consultaExternaService.findLastByFiltro(filtro);
				String resultado = consultaExterna.getResultado();
				String varConsulta = sb.getVarConsulta();
				js.append("var ").append(varConsulta).append(" = ").append(resultado).append(";\n");
			}

			js.append(voJs);
			vo.setJs(js);
			vo.setProcessoRegra(pr);
			processoRegraService.saveOrUpdate(pr);
		}
		catch (Exception e) {
			Long regraId = regra.getId();
			String regraNome = regra.getNome();
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			String stackTrace = DummyUtils.getStackTrace(e);
			systraceThread("Erro ao iniciar processamento da regra #" + regraId + " " + regraNome + ". Processo: " + processoId + ": " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();
			pr.setMensagem(exceptionMessage);
			pr.setStackTrace(stackTrace);
			pr.setStatus(StatusProcessoRegra.ERRO);
			processoRegraService.saveOrUpdate(pr);
		}
		executarProximaSubRegra(vo, sb, usuario);

		processoRegraService.saveOrUpdate(pr);

		List<ProcessoRegraLog> logs = pr.getProcessoRegraLogs();
		for (ProcessoRegraLog log : logs) {
			processoRegraLogService.saveOrUpdate(log);
		}

		Date inicio = pr.getDataExecucao();
		pr.setTempo(System.currentTimeMillis() - inicio.getTime());
		processoRegraService.saveOrUpdate(pr);
	}

	private class RegraEngineVO {

		private StringBuilder js;
		private ObjectMapper objectMapper;
		private ScriptEngine scriptEngine;
		private ProcessoRegra processoRegra;
		private Usuario usuario;
		private boolean reprocessar;
		private boolean isTesteRegra;
		private Map<CampoGrupo, List<Campo>> camposMap;

		public Usuario getUsuario() {
			return usuario;
		}

		public void setUsuario(Usuario usuario) {
			this.usuario = usuario;
		}

		public ProcessoRegra getProcessoRegra() {
			return processoRegra;
		}

		public void setProcessoRegra(ProcessoRegra processoRegra) {
			this.processoRegra = processoRegra;
		}

		public StringBuilder getJs() {
			return js;
		}

		public void setJs(StringBuilder js) {
			this.js = js;
		}

		public ObjectMapper getObjectMapper() {
			return objectMapper;
		}

		public void setObjectMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		public ScriptEngine getScriptEngine() {
			return scriptEngine;
		}

		public void setScriptEngine(ScriptEngine scriptEngine) {
			this.scriptEngine = scriptEngine;
		}

		public boolean isReprocessar() {
			return reprocessar;
		}

		public void setReprocessar(boolean reprocessar) {
			this.reprocessar = reprocessar;
		}

		@SuppressWarnings("unused")
		public boolean isTesteRegra() {
			return isTesteRegra;
		}

		public void setTesteRegra(boolean isTesteRegra) {
			this.isTesteRegra = isTesteRegra;
		}

		public Map<CampoGrupo, List<Campo>> getCamposMap() {
			return camposMap;
		}

		public void setCamposMap(Map<CampoGrupo, List<Campo>> camposMap) {
			this.camposMap = camposMap;
		}
	}
}
