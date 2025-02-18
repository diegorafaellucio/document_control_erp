package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.RelatorioGeralSituacaoRepository;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.HorasUteisCalculator;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatorioGeralSituacaoService {

	@Autowired private ProcessoLogService processoLogService;

	@Autowired private RelatorioGeralSituacaoRepository relatorioGeralSituacaoRepository;

	public RelatorioGeralSituacao get(Long id) {
		return relatorioGeralSituacaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RelatorioGeralSituacao rg) throws MessageKeyException {
		relatorioGeralSituacaoRepository.saveOrUpdate(rg);
	}

	public List<RelatorioGeralSituacao> findByProcessosIds(List<Long> processosIds) {
		List<RelatorioGeralSituacao> list = relatorioGeralSituacaoRepository.findByProcessosIds(processosIds);
		return list;
	}

	public Map<Date, RelatorioGeralSituacao> findByProcessosIdsMap(List<Long> processosIds2) {

		List<RelatorioGeralSituacao> list = relatorioGeralSituacaoRepository.findByProcessosIds(processosIds2);
		Map<Date, RelatorioGeralSituacao> map = new HashMap<>();

		for (RelatorioGeralSituacao rga : list) {
			map.put(rga.getData(), rga);
		}

		return map;
	}

	public Collection<RelatorioGeralSituacao> criaRelatorioGeral(Processo processo, RelatorioGeral rg, Map<Date, RelatorioGeralSituacao> mapRelatorioSolicitacao, HorasUteisCalculator huc) {

		Long processoId = processo.getId();

		Set<RelatorioGeralSituacao> set = new LinkedHashSet<>();

		ProcessoLogFiltro filtro = new ProcessoLogFiltro();
		filtro.setProcessoId(processoId);
		filtro.setApenasComSituacao(true);
		filtro.setFiltrarRoles(false);
		filtro.setOrdenar("pl.id", SortOrder.ASCENDING);
		List<ProcessoLog> logs = processoLogService.findByFiltro(filtro, null, null);

		RelatorioGeralSituacao rgaAnterior = null;

		for (ProcessoLog log : logs) {

			Situacao situacao = log.getSituacao();
			if (situacao == null) {
				continue;
			}

			Date data = log.getData();
			Usuario usuario = log.getUsuario();
			Situacao situacaoAnterior = log.getSituacaoAnterior();

			Date prazoLimiteSituacao = null;

			ProcessoLog alteracaoSituacaoAnterior = processoLogService.findAlteracaoSituacaoAnteriorWithPrazo(log);
			if(alteracaoSituacaoAnterior != null) {
				prazoLimiteSituacao = alteracaoSituacaoAnterior.getPrazoLimiteSituacao();
			} else {
				prazoLimiteSituacao = log.getPrazoLimiteSituacao();
			}

			RelatorioGeralSituacao rga = mapRelatorioSolicitacao.get(data);
			if(rga == null) {
				rga = new RelatorioGeralSituacao();
				rga.setUsuarioInicio(usuario);
				if (prazoLimiteSituacao == null) {
					prazoLimiteSituacao = huc.addHoras(data, log.getSituacao().getHorasPrazo());
				}
				rga.setPrazoLimite(prazoLimiteSituacao);
			}

			mapRelatorioSolicitacao.put(data, rga);
			set.add(rga);

			rga.setRelatorioGeral(rg);
			rga.setProcessoId(processoId);
			rga.setSituacao(situacao);
			rga.setData(data);
			rga.setSituacaoAnterior(situacaoAnterior);

			if (rgaAnterior != null) {
				rgaAnterior.setDataFim(data);
				BigDecimal horas = huc.getHoras(rgaAnterior.getData(), data);
				rgaAnterior.setTempo(horas);
				rgaAnterior.setUsuarioFim(usuario);

				if (prazoLimiteSituacao != null) {
					BigDecimal horas2 = huc.getHoras(data, prazoLimiteSituacao);

					BigDecimal horasPrazoAdvertirSituacao = situacao.getHorasPrazoAdvertir();

					StatusPrazo statusPrazo = StatusPrazo.getByHorasRestantes(horas2, horasPrazoAdvertirSituacao);
					rgaAnterior.setStatusPrazoSituacao(statusPrazo);
				}

				if(usuario != null) {
					Long usuarioId = usuario.getId();
					Long rgaAnteriorProcessoId = rga.getProcessoId();
					Long tempoConferencia = processoLogService.getTempoTratativa(data, usuarioId, rgaAnteriorProcessoId);
					rgaAnterior.setTempoTratativa(tempoConferencia);
				}
			}
			rgaAnterior = rga;
		}

		for (RelatorioGeralSituacao rga2 : set) {
			relatorioGeralSituacaoRepository.saveOrUpdateWithoutFlush(rga2);
		}

		return set;
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralSituacaoRepository.findIdsByFiltro(filtro);
	}

	public List<RelatorioGeralSituacao> findByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralSituacaoRepository.findByFiltro(filtro, null, null);
	}

	public List<RelatorioGeralSituacao> findByIds(List<Long> ids) {
		return relatorioGeralSituacaoRepository.findByIds(ids);
	}

	public List<RelatorioGeralSituacao> findByRelatorioGeralIds(List<Long> relatorioGeralIds, RelatorioGeralFiltro filtro) {
		return relatorioGeralSituacaoRepository.findByRelatorioGeralIds(relatorioGeralIds, filtro);
	}

	public List<RelatorioGeralSituacao> find(Area area, Date dataInicio, Date dataFim) {
		return relatorioGeralSituacaoRepository.find(area,dataInicio, dataFim);
	}

	public List<Processo> findProcessos(Area area, int mes, int ano, Date dataInicio, Date dataFim) {
		return relatorioGeralSituacaoRepository.findProcessos(area, mes, ano, dataInicio, dataFim);
	}

	public Map<String, BigDecimal> findTempoMedioSituacao(Date dataInicio, Date dataFim) {
		return relatorioGeralSituacaoRepository.findTempoMedioSituacao(dataInicio, dataFim);
	}

	public Map<StatusProcesso, BigDecimal> findTempoMedioStatusProcesso(Date dataInicio, Date dataFim) {
		return relatorioGeralSituacaoRepository.findTempoMedioStatusProcesso(dataInicio, dataFim);
	}

	public BigDecimal getTempoMedioSituacaoTotal(Date dataInicio, Date dataFim) {
		return relatorioGeralSituacaoRepository.getTempoMedioSituacaoTotal(dataInicio, dataFim);
	}
}
