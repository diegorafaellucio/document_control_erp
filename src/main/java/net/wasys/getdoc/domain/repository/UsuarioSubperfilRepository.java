package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.UsuarioSubperfil;
import net.wasys.getdoc.rest.response.vo.SubPerfilResponse;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UsuarioSubperfilRepository extends HibernateRepository<UsuarioSubperfil> {

	public UsuarioSubperfilRepository() {
		super(UsuarioSubperfil.class);
	}

	public List<SubPerfilResponse> getUsuarioSubperfil(Long usuarioId){
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" usp ");
		hql.append(" where usp.usuario.id = ? ");
		params.add(usuarioId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<UsuarioSubperfil> findByAnalista(Long usuarioId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" usp ");
		hql.append(" where usp.usuario.id = :usuarioId ");
		params.put("usuarioId", usuarioId);

		Query query = createQuery(hql, params);

		return query.list();
	}
}
