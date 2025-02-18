package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class AreaRepository extends HibernateRepository<Area> {

	public AreaRepository() {
		super(Area.class);
	}

	public List<Area> findAtivas() {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where ativo is true ");
		hql.append(" order by descricao ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Area> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public Date getUltimaDataAtualizacao() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select max(a.dataAtualizacao) from ").append(clazz.getName()).append(" a ");

		Query query = createQuery(hql.toString());

		return (Date) query.uniqueResult();
	}

	public Area getByGeralId(Long geralId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where geralId = ? ");
		params.add(geralId);

		Query query = createQuery(hql.toString(), params);

		return (Area) query.uniqueResult();
	}
}
