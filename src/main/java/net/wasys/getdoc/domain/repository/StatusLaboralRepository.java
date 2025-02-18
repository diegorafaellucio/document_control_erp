package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.vo.filtro.StatusLaboralFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class StatusLaboralRepository extends HibernateRepository<StatusLaboral> {

	public StatusLaboralRepository() {
		super(StatusLaboral.class);
	}

	private Map<String, Object> makeQuery(StatusLaboralFiltro filtro, StringBuilder hql) {

		Boolean ativa = filtro.getAtiva();
		List<StatusAtendimento> statusAtendimentoList = filtro.getStatusAtendimentoList();
		Boolean fixo = filtro.getFixo();

		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" where 1=1 ");

		if (ativa != null) {
			hql.append(" and s.ativa = :ativa ");
			params.put("ativa", ativa);
		}

		if (fixo != null) {
			hql.append(" and s.fixo = :fixo ");
			params.put("fixo", fixo);
		}

		if (CollectionUtils.isNotEmpty(statusAtendimentoList)) {
			hql.append(" and s.statusAtendimento in (:statusAtendimentoList) ");
			params.put("statusAtendimentoList", statusAtendimentoList);
		}

		return params;
	}

	public List<StatusLaboral> findByFiltro(StatusLaboralFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" s ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by fixo, s.nome ");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int countByFiltro(StatusLaboralFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" s ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<StatusLaboral> findAtivas() {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" order by s.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<StatusLaboral> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" s ");

		hql.append(" where s.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by s.id ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<StatusLaboral> findAll() {

		Query query = createQuery(" from " + clazz.getName() + " order by nome desc ");

		return query.list();
	}

	public StatusLaboral getFixo(StatusAtendimento statusAtendimento) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" and s.statusAtendimento = :statusAtendimento ");
		hql.append(" and s.fixo is true ");
		hql.append(" order by s.nome ");
		params.put("statusAtendimento", statusAtendimento);

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return (StatusLaboral) query.uniqueResult();
	}
}
