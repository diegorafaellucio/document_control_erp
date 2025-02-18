package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.RelatorioGeralDocumento;
import net.wasys.getdoc.domain.entity.RelatorioGeralSituacao;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioGeralDocumentoRepository extends HibernateRepository<RelatorioGeralDocumento> {

	public RelatorioGeralDocumentoRepository() {
		super(RelatorioGeralDocumento.class);
	}

	public List<RelatorioGeralDocumento> findByProcessosIds(List<Long> processosIds2) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append( " r");

		hql.append(" where r.documento.processo.id in (-1");
		for (Long processoId : processosIds2) {
			hql.append(", ").append(processoId);
		}
		hql.append(" ) ");
		hql.append(" order by id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<RelatorioGeralSituacao> findByFiltro(RelatorioGeralFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append(" order by r.id");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int countByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private List<Object> makeQuery(RelatorioGeralFiltro filtro, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		List<Object> params = new ArrayList<Object>();

		hql.append(" where r.dataCriacao between ? and ? ");
		params.add(dataInicio);
		params.add(dataFim);

		return params;
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {

		Date dataInicio = filtro.getDataInicio();
		dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
		Date dataFim = filtro.getDataFim();
		Calendar dataFimC = Calendar.getInstance();
		dataFimC.setTime(dataFim);
		dataFimC.add(Calendar.DAY_OF_MONTH, 1);
		dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
		dataFimC.add(Calendar.MILLISECOND, -1);
		dataFim = dataFimC.getTime();

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select id from ").append(clazz.getName()).append(" rg where 1=1 ");

		if(dataFim != null && dataInicio != null) {

			hql.append(" or rg.data between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		hql.append(" and rg.documento.status in (:DIGITALIZADO,:INCLUIDO,:PENDENTE,:APROVADO,:PROCESSANDO) ");
		params.put("DIGITALIZADO", StatusDocumento.DIGITALIZADO);
		params.put("INCLUIDO", StatusDocumento.INCLUIDO);
		params.put("PENDENTE", StatusDocumento.PENDENTE);
		params.put("APROVADO", StatusDocumento.APROVADO);
		params.put("PROCESSANDO", StatusDocumento.PROCESSANDO);

		hql.append(" order by rg.data ");

		Query query = createQuery(hql.toString(), params);

		query.setFetchSize(100);

		return query.list();

	}

	public List<RelatorioGeralDocumento> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(getStartQuery()).append(" rg");

		hql.append(" where id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" and rg.documento.status in (:DIGITALIZADO,:INCLUIDO,:PENDENTE,:APROVADO,:PROCESSANDO) ");
		params.put("DIGITALIZADO", StatusDocumento.DIGITALIZADO);
		params.put("INCLUIDO", StatusDocumento.INCLUIDO);
		params.put("PENDENTE", StatusDocumento.PENDENTE);
		params.put("APROVADO", StatusDocumento.APROVADO);
		params.put("PROCESSANDO", StatusDocumento.PROCESSANDO);

		hql.append(" order by rg.documento.processo.id, data ");

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);

		return query.list();
	}

	public List<RelatorioGeralSituacao> find(Area area, Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(getStartQuery());
		hql.append(" where dataFinalizacao is not null ");

		if(area != null) {
			hql.append(" and area.id = :areaId ");
			Long areaId = area.getId();
			params.put("areaId", areaId);
		}

		if(dataInicio != null) {
			hql.append(" and data >= :dataInicio ");
			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
			params.put("dataInicio", dataInicio);
		}

		if(dataFim != null) {
			hql.append(" and data <= :dataFim ");
			dataFim = DateUtils.truncate(dataFim, Calendar.DAY_OF_MONTH);
			dataFim = DateUtils.addDays(dataFim, 1);
			dataFim = DateUtils.addSeconds(dataFim, -1);
			params.put("dataFim", dataFim);
		}

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Processo> findProcessos(Area area, int mes, int ano, Date dataInicio, Date dataFim) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select p from net.wasys.getdoc.domain.entity.Processo p  ");
		hql.append(" where p.id in (select s.processoId from net.wasys.getdoc.domain.entity.RelatorioGeralSolicitacao s where s.dataFinalizacao is not null ");

		if(area != null) {
			hql.append(" and s.area.id = :areaId ");
			Long areaId = area.getId();
			params.put("areaId", areaId);
		}

		if(dataInicio != null) {
			hql.append(" and s.dataSolicitacao >= :dataInicio and date_part('month', s.dataSolicitacao) = :mes and date_part('year',s.dataSolicitacao) = :ano ");
			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
			params.put("dataInicio", dataInicio);
			params.put("mes", mes);
			params.put("ano", ano);
		}

		if(dataFim != null) {
			hql.append(" and s.dataSolicitacao <= :dataFim ");
			dataFim = DateUtils.truncate(dataFim, Calendar.DAY_OF_MONTH);
			dataFim = DateUtils.addSeconds(dataFim, -1);
			dataFim = DateUtils.addDays(dataFim, 1);
			params.put("dataFim", dataFim);
		}
		hql.append(" ) ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
