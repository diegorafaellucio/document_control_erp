package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.FonteExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class FonteExternaRepository extends HibernateRepository<FonteExterna> {

	public FonteExternaRepository() {
		super(FonteExterna.class);
	}

	public FonteExterna findByNome(TipoConsultaExterna tipoConsultaExterna) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery());
		hql.append(" where nome = :nome ");
		params.put("nome", tipoConsultaExterna);

		Query query = createQuery(hql.toString(), params);

		return (FonteExterna) query.uniqueResult();
	}

	public List<FonteExterna> findAll() {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery());

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
