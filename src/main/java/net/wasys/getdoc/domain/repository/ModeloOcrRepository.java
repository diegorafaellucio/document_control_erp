package net.wasys.getdoc.domain.repository;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class ModeloOcrRepository extends HibernateRepository<ModeloOcr> {

	public ModeloOcrRepository() {
		super(ModeloOcr.class);
	}

	public List<ModeloOcr> findAtivos() {

		Query query = createQuery(getStartQuery() + " where ativo is true order by descricao ");
		return query.list();
	}

	public List<ModeloOcr> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}
}