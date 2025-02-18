package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.UsuarioCampus;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioCampusRepository extends HibernateRepository<UsuarioCampus> {

	public UsuarioCampusRepository() {
		super(UsuarioCampus.class);
	}


	public UsuarioCampus findByUsuarioIdAndCampusId(Long usuarioId, Long campusId) {
		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select uc ");
		hql.append(getStartQuery()).append(" uc ");
		hql.append(" where uc.usuario.id = ? ");
		params.add(usuarioId);
		hql.append(" and uc.campus.id = ? ");
		params.add(campusId);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (UsuarioCampus) query.uniqueResult();
	}
}
