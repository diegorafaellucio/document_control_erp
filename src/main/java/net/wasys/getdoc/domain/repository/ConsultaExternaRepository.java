package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.entity.ConsultaExternaLog;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaFiltro;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;

@Repository
public class ConsultaExternaRepository extends HibernateRepository<ConsultaExterna> {

	public ConsultaExternaRepository() {
		super(ConsultaExterna.class);
	}

	public ConsultaExterna findByParametrosAndConsultaExterna(String parametrosJson, TipoConsultaExterna tipo, Date aPartirDe) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" c ")
		.append(" where c.parametros = :parametros ")
		.append(" and c.tipo = :tipo ")
		.append(" and c.data >= :data ")
		.append(" and c.status = :status ");

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("parametros", parametrosJson);
		params.put("tipo", tipo);
		params.put("data", aPartirDe);
		params.put("status", StatusConsultaExterna.SUCESSO);

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return (ConsultaExterna) query.uniqueResult();
	}

	public Integer countByFiltro(ConsultaExternaFiltro filtro) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = buildInitialQuery(true);

		buildHqlFromFiltro(filtro, hql, params);

		Query query = createQuery(hql.toString(), params);

		return ((Long) query.uniqueResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	public List<ConsultaExterna> findByFiltro(ConsultaExternaFiltro filtro, Integer first, Integer pageSize) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = buildInitialQuery(false);

		buildHqlFromFiltro(filtro, hql, params);

		hql.append(" order by ce.id desc ");

		Query query = createQuery(hql.toString(), params);

		if(first != null && pageSize != null) {
			query.setFirstResult(first);
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	private void buildHqlFromFiltro(ConsultaExternaFiltro filtro, StringBuilder hql, Map<String, Object> params) {
		if(filtro != null) {

			if(filtro.getId() != null) {
				hql.append("AND ce.id = :id ");
				params.put("id", filtro.getId());
			}
			if(filtro.getProcessoId() != null) {
				hql.append(" AND ( ");
				hql.append(" 	SELECT COUNT(*) ");
				hql.append(" 	FROM ").append(ConsultaExternaLog.class.getName()).append(" cel ");
				hql.append(" 	WHERE cel.consultaExterna.id = ce.id ");
				hql.append(" 	AND cel.processo.id = :id ");
				hql.append(" ) > 0 ");
				params.put("id", filtro.getProcessoId());
			}
			if(filtro.getTipo() != null) {
				hql.append("AND ce.tipo = :tipo ");
				params.put("tipo", filtro.getTipo());
			}
			if(filtro.getStatus() != null) {
				hql.append("AND ce.status = :status ");
				params.put("status", filtro.getStatus());
			}
			if(StringUtils.isNotBlank(filtro.getStackTrace())) {
				hql.append("AND ce.stackTrace = :stackTrace ");
				params.put("stackTrace", filtro.getStackTrace());
			}
			if(StringUtils.isNotBlank(filtro.getResultado())) {
				hql.append("AND ce.resultado = :resultado ");
				params.put("resultado", filtro.getResultado());
			}
			if(StringUtils.isNotBlank(filtro.getParametros())) {
				hql.append("AND ce.parametros = :parametros ");
				params.put("parametros", filtro.getParametros());
			}
			if(filtro.getDataInicio() != null) {
				hql.append("AND ce.data >= :dataInicio ");
				params.put("dataInicio", filtro.getDataInicio());
			}
			Date dataFim = filtro.getDataFim();
			if(dataFim != null) {
				dataFim = DummyUtils.truncateFinalDia(dataFim);
				hql.append("AND ce.data <= :dataFim ");
				params.put("dataFim", dataFim);
			}
			if(filtro.getUsuario() != null && filtro.getUsuario().getId() != null) {
				hql.append("AND ce.usuario.id = :usuarioId ");
				params.put("usuarioId", filtro.getUsuario().getId());
			}
		}
	}

	private StringBuilder buildInitialQuery(Boolean count) {
		StringBuilder hql = new StringBuilder();
		if(count) {
			hql.append("SELECT COUNT(ce) ");
		}else {
			hql.append("SELECT ce ");
		}
		hql.append("FROM " + clazz.getName()).append(" ce ");
		hql.append("WHERE 1=1 ");
		return hql;
	}

	public ConsultaExterna findLastByFiltro(ConsultaExternaFiltro filtro) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = buildInitialQuery(false);

		buildHqlFromFiltro(filtro, hql, params);

		hql.append(" order by ce.data desc ");

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return (ConsultaExterna) query.uniqueResult();
	}

	public List<Object[]> findToMonitoramento(Date date, List<TipoConsultaExterna> tiposConsultaExterna) {

		Date dataInicio = DateUtils.truncate(date, Calendar.MINUTE);
		Date dataFim = DateUtils.addMinutes(dataInicio, 1);

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tipo, status, count(*), avg(tempoExecucao) ");
		hql.append(" from ").append(ConsultaExterna.class.getName());
		hql.append(" where data between :dataInicio and :dataFim ");
		hql.append(" and tipo in (:tiposConsultaExterna) ");
		hql.append(" group by tipo, status ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);
		params.put("tiposConsultaExterna", tiposConsultaExterna);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public HashMap<String, Object> makeQueryWsPorTempo(LogAcessoFiltro filtro, StringBuilder sql) {

		HashMap<String, Object> params = new HashMap<>();
		Integer intervalo = filtro.getIntervalo();

		sql.append(" select ");
		sql.append("	tipo, ");
		sql.append("	to_char(date_trunc('hour', data) + cast(date_part('minute', data) as int) / " + intervalo + " * interval '" + intervalo + " min', 'DD/MM HH24:MI') AS horaMinuto, ");
		sql.append("	round(avg(tempo_execucao)) as tempoMedio, ");
		sql.append("	round(sum(tempo_execucao)) as tempoTotal, ");
		sql.append("	sum(octet_length(resultado)) / 1024 as tamanhoTotal, ");
		sql.append("	count(*) as acessos ");
		sql.append(" from ");
		sql.append("	consulta_externa ");
		sql.append(" where 1=1");

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		if(dataInicio != null && dataFim != null) {
			sql.append(" and data between :dataInicio ");
			params.put("dataInicio", dataInicio);
			sql.append(" and :dataFim ");
			params.put("dataFim", dataFim);
		}

		sql.append(" group by ");
		sql.append("	tipo, ");
		sql.append("	horaMinuto ");

		return params;
	}

	public List<Object[]> findWsPorTempo(LogAcessoFiltro filtro, int first, int pageSize){

		StringBuilder sql = new StringBuilder();

		HashMap<String, Object> params = makeQueryWsPorTempo(filtro, sql);
		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem != null) {
			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			sql.append(" order by ").append(campoOrdem).append(ordemStr);
		}
		else {
			sql.append(" order by ");
			sql.append("	horaMinuto, ");
			sql.append("	tempoTotal desc");
		}

		Query query = createSQLQuery(sql.toString(), params);

		query.setFirstResult(first);
		query.setMaxResults(pageSize);

		return query.list();
	}

	public int countWSPorTempo(LogAcessoFiltro filtro) {
		StringBuilder sql = new StringBuilder();
		sql.append(" select count(*) from ( ");
		HashMap<String, Object> params = makeQueryWsPorTempo(filtro, sql);
		sql.append(") as count ");

		Query query = createSQLQuery(sql.toString(), params);

		return ((BigInteger) query.uniqueResult()).intValue();
	}

	public List<Object[]> findWsStatus(Date dia) {

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();
		sql.append(" SELECT * ");
		sql.append(" FROM ( ");
		sql.append("    SELECT ");
		sql.append("        ROW_NUMBER() OVER (PARTITION BY t.tipo ORDER BY t.id desc) AS r, ");
		sql.append("        t.tipo, t.data, t.status, t.mensagem, t.stack_trace ");
		sql.append("    FROM consulta_externa t ");
		sql.append("    WHERE t.data >= :dia ");
		sql.append("    order by t.data asc ");
		sql.append(" ) x ");
		sql.append(" WHERE x.r <= 3 ");
		sql.append(" ORDER BY x.tipo, x.r desc ");

		dia = DummyUtils.truncateInicioDia(dia);
		params.put("dia", dia);

		Query query = createSQLQuery(sql.toString(), params);

		return query.list();
	}

	public List<Long> findToExpurgo(int max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select c.id from ").append(clazz.getName()).append(" c ");
		hql.append(" where c.id not in (");
		hql.append("	select max(c2.id) from ").append(clazz.getName()).append(" c2 ");
		hql.append(" 		where c2.parametros = c.parametros) ");

		Query query = createQuery(hql);
		query.setMaxResults(max);

		return query.list();
	}

	public void expurgar(List<Long> list) {

		StringBuilder hql = new StringBuilder();

		hql.append(" delete from ").append(clazz.getName());
		hql.append(" where id in ( -1 ");
		for (Long prlId : list) {
			hql.append(",").append(prlId).append(" ");
		}
		hql.append(" ) ");

		Query query = createQuery(hql);
		query.executeUpdate();
	}
}