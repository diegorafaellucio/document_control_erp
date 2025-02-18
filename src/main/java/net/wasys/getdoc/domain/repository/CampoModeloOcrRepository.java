package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CampoModeloOcrRepository extends HibernateRepository<CampoModeloOcr> {

	public CampoModeloOcrRepository() {
		super(CampoModeloOcr.class);
	}

	public int countByModeloOcr(Long modeloOcrId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" cmo where cmo.modeloOcr.id = ? ");
		params.add(modeloOcrId);

		Query query = createQuery(hql.toString(), params);
		return ((Number)query.uniqueResult()).intValue();
	}

	public List<CampoModeloOcr> findByModeloOcr(Long modeloOcrId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" cmo ");
		hql.append(" where cmo.modeloOcr.id = ? ");
		hql.append(" order by cmo.descricao ");

		params.add(modeloOcrId);

		Query query = createQuery(hql.toString(), params);
		List list = query.list();
		return list;
	}

}
