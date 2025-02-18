package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.TipoProcessoPermissao;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class TipoProcessoPermissaoRepository extends HibernateRepository<TipoProcessoPermissao> {

	public TipoProcessoPermissaoRepository() {
		super(TipoProcessoPermissao.class);
	}

	public List<TipoProcessoPermissao> findByTipoProcesso(Long tipoProcessoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql	.append(" from ").append(clazz.getName()).append(" ");
		hql	.append(" where tipoProcesso.id = :tipoProcessoId ");
		params.put("tipoProcessoId", tipoProcessoId);

		Query query = createQuery(hql, params);
		return query.list();
	}
}
