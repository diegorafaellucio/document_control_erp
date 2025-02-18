package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.LogAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoRegistro;
import net.wasys.getdoc.domain.vo.filtro.LogAlteracaoFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class LogAlteracaoRepository extends HibernateRepository<LogAlteracao> {

	public LogAlteracaoRepository() {
		super(LogAlteracao.class);
	}

	public int countByFiltro(LogAlteracaoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" la ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<LogAlteracao> findByFiltro(LogAlteracaoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" la ");

		List<Object> params = makeQuery(filtro, hql);

		String campoOrdem = filtro.getCampoOrdem();
		if(StringUtils.isNotBlank(campoOrdem)) {

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by la.").append(campoOrdem).append(ordemStr);
		}
		else {

			hql.append(" order by la.id desc ");
		}

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<LogAlteracao> list = query.list();
		return list;
	}

	private List<Object> makeQuery(LogAlteracaoFiltro filtro, StringBuilder hql) {

		List<Object> params = new ArrayList<Object>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Long usuarioId = filtro.getUsuarioId();
		Long registroId = filtro.getRegistroId();
		TipoRegistro tipoRegistro = filtro.getTipoRegistro();
		TipoAlteracao tipoAlteracao = filtro.getTipoAlteracao();

		hql.append(" where 1=1 ");

		if(dataInicio != null) {
			hql.append(" and la.data >= ? ");
			params.add(dataInicio);
		}

		if(dataFim != null) {

			Calendar c = Calendar.getInstance();
			c.setTime(dataFim);
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.SECOND, -1);
			dataFim = c.getTime();

			hql.append(" and la.data <= ? ");
			params.add(dataFim);
		}

		if(usuarioId != null && usuarioId != 0) {
			hql.append(" and la.usuario.id = ? ");
			params.add(usuarioId);
		}

		if(registroId != null) {
			hql.append(" and la.registroId = ? ");
			params.add(registroId);
		}

		if(tipoRegistro != null) {
			hql.append(" and la.tipoRegistro = ? ");
			params.add(tipoRegistro);
		}

		if(tipoAlteracao != null) {
			hql.append(" and la.tipoAlteracao = ? ");
			params.add(tipoAlteracao);
		}

		return params;
	}

	public LogAlteracao getPrevio(LogAlteracao log) {

		Long logId = log.getId();
		TipoRegistro tipoRegistro = log.getTipoRegistro();
		Long registroId = log.getRegistroId();

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName());
		hql.append(" where id < ? ");
		params.add(logId);
		hql.append(" and tipoRegistro = ? ");
		params.add(tipoRegistro);
		hql.append(" and registroId = ? ");
		params.add(registroId);

		hql.append(" order by id desc ");

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		return (LogAlteracao) query.uniqueResult();
	}

	public LogAlteracao getProximo(LogAlteracao log) {

		Long logId = log.getId();
		TipoRegistro tipoRegistro = log.getTipoRegistro();
		Long registroId = log.getRegistroId();

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName());
		hql.append(" where id > ? ");
		params.add(logId);
		hql.append(" and tipoRegistro = ? ");
		params.add(tipoRegistro);
		hql.append(" and registroId = ? ");
		params.add(registroId);

		hql.append(" order by id asc ");

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		return (LogAlteracao) query.uniqueResult();
	}
}
