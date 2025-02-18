package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Irregularidade;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class PendenciaRepository extends HibernateRepository<Pendencia> {

	public PendenciaRepository() {
		super(Pendencia.class);
	}

	public List<Pendencia> getLast(List<Long> documentoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" p ");
		hql.append(" left outer join fetch p.documento ");
		hql.append(" left outer join fetch p.irregularidade ");
		hql.append(" where p.id in ( ");
		hql.append(" 	 select max(p2.id) from ").append(clazz.getName()).append(" p2 where p2.documento.id in (:documentoId) group by p2.documento.id ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<Pendencia> findToNotificar(Long processoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" p ");
		hql.append(" where p.documento.processo.id = :processoId ");
		hql.append(" and p.notificadoAtila is false ");
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public Long getLastIrregularidade(Long documentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.irregularidade.id ");
		hql.append(" from " + Pendencia.class.getName() + " p ");
		hql.append(" inner join " + Documento.class.getName() + " d on d.id = p.documento.id ");
		hql.append(" where d.status = 'PENDENTE' ");
		hql.append(" and p.id in ( ");
		hql.append(" 	 select max(p2.id) from " + Pendencia.class.getName() + " p2 where p2.documento.id in (:documentoId) group by p2.documento.id ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		return (Long) query.uniqueResult();
	}

	public Map<Irregularidade, Integer> countIrregularidadesByProcesso(Long processoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.irregularidade.id, p.irregularidade.nome, count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" where p.documento.processo.id = :processoId ");
		hql.append(" group by p.irregularidade.id, p.irregularidade.nome ");
		hql.append(" order by p.irregularidade.nome ");
		params.put("processoId", processoId);

		Map<Irregularidade, Integer> map = new LinkedHashMap<>();
		Query query = createQuery(hql, params);
		List<Object[]> list = query.list();
		for (Object[] objs : list) {
			Irregularidade irregularidade = new Irregularidade();
			irregularidade.setId((Long) objs[0]);
			irregularidade.setNome((String) objs[1]);
			Long count = (Long) objs[2];
			map.put(irregularidade, count.intValue());
		}
		return map;
	}
}
