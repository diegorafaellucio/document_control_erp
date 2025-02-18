package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.RelatorioGeralSituacao;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO.TipoProcessoPorDia;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DashboardIntradayRepository extends HibernateRepository<RelatorioGeralSituacao> {

	public DashboardIntradayRepository() {
		super(RelatorioGeralSituacao.class);
	}

	public TipoProcessoPorDia getTipoProcessoPorDia (DashboardDiarioFiltro filtro, int diaInicio, int diaFim) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();

		String colunaData = "p.dataEnvioAnalise";

		hql.append(" select ");
		hql.append("(p.tipoProcesso.nome || ',' || count(case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndReal, ");

		//no Dia
		for (int i = diaInicio; i <= diaFim; i++) {
			hql.append("(p.tipoProcesso.nome || ',' || count(case when DAY( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndCount, ");
		}

		hql.append(" 'fim' ");

		hql.append(" from " + Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" group by p.tipoProcesso.nome");


		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);

		Query query = createQuery(hql, params);
		List<Object[]> result = (List<Object[]>) query.list();

		return getTipoProcessoPorDia(result);
	}

	private TipoProcessoPorDia getTipoProcessoPorDia(List<Object[]> result) {

		long totalPeriodo = 0L;
		HashMap<String, List<Long>> map = new HashMap<>();

		for (Object[] objects : result) {
			List<Long> dias = new ArrayList<>();
			for (int i = 0; i < objects.length; i++) {
				String string = (String) objects[i];
				if (i == 0) {
					String[] split = string.split(",");
					String tipoProcessoNome = split[0];
					Long total = new Long(split[1]);
					totalPeriodo += total;
					dias.add(total);
					map.put(tipoProcessoNome, dias);
				}
				else if (!string.contains("fim")) {
					String[] split = string.split(",");
					String tipoProcessoNome = split[0];
					dias.add(new Long(split[1]));
					map.put(tipoProcessoNome, dias);
				}
			}
		}

		TipoProcessoPorDia tipoProcessoPorDia = new TipoProcessoPorDia();
		tipoProcessoPorDia.setQtdPorDia(map);
		tipoProcessoPorDia.setTotalPeriodo(totalPeriodo);
		return tipoProcessoPorDia;
	}

	public List<Double> findTempoMedioBySituacao (DashboardDiarioFiltro filtro, StatusProcesso statusProcesso){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		Date dataInicioMes = filtro.getDataInicioMes();
		Date dataFimMes = filtro.getDataFimMes();

		hql.append(" select ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 7 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 8 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 9 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 10 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 11 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 12 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 13 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 14 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 15 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 16 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 17 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 18 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" ( ");
		hql.append("	select avg(rgs1.tempo) ");
		hql.append("	from " + RelatorioGeralSituacao.class.getName()).append(" rgs1, ");
		hql.append(		Processo.class.getName()).append(" p ");
		hql.append("	where rgs1.processoId = p.id ");
		hql.append("	and HOUR(rgs1.dataFim) >= 7 ");
		hql.append("	and HOUR(rgs1.dataFim) < 18 ");
		hql.append("	and rgs1.dataFim >= :dataInicioDia ");
		hql.append("	and rgs1.dataFim <= :dataFimDia ");
		hql.append("	and rgs1.situacao.status = :statusProcesso ");
		makeWhere(filtro, hql, params);
		hql.append(" ) as dia, ");
		hql.append(" ( ");
		hql.append("	select avg(rgs2.tempo) ");
		hql.append("	from " + RelatorioGeralSituacao.class.getName()).append(" rgs2, ");
		hql.append(		Processo.class.getName()).append(" p ");
		hql.append("	where rgs2.processoId = p.id ");
		hql.append("	and HOUR(rgs2.dataFim) >= 7 ");
		hql.append("	and HOUR(rgs2.dataFim) < 18 ");
		hql.append("	and rgs2.dataFim >= :dataInicioMes ");
		hql.append("	and rgs2.dataFim <= :dataFimMes ");
		hql.append("	and rgs2.situacao.status = :statusProcesso ");
		makeWhere(filtro, hql, params);
		hql.append(" ) as mes ");
		hql.append(" from " + RelatorioGeralSituacao.class.getName()).append(" rgs, ");
		hql.append(	 Processo.class.getName()).append(" p ");
		hql.append(" where rgs.processoId = p.id ");
		hql.append(" and rgs.situacao.status = :statusProcesso ");
		hql.append(" and rgs.tempo is not null ");
		makeWhere(filtro, hql, params);

		params.put("statusProcesso", statusProcesso);
		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("dataInicioMes", dataInicioMes);
		params.put("dataFimMes", dataFimMes);

		Query query = createQuery(hql, params);
		Object[] objs = (Object[]) query.uniqueResult();
		List<Double> result = new ArrayList<>();

		for(Object obj : objs){
			if(obj != null){
				result.add((Double) obj);
			} else{
				result.add(0.0);
			}
		}
		return result;
	}

	private Map<String, Object> makeWhere(DashboardDiarioFiltro filtro, StringBuilder hql, Map<String, Object> params) {

		List<Long> tiposProcessoId = filtro.getTiposProcessoId();
		Origem origemProcesso = filtro.getOrigemProcesso();
		String regional = filtro.getRegional();
		String campus = filtro.getCampus();
		String curso = filtro.getCurso();
		boolean tratados = filtro.isTratados();

		if(tratados){
			hql.append(" and p.dataFinalizacaoAnalise > p.dataEnvioAnalise");
		}
		if(tiposProcessoId != null && !tiposProcessoId.isEmpty()){
			hql.append(" and p.tipoProcesso.id in (:tiposProcessoId) ");
			params.put("tiposProcessoId", tiposProcessoId);
		}

		if(origemProcesso != null){
			hql.append(" and p.origem = :origemProcesso ");
			params.put("origemProcesso", origemProcesso);
		}

		if(StringUtils.isNotBlank(curso) || StringUtils.isNotBlank(campus) || StringUtils.isNotBlank(regional)) {
			hql.append(" and p.id in ");
			hql.append(" ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append("	where 1=1 ");

			if (StringUtils.isNotBlank(curso)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CURSO.getNome());
				params.put("valor", curso);
			} else if (StringUtils.isNotBlank(campus)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CAMPUS.getNome());
				params.put("valor", campus);
			} else if (StringUtils.isNotBlank(regional)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.REGIONAL.getNome());
				params.put("valor", regional);
			}
			hql.append(" ) ");
		}

		return params;
	}

	public List<Long> getProdutividadePorDia(DashboardDiarioFiltro filtro) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		int diaInicio = 1;
		int diaFim = 31;
		String colunaData = "ml.data";

		hql.append(" select ");
		hql.append("count(distinct case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		for (int i = diaInicio; i <= diaFim; i++) {
			hql.append("count(distinct case when DAY( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)");
			if(i < diaFim){
				hql.append(", ");
			}
		}
		hql.append(" from ");
		hql.append(ProcessoLog.class.getName()).append(" ml, ");
		hql.append(Processo.class.getName()).append(" p ");
		hql.append(" where 	1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" 	and p.id = ml.processo.id ");
		hql.append(" 	and ml.data > :dataInicioDia and ml.data < :dataFimDia ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());

		Query query = createQuery(hql, params);

		Object[] objects = (Object[]) query.uniqueResult();
		List<Long> list = new ArrayList<>();
		for (Object object : objects) {
			list.add(new Long(object.toString()));
		}

		return list;
	}
}
