package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class EtapaRepository extends HibernateRepository<Etapa> {

	public EtapaRepository() {
		super(Etapa.class);
	}

	public int countByTipoProcesso(Long tipoProcessoId) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select count(*) from " + clazz.getName() + " e ");
		hql.append(" where e.tipoProcesso.id = ").append(tipoProcessoId);

		Query query = createQuery(hql.toString());
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Etapa> findByTipoProcesso(Long tipoProcessoId) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" e ");
		hql.append(" where e.tipoProcesso.id = ").append(tipoProcessoId);
		hql.append(" order by e.nome asc ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<String> findNomesAtivas() {

		StringBuilder hql = new StringBuilder();
		hql.append(" select distinct e.nome from ").append(Etapa.class.getName()).append(" e ");
		hql.append(" order by e.nome ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}
}
