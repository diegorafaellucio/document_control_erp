package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BacalhauEmail;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class BacalhauEmailRepository extends HibernateRepository<BacalhauEmail> {

	public BacalhauEmailRepository() {
		super(BacalhauEmail.class);
	}

	public int count() {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(clazz.getName()).append(" la where 1=1 ");


		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<BacalhauEmail> list(int first, int pageSize) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(clazz.getName()).append(" la where 1=1 ");

		hql.append(" order by nome asc ");

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(first);
		query.setMaxResults(pageSize);

		return query.list();
	}

}
