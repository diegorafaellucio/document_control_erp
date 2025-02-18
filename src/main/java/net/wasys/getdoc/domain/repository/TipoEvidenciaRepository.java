package net.wasys.getdoc.domain.repository;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class TipoEvidenciaRepository extends HibernateRepository<TipoEvidencia> {

	public TipoEvidenciaRepository() {
		super(TipoEvidencia.class);
	}

	public List<TipoEvidencia> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<TipoEvidencia> findAtivas() {

		Query query = createQuery(" from " + clazz.getName() + " where ativo is true order by descricao ");

		return query.list();
	}
}
