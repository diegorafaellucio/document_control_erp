package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.RelatorioAtividadesSinteticoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioAtividadeFiltro;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatorioAtividadesSinteticoService {

	@Autowired private ProcessoLogService processoLogService;

	public RelatorioAtividadesSinteticoVO getRelatorioSintetico(RelatorioAtividadeFiltro filtro) {

		RelatorioAtividadesSinteticoVO vo = new RelatorioAtividadesSinteticoVO();
		BigDecimal horaMin = new BigDecimal(25);
		BigDecimal horaMax = new BigDecimal(0);
		RelatorioAtividadesSinteticoVO.AtividadeVO ativVO = null;
		List<Object[]> logs = processoLogService.findAtividadesByFiltro(filtro);
		Map<String, Date> lastDataAvancouProximaMap = new HashMap<>();

		for (Object[] log : logs) {

			Long processoId = (Long) log[0];
			String processoNumero = (String) log[1];
			String analista = StringUtils.trim((String) log[2]);
			Date data = (Date) log[3];
			AcaoProcesso acao = (AcaoProcesso) log[4];
			String alteracaoSituacaoObs= (String) log[5];
			String tipoevidenciaObs = (String) log[6];
			String novaSolicitacaoObs = log[7] + " / " + log[8];
			String key = analista + " - " + DummyUtils.format(data, "dd/MM");

			String lastAnalista = ativVO != null ? ativVO.getAnalista() : null;
			Long lastProcessoId = ativVO != null ? ativVO.getProcessoId() : null;
			Date lastData = ativVO != null ? ativVO.getInicio() : new Date(0);

			if(ativVO == null || !analista.equals(lastAnalista) || !processoId.equals(lastProcessoId) || !DateUtils.isSameDay(data, lastData)) {
				ativVO = vo.new AtividadeVO();
				ativVO.setAnalista(analista);
				ativVO.setProcessoId(processoId);
				ativVO.setProcessoNumero(processoNumero);
				vo.addAtividadeVO(key, ativVO);

				Date lastDataAvancouProxima = lastDataAvancouProximaMap.get(key);
				if(lastDataAvancouProxima != null) {
					data = lastDataAvancouProxima;
					lastDataAvancouProximaMap.put(key, null);
				}
			}

			String observacao = "";
			if(AcaoProcesso.REGISTRO_EVIDENCIA.equals(acao)){
				observacao = tipoevidenciaObs;
			} else if(AcaoProcesso.ALTERACAO_SITUACAO.equals(acao)){
				observacao = alteracaoSituacaoObs;
			} else if(AcaoProcesso.SOLICITACAO_CRIACAO.equals(acao)){
				observacao = novaSolicitacaoObs;
			}

			ativVO.addLog(data, acao, observacao);

			if(AcaoProcesso.AVANCOU_PROCESSO.equals(acao)) {
				lastDataAvancouProximaMap.put(key, data);
			}

			BigDecimal hora = DummyUtils.getHoras(DummyUtils.format(data, "HH:mm"));
			horaMin = horaMin.min(hora);
			horaMax = horaMax.max(hora);
		}

		List<String> horas = new ArrayList<>();
		DateTime dateTimeIni = new DateTime().dayOfMonth().roundFloorCopy();
		DateTime dateTime = dateTimeIni;

		while (dateTime.dayOfMonth().equals(dateTimeIni.dayOfMonth())) {

			String horaStr = dateTime.toString("HH:mm");
			BigDecimal hora = DummyUtils.getHoras(horaStr);
			DateTime dateTimePlus = dateTime.plusMinutes(20);
			String horaPlusStr = dateTimePlus.toString("HH:mm");
			BigDecimal horaPlus = DummyUtils.getHoras(horaPlusStr);

			if(horaPlus.compareTo(horaMin) > 0 && hora.compareTo(horaMax) < 0) {
				horas.add(horaStr);
			}

			dateTime = dateTimePlus;
		}
		vo.setHoras(horas);

		return vo;
	}
}
