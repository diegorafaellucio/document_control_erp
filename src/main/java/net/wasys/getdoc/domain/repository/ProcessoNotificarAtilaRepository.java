package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Resposta;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoNotificarAtilaRepository extends HibernateRepository<ProcessoNotificarAtila> {

	private final static long TIMEOUT_CACHE = (1000 * 60 * 10);//10 minutos

	public ProcessoNotificarAtilaRepository() {
		super(ProcessoNotificarAtila.class);
	}

	public ProcessoNotificarAtila findByProcessoId(Long processoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashedMap();

		hql.append(getStartQuery()).append(" pna ");
		hql.append(" where pna.processo.id = :processoId ");
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (ProcessoNotificarAtila) query.uniqueResult();
	}

	public List<ProcessoNotificarAtila> findByProcessoIds(List<Long> processoIds) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashedMap();

		hql.append(getStartQuery()).append(" pna ");
		hql.append(" where pna.processo.id in ( :processoIds ) ");
		params.put("processoIds", processoIds);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<ProcessoNotificarAtila> findToNotificar() {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashedMap();

		hql.append(getStartQuery()).append(" pna ");
		hql.append(" where pna.notificarAtila is true ");

		Query query = createQuery(hql, params);

		return query.list();
	}
}
