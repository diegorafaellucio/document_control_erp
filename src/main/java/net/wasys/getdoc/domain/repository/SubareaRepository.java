package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class SubareaRepository extends HibernateRepository<Subarea> {

	public SubareaRepository() {
		super(Subarea.class);
	}

	public Date getUltimaDataAtualizacao() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select max(s.dataAtualizacao) from ").append(clazz.getName()).append(" s ");

		Query query = createQuery(hql.toString());

		return (Date) query.uniqueResult();
	}

	public Subarea getByGeralId(Long geralId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where geralId = ? ");
		params.add(geralId);

		Query query = createQuery(hql.toString(), params);

		return (Subarea) query.uniqueResult();
	}

	public List<Subarea> findByArea(Long areaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName()).append(" s ");
		hql.append(" where s.area.id = ? ");
		params.add(areaId);

		hql.append(" order by s.descricao ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Subarea> findAtivasByArea(Long areaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName()).append(" s ");
		hql.append(" left outer join fetch s.area a ");
		hql.append(" where s.ativo is true ");
		hql.append(" and s.area.id = ? ");
		params.add(areaId);

		hql.append(" order by s.descricao ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
