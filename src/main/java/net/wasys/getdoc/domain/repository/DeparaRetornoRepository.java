package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.DeparaParam;
import net.wasys.getdoc.domain.entity.DeparaRetorno;
import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class DeparaRetornoRepository extends HibernateRepository<DeparaRetorno> {

	public DeparaRetornoRepository() {
		super(DeparaRetorno.class);
	}

	public List<DeparaRetorno> findBySubRegraId(Long subRegraId) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select dp ");
		hql.append(getStartQuery()).append(" dp ");
		hql.append(" join ").append(SubRegra.class.getName()).append(" sr ");
		hql.append(" on dp.subRegra.id = sr.id");
		hql.append(" where dp.subRegra.id = :subRegraId");

		Query query = createQuery(hql.toString());
		query.setParameter("subRegraId", subRegraId);

		return query.list();
	}
}
