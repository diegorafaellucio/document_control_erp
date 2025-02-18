package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.TipoEvidenciaRole;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TipoEvidenciaRoleRepository extends HibernateRepository<TipoEvidenciaRole> {

	public TipoEvidenciaRoleRepository() {
		super(TipoEvidenciaRole.class);
	}

	@Transactional(rollbackFor=Exception.class)
	public void deleteByTipoEvidencia(Long tipoEvidenciaId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" delete from ").append(clazz.getName());
		hql.append(" where tipoEvidencia.id = :tipoEvidenciaId");
		params.put("tipoEvidenciaId", tipoEvidenciaId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}

	public List<TipoEvidenciaRole> findByTipoEvidencia(Long tipoEvidenciaId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql	.append(" from ").append(clazz.getName()).append(" ");
		hql	.append(" where tipoEvidencia.id = :tipoEvidenciaId ");
		params.put("tipoEvidenciaId", tipoEvidenciaId);

		Query query = createQuery(hql, params);
		return query.list();
	}
}