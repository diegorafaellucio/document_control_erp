package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class BaseRegistroValorRepository extends HibernateRepository<BaseRegistroValor> {

	public BaseRegistroValorRepository() {
		super(BaseRegistroValor.class);
	}

	public int deleteByBaseRegistroId(Long id) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" delete ");
		hql.append(getStartQuery()).append(" bv ");
		hql.append(" where bv.baseRegistro.id = ? ");
		params.add(id);

		Query query = createQuery(hql.toString(), params);

		return query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<String> getColunasRegistro(Long baseInternaId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select brv.nome ");
		hql.append(" from ").append(BaseRegistroValor.class.getName()).append(" brv ");
		hql.append(" where brv.baseRegistro.id = ( ");
		hql.append(" 		select max(br.id) ");
		hql.append(" 		from ").append(BaseRegistro.class.getName()).append(" br  ");
		hql.append(" 		where br.baseInterna.id = :baseInternaId ");
		hql.append(" ) ");
		hql.append(" order by brv.nome ");

		params.put("baseInternaId", baseInternaId);

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public void deleteByRegistro(Long baseRegistroIdId) {
		Query query = createQuery("delete " + clazz.getName() + " where baseRegistro.id = :baseRegistroId ");
		query.setParameter("baseRegistroId", baseRegistroIdId);
		query.executeUpdate();
	}

	public BaseRegistroValor getByNomeAndBaseInterna(String valor, Long baseInternaId) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select brv ");
		hql.append(getStartQuery()).append(" brv ");
		hql.append(" where brv.valor = ? ");
		params.add(valor);
		hql.append(" and brv.baseRegistro.baseInterna.id = ? ");
		params.add(baseInternaId);

		Query query = createQuery(hql.toString(), params);

		return (BaseRegistroValor) query.uniqueResult();
	}
}