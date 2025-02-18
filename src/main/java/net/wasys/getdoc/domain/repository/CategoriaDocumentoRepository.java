package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.CategoriaDocumento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class CategoriaDocumentoRepository extends HibernateRepository<CategoriaDocumento> {

	public CategoriaDocumentoRepository() {
		super(CategoriaDocumento.class);
	}

	public List<CategoriaDocumento> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {
		Query query = createQuery(" select count(*) from " + clazz.getName());
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<CategoriaDocumento> findAtivos() {
		Query query = createQuery(getStartQuery() + " where ativo is true order by descricao ");
		return query.list();
	}
}
