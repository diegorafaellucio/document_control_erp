package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.SubRegraAcao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SubRegraAcaoRepository extends HibernateRepository<SubRegraAcao> {

	public SubRegraAcaoRepository() {
		super(SubRegraAcao.class);
	}

	public List<SubRegraAcao> findBySubRegra(Long subRegraId) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" sra ");
		hql.append(" where sra.subRegra.id = :subRegraId");
		params.put("subRegraId", subRegraId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
