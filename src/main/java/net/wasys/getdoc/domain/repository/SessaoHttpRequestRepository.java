package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class SessaoHttpRequestRepository extends HibernateRepository<SessaoHttpRequest> {

	public SessaoHttpRequestRepository() {
		super(SessaoHttpRequest.class);
	}

	public int mataSessaoUsuario(Usuario usuario) {
		Query query = createQuery(" update " + clazz.getName() +" set ATIVA = false where USUARIO_ID = " + usuario.getId());
		return query.executeUpdate();
	}

	public SessaoHttpRequest findByJSessionId(String sessionId) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where JSESSIONID = ? and ATIVA = true ");
		params.add(sessionId);
		hql.append(" order by id desc ");

		Query query = createQuery(hql.toString(), params);
		query.setFirstResult(0);
		query.setMaxResults(1);
		return (SessaoHttpRequest) query.uniqueResult();
	}

}
