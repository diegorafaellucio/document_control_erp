package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Advertencia;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdvertenciaRepository extends HibernateRepository<Advertencia> {

	public AdvertenciaRepository() {
		super(Advertencia.class);
	}

	public List<Advertencia> getLast(List<Long> documentoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" a ");
		hql.append(" left outer join fetch a.documento ");
		hql.append(" left outer join fetch a.irregularidade ");
		hql.append(" where a.id in ( ");
		hql.append(" 	 select max(a2.id) from ").append(clazz.getName()).append(" a2 where a2.documento.id in (:documentoId) and a2.dataFinalizacao is not null group by a2.documento.id ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql, params);

		return query.list();
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
