package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.EnderecoCep;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class EnderecoCepRepository extends HibernateRepository<EnderecoCep> {

	public EnderecoCepRepository() {
		super(EnderecoCep.class);
	}

	public EnderecoCep getByCep(String cep) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where cep = ? ");
		params.add(cep);

		Query query = createQuery(hql.toString(), params);

		return (EnderecoCep) query.uniqueResult();
	}

	public int count() {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ");
		hql.append(getStartQuery());
		hql.append(" where dataAtualizacao is null ");

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<EnderecoCep> find(int init, int max) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where dataAtualizacao is null ");

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(init);
		query.setMaxResults(max);

		return query.list();
	}
}
