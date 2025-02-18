package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.CalendarioCriterio;
import net.wasys.getdoc.domain.entity.CalendarioCriterioSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class CalendarioCriterioSituacaoRepository extends HibernateRepository<CalendarioCriterioSituacao> {

	public CalendarioCriterioSituacaoRepository() {
		super(CalendarioCriterioSituacao.class);
	}

	public List<CalendarioCriterioSituacao> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by situacao desc ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Situacao> findSituacaoByCalendarioCriterio(CalendarioCriterio calendarioCriterio) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append("select ccs.situacao from ").append(clazz.getName()).append(" ccs ");
		hql.append(" where ccs.calendarioCriterio.id = :calendarioCriterioId");
		hql.append(" order by ccs.situacao.nome desc");

		params.put("calendarioCriterioId", calendarioCriterio.getId());

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public CalendarioCriterioSituacao findBySituacaoAndCalendarioCriterio(Situacao situacao, CalendarioCriterio calendarioCriterio) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" ccs ");
		hql.append(" where ccs.situacao.id = :situacaoId");
		hql.append(" and ccs.calendarioCriterio.id = :calendarioCriterioId");

		params.put("calendarioCriterioId", calendarioCriterio.getId());
		params.put("situacaoId", situacao.getId());

		Query query = createQuery(hql.toString(), params);
		return (CalendarioCriterioSituacao) query.uniqueResult();
	}
}
