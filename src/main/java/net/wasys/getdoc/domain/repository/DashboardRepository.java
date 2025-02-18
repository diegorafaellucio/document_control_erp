package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.RelatorioGeralSituacao;
import net.wasys.getdoc.domain.enumeration.DashboardCampos;
import net.wasys.getdoc.domain.vo.DashboardSituacaoVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.DashboardFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class DashboardRepository extends HibernateRepository<RelatorioGeralSituacao> {

	public DashboardRepository() {
		super(RelatorioGeralSituacao.class);
	}

	public DashboardSituacaoVO getDadosDashboard(DashboardFiltro filtro) {

		DashboardSituacaoVO dashboardSituacaoVO = new DashboardSituacaoVO();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		DashboardCampos.IntervaloEnum interval = filtro.getInterval();
		DashboardCampos.TipoAgrupamentoEnum agrupamento = filtro.getTipoAgrupamento();
		DashboardCampos.SituacaoEnum situacao = filtro.getSituacao();
		DashboardCampos.SituacaoEnum situacaoCompara = filtro.getSituacaoCompara();
		RegistroValorVO regional = filtro.getRegional();
		String campus = filtro.getCampus();

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioTruncate = DummyUtils.truncateFinalDia(dataInicio);
		Date dataInicioPeriodo = dataInicioTruncate;
		if (DashboardCampos.IntervaloEnum.SEMANA.equals(interval)) {
			dataInicioPeriodo = DateUtils.addDays(dataInicioPeriodo, 6);
		}
		else if (DashboardCampos.IntervaloEnum.MES.equals(interval)) {
			dataInicioPeriodo = DateUtils.addMonths(dataInicioPeriodo, 1);
		}
		else if (DashboardCampos.IntervaloEnum.ANO.equals(interval)) {
			dataInicioPeriodo = DateUtils.addYears(dataInicioPeriodo, 1);
		}

		String dataInicioString = DummyUtils.formatDateTime5(dataInicio);
		String dataFimString = DummyUtils.formatDateTime5(dataFim);
		String dataInicioTruncateString = DummyUtils.formatDateTime5(dataInicioTruncate);
		String dataInicioPeriodoString = DummyUtils.formatDateTime5(dataInicioPeriodo);

		hql.append(" select ");
		hql.append(" to_date(to_char(periodo.inicio, 'YYYY-MM-DD HH:MM:SS'), 'YYYY-MM-DD HH:MM:SS') AS periodo, ");
		hql.append(" periodo.tipoProcessoNome, ");
		hql.append(" ( ");
		hql.append("  select count(rgs.processo_id) ");
		hql.append("  from relatorio_geral_situacao rgs, relatorio_geral rg, tipo_processo tp ");
		hql.append("  where rg.id = rgs.relatorio_geral_id ");
		hql.append("  and tp.id = rg.tipo_processo_id ");
		hql.append("  and tp.nome = periodo.tipoProcessoNome ");
		hql.append("  and rgs.situacao_id IN (:situacoes) ");

		String filtroRegionalCampus = "";
		if(StringUtils.isNotBlank(campus)){
			filtroRegionalCampus += " AND ((rg.campos_dinamicos->'Dados do Inscrito'->>'CAMPUS Desc.') LIKE '%" + campus + "%' " +
					" or (rg.campos_dinamicos->'Dados do Candidato (Curso/IES)'->>'CAMPUS Desc.') LIKE '%" + campus + "%') ";
		}
		else if(regional != null){
			String regionalNome = regional.getLabel();
			filtroRegionalCampus += " AND ((rg.campos_dinamicos->'Dados do Inscrito'->>'REGIONAL Desc.') LIKE '%" + regionalNome + "%' " +
					" or (rg.campos_dinamicos->'Dados do Candidato (Curso/IES)'->>'REGIONAL Desc.') LIKE '%" + regionalNome + "%') ";
		}
		hql.append(filtroRegionalCampus);

		if(DashboardCampos.TipoAgrupamentoEnum.PERIODO.equals(agrupamento)) {
			hql.append(" and rgs.data BETWEEN periodo.inicio AND periodo.fim ");
		}
		else if(DashboardCampos.TipoAgrupamentoEnum.ACUMULADO.equals(agrupamento)) {
			hql.append(" and rgs.data < periodo.inicio ");
		}

		hql.append(" ) ");
		hql.append(" from ");

		if(DashboardCampos.TipoAgrupamentoEnum.ACUMULADO.equals(agrupamento)) {
			hql.append(" (SELECT GENERATE_SERIES( TIMESTAMP '" + dataInicioTruncateString + "', TIMESTAMP '" + dataFimString + "', '" + interval.getValue() + "' ) inicio, ");
		}
		else if(DashboardCampos.TipoAgrupamentoEnum.PERIODO.equals(agrupamento)) {
			hql.append(" (SELECT GENERATE_SERIES( TIMESTAMP '" + dataInicioString + "', TIMESTAMP '" + dataFimString + "', '" + interval.getValue() + "' ) inicio, ");
			hql.append("  GENERATE_SERIES( TIMESTAMP '" + dataInicioPeriodoString + "', TIMESTAMP '" + dataFimString + "', '" + interval.getValue() + "' ) fim, ");
		}

		hql.append(" tp.nome AS tipoProcessoNome ");
		hql.append(" from tipo_processo tp ");
		hql.append(" ) as periodo ");
		hql.append(" order by periodo.inicio, periodo.tipoProcessoNome ");

		List<Long> situacaoIds = situacao.getIds();
		params.put("situacoes", situacaoIds);
		Query query = createSQLQuery(hql, params);
		List<Object[]> situacaoObject = query.list();
		dashboardSituacaoVO.setSituacao(situacaoObject);

		List<Long> situacaoComparaIds = situacaoCompara.getIds();
		params.replace("situacoes", situacaoIds, situacaoComparaIds);
		Query queryCompara = createSQLQuery(hql, params);
		List<Object[]> situacaoComparaObject = queryCompara.list();
		dashboardSituacaoVO.setSituacaoCompara(situacaoComparaObject);

		return dashboardSituacaoVO;
	}
}