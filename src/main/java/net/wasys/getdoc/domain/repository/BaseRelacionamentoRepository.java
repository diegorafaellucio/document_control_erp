package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BaseRelacionamento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class BaseRelacionamentoRepository extends HibernateRepository<BaseRelacionamento> {

	public BaseRelacionamentoRepository() {
		super(BaseRelacionamento.class);
	}

	public List<BaseRelacionamento> findByBaseInterna(Long baseInternaId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(startQuery()).append(" br ");
		hql.append(" where br.baseInterna.id = :baseInternaId ");
		params.put("baseInternaId", baseInternaId);

		Query query = createQuery(hql, params);
		return query.list();
	}
}