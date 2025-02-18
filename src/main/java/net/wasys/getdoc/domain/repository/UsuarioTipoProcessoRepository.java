package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.UsuarioTipoProcesso;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class UsuarioTipoProcessoRepository extends HibernateRepository<UsuarioTipoProcesso> {

	public UsuarioTipoProcessoRepository() {
		super(UsuarioTipoProcesso.class);
	}

	public boolean usuarioAtendeTipoProcesso(Long usuarioId, Long tipoProcessoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" utp ");
		hql.append(" where utp.usuario.id = ? ");
		params.add(usuarioId);
		hql.append(" and utp.tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}
}
