package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.domain.vo.RelatorioOcrVO;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.domain.vo.filtro.LogOcrFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatoriosService {

	@Autowired private SolicitacaoService solicitacaoService;
	@Autowired private ProcessoService processoService;
	@Autowired private LogOcrService logOcrService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private RelatorioGeralSituacaoService relatorioGeralSituacaoService;

	private class UsuarioComparator implements Comparator<Usuario> {

		@Override
		public int compare(Usuario o1, Usuario o2) {

			String nome1 = o1 != null ? o1.getNome() : "";
			String nome2 = o2 != null ? o2.getNome() : "";

			int compareTo = nome1.compareTo(nome2);
			if(compareTo == 0) {

				Long id1 = o1 != null ? o1.getId() : 0l;
				Long id2 = o2 != null ? o2.getId() : 0l;

				compareTo = id1.compareTo(id2);
			}

			return compareTo;
		}
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioEmAbertoStatus(ProcessoFiltro filtro, Boolean allStatus) {

	    List<StatusProcesso> statusEmAndamento;
		if(allStatus){
            statusEmAndamento = StatusProcesso.getTodosStatus();
        }else {
            statusEmAndamento = StatusProcesso.getStatusEmAndamento();
        }
		List<StatusProcesso> statusList = new ArrayList<>();
		statusList = ListUtils.union(statusList, statusEmAndamento);

		filtro.setStatusList(statusList);

		List<Processo> processos = processoService.findByFiltro(filtro, null, null);

		if(!allStatus) {
            Date dataInicio = DateUtils.getFirstTimeOfDay();
            List<StatusProcesso> statusFechado = StatusProcesso.getStatusFechado();
            filtro.setStatusList(statusFechado);
            filtro.setDataInicio(dataInicio);
            filtro.setConsiderarData(ProcessoFiltro.ConsiderarData.FINALIZACAO);

            List<Processo> processosFechadosHoje = processoService.findByFiltro(filtro, null, null);

            processos = ListUtils.union(processos, processosFechadosHoje);
            statusList = ListUtils.union(statusList, statusFechado);
        }

		Map<StatusProcesso, RelatorioAcompanhamentoVO> map = new TreeMap<>();

		for (StatusProcesso status : statusList) {

			RelatorioAcompanhamentoVO vo = new RelatorioAcompanhamentoVO();
			vo.setProcessoService(processoService);
			vo.setNomeLinha(status.name());
			vo.setStatus(status);
			map.put(status, vo);
		}

		for (Processo processo : processos) {

			StatusProcesso status = processo.getStatus();
			RelatorioAcompanhamentoVO vo = map.get(status);
			vo.add(processo);
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);

		return result;
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioEmAbertoAnalista(ProcessoFiltro filtro) {

		ProcessoFiltro filtro2 = filtro.clone();
		List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();
		filtro2.setStatusList(statusEmAndamento);

		List<Processo> list = processoService.findByFiltro(filtro2, null, null);
		Map<Usuario, RelatorioAcompanhamentoVO> map = new TreeMap<>(new UsuarioComparator());

		for (Processo processo : list) {
			Usuario analista = processo.getAnalista();
			String analistaNome = analista != null ? analista.getNome() : "";
			RelatorioAcompanhamentoVO vo = map.get(analista);

			if(vo == null) {
				vo = new RelatorioAcompanhamentoVO();
				vo.setProcessoService(processoService);
				vo.setNomeLinha(analistaNome);
				vo.setAnalista(analista);
				map.put(analista, vo);
			}

			vo.add(processo);
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);
		return result;
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioEmAbertoMotivo(ProcessoFiltro filtro) {

		ProcessoFiltro filtro2 = filtro.clone();
		List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();
		filtro2.setStatusList(statusEmAndamento);

		List<Processo> list = processoService.findByFiltro(filtro2, null, null);

		Map<TipoProcesso, RelatorioAcompanhamentoVO> map = new TreeMap<>(new SuperBeanComparator<TipoProcesso>("nome"));

		for (Processo processo : list) {

			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			if(tipoProcesso != null) {

				RelatorioAcompanhamentoVO vo = map.get(tipoProcesso);

				if(vo == null) {
					vo = new RelatorioAcompanhamentoVO();
					vo.setProcessoService(processoService);
					vo.setNomeLinha(tipoProcesso.getNome());
					vo.setTipoProcesso(tipoProcesso);
					map.put(tipoProcesso, vo);
				}

				vo.add(processo);
			}
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);
		return result;
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioEmAbertoSituacao(ProcessoFiltro filtro, boolean agrupar, boolean historico) {

		ProcessoFiltro filtro2 = filtro.clone();
		List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();
		List<StatusProcesso> statusList = new ArrayList<>();
		statusList.addAll(statusEmAndamento);
		filtro2.setStatusList(statusList);

		List<Processo> list = new ArrayList<>();
		List<RelatorioGeralSituacao> listHistorico = new ArrayList<>();
		if (historico) {
			RelatorioGeralFiltro filtro3 = new RelatorioGeralFiltro();
			statusList.add(StatusProcesso.CONCLUIDO);
			statusList.add(StatusProcesso.CANCELADO);
			filtro3.setStatusList(statusList);
			filtro3.setDataFim(filtro.getDataFim());
			filtro3.setDataInicio(filtro.getDataInicio());
			filtro3.setTiposProcessoList(filtro.getTiposProcesso());
			listHistorico = relatorioGeralSituacaoService.findByFiltro(filtro3);
		}
		else {
			list = processoService.findByFiltro(filtro2, null, null);
		}

		Map<Situacao, RelatorioAcompanhamentoVO> map = null;

		if (agrupar) {
			map = new TreeMap<>(new SuperBeanComparator<>("nome"));
		} else {
			map = new TreeMap<>(new SuperBeanComparator<>("tipoProcesso.nome,nome"));
		}

		RelatorioAcompanhamentoVO analise = new RelatorioAcompanhamentoVO();
		analise.setProcessoService(processoService);
		analise.setNomeLinha("1.x - Análise");
		Situacao situacaoAnalise = new Situacao();
		situacaoAnalise.setNome(" 1.x - Análise");
		map.put(situacaoAnalise, analise);

		RelatorioAcompanhamentoVO acompanhamento = new RelatorioAcompanhamentoVO();
		acompanhamento.setProcessoService(processoService);
		acompanhamento.setNomeLinha("2.x - Acompanhamento");
		Situacao situacaoAcompanhamento = new Situacao();
		situacaoAcompanhamento.setNome(" 2.x - Acompanhamento");
		map.put(situacaoAcompanhamento, acompanhamento);

		List<Situacao> situacoes = situacaoService.findAtivas(null);
		for (Situacao situacao : situacoes) {

			StatusProcesso status = situacao.getStatus();
			if(StatusProcesso.RASCUNHO.equals(status)) {
				continue;
			}
			String situacaoNome = situacao.getNome();
			RelatorioAcompanhamentoVO vo = map.get(situacao);
			if(vo == null) {
				vo = new RelatorioAcompanhamentoVO();
				vo.setProcessoService(processoService);
				vo.setNomeLinha(situacaoNome);
				vo.setSituacao(situacao);
			}

			ProcessoLogFiltro filtro3 = new ProcessoLogFiltro();
			filtro3.setSituacao(Arrays.asList(situacao));
			Date dataInicio = DateUtils.getFirstTimeOfDay();
			filtro3.setDataInicio(dataInicio);
			Date dataFim = DummyUtils.truncateFinalDia(dataInicio);
			filtro3.setDataFim(dataFim);
			Integer count = processoLogService.countByFiltro(filtro3);
			Integer hoje = vo.getHoje();
			hoje = hoje != null ? hoje : 0;
			vo.setHoje(hoje + count);

			StatusProcesso situacaoStatus = situacao.getStatus();
			if(StatusProcesso.EM_ANALISE.equals(situacaoStatus)) {
				Integer hoje2 = analise.getHoje();
				hoje2 = hoje2 != null ? hoje2 : 0;
				analise.setHoje(hoje2 + count);
			}
			else if(StatusProcesso.EM_ACOMPANHAMENTO.equals(situacaoStatus)) {
				Integer hoje2 = acompanhamento.getHoje();
				hoje2 = hoje2 != null ? hoje2 : 0;
				acompanhamento.setHoje(hoje2 + count);
			}

			map.put(situacao, vo);
		}

		for (Processo processo : list) {

			Situacao situacao = processo.getSituacao();
			if(situacao != null) {

				RelatorioAcompanhamentoVO vo = map.get(situacao);

				if(vo == null) {
					vo = new RelatorioAcompanhamentoVO();
					vo.setProcessoService(processoService);
					TipoProcesso tipoProcesso = situacao.getTipoProcesso();
					vo.setNomeLinha((!agrupar ? "(" + tipoProcesso.getNome() + ") " : "") + situacao.getNome());
					vo.setSituacao(situacao);
					map.put(situacao, vo);
				}
				vo.add(processo);

				StatusProcesso situacaoStatus = situacao.getStatus();
				if(StatusProcesso.EM_ANALISE.equals(situacaoStatus)) {
					analise.add(processo);
				}
				else if(StatusProcesso.EM_ACOMPANHAMENTO.equals(situacaoStatus)) {
					acompanhamento.add(processo);
				}
			}
		}

		for (RelatorioGeralSituacao processo : listHistorico) {

			Situacao situacao = processo.getSituacao();
			if(situacao != null) {

				RelatorioAcompanhamentoVO vo = map.get(situacao);

				if(vo == null) {
					vo = new RelatorioAcompanhamentoVO();
					TipoProcesso tipoProcesso = situacao.getTipoProcesso();
					vo.setProcessoService(processoService);
					vo.setNomeLinha((!agrupar ? "(" + tipoProcesso.getNome() + ") " : "") + situacao.getNome());
					vo.setSituacao(situacao);
					map.put(situacao, vo);
				}
				vo.add(processo);

				StatusProcesso situacaoStatus = situacao.getStatus();
				if(StatusProcesso.EM_ANALISE.equals(situacaoStatus)) {
					analise.add(processo);
				}
				else if(StatusProcesso.EM_ACOMPANHAMENTO.equals(situacaoStatus)) {
					acompanhamento.add(processo);
				}
			}
		}

		if(!historico && agrupar) {
			Date dataInicio = DateUtils.getFirstTimeOfDay();
			Date dataFim = DateUtils.getLastTimeOfDay();
			Map<String, BigDecimal> tempoMedioSituacao = relatorioGeralSituacaoService.findTempoMedioSituacao(dataInicio, dataFim);
			Set<String> situacoesNomes = tempoMedioSituacao.keySet();

			for (String situacaoNome : situacoesNomes) {
				Situacao situacao = null;
				for (Situacao situacaokey : map.keySet()) {
					String situacaoNomeKey = situacaokey.getNome();
					if (situacaoNome.equals(situacaoNomeKey)) {
						situacao = situacaokey;
						break;
					}
				}
				if (situacao == null) {
					RelatorioAcompanhamentoVO vo = new RelatorioAcompanhamentoVO();
					situacao = new Situacao();
					situacao.setNome(situacaoNome);
					vo.setSituacao(situacao);
					vo.setNomeLinha(situacaoNome);
					vo.setProcessoService(processoService);
					map.put(situacao, vo);
				}

				BigDecimal tempoMedio = tempoMedioSituacao.get(situacaoNome);
				String tempoMedioStr = "";

				if (tempoMedio.compareTo(BigDecimal.ZERO) == 1) {
					tempoMedioStr = DummyUtils.getHoras(null, tempoMedio, false);
				}

				RelatorioAcompanhamentoVO vo = map.get(situacao);
				vo.setTempoMedio(tempoMedioStr);
			}

			Map<StatusProcesso, BigDecimal> tempoMedioStatusProcesso = relatorioGeralSituacaoService.findTempoMedioStatusProcesso(dataInicio, dataFim);
			Set<StatusProcesso> statusSet = tempoMedioStatusProcesso.keySet();

			for (StatusProcesso statusProcesso : statusSet) {

				BigDecimal tempoMedio = tempoMedioStatusProcesso.get(statusProcesso);
				String tempoMedioStr = "";

				if (tempoMedio.compareTo(BigDecimal.ZERO) == 1) {
					tempoMedioStr = DummyUtils.getHoras(null, tempoMedio, false);
				}

				if (StatusProcesso.EM_ANALISE.equals(statusProcesso)) {
					analise.setTempoMedio(tempoMedioStr);
				} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(statusProcesso)) {
					acompanhamento.setTempoMedio(tempoMedioStr);
				}
			}
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);
		return result;
	}

	private List<RelatorioAcompanhamentoVO> calcTotal(Map<?, RelatorioAcompanhamentoVO> map) {

		Collection<RelatorioAcompanhamentoVO> values = map.values();
		List<RelatorioAcompanhamentoVO> result = new ArrayList<>(values);

		int totalTotal = 0;
		int alertasTotal = 0;
		int atrasadosTotal = 0;
		int okTotal = 0;
		int alertasEmAcompanhamento = 0;
		int alertasEmAnalise = 0;
		int atrasadosEmAnalise = 0;
		int okEmAnalise = 0;
		int totalEmAnalise = 0;
		int atrasadosEmAcompanhamento = 0;
		int okEmAcompanhamento = 0;
		int totalEmAcompanhamento = 0;

		for (RelatorioAcompanhamentoVO vo : result) {
			String nomeLinha = vo.getNomeLinha();
			if(!"2.x - Acompanhamento".equals(nomeLinha) && !"1.x - Análise".equals(nomeLinha)) {
				int total = vo.getTotal();
				totalTotal += total;

				int alertas = vo.getAlertas();
				alertasTotal += alertas;

				int atrasados = vo.getAtrasados();
				atrasadosTotal += atrasados;

				int ok = vo.getOk();
				okTotal += ok;
			}

			alertasEmAcompanhamento += vo.getAtrasadosEmAcompanhamento();
			alertasEmAnalise += vo.getAlertasEmAnalise();
			atrasadosEmAnalise += vo.getAtrasadosEmAnalise();
			okEmAnalise += vo.getOkEmAnalise();
			totalEmAnalise += vo.getTotalEmAnalise();
			atrasadosEmAcompanhamento += vo.getAtrasadosEmAcompanhamento();
			okEmAcompanhamento += vo.getOkEmAcompanhamento();
			totalEmAcompanhamento += vo.getTotalEmAcompanhamento();
		}

		RelatorioAcompanhamentoVO voTotal = new RelatorioAcompanhamentoVO();
		voTotal.setNomeLinha("TOTAL");
		voTotal.setTotal(totalTotal);
		voTotal.setAlertas(alertasTotal);
		voTotal.setAtrasados(atrasadosTotal);
		voTotal.setOk(okTotal);
		voTotal.setOkEmAnalise(okEmAnalise);
		voTotal.setAlertasEmAnalise(alertasEmAnalise);
		voTotal.setAtrasadosEmAnalise(atrasadosEmAnalise);
		voTotal.setTotalEmAnalise(totalEmAnalise);
		voTotal.setOkEmAcompanhamento(okEmAcompanhamento);
		voTotal.setAtrasadosEmAcompanhamento(atrasadosEmAcompanhamento);
		voTotal.setAlertasEmAcompanhamento(alertasEmAcompanhamento);
		voTotal.setTotalEmAcompanhamento(totalEmAcompanhamento);

		Date dataInicio = DateUtils.getFirstTimeOfDay();
		Date dataFim = DateUtils.getLastTimeOfDay();
		BigDecimal tempoMedioBD = relatorioGeralSituacaoService.getTempoMedioSituacaoTotal(dataInicio, dataFim);
		if (tempoMedioBD.compareTo(BigDecimal.ZERO) == 1) {
			String tempoMedio = DummyUtils.getHoras(null, tempoMedioBD, false);
			voTotal.setTempoMedio(tempoMedio);
		}

		result.add(voTotal);
		return result;
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioEmAbertoArea(ProcessoFiltro filtro, Usuario usuario) {

		ProcessoFiltro filtro2 = filtro.clone();
		List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();
		filtro2.setStatusList(statusEmAndamento);
		List<Processo> list = processoService.findByFiltro(filtro2, null, null);

		Map<Area, RelatorioAcompanhamentoVO> map = new LinkedHashMap<>();

		for (Processo processo : list) {

			Set<Area> areas = getAreasPendentes(processo, usuario);

			for (Area area : areas) {

				RelatorioAcompanhamentoVO vo = map.get(area);

				if(vo == null) {
					vo = new RelatorioAcompanhamentoVO();
					vo.setProcessoService(processoService);
					vo.setNomeLinha(area.getDescricao());
					vo.setArea(area);
					map.put(area, vo);
				}

				vo.add(processo);
			}
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);

		SuperBeanComparator<RelatorioAcompanhamentoVO> sbc = new SuperBeanComparator<>("area.descricao");
		sbc.setNullFirst(false);
		Collections.sort(result, sbc);

		return result;
	}

	private Set<Area> getAreasPendentes(Processo processo, Usuario usuario) {

		Long processoId = processo.getId();
		RoleGD roleGD = usuario.getRoleGD();

		List<SolicitacaoVO> solicitacoes = solicitacaoService.findVosPendentesByProcesso(processoId, roleGD);

		Set<Area> areas = new HashSet<>();
		for (SolicitacaoVO log : solicitacoes) {

			Solicitacao solicitacao = log.getSolicitacao();
			Subarea subarea = solicitacao.getSubarea();
			Area area = subarea.getArea();
			areas.add(area);
		}

		return areas;
	}

	public List<RelatorioAcompanhamentoVO> getRelatorioOcr(LogOcrFiltro filtro) {

		List<RelatorioOcrVO> list = logOcrService.findRelatorioByFiltro(filtro, null, null, false);

		Map<StatusOcr, RelatorioAcompanhamentoVO> map = new TreeMap<>();

		for (RelatorioOcrVO voOcr : list) {

			Processo processo = voOcr.getProcesso();
			StatusOcr statusOcr = processo.getStatusOcr();
			if(statusOcr != null) {

				RelatorioAcompanhamentoVO vo = map.get(statusOcr);

				if(vo == null) {
					vo = new RelatorioAcompanhamentoVO();
					vo.setProcessoService(processoService);
					vo.setNomeLinha(statusOcr.name());
					TipoProcesso tp = new TipoProcesso();
					tp.setNome(statusOcr.name());
					vo.setTipoProcesso(tp);
					vo.setTotal(0);
					map.put(statusOcr, vo);
				}
				vo.setTotal(vo.getTotal()+1);
			}
		}

		List<RelatorioAcompanhamentoVO> result = calcTotal(map);
		return result;
	}
}
