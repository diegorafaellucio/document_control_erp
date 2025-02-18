package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Calendario;
import net.wasys.getdoc.domain.enumeration.TipoParceiro;
import net.wasys.getdoc.domain.enumeration.TipoProuni;
import net.wasys.getdoc.domain.vo.filtro.CalendarioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class CalendarioRepository extends HibernateRepository<Calendario> {

	public CalendarioRepository() {
		super(Calendario.class);
	}

	public List<Calendario> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by dataInicio desc ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public Integer countByFiltro(CalendarioFiltro filtro) {
		StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " c ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql, params);

		return ((Long) query.uniqueResult()).intValue();
	}

    public List<Calendario> findByFiltro(CalendarioFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" c ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by c.dataInicio desc");

		Query query = createQuery(hql.toString(), params);

		return (List<Calendario>) query.list();
    }

	public Calendario getByFiltro(CalendarioFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" c ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by c.dataInicio desc");

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return (Calendario) query.uniqueResult() ;
	}

	private Map<String, Object> makeQuery(CalendarioFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new LinkedHashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		String periodoIngresso = filtro.getPeriodoIngresso();
		Long tipoProcessoId = filtro.getTipoProcessoId();
		TipoParceiro tipoParceiro = filtro.getTipoParceiro();
		TipoProuni tipoProuni = filtro.getTipoProuni();
		Boolean ativo = filtro.getAtivo();

		hql.append(" where 1=1 ");

		if(dataInicio != null && dataFim != null) {
			hql.append(" and c.dataInicio <= :dataInicio and c.dataFim >= :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		if(StringUtils.isNotBlank(periodoIngresso)) {
			hql.append(" and c.periodoIngresso = :periodoIngresso ");
			params.put("periodoIngresso", periodoIngresso);
		}

		if(tipoProcessoId != null) {
			hql.append(" and c.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(tipoParceiro != null) {
			hql.append(" and c.tipoParceiro = :tipoParceiro ");
			params.put("tipoParceiro", tipoParceiro);
		}

		if(tipoProuni != null) {
			hql.append(" and c.tipoProuni = :tipoProuni ");
			params.put("tipoProuni", tipoProuni);
		}

		if(ativo != null) {
			hql.append(" and c.ativo = :ativo ");
			params.put("ativo", ativo);
		}

		return params;
	}
}
