package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.RelatorioGeralSolicitacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.vo.RelatorioEficienciaAreasVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.HorasUteisCalculator;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatorioEficienciaAreasService {

	@Autowired private RelatorioGeralSolicitacaoService relatorioGeralSolicitacaoService;
	@Autowired private ProcessoService processoService;

	public Map<Date, List<RelatorioEficienciaAreasVO>> getRelatorioEficienciaAreas(Area areaFiltro, Date dataInicio, Date dataFim) {

		dataInicio = org.apache.commons.lang.time.DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);

		dataFim = org.apache.commons.lang.time.DateUtils.truncate(dataFim, Calendar.DAY_OF_MONTH);
		dataFim = org.apache.commons.lang.time.DateUtils.addDays(dataFim, 1);
		dataFim = org.apache.commons.lang.time.DateUtils.addSeconds(dataFim, -1);

		List<RelatorioGeralSolicitacao> result = relatorioGeralSolicitacaoService.find(areaFiltro, dataInicio, dataFim);
		Map<Date, Map<Area, RelatorioEficienciaAreasVO>> relatorio0 = new HashMap<>();

		for (RelatorioGeralSolicitacao rga : result) {

			Long processoId = rga.getProcessoId();
			Processo processo = processoService.get(processoId);
			Situacao situacao = processo.getSituacao();
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = processoService.buildHUC(situacaoId);
			Area area = rga.getArea();
			Date data = rga.getDataSolicitacao();

			if(areaFiltro != null && !area.equals(areaFiltro)) {
				continue;
			}
			else if( data.after(dataFim) || data.before(dataInicio)) {
				continue;
			}

			Date mes = DateUtils.truncate(data, Calendar.MONTH);

			Map<Area, RelatorioEficienciaAreasVO> map = relatorio0.get(mes);
			if(map == null) {
				map = new HashMap<Area, RelatorioEficienciaAreasVO>();
				relatorio0.put(mes, map);
			}

			RelatorioEficienciaAreasVO vo = map.get(area);
			if(vo == null) {
				vo = new RelatorioEficienciaAreasVO();
				vo.setArea(area);
				vo.setCalculator(huc);
				map.put(area, vo);
			}

			vo.add(rga);
		}

		Map<Date, List<RelatorioEficienciaAreasVO>> relatorio = new TreeMap<Date, List<RelatorioEficienciaAreasVO>>();

		HorasUteisCalculator huc = processoService.buildHUC();

		for (Date mes : relatorio0.keySet()) {

			Map<Area, RelatorioEficienciaAreasVO> map = relatorio0.get(mes);
			Collection<RelatorioEficienciaAreasVO> values = map.values();
			List<RelatorioEficienciaAreasVO> values2 = new ArrayList<RelatorioEficienciaAreasVO>(values);

			Integer totalTotal = 0;
			Integer totalQtdeRetrabalho = 0;
			Integer totalQtdeAtraso = 0;
			Set<BigDecimal> tempos = new HashSet<BigDecimal>();

			for (RelatorioEficienciaAreasVO vo : values2) {

				int total = vo.getTotal();
				totalTotal += total;

				int qtdeRetrabalho = vo.getQtdeRetrabalho();
				totalQtdeRetrabalho += qtdeRetrabalho;

				int qtdeAtraso = vo.getQtdeAtraso();
				totalQtdeAtraso += qtdeAtraso;

				Set<BigDecimal> tempos2 = vo.getTempos();
				tempos.addAll(tempos2);
			}

			BigDecimal media = DummyUtils.getMedia1(totalTotal, tempos);

			RelatorioEficienciaAreasVO totalVO = new RelatorioEficienciaAreasVO();
			totalVO.setTotal(totalTotal);
			totalVO.setQtdeRetrabalho(totalQtdeRetrabalho);
			totalVO.setQtdeAtraso(totalQtdeAtraso);
			totalVO.setMediaTempoArea(media);
			totalVO.setCalculator(huc);
			values2.add(totalVO);

			relatorio.put(mes, values2);
		}

		return relatorio;
	}
}
