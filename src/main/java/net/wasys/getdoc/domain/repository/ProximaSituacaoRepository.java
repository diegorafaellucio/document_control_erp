package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class ProximaSituacaoRepository extends HibernateRepository<ProximaSituacao> {

	public ProximaSituacaoRepository() {
		super(ProximaSituacao.class);
	}

	public List<ProximaSituacao> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by nome ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<ProximaSituacao> findAtivas(StatusProcesso status) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		if(status != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from s.statusProcessos sp where sp.status = ? ");
			params.add(status);
			hql.append(" ) > 0 ");
		}
		hql.append(" order by s.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<ProximaSituacao> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" s ");

		hql.append(" where s.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by s.id ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}
}
