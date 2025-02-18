package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.ProcessoRegraRepository;
import net.wasys.getdoc.domain.vo.ResultadoConsultaVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ProcessoRegraService {

	@Autowired private ProcessoRegraRepository processoRegraRepository;
	@Autowired private RegraLinhaService regraLinhaService;
	@Autowired private ProcessoRegraLogService processoRegraLogService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private RegraService regraService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private CampoService campoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private DeparaParamService deparaParamService;
	@Autowired private TipoCampoService tipoCampoService;

	public ProcessoRegra get(Long id) {
		return processoRegraRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public void saveOrUpdate(ProcessoRegra processoRegra) {
		Situacao situacaoDestino = processoRegra.getSituacaoDestino();
		Regra regra = processoRegra.getRegra();
		Processo processo = processoRegra.getProcesso();
		//feito para evitar "illegally attempted to associate a proxy with two open Sessions"
		if(situacaoDestino != null) {
			processoRegraRepository.deatach(situacaoDestino);
		}
		if(regra != null) {
			processoRegraRepository.deatach(regra);
		}
		if(processo != null){
			processoRegraRepository.deatach(processo);
		}

		processoRegraRepository.saveOrUpdate(processoRegra);
	}

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public void merge(ProcessoRegra processoRegra) {
		Situacao situacaoDestino = processoRegra.getSituacaoDestino();
		Regra regra = processoRegra.getRegra();
		Processo processo = processoRegra.getProcesso();
		//feito para evitar "illegally attempted to associate a proxy with two open Sessions"
		if(situacaoDestino != null) {
			processoRegraRepository.deatach(situacaoDestino);
		}
		if(regra != null) {
			processoRegraRepository.deatach(regra);
		}
		if(processo != null){
			processoRegraRepository.deatach(processo);
		}
		processoRegraRepository.merge(processoRegra);
	}

	public List<ProcessoRegra> findLasts(ProcessoRegraFiltro filtro) {
		return processoRegraRepository.findLasts(filtro);
	}

	public ProcessoRegra getById(Long id) {
		return processoRegraRepository.getById(id);
	}

	public int countByFiltro(ProcessoRegraFiltro filtro) {
		return processoRegraRepository.countByFiltro(filtro);
	}

	public List<ProcessoRegra> findByFiltro(ProcessoRegraFiltro filtro, Integer inicio, Integer max) {
		return processoRegraRepository.findByFiltro(filtro, inicio, max);
	}

	public List<String> getDistinctRegrasNomes() {
		return processoRegraRepository.getDistinctRegrasNomes();
	}

	public List<ResultadoConsultaVO> carregarConsultas(Long processoId) {
		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setProcessoId(processoId);
		List<ProcessoRegra> processoRegras = processoRegraRepository.findLasts(filtro);
		return carregarConsultas(processoRegras);
	}

	public List<ResultadoConsultaVO> carregarConsultas(List<ProcessoRegra> processoRegras) {

		List<ResultadoConsultaVO> resultadosConsultas = new ArrayList<>();

		ObjectMapper om = new ObjectMapper();
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		for (ProcessoRegra pr : processoRegras) {

			Regra regra = pr.getRegra();
			String nomeRegra = regra.getNome();
			Long id = pr.getId();
			List<ProcessoRegraLog> prLogs = processoRegraLogService.findByProcessoRegra(id);

			//usa um map para garantir que apenas a última consulta seja exibida
			Map<String, ResultadoConsultaVO> resultadosConsultasRegra = new LinkedHashMap<>();

			for (ProcessoRegraLog prLog : prLogs) {

				TipoSubRegra tipo = prLog.getTipo();
				if(TipoSubRegra.CONSULTA_EXTERNA.equals(tipo)) {

					String params = prLog.getParams();
					String result = prLog.getResult();
					Long subRegraId = prLog.getSubRegraId();
					TipoConsultaExterna tipoConsultaExterna = prLog.getConsultaExterna();

					if(StringUtils.isNotBlank(result) && !"{}".equals(result)) {

						Map<?, ?> parametros = om.readValue(params, Map.class);
						Map<?, ?> valores = om.readValue(result, Map.class);

						String key = tipoConsultaExterna + " - " + parametros;

						ResultadoConsultaVO vo = new ResultadoConsultaVO();
						vo.setValores(valores);
						vo.setNomeRegra(nomeRegra);
						vo.setParametros(parametros);
						vo.setSubRegraId(String.valueOf(subRegraId));
						vo.setTipoConsultaExterna(tipoConsultaExterna);
						resultadosConsultasRegra.put(key, vo);
					}
				}
			}

			resultadosConsultas.addAll(resultadosConsultasRegra.values());
		}

		return resultadosConsultas;
	}

	public List<Regra> getRegrasDoProcesso(Processo processo, Situacao situacao) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		RegraFiltro filtro = new RegraFiltro();
		filtro.setTipoProcessoId(tipoProcessoId);
		filtro.setAtiva(true);
		filtro.setVigencia(new Date());
		filtro.setSituacao(situacao);
		List<Regra> regras = regraService.findByFiltro(filtro , null, null);

		return regras;
	}

	@Transactional(rollbackFor=Exception.class)
	public String criarRegras(Processo processo, Situacao situacao, Usuario usuario, boolean criarImediatas, boolean criarAgendadas) throws Exception {

		List<Regra> regras = getRegrasDoProcesso(processo, situacao);
		List<String> mensagensErro = new ArrayList<>();
		List<ProcessoRegra> imediatas = new ArrayList<>();
		List<ProcessoRegra> agendadas = new ArrayList<>();
		for (Regra regra : regras) {

			ProcessoRegra pr = new ProcessoRegra();
			pr.setStatus(StatusProcessoRegra.PENDENTE);
			pr.setProcesso(processo);
			pr.setRegra(regra);
			Date dataAlteracao = regra.getDataAlteracao();
			pr.setDataRegra(dataAlteracao);

			TipoExecucaoRegra tipoExecucao = regra.getTipoExecucao();
			if(TipoExecucaoRegra.IMEDIATA.equals(tipoExecucao)) {
				imediatas.add(pr);
			}else{
				agendadas.add(pr);
			}
		}

		if(criarImediatas) {
			for(ProcessoRegra pr: imediatas){
				Map<CampoGrupo, List<Campo>> camposMap = campoService.findMapByProcesso(null, processo.getId(), false);

				regraService.executarRegra(pr, usuario, camposMap,false);

				FarolRegra farol = pr.getFarol();
				StatusProcessoRegra status = pr.getStatus();
				if(FarolRegra.VERMELHO.equals(farol) || StatusProcessoRegra.ERRO.equals(status)) {
					String mensagem = pr.getMensagem();
					mensagensErro.add(mensagem);
				}

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					ProcessoRegraService processoRegraService = applicationContext.getBean(ProcessoRegraService.class);
					processoRegraService.saveOrUpdate(pr);
				});

				tw.runNewThread();
				tw.throwException();
			}
		}

		if(criarAgendadas && mensagensErro.isEmpty()) {
			for(ProcessoRegra pr: agendadas){
				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					ProcessoRegraService processoRegraService = applicationContext.getBean(ProcessoRegraService.class);
					processoRegraService.saveOrUpdate(pr);
				});

				tw.runNewThread();
				tw.throwException();
			}
		}

		if(!mensagensErro.isEmpty()) {
			String mensagens = String.join("\n", mensagensErro);
			return mensagens;
		}
		return null;
	}

	@Transactional(rollbackFor=Exception.class)
	public List<ProcessoRegra> reprocessarTodasRegras(Processo processo, Usuario usuario) throws MessageKeyListException {

		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		Long processoId = processo.getId();
		filtro.setProcessoId(processoId);
		filtro.setStatusList(Arrays.asList(StatusProcessoRegra.PENDENTE));
		List<ProcessoRegra> prs = findLasts(filtro);
		Map<Regra, ProcessoRegra> mapPrs = new LinkedHashMap<>();
		for (ProcessoRegra pr : prs) {
			Regra regra = pr.getRegra();
			Integer decisaoFluxo = regra.getDecisaoFluxo();
			if(decisaoFluxo == null || !decisaoFluxo.equals(1)) {
				mapPrs.put(regra, pr);
			}
		}

		List<Situacao> situacoes = processoLogService.findSituacoesProcesso(processoId);
		for (Situacao situacao : situacoes) {
			List<Regra> regras = getRegrasDoProcesso(processo, situacao);
			for (Regra regra : regras) {
				ProcessoRegra pr = mapPrs.get(regra);
				if(pr == null) {
					Integer decisaoFluxo = regra.getDecisaoFluxo();
					if(decisaoFluxo != null && decisaoFluxo.equals(1)){
						continue;
					}
					pr = new ProcessoRegra();
					pr.setStatus(StatusProcessoRegra.REPROCESSANDO);
					pr.setProcesso(processo);
					pr.setRegra(regra);
					Date dataAlteracao = regra.getDataAlteracao();
					pr.setDataRegra(dataAlteracao);
					mapPrs.put(regra, pr);
				}
			}
		}

		Collection<ProcessoRegra> prsProcessar = mapPrs.values();

		String observacao = "Processamento de regras.";
		processarRegras(processo, usuario, observacao, prsProcessar, false, false);

		return new ArrayList<>(prsProcessar);
	}

	@Transactional(rollbackFor=Exception.class)
	public void processarRegras(Processo processo, Usuario usuario, String observacao, Collection<ProcessoRegra> prsProcessar, boolean reprocessar, boolean avancarSituacao) throws MessageKeyListException {
        processarRegras(processo, usuario, observacao, prsProcessar, null, reprocessar, avancarSituacao);
    }

	@Transactional(rollbackFor=Exception.class)
	public void processarRegras(Processo processo, Usuario usuario, String observacao, Collection<ProcessoRegra> prsProcessar, Map<CampoGrupo, List<Campo>> camposMap, boolean reprocessar, boolean avancarSituacao) throws MessageKeyListException {

		List<MessageKeyException> exceptions = new ArrayList<>();
		for (ProcessoRegra pr : prsProcessar) {
			try {
				Processo p = pr.getProcesso();
				Hibernate.initialize(p);

				Regra regra = pr.getRegra();
				Long regraId = regra.getId();
				RegraLinha raiz = regraLinhaService.getRaiz(regraId);
				Set<SubRegra> subRegras = raiz.getSubRegras();
				for(SubRegra subRegra : subRegras) {
					List<SubRegraAcao> subRegraAcoes = subRegra.getSubRegraAcoes();
					for (SubRegraAcao subRegraAcao : subRegraAcoes) {
						SubRegraAcoes acao = subRegraAcao.getAcao();
						if (SubRegraAcoes.ALTERAR_OBRIGATORIEDADE_CAMPO.equals(acao)) {
							observacao = registrarLogRegrasAlteracaoObrigatoriedade(processo, usuario, exceptions, observacao, subRegraAcao);
						}
					}
				}

				ProcessoRegra processoRegra = regraService.executarRegraPropagation(pr, usuario, camposMap, reprocessar);

				Situacao situacaoDestino = processoRegra.getSituacaoDestino();
				if(situacaoDestino != null && avancarSituacao) {
					Regra regraProcesso = processoRegra.getRegra();
					mudarSituacaoProcesso(processo, regraProcesso, usuario, exceptions, situacaoDestino);
				}
			}
			catch (MessageKeyException e) {
				exceptions.add(e);
			}
			catch (Exception e) {
				e.printStackTrace();
				String exceptionMessage = DummyUtils.getExceptionMessage(e);
				Regra regra = pr.getRegra();
				Long regraId = regra.getId();
				String regraNome = regra.getNome();
				Long processoId = processo.getId();
				exceptions.add(new MessageKeyException("erroInesperadoRegra.error", "#" + regraId + " " + regraNome, processoId.toString(), exceptionMessage));
			}
		}
		observacao = observacao != null ? observacao : "";
		registrarLogRegras(processo, usuario, exceptions, observacao);
	}

	private String registrarLogRegrasAlteracaoObrigatoriedade(Processo processo, Usuario usuario, List<MessageKeyException> exceptions, String observacao, SubRegraAcao subRegraAcao) {

		List<Long> tipoCampoIdsList = subRegraAcao.getTipoCampoIdList();
		Long processoId = processo.getId();
		List<Campo> processoCampos = campoService.findByProcessoTipoCampos(processoId, tipoCampoIdsList);

		for(Campo campo : processoCampos){

			String nomeCampo = campo.getNome();
			boolean campoProcessoObrigatoriedade = campo.getObrigatorio();

			Boolean obrigatoriedade = subRegraAcao.getObrigatoriedadeCampo();

			if(!obrigatoriedade.equals(campoProcessoObrigatoriedade)) {
				if(observacao != null) {
					observacao = observacao + ("Campo " + nomeCampo + "\nDe: " + campoProcessoObrigatoriedade +"\nPara: " + obrigatoriedade + "\n\n");
				} else {
					observacao = "Campo " + nomeCampo + "\nDe: " + campoProcessoObrigatoriedade +"\nPara: " + obrigatoriedade + "\n\n";
				}
			}
		}
		return observacao;
	}

	private void mudarSituacaoProcesso(Processo processo, Regra regra, Usuario usuario, List<MessageKeyException> exceptions, Situacao situacaoDestino) {
		try {
			Situacao situacaoDestino2 = situacaoService.get(situacaoDestino.getId());
			Processo processo2 = processoService.get(processo.getId());
			processoService.alterarSituacaoMudancaDeFluxoRegra(processo2, usuario, situacaoDestino2, null, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			String message = exceptionService.getMessage(e);
			String situacaoNome = situacaoDestino.getNome();
			Long regraId = regra.getId();
			String regraNome = regra.getNome();
			Long processoId = processo.getId();
			exceptions.add(new MessageKeyException("erroInesperadoRegra.error", "#" + regraId + " " + regraNome, processoId.toString(), "Falha ao tentar alterar para a situação " + situacaoNome + ": " + message));
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void reprocessarRegrasComErro(Processo processo, Usuario usuario) throws MessageKeyException, MessageKeyListException {

		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		Long processoId = processo.getId();
		filtro.setProcessoId(processoId);
		filtro.setStatusList(Arrays.asList(StatusProcessoRegra.ERRO));
		List<ProcessoRegra> prsComErro = findLasts(filtro);

		List<ProcessoRegra> prsNovos = new ArrayList<>();
		for (ProcessoRegra pr : prsComErro) {
			Regra regra = pr.getRegra();
			ProcessoRegra prNovo = new ProcessoRegra();
			prNovo.setStatus(StatusProcessoRegra.REPROCESSANDO);
			prNovo.setProcesso(processo);
			prNovo.setRegra(regra);
			Date dataAlteracao = regra.getDataAlteracao();
			pr.setDataRegra(dataAlteracao);
			prsNovos.add(prNovo);
		}

		String observacao = "Reprocessando regras com erros.";
		processarRegras(processo, usuario, observacao, prsNovos, false, false);
	}

	private void registrarLogRegras(Processo processo, Usuario usuario, List<MessageKeyException> exceptions, String observacao) throws MessageKeyListException {

		StringBuilder observacao2 = new StringBuilder();
		observacao2.append(observacao).append("\n");
		try {
			if(exceptions.size() > 0) {
				observacao2.append("Erro(s) na execução:\n");
				exceptionService.getMessage(observacao2, exceptions);
				throw new MessageKeyListException(exceptions);
			}
		}
		finally {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				Processo processo2 = processoService.get(processo.getId());
				ProcessoLog log = processoLogService.criaLog(processo2, usuario, AcaoProcesso.REPROCESSAMENTO_REGRAS);
				log.setObservacao(observacao2.toString());
				processoLogService.saveOrUpdate(log);
			});
			tw.runNewThread();
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public ProcessoRegra reprocessarRegra(Processo processo, Regra regra, Usuario usuario) throws MessageKeyException, MessageKeyListException {

		ProcessoRegra pr = new ProcessoRegra();
		pr.setStatus(StatusProcessoRegra.REPROCESSANDO);
		pr.setProcesso(processo);
		pr.setRegra(regra);
		Date dataAlteracao = regra.getDataAlteracao();
		pr.setDataRegra(dataAlteracao);

		String observacao = "Reprocessando regra " + regra.getNome() + "\n";
		processarRegras(processo, usuario, observacao, Arrays.asList(pr), true, false);

		return pr;
	}

	@Transactional(rollbackFor = Exception.class)
    public void reprocessaRegrasAoAlterarCampos(Processo processo, Usuario usuario, Map<CampoGrupo, List<Campo>> camposMap) {

		/*StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.RASCUNHO.equals(status) && !StatusProcesso.PENDENTE.equals(status)){
			return;
		}*/

        ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
        Long processoId = processo.getId();
		filtro.setProcessoId(processoId);
		filtro.setAtivo(true);
        filtro.setReprocessaRegraEditarCampos(true);

        List<ProcessoRegra> lasts = findLasts(filtro);
        List<ProcessoRegra> regras = new ArrayList<>();
        for(ProcessoRegra pr: lasts) {
            Regra regra = pr.getRegra();
            boolean reprocessaEditarCampos = regra.isReprocessaEditarCampos();
            if(reprocessaEditarCampos){
                regras.add(pr);
            }
        }

        processarRegras(processo, usuario, null, regras, camposMap,false, false);
    }

	@Transactional(rollbackFor=Exception.class)
	public void reprocessaRegrasAoAtualizarDocumentos(Processo processo, Usuario usuario) {

		Long processoId = processo.getId();
		processo = processoService.get(processoId);

		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setProcessoId(processoId);
		filtro.setReprocessaRegraAtualizarDocumentos(true);

		DummyUtils.systraceThread("Reprocessando regras", LogLevel.FATAL);

		List<ProcessoRegra> lasts = findLasts(filtro);
		List<ProcessoRegra> regras = new ArrayList<>();
		for(ProcessoRegra pr: lasts) {
			Regra regra = pr.getRegra();
			String nome = regra.getNome();
			DummyUtils.systraceThread("Reprocessando regra " + nome, LogLevel.FATAL);
			boolean isReprocessaAtualizarDocumentos = regra.isReprocessaAtualizarDocumentos();
			if(isReprocessaAtualizarDocumentos){
				pr.setProcesso(processo);
				regras.add(pr);
			}
		}

		processarRegras(null, usuario, null, regras, null,false, false);
	}

	public List<Object[]> findToExpurgo(Date dataCorte, int preservar, int max) {
		if(preservar < 1) {
			throw new IllegalArgumentException("O número de registros a serem preservados não pode ser menor que 1: " + preservar);
		}
		return processoRegraRepository.findToExpurgo(dataCorte, preservar, max);
	}

	public void expurgar(Object[] obj, int preservar) {
		if(preservar < 1) {
			throw new IllegalArgumentException("O número de registros a serem preservados não pode ser menor que 1: " + preservar);
		}

		systraceThread("expurgando: " + Arrays.asList(obj));

		Long processoId = (Long) obj[0];
		Long regraId = (Long) obj[1];
		processoRegraRepository.expurgar(processoId, regraId, preservar);
	}
}
