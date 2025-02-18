package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.RegraSubperfil;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RegraSubperfilRepository extends HibernateRepository<RegraSubperfil> {

	public RegraSubperfilRepository() {
		super(RegraSubperfil.class);
	}

	@Transactional(rollbackFor=Exception.class)
	public void deleteByRegra(Long regraId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" delete from ").append(clazz.getName());
		hql.append(" where regra.id = :regraId");
		params.put("regraId", regraId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}

	public List<RegraSubperfil> findByRegra(Long regraId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql	.append(" from ").append(clazz.getName()).append(" ");
		hql	.append(" where regra.id = :regraId ");
		params.put("regraId", regraId);

		Query query = createQuery(hql, params);
		return query.list();
	}
}