package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.ConsultaExternaLog;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaLogFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class ConsultaExternaLogRepository extends HibernateRepository<ConsultaExternaLog> {

	public ConsultaExternaLogRepository() {
		super(ConsultaExternaLog.class);
	}

	@SuppressWarnings("unchecked")
	public List<Integer> total() {

		StringBuilder hql = buildInitialQuery(true);

		hql.append("GROUP BY cel.consultaExterna ");

		Query query = createQuery(hql.toString());

		List<Long> listObj = query.list();
		List<Integer> listQtd = new ArrayList<>();

		for (Long object : listObj) {
			listQtd.add(object.intValue());
		}

		return listQtd;
	}

	public Integer countByFiltro(ConsultaExternaLogFiltro filtro) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = buildInitialQuery(true);

		buildHqlFromFiltro(filtro, hql, params);

		Query query = createQuery(hql.toString(), params);

		return ((Long) query.uniqueResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	public List<ConsultaExternaLog> findByFiltro(ConsultaExternaLogFiltro filtro, Integer first, Integer pageSize) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = buildInitialQuery(false);

		buildHqlFromFiltro(filtro, hql, params);

		hql.append(" order by cel.id ");

		Query query = createQuery(hql.toString(), params);

		if(first != null && pageSize != null) {
			query.setFirstResult(first);
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	private void buildHqlFromFiltro(ConsultaExternaLogFiltro filtro, StringBuilder hql, Map<String, Object> params) {
		if(filtro != null) {

			if(filtro.getId() != null) {
				hql.append("AND cel.id = :id ");
				params.put("id", filtro.getId());
			}
			if(filtro.getData() != null) {
				hql.append("AND cel.data = :data ");
				params.put("data", filtro.getData());
			}
			if(filtro.getUsuario() != null && filtro.getUsuario().getId() != null) {
				hql.append("AND cel.usuario.id = :usuarioId ");
				params.put("usuarioId", filtro.getUsuario().getId());
			}
			if(filtro.getProcesso() != null && filtro.getProcesso().getId() != null) {
				hql.append("AND cel.processo.id = :processoId ");
				params.put("processoId", filtro.getProcesso().getId());
			}
			if(filtro.getConsultaExterna() != null && filtro.getConsultaExterna().getId() != null) {
				hql.append("AND cel.consultaExterna.id = :consultaExternaId ");
				params.put("consultaExternaId", filtro.getConsultaExterna().getId());
			}
		}
	}

	private StringBuilder buildInitialQuery(Boolean count) {
		StringBuilder hql = new StringBuilder();
		if(count) {
			hql.append("SELECT COUNT(cel) ");
		}else {
			hql.append("SELECT cel ");
		}
		hql.append("FROM " + clazz.getName()).append(" cel ");
		hql.append("WHERE 1=1 ");
		return hql;
	}
}