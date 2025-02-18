package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class BaseInternaRepository extends HibernateRepository<BaseInterna> {

	public BaseInternaRepository() {
		super(BaseInterna.class);
	}

	public List<BaseInterna> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by nome ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int countByFiltro(BaseInternaFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" b ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<BaseInterna> findByFiltro(BaseInternaFiltro filtro, Integer first, Integer pageSize) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" b ");
		hql.append(" left outer join fetch b.relacionamentos ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append("order by b.nome");

		Query query = createQuery(hql.toString(), params);

		if(first != null) {
			query.setFirstResult(first);
		}

		if(pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	private List<Object> makeQuery(BaseInternaFiltro filtro, StringBuilder hql) {

		String nome = filtro.getNome();
		List<Object> params = new ArrayList<Object>();

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and lower(b.nome) like ? ");
			nome = StringUtils.lowerCase(nome);
			params.add("%" + nome + "%");
		}

		return params;
	}

	public List<BaseInterna> findAtivos() {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" b ");
		hql.append(" left outer join fetch b.relacionamentos");
		hql.append(" where b.ativa = true");
		hql.append(" order by b.nome ");

		Query query = createQuery(hql.toString());

		List<BaseInterna> list = query.list();
		Set<BaseInterna> set = new LinkedHashSet<>(list);
		return new ArrayList<>(set);
	}

	public Set<String> findCamposById(Long baseInternaId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select distinct('[''' || v.nome || ''']') ");
		hql.append(" from ").append(clazz.getName()).append(" b ");
		hql.append(" join " + BaseRegistro.class.getName() + " r on b.id = r.baseInterna.id ");
		hql.append(" join " + BaseRegistroValor.class.getName() + " v on r.id = v.baseRegistro.id ");
		hql.append(" where b.id = ? ");
		params.add(baseInternaId);

		Query query = createQuery(hql.toString(), params);

		List<String> result = query.list();

		return new TreeSet<>(result);
	}
}