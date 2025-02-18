package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.enumeration.DashboardCampos;
import net.wasys.getdoc.domain.repository.DashboardRepository;
import net.wasys.getdoc.domain.vo.DashboardCountVO;
import net.wasys.getdoc.domain.vo.DashboardSituacaoVO;
import net.wasys.getdoc.domain.vo.filtro.DashboardFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class DashboardService {

	@Autowired private DashboardRepository dashboardRepository;

	public void countBySituacaoFormaIngressoCompara(DashboardFiltro filtro, List<DashboardCountVO> result) {

		DashboardSituacaoVO dashboardSituacaoVO;
		dashboardSituacaoVO = dashboardRepository.getDadosDashboard(filtro);

		DashboardCampos.SituacaoEnum situacao = filtro.getSituacao();
		List<Object[]> situacaoObject = dashboardSituacaoVO.getSituacao();
		DashboardCampos.SituacaoEnum situacaoCompara = filtro.getSituacaoCompara();
		List<Object[]> situacaoComparaObject = dashboardSituacaoVO.getSituacaoCompara();

		fillSituacaoByFormaIngresso(result, situacaoObject, situacao);
		fillSituacaoByFormaIngresso(result, situacaoComparaObject, situacaoCompara);

	}

	private void fillSituacaoByFormaIngresso(List<DashboardCountVO> result, List<Object[]> objects, DashboardCampos.SituacaoEnum situacao) {

		String nomeSituacao = situacao.getNome();
		for (Object[] object : objects) {
			DashboardCountVO vo = new DashboardCountVO();
			Date periodo = (Date) object[0];
			vo.setTipo(nomeSituacao);
			vo.setPeriodo(periodo);
			vo.setFormaIngresso((String) object[1]);
			vo.setProcessos((BigInteger) object[2]);

			result.add(vo);
		}
	}
}