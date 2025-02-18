package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.SituacaoLock;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.SubperfilSituacao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Repository
public class SituacaoLockRepository extends HibernateRepository<SituacaoLock> {

	public SituacaoLockRepository() {
		super(SituacaoLock.class);
	}

	public List<SituacaoLock> getSituacoesLock(List<Long> situacoesIds) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" sl ");
		hql.append(" where 1=1 ");
		hql.append(" and sl.situacaoId in :situacoesIds ");
		hql.append(" order by sl.id asc ");

		params.put("situacoesIds", situacoesIds);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<SituacaoLock> findSituacoesLockBySubperfis(List<Subperfil> subperfilAtivo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select sl from ").append(clazz.getName()).append(" sl ");
		hql.append(" join ").append(SubperfilSituacao.class.getName()).append(" ss on ss.situacao.id = sl.situacaoId ");
		hql.append(" where 1=1 ");
		hql.append(" and ss.subperfil.id in (:subperfisIds) ");
		hql.append(" order by sl.id asc ");

		List<Long> ids = subperfilAtivo.stream().map(Subperfil::getId).collect(toList());
		params.put("subperfisIds", ids);

		Query query = createQuery(hql, params);

		return query.list();
	}
}
