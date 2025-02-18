package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class ModeloDocumentoRepository extends HibernateRepository<ModeloDocumento> {

	public ModeloDocumentoRepository() {
		super(ModeloDocumento.class);
	}

	public List<ModeloDocumento> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<ModeloDocumento> findAtivos() {

		Query query = createQuery(getStartQuery() + " where ativo is true order by descricao ");
		return query.list();
	}

	public List<ModeloDocumento> findByLabelDarknet(String labelDarknet) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(startQuery()).append(" md ");
		hql.append(" where md.labelDarknet = :labelDarknet ");
		params.put("labelDarknet", labelDarknet);

		Query query = createQuery(hql, params);
		return query.list();
	}
}
