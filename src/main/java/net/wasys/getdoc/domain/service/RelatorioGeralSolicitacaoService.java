package net.wasys.getdoc.domain.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.wasys.util.other.HorasUteisCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.entity.RelatorioGeralSolicitacao;
import net.wasys.getdoc.domain.entity.Solicitacao;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;
import net.wasys.getdoc.domain.repository.RelatorioGeralSolicitacaoRepository;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class RelatorioGeralSolicitacaoService {

	@Autowired private ProcessoLogService processoLogService;

	@Autowired private RelatorioGeralSolicitacaoRepository	relatorioGeralSolicitacaoRepository;

	public RelatorioGeralSolicitacao get(Long id) {
		return relatorioGeralSolicitacaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RelatorioGeralSolicitacao rg) throws MessageKeyException {
		relatorioGeralSolicitacaoRepository.saveOrUpdate(rg);
	}

	public Map<Long, RelatorioGeralSolicitacao> findByProcessosIds(List<Long> processosIds2) {

		List<RelatorioGeralSolicitacao> list = relatorioGeralSolicitacaoRepository.findByProcessosIds(processosIds2);
		Map<Long, RelatorioGeralSolicitacao> map = new HashMap<>();

		for (RelatorioGeralSolicitacao rga : list) {

			Long solicitacaoId = rga.getSolicitacaoId();
			map.put(solicitacaoId, rga);
		}

		return map;
	}

	public void criaRelatorioGeral(Processo processo, RelatorioGeral rg, Map<Long, RelatorioGeralSolicitacao> mapRelatorioSolicitacao, HorasUteisCalculator huc) {

		Long processoId = processo.getId();

		Map<Long, RelatorioGeralSolicitacao> map = new LinkedHashMap<>();

		List<ProcessoLog> logs = processoLogService.findByProcesso(processoId);

		ProcessoLog ultimoLog = null;
		ProcessoLog ultimoLogCriacaoOuNaoAceite = null;
		RelatorioGeralSolicitacao rga = null;

		for (ProcessoLog log : logs) {

			Solicitacao solicitacao = log.getSolicitacao();
			if (solicitacao == null) {
				continue;
			}

			ultimoLog  = log;

			Long solicitacaoId = solicitacao.getId();

			rga = mapRelatorioSolicitacao.get(solicitacaoId);
			rga = rga != null ? rga : new RelatorioGeralSolicitacao();
			mapRelatorioSolicitacao.put(solicitacaoId, rga);
			map.put(solicitacaoId, rga);

			Usuario usuario = log.getUsuario();
			Date data = log.getData();
			AcaoProcesso acao = log.getAcao();

			if(AcaoProcesso.SOLICITACAO_CRIACAO.equals(acao)) {
				rga.setDataSolicitacao(data);
				rga.setAnalistaSolicitante(usuario);
				rga.setNumeroRetrabalhos(0);
				rga.setTempoAteFimSolicitacao(BigDecimal.ZERO);
				rga.setTempoComArea(BigDecimal.ZERO);
				rga.setTempoComAnalista(BigDecimal.ZERO);

				ultimoLogCriacaoOuNaoAceite = log;
			}
			else if (AcaoProcesso.SOLICITACAO_NAO_ACEITE_RESPOSTA.equals(acao)) {
				ultimoLogCriacaoOuNaoAceite = log;
			}

			if(usuario != null && usuario.isAreaRole()) {
				rga.setAnalistaArea(usuario);
			}

			if(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA.equals(acao) || AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO.equals(acao)) {
                BigDecimal horas = huc.getHoras(ultimoLogCriacaoOuNaoAceite.getData(), log.getData());
                BigDecimal tempoComArea = rga.getTempoComArea() == null ? BigDecimal.ZERO : rga.getTempoComArea();
                tempoComArea = tempoComArea.add(horas);
                rga.setTempoComArea(tempoComArea);
			}

			if (AcaoProcesso.SOLICITACAO_NAO_ACEITE_RESPOSTA.equals(acao)) {
				Integer numeroRetrabalhos = rga.getNumeroRetrabalhos();
				if (numeroRetrabalhos == null) {
					numeroRetrabalhos = 0;
				}
				numeroRetrabalhos = numeroRetrabalhos + 1;
				rga.setNumeroRetrabalhos(numeroRetrabalhos);
			}

			rga.setRelatorioGeral(rg);
			rga.setProcessoId(processoId);
			rga.setSolicitacaoId(solicitacaoId);

			Subarea subarea = solicitacao.getSubarea();
			Area area = subarea.getArea();
			rga.setArea(area);

			Date prazoLimite = solicitacao.getPrazoLimite();
			rga.setPrazoLimite(prazoLimite);

			StatusSolicitacao status = solicitacao.getStatus();
			rga.setStatus(status);

			Integer horasPrazo = solicitacao.getHorasPrazo();
			rga.setHorasPrazo(horasPrazo);

			Date dataFinalizacao = solicitacao.getDataFinalizacao();
			rga.setDataFinalizacao(dataFinalizacao);

			//rga.setTempoAteFimSolicitacao(tempoAteFimSolicitacao);
			//rga.setTempoComAnalista(tempoComAnalista);
			//rga.setTempoComArea(tempoComArea);
		}

		if (rga != null && ultimoLog != null) {
			BigDecimal horas = huc.getHoras(rga.getDataSolicitacao(), ultimoLog.getData());
			BigDecimal tempoTotal = rga.getTempoAteFimSolicitacao() == null ? BigDecimal.ZERO : rga.getTempoComArea();
			tempoTotal = tempoTotal.add(horas);
			rga.setTempoAteFimSolicitacao(tempoTotal);

			BigDecimal tempoComAnalista = rga.getTempoAteFimSolicitacao().subtract(rga.getTempoComArea());
			rga.setTempoComAnalista(tempoComAnalista);
			rga.setAcao(ultimoLog.getAcao());
		}

		Collection<RelatorioGeralSolicitacao> values = map.values();
		for (RelatorioGeralSolicitacao rga2 : values) {
			relatorioGeralSolicitacaoRepository.saveOrUpdateWithoutFlush(rga2);
		}
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralSolicitacaoRepository.findIdsByFiltro(filtro);
	}

	public List<RelatorioGeralSolicitacao> findByIds(List<Long> ids) {
		return relatorioGeralSolicitacaoRepository.findByIds(ids);
	}

	public List<RelatorioGeralSolicitacao> find(Area area, Date dataInicio, Date dataFim) {
		return relatorioGeralSolicitacaoRepository.find(area,dataInicio, dataFim);
	}

	public List<Processo> findProcessos(Area area, int mes, int ano, Date dataInicio, Date dataFim) {
		return relatorioGeralSolicitacaoRepository.findProcessos(area, mes, ano, dataInicio, dataFim);
	}

}
