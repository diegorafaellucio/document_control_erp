package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.CalendarioCriterio;
import net.wasys.getdoc.domain.enumeration.ListaChamada;
import net.wasys.getdoc.domain.enumeration.TipoCalendario;
import net.wasys.getdoc.domain.vo.filtro.CalendarioCriterioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class CalendarioCriterioRepository extends HibernateRepository<CalendarioCriterio> {

	public CalendarioCriterioRepository() {
		super(CalendarioCriterio.class);
	}

	public List<CalendarioCriterio> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by dataInicio desc ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<CalendarioCriterio> findByFiltro(CalendarioCriterioFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" c ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by c.dataInicio desc");

		Query query = createQuery(hql.toString(), params);

		DummyUtils.systraceThread("aaaa: count " + getLogQuery(query, params));

		return (List<CalendarioCriterio>) query.list();
	}

	public CalendarioCriterio getByFiltro(CalendarioCriterioFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" c ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by c.dataInicio desc");

		Query query = createQuery(hql.toString(), params);

		query.setFetchSize(1);
		query.setMaxResults(1);

		return (CalendarioCriterio) query.uniqueResult();
	}

	private Map<String, Object> makeQuery(CalendarioCriterioFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new LinkedHashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Long calendarioId = filtro.getCalendarioId();
		ListaChamada chamada = filtro.getChamada();
		TipoCalendario tipoCalendario = filtro.getTipoCalendario();
		Boolean ativo = filtro.getAtivo();

		hql.append(" where 1=1 ");

		if(dataInicio != null && dataFim != null) {
			hql.append(" and c.dataInicio <= :dataInicio and c.dataFim >= :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		if(calendarioId != null) {
			hql.append(" and c.calendario.id = :calendarioId ");
			params.put("calendarioId", calendarioId);
		}

		if(chamada != null) {
			hql.append(" and c.chamada = :chamada ");
			params.put("chamada", chamada);
		}

		if(chamada != null) {
			hql.append(" and c.chamada = :chamada ");
			params.put("chamada", chamada);
		}

		if(tipoCalendario != null) {
			hql.append(" and c.tipoCalendario = :tipoCalendario ");
			params.put("tipoCalendario", tipoCalendario);
		}

		if(ativo != null) {
			hql.append(" and c.ativo = :ativo ");
			params.put("ativo", ativo);
		}

		return params;
	}

    public Date getFirstDataFimEmissaoTermo() {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append("select min(c.dataFim) from ").append(clazz.getName()).append(" c ");
		hql.append(" where c.executado is false ");
		hql.append(" and c.tipoCalendario = :tipoCalendario");
		params.put("tipoCalendario", TipoCalendario.REGISTRO_EMISSAO_TERMO);

		Query query = createQuery(hql.toString(), params);

		return (Date) query.uniqueResult();
    }

	public List<CalendarioCriterio> findByDataFim(Date dataFim) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append("select c from ").append(clazz.getName()).append(" c ");
		hql.append(" where c.dataFim = :dataFim ");
		hql.append(" and c.tipoCalendario = :tipoCalendario");
		params.put("dataFim", dataFim);
		params.put("tipoCalendario", TipoCalendario.REGISTRO_EMISSAO_TERMO);

		Query query = createQuery(hql.toString(), params);

		return (List<CalendarioCriterio>) query.list();
	}
}
