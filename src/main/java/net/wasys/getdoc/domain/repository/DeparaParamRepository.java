package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.DeparaParam;
import net.wasys.getdoc.domain.entity.RegraLinha;
import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.getdoc.domain.enumeration.TipoSubRegra;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class DeparaParamRepository extends HibernateRepository<DeparaParam> {

	public DeparaParamRepository() {
		super(DeparaParam.class);
	}

	public List<DeparaParam> findBySubRegraId(Long subRegraId) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select dp ");
		hql.append(getStartQuery()).append(" dp ");
		hql.append(" join ").append(SubRegra.class.getName()).append(" sr ");
		hql.append(" on dp.subRegra.id = sr.id");
		hql.append(" where dp.subRegra.id = :subRegraId");

		Query query = createQuery(hql.toString());
		query.setParameter("subRegraId", subRegraId);

		return query.list();
	}

    public List<DeparaParam> findByRegraId(Long regraId) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select dp ");
		hql.append(getStartQuery()).append(" dp, ");
		hql.append(SubRegra.class.getName()).append(" sr, ");
		hql.append(RegraLinha.class.getName()).append(" rl where ");
		hql.append(" rl.regra.id = :regraId and ");
		params.put("regraId", regraId);
		hql.append(" sr.linha.id = rl.id and ");
		hql.append(" tipo = '").append(TipoSubRegra.CONSULTA_EXTERNA).append("' and ");
		hql.append(" dp.subRegra.id = sr.id ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
