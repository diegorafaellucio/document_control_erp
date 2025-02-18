package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.entity.ProcessoRegraLog;
import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProcessoRegraLogRepository extends HibernateRepository<ProcessoRegraLog> {

	public ProcessoRegraLogRepository() {
		super(ProcessoRegraLog.class);
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoRegraLog> findByProcessoRegra(Long processoRegraId) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(getStartQuery());
		hql.append(" pr ");
		hql.append("where 1=1");

		if(processoRegraId != null) {
			hql.append("  and pr.processoRegra.id = :processoRegraId ");
			params.put("processoRegraId", processoRegraId);
		}
		hql.append(" order by pr.id ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoRegraLog> findByFiltro(ProcessoRegraFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql	.append("select distinct prl ");
		hql.append(" from ").append(ProcessoRegraLog.class.getName()).append(" prl ");
		hql.append(", ").append(ProcessoRegra.class.getName()).append(" pr ");
		hql.append(" where prl.processoRegra.id = pr.id ");

		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by prl.id ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public int count(ProcessoRegraFiltro filtro) {
		StringBuilder hql = new StringBuilder();
		hql.append(" select count(*) from ").append(clazz.getName()).append(" r ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String, Object> makeQuery(ProcessoRegraFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<String, Object>();
		Long processoId = filtro.getProcessoId();
		Long regraId = filtro.getRegraId();
		String regraNome = filtro.getRegraNome();
		Long processoIgnorarId = filtro.getProcessoIgnorarId();
		List<StatusProcessoRegra> statusList = filtro.getStatusList();
		Boolean possuiConsultaExterna = filtro.getPossuiConsultaExterna();

		if(processoId != null) {
			hql.append("  and pr.processo.id = :processoId ");
			params.put("processoId", processoId);
		}

		if(processoIgnorarId != null) {
			hql.append("  and pr.processo.id <> :processoIgnorarId ");
			params.put("processoIgnorarId", processoIgnorarId);
		}

		if(regraId != null) {
			hql.append("  and (pr.regra.id = :regraId or prl.processoRegra.id = :processoRegraId) ");
			params.put("regraId", regraId);
			params.put("processoRegraId", regraId);
		}

		if(regraNome != null && !regraNome.equals("")) {
			String lowerRegraNome = regraNome.toLowerCase();
			hql.append(" and lower(pr.regra.nome) = :lowerRegraNome ");
			params.put("lowerRegraNome", lowerRegraNome);
		}

		if(statusList != null) {
			hql.append(" and pr.status in ( -1 ");
			for (StatusProcessoRegra status1 : statusList) {
				hql.append(", '").append(status1).append("'");
			}
			hql.append(" )");
		}

		if(possuiConsultaExterna != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from ").append(SubRegra.class.getName()).append(" sr where sr.regra.id = pr.regra.id and sr.consultaExterna is not null ");
			hql.append(" ) ").append(possuiConsultaExterna ? " > 0 " : " = 0 ");
		}

		return params;
	}

	public List<Long> findToExpurgo(Date dataCorte, int max) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select prl.id ");
		hql.append(" from ").append(clazz.getName()).append(" prl ");
		hql.append(" where prl.data < :dataCorte ");
		hql.append(" order by id ");

		params.put("dataCorte", dataCorte);

		Query query = createQuery(hql, params);

		query.setMaxResults(max);

		List<Long> list = query.list();
		return list;
	}

	public void expurgar(List<Long> list) {

		StringBuilder hql = new StringBuilder();

		hql.append(" delete from ").append(clazz.getName());
		hql.append(" where id in ( -1 ");
		for (Long prlId : list) {
			hql.append(",").append(prlId).append(" ");
		}
		hql.append(" ) ");

		Query query = createQuery(hql);
		query.executeUpdate();
	}
}