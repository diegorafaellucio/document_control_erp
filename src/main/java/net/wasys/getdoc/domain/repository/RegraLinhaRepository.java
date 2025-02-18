package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.RegraLinha;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class RegraLinhaRepository extends HibernateRepository<RegraLinha> {

	public RegraLinhaRepository() {
		super(RegraLinha.class);
	}

	public RegraLinha getRaiz(Long regraId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql	.append(" from ").append(clazz.getName()).append(" sr ");
		hql	.append(" where sr.regra.id = :regraId ");
		params.put("regraId", regraId);
		hql	.append(" and linhaPai is null ");
		params.put("regraId", regraId);

		Query query = createQuery(hql, params);
		return (RegraLinha) query.uniqueResult();
	}
}
