package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import net.wasys.getdoc.domain.service.ParametroService;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Parametro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class ParametroRepository extends HibernateRepository<Parametro> {

	public ParametroRepository() {
		super(Parametro.class);
	}

	public Parametro getByChave(String chave) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		hql.append(" where chave = ? ");
		params.add(chave);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (Parametro) query.uniqueResult();
	}

	public List<Parametro> findLikeChave(ParametroService.P p) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		hql.append(" where chave like ? ");
		params.add(p.name() + "%");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
