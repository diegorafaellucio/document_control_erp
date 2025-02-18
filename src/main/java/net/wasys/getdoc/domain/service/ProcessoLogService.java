package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.ProcessoLogRepository;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeProuniVO;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.RendimentoComposicaoFamiliarVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioAtividadeFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeProuniFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProcessoLogService {

	@Autowired private ProcessoLogRepository processoLogRepository;
	@Autowired private ApplicationContext applicationContext;

	public ProcessoLog get(Long id) {
		return processoLogRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ProcessoLog processoLog) {
		if (processoLog.getData() == null) {
			processoLog.setData(new Date());
		}

		processoLogRepository.saveOrUpdate(processoLog);
	}

	@Transactional(rollbackFor=Exception.class)
	public ProcessoLog criaLog(Processo processo, Usuario usuario, AcaoProcesso acao) {
		return criaLog(processo, usuario, acao, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public ProcessoLog criaLog(Processo processo, Usuario usuario, AcaoProcesso acao, String observacao) {

		ProcessoLog log = new ProcessoLog();
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setAcao(acao);
		log.setData(new Date());
		log.setObservacao(observacao);
		StatusProcesso status = processo.getStatus();
		log.setStatusProcesso(status);

		processoLogRepository.saveOrUpdate(log);

		return log;
	}

	public ProcessoLog criaLogErro(Processo processo, Usuario usuario, AcaoProcesso acao, ConsultaExterna consultaExterna, String observacao) {
		Bolso<ProcessoLog> bolso = new Bolso<>();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			ProcessoLogService processoLogService = applicationContext.getBean(ProcessoLogService.class);
			ProcessoLog log = processoLogService.criaLog(processo, usuario, acao, observacao);
			bolso.setObjeto(log);
		});
		tw.runNewThread();
		return bolso.getObjeto();
	}

	public List<ProcessoLog> findByProcesso(Long processoId) {
		return processoLogRepository.findByProcesso(processoId);
	}

	public ProcessoLog findLastLogByProcesso(Long processoId, Long situacaoId) {
		return processoLogRepository.findLastLogByProcesso(processoId, situacaoId);
	}

	public boolean isImportacao(Long processoId) {
		return processoLogRepository.isImportacao(processoId);
	}

	public List<ProcessoLog> findStatusByProcesso(Long processoId) {
		return processoLogRepository.findStatusByProcesso(processoId);
	}

	public Date getDataPrimeiroEnvioAnalise(Long processoId) {
		return processoLogRepository.getDataPrimeiroEnvioAnalise(processoId);
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalista(RelatorioProdutividadeFiltro filtro) {
		List<RelatorioProdutividadeVO> list = null;
		TipoProcesso tipoProcesso = filtro.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso != null ? tipoProcesso.getId() : null;
		if (!filtro.isAgrupar()) {
			list = new ArrayList<>();
			Date data = filtro.getDataInicio();
			while (!data.after(filtro.getDataFim())) {
				List<RelatorioProdutividadeVO> list2 = processoLogRepository.getRelatorioProdutividadeAnalista(data, data, tipoProcessoId, filtro.getSituacao(), filtro.getCamposFiltro());
				for(RelatorioProdutividadeVO vo : list2) {
					vo.setOrdenacao(vo.getRegistroDescricao() + " " + DummyUtils.formatDate(data));
					vo.setRegistroDescricao(DummyUtils.format(data, "dd/MM") + " " + vo.getRegistroDescricao());
				}
				list.addAll(list2);
				data = DateUtils.addDays(data, 1);
			}
			Collections.sort(list, Comparator.comparing(RelatorioProdutividadeVO::getOrdenacao));
		}
		else {
			list = processoLogRepository.getRelatorioProdutividadeAnalista(filtro.getDataInicio(), filtro.getDataFim(),tipoProcessoId, filtro.getSituacao(), filtro.getCamposFiltro());
			List<RelatorioProdutividadeVO> list2 = processoLogRepository.getRelatorioProdutividadeAnalistaNull(filtro.getDataInicio(), filtro.getDataFim(), tipoProcessoId, filtro.getSituacao());
			list.addAll(list2);
		}
		return list;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaCSC(RelatorioProdutividadeFiltro filtro) {
		List<RelatorioProdutividadeVO> list;
		if (!filtro.isAgrupar()) {
			list = new ArrayList<>();
			Date data = filtro.getDataInicio();
			while (!data.after(filtro.getDataFim())) {
				List<RelatorioProdutividadeVO> list2 = processoLogRepository.getRelatorioProdutividadeAnalistaCSC(data, data, filtro.getCamposFiltro());
				for(RelatorioProdutividadeVO vo : list2) {
					vo.setOrdenacao(vo.getRegistroDescricao() + " " + DummyUtils.formatDate(data));
					vo.setRegistroDescricao(DummyUtils.format(data, "dd/MM") + " " + vo.getRegistroDescricao());
				}
				list.addAll(list2);
				data = DateUtils.addDays(data, 1);
			}
			Collections.sort(list, Comparator.comparing(RelatorioProdutividadeVO::getOrdenacao));
		}
		else {
			list = processoLogRepository.getRelatorioProdutividadeAnalistaCSC(filtro.getDataInicio(), filtro.getDataFim(), filtro.getCamposFiltro());
		}
		return list;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaMedicina(RelatorioProdutividadeFiltro filtro) {
		List<RelatorioProdutividadeVO> list;
		if (!filtro.isAgrupar()) {
			list = new ArrayList<>();
			Date data = filtro.getDataInicio();
			while (!data.after(filtro.getDataFim())) {
				List<RelatorioProdutividadeVO> list2 = processoLogRepository.getRelatorioProdutividadeAnalistaMedicina(data, data, filtro.getCamposFiltro());
				for(RelatorioProdutividadeVO vo : list2) {
					vo.setOrdenacao(vo.getRegistroDescricao() + " " + DummyUtils.formatDate(data));
					vo.setRegistroDescricao(DummyUtils.format(data, "dd/MM") + " " + vo.getRegistroDescricao());
				}
				list.addAll(list2);
				data = DateUtils.addDays(data, 1);
			}
			Collections.sort(list, Comparator.comparing(RelatorioProdutividadeVO::getOrdenacao));
		}
		else {
			list = processoLogRepository.getRelatorioProdutividadeAnalistaMedicina(filtro.getDataInicio(), filtro.getDataFim(), filtro.getCamposFiltro());
		}
		return list;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeTipoRequisicao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {
		return processoLogRepository.getRelatorioProdutividadeTipoRequisicao(dataInicio, dataFim, camposFiltros);
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeSituacao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {
		return processoLogRepository.getRelatorioProdutividadeSituacao(dataInicio, dataFim, camposFiltros);
	}

	public List<Long> findIdsByFiltro(ProcessoLogFiltro filtro) {
		return processoLogRepository.findIdsByFiltro(filtro);
	}

	public List<ProcessoLog> findByIds(List<Long> ids) {
		return processoLogRepository.findByIds(ids);
	}

	public List<ProcessoLog> findByFiltro(ProcessoLogFiltro filtro, Integer inicio, Integer pagemax) {
		return processoLogRepository.findByFiltro(filtro, inicio, pagemax);
	}

	public Integer countByFiltro(ProcessoLogFiltro filtro) {
		return processoLogRepository.countByFiltro(filtro);
	}

	public Set<Long> getEvidenciasNaoLidas(List<Long> processosIds) {
		return processoLogRepository.getLogsNaoLidos(processosIds, AcaoProcesso.REGISTRO_EVIDENCIA, false);
	}

	public Set<Long> getReenviosNaoLidas(List<Long> processosIds) {
		return processoLogRepository.getLogsNaoLidos(processosIds, AcaoProcesso.REENVIO_ANALISE, false);
	}

	public void marcarComoLido(Long processoId, Usuario analista) {

		List<Long> ids = new ArrayList<>();
		ids.add(processoId);
		Set<Long> logs = processoLogRepository.getLogsNaoLidos(ids, null, true);
		for (Long logId : logs) {
			ProcessoLog log = get(logId);
			log.setLido(true);
			saveOrUpdate(log);
		}
	}

	public List<Situacao> findSituacoesProcesso(Long processoId) {
		return processoLogRepository.findSituacoesProcesso(processoId);
	}

	public List<Object[]> findAtividadesByFiltro(RelatorioAtividadeFiltro filtro) {
		return processoLogRepository.findAtividadesByFiltro(filtro);
	}

	public Boolean existPendenciaByProcesso(Long processoId) {
		return processoLogRepository.existPendenciaByProcesso(processoId);
	}

	public boolean existePosteriorA(Date data) {
		return processoLogRepository.existePosteriorA(data);
	}

	public List<RelatorioProdutividadeProuniVO> getRelatorioProdutividadeProuni(RelatorioProdutividadeProuniFiltro filtro) {

		List<Object[]> list = processoLogRepository.getRelatorioProdutividadeProuni(filtro);

		List<RelatorioProdutividadeProuniVO> voList = new ArrayList<>();
		for (Object[] obj : list) {

			RelatorioProdutividadeProuniVO vo = new RelatorioProdutividadeProuniVO();

			Processo processo = (Processo) obj[0];
			Long processoId = processo.getId();

			vo.setProcessoId(processoId);
			vo.setDiretoria(RelatorioProdutividadeProuniVO.DIRETORIA);
			vo.setNomeProcesso(RelatorioProdutividadeProuniVO.PROCESSO_NOME);
			vo.setSubNomeProcesso(RelatorioProdutividadeProuniVO.SUB_PROCESSO_NOME);

			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			String tipoProcessoNome = tipoProcesso.getNome();
			vo.setServico(tipoProcessoNome);

			Date dataVencimento = null;
			ProcessoLog logAbertura = (ProcessoLog) obj[1];
			if(logAbertura != null) {
				Situacao situacao = logAbertura.getSituacao();
				BigDecimal horasPrazo = situacao.getHorasPrazo();
				vo.setSla(horasPrazo.toString());

				Date dataAbertura = logAbertura.getData();
				String dataAberturaStr = DummyUtils.formatDateTime2(dataAbertura);
				vo.setDataAbertura(dataAberturaStr);

				dataVencimento = DateUtils.addDays(dataAbertura, 1);
				String dataVencimentoStr = DummyUtils.formatDateTime2(dataVencimento);
				vo.setDataVencimento(dataVencimentoStr);

				Usuario usuarioSolicitante = logAbertura.getUsuario();
				String solicitante = usuarioSolicitante != null ? usuarioSolicitante.getNome() : "";
				vo.setSolicitante(solicitante);
			}

			ProcessoLog logFechamento = (ProcessoLog) obj[6];
			if(logFechamento != null) {
				Date dataFechamento = logFechamento.getData();
				String dataFechamentoStr = DummyUtils.formatDateTime2(dataFechamento);
				vo.setDataFechamento(dataFechamentoStr);
			}

			ProcessoLog logResponsavel = (ProcessoLog) obj[5];
			Usuario usuarioResponsavel = logResponsavel != null ? logResponsavel.getUsuario() : null;
			String responsavel = usuarioResponsavel != null ? usuarioResponsavel.getNome() : "";
			vo.setResponsavel(responsavel);

			Long qtdReabertura = (Long) obj[2];
			vo.setQuantidadeReabertura(Integer.parseInt(qtdReabertura.toString()));

			Long qtdImagens = (Long) obj[3];
			vo.setQuantidadeImagens(Integer.parseInt(qtdImagens.toString()));

			Long qtdAcoes = (Long) obj[4];
			vo.setQuantidadeAcoes(Integer.parseInt(qtdAcoes.toString()));

			Date hoje = new Date();
			if(dataVencimento != null && logFechamento == null) {
				if(dataVencimento.after(hoje)) {
					vo.setStatus("Aberto no Prazo");
				}
				else if(dataVencimento.before(hoje)) {
					vo.setStatus("Aberto Expirado");
				}
				else if(dataVencimento != null) {
					vo.setStatus("Em Tratativa");
				}
			}
			else if(logFechamento != null) {
				Situacao situacaoAnterior = logFechamento.getSituacaoAnterior();
				Long situacaoAnteriorId = situacaoAnterior.getId();
				if(Situacao.SOLICITAR_EMISSAO_TCB_TR == situacaoAnteriorId) {
					vo.setStatus("Finalizado - No Escopo");
				}
				else {
					vo.setStatus("Finalizado - Fora de Escopo");
				}
			}

			voList.add(vo);
		}

		return voList;
	}

	public Usuario getAnalistaByProcessoIdAndData(Long processoId, Date data){
		return processoLogRepository.getAnalistaByProcessoIdAndData(processoId, data);
	}

	public Long getTempoTratativa(Date data, Long usuarioId, Long processoId){
		return processoLogRepository.getTempoTratativa(data, usuarioId, processoId);
	}

	public void registrarPrimeiraEntrada(Processo processo, Usuario usuarioLogado) {

		Long processoId = processo.getId();
		Long usuarioLogadoId = usuarioLogado.getId();
		StatusProcesso status = processo.getStatus();

		if(StatusProcesso.getStatusEmAndamento().contains(status) && isPodeRegitrarVisualizacao(processoId, usuarioLogadoId)) {
			criaLog(processo, usuarioLogado, AcaoProcesso.VISUALIZOU_PROCESSO);
		}
	}

	public boolean isPodeRegitrarVisualizacao(Long processoId, Long usuarioId){
		return processoLogRepository.isPodeRegitrarVisualizacao(processoId,usuarioId);
	}

	public ProcessoLog findSituacaoAnterior(Processo processo, Situacao situacaoAtual, ProcessoLog processoLogAtual) {
		return processoLogRepository.findSituacaoAnterior(processo, situacaoAtual, processoLogAtual);
	}

	public List<Long> findProcessoIdToAjustarEtapa(Date date, Date dataFim) {
		return processoLogRepository.findProcessoIdToAjustarEtapa(date, dataFim);
	}

	public List<ProcessoLog> findLogToAjustarEtapa(Date dataInicio, Date dataFim) {
		return processoLogRepository.findLogToAjustarEtapa(dataInicio, dataFim);
	}

	public List<ProcessoLog> findLogToAjustarEtapaEPrazoLimite(Date dataInicio, Date dataFim) {
		return processoLogRepository.findLogToAjustarEtapaEPrazoLimiteSituacao(dataInicio, dataFim);
	}

	public ProcessoLog findAlteracaoSituacaoAnterior(ProcessoLog processoLogAtual) {
		return processoLogRepository.findAlteracaoSituacaoAnterior(processoLogAtual);
	}

	public ProcessoLog findAlteracaoSituacaoAnteriorWithPrazo(ProcessoLog processoLogAtual) {
		return processoLogRepository.findAlteracaoSituacaoAnteriorWithPrazo(processoLogAtual);
	}

	public void criaLogCalculoSalarioMinimo(Processo processo, Usuario usuarioLogado, RendimentoComposicaoFamiliarVO vo) throws JsonProcessingException {

		ObjectMapper om = new ObjectMapper();
		String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(vo);
		criaLog(processo, usuarioLogado, AcaoProcesso.CALCULO_SALARIO_MINIMO, json);
	}

	public ProcessoLog findLastLogByProcessoAndAcao(Long processoId, AcaoProcesso acao) {
		return processoLogRepository.findLastLogByProcessoAndAcao(processoId, acao);
	}

	public Map<Long, Date> findMapToAtualizarDataUltimaAtualizacao(Date inicio, Date fim) {
		return processoLogRepository.findMapToAtualizarDataUltimaAtualizacao(inicio, fim);
	}

	public ProcessoLog getLastProcessoLogByUsuarioAndData(Usuario usuario, Date data) {
		return processoLogRepository.getLastProcessoLogByUsuarioAndData(usuario, data);
	}
}
