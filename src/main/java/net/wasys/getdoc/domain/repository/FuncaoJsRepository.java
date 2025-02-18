package net.wasys.getdoc.domain.repository;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.FuncaoJs;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class FuncaoJsRepository extends HibernateRepository<FuncaoJs> {

	public FuncaoJsRepository() {
		super(FuncaoJs.class);
	}

	public List<FuncaoJs> findAll() {

		Query query = createQuery(" from " + clazz.getName() + " order by id ");

		return query.list();
	}
}
