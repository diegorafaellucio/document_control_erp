package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.enumeration.TipoRegistroLogAcesso;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class LogAcessoRepository extends HibernateRepository<LogAcesso> {

	public LogAcessoRepository() {
		super(LogAcesso.class);
	}

	public int countByFiltro(LogAcessoFiltro filtro) {

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();

		sql.append("select count(*) from log_acesso la where 1=1 ");

		makeWhere(filtro, sql, params);

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		if(dataInicio != null && dataFim != null) {
			sql.append(" and la.inicio between :dataInicio ");
			params.put("dataInicio", dataInicio);
			sql.append(" and :dataFim ");
			params.put("dataFim", dataFim);
		}

		Query query = createSQLQuery(sql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<LogAcesso> findByFiltro(LogAcessoFiltro filtro, int first, int pageSize) {

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();

		sql.append("select * from log_acesso la where 1=1 ");

		makeWhere(filtro, sql, params);

		sql.append(" order by ");

		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem != null) {

			campoOrdem = campoOrdem.replace("log.", "la.");
			campoOrdem = campoOrdem.replace("contentLenght", "content_length");
			campoOrdem = campoOrdem.replace("contentSize", "content_size");

			sql.append("la." + campoOrdem);

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";
			sql.append(ordemStr);
		}
		else {
			sql.append("	la.inicio desc ");
		}

		Query query = createSQLQuery(sql.toString(), params);

		query.setFirstResult(first);
		query.setMaxResults(pageSize);

		List<Object[]> objs = query.list();

		List<LogAcesso> list = new ArrayList<>();

		for (Object[] objects : objs) {

			Long id = ((Integer) objects[0]).longValue();
			Boolean ajax = (Boolean) objects[1];
			String server = (String) objects[2];
			String locale = (String) objects[3];
			String method = (String) objects[4];
			String parameters = (String) objects[5];
			String protocol = (String) objects[6];
			String remoteHost = (String) objects[7];
			String scheme = (String) objects[8];
			String serverName = (String) objects[9];
			Integer serverPort = (Integer) objects[10];
			String contextPath = (String) objects[11];
			String servletPath = (String) objects[12];
			String remoteUser = (String) objects[13];
			Date inicio = (Date) objects[14];
			String exception = (String) objects[15];
			String forward = (String) objects[16];
			String redirect = (String) objects[17];
			String contentType = (String) objects[18];
			Integer status = (Integer) objects[19];
			Date fim = objects[20] == null ? null : (Date) objects[20];
			Long tempo = objects[21] == null ? null : ((BigInteger) objects[21]).longValue();
			String userAgent = (String) objects[22];
			Integer contentSize = (Integer) objects[23];
			Integer contentLength = (Integer) objects[24];
			String headers = (String) objects[25];

			LogAcesso logAcesso = new LogAcesso();
			logAcesso.setId(id);
			logAcesso.setAjax(ajax);
			logAcesso.setServer(server);
			logAcesso.setLocale(locale);
			logAcesso.setMethod(method);
			logAcesso.setParameters(parameters);
			logAcesso.setProtocol(protocol);
			logAcesso.setRemoteHost(remoteHost);
			logAcesso.setScheme(scheme);
			logAcesso.setServerName(serverName);
			logAcesso.setServerPort(serverPort);
			logAcesso.setContextPath(contextPath);
			logAcesso.setServletPath(servletPath);
			logAcesso.setRemoteUser(remoteUser);
			logAcesso.setInicio(inicio);
			logAcesso.setException(exception);
			logAcesso.setForward(forward);
			logAcesso.setRedirect(redirect);
			logAcesso.setContentType(contentType);
			logAcesso.setStatus(status);
			logAcesso.setFim(fim);
			logAcesso.setTempo(tempo);
			logAcesso.setUserAgent(userAgent);
			logAcesso.setContentSize(contentSize);
			logAcesso.setContentLength(contentLength);
			logAcesso.setHeaders(headers);

			list.add(logAcesso);
		}

		return list;
	}

	private void makeWhere(LogAcessoFiltro filtro, StringBuilder sql, HashMap<String, Object> params) {

		Long id = filtro.getId();
		List<String> tipoDoRegistro = filtro.getTipoDoRegistro();
		String servletPath = filtro.getServletPath();
		Usuario usuario = filtro.getUsuario();
		Boolean apenasErros = filtro.getApenasErros();
		Boolean semFim = filtro.isSemFim();
		String parameters = filtro.getParameters();
		String headers = filtro.getHeaders();
		String threadName = filtro.getThreadName();
		String server = filtro.getServer();
		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		if(dataInicio != null && dataFim != null) {
			sql.append(" and la.inicio between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		if(id != null){
			sql.append(" and la.id = :id ");
			params.put("id", id);
		}

		if(StringUtils.isNotBlank(threadName)){
			threadName = StringUtils.trim(threadName);
			sql.append(" and la.thread_name like :threadName ");
			params.put("threadName", "%" + threadName + "%");
		}

		if(StringUtils.isNotBlank(usuario.getLogin())) {
			sql.append(" and upper(la.remote_user) like upper(:usuarioLogin)");
			params.put("usuarioLogin", "%"+usuario.getLogin()+"%");
		}

		if(usuario.getRoleGD() != null) {
			sql.append(" and la.remote_user in (select usuario.login from Role rl ");
			sql.append(" where rl.nome = :usuarioRole ) ");
			params.put("usuarioRole", usuario.getRoleGD().name());
		}

		if(StringUtils.isNotBlank(servletPath)) {
			sql.append(" and la.servlet_path like :servletPath ");
			params.put("servletPath", "%"+servletPath+"%");
		}

		if(StringUtils.isNotBlank(parameters)) {
			parameters = parameters.replaceAll("\\s","");
			sql.append(" and upper(la.parameters) like upper(:parameters) ");
			params.put("parameters", "%"+parameters+"%");
		}

		if(StringUtils.isNotBlank(headers)) {
			headers = headers.replaceAll("\\s", " ");
			sql.append(" and upper(la.headers) like upper(:headers) ");
			params.put("headers", "%"+headers+"%");
		}

		if(tipoDoRegistro != null && !tipoDoRegistro.isEmpty()) {
			sql.append(" and ( 1 <> 1 ");
			if (tipoDoRegistro.contains(TipoRegistroLogAcesso.AJAX.name())) {
				sql.append(" or la.ajax = 'true' ");
			}
			if (tipoDoRegistro.contains(TipoRegistroLogAcesso.JOB.name())) {
				sql.append(" or la.servlet_path like '%Job' ");
			}
			if (tipoDoRegistro.contains(TipoRegistroLogAcesso.NORMAL.name())) {
				sql.append(" or (la.servlet_path like '%/%' and la.ajax = 'false' and la.servlet_path not like '%rest%') ");
			}
			if (tipoDoRegistro.contains(TipoRegistroLogAcesso.REST.name())) {
				sql.append(" or la.servlet_path like '%rest%' ");
			}
			sql.append(" ) ");
		}

		if(apenasErros){
			sql.append(" and la.exception is not null ");
		}

		if(semFim){
			sql.append(" and la.fim is null ");
		}

		if(server != null) {
			sql.append(" and la.server = :server ");
			params.put("server", server);
		}
	}

	public int excluirAnteriorA(int idInicio) {
		Query query = createQuery(" delete from " + clazz.getName() + " where id < " + idInicio + " and " + getFiltroExcluir());
		query.setTimeout(GetdocConstants.QUERY_TIMEOUT);
		return query.executeUpdate();
	}

	private String getFiltroExcluir() {
		return " servlet_path not like '/cadastros/%' and servlet_path not like '/admin/%' ";
	}

	public List<LogAcesso> findLastsJobs(List<LogAcessoJob> jobs, String server) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from " + clazz.getName() + " la where la.id in ( ");
		hql.append(" 	select max(la2.id) from " + clazz.getName() + " la2 ");
		hql.append(" 	where 1 = 1 ");
		hql.append(" 		and la2.inicio > now() + '-1 day' ");
		if(StringUtils.isNotBlank(server)) {
			hql.append(" 	and la2.server = '").append(server).append("' ");
		}
		if(CollectionUtils.isNotEmpty(jobs)) {
			hql.append(" 	and la2.servletPath in ( ");
			for (LogAcessoJob job : jobs) {
				String key = job.getKey();
				hql.append("'").append(key).append("'");
				hql.append(jobs.indexOf(job) < jobs.size() -1 ? ", " : "");
			}
			hql.append(" 	) ");
		}
		else {
			hql.append(" 	and la2.servletPath like '%Job' ");
		}
		hql.append(" 	group by la2.servletPath ");
		hql.append(" ) ");
		hql.append(" order by la.servletPath ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<Object[]> findPathPorTempo(LogAcessoFiltro filtro, int first, int pageSize){

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();
		Integer intervalo = filtro.getIntervalo();

		sql.append(" select ");
		sql.append("	servlet_path as path, ");
		sql.append("	to_char(date_trunc('hour', inicio) + cast(date_part('minute', inicio) as int)  / " + intervalo + " * interval '" + intervalo + " min', 'DD/MM HH24:MI') AS horaMinuto, ");
		sql.append("	round(avg(tempo)) as tempoMedio, ");
		sql.append("	round(sum(tempo)) as tempoTotal, ");
		sql.append("	sum(content_size) / 1024 as tamanhoTotal, ");
		sql.append("	count(*) as acessos ");
		sql.append(" from ");
		sql.append("	log_Acesso la ");
		sql.append(" where 1=1");

		makeWhere(filtro, sql, params);

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		if(dataInicio != null && dataFim != null) {
			sql.append(" and la.inicio between :dataInicio ");
			params.put("dataInicio", dataInicio);
			sql.append(" and :dataFim ");
			params.put("dataFim", dataFim);
		}

		sql.append(" group by ");
		sql.append("	path, ");
		sql.append("	horaMinuto ");

		sql.append(" order by ");
		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem != null) {
			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";
			sql.append(campoOrdem).append(ordemStr).append(", ");
		}

		sql.append("	horaMinuto desc, tempoTotal desc ");

		Query query = createSQLQuery(sql.toString(), params);

		query.setFirstResult(first);
		query.setMaxResults(pageSize);

		return query.list();
	}

	public List<Object[]> findTempoExecucaoQuery(){

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();
		sql.append(" SELECT ");
		sql.append("	pid, ");
		sql.append("	datname, ");
		sql.append("	EXTRACT(EPOCH FROM (now() - query_start)) as tempo_execucao_seg, ");
		sql.append("	cast(client_addr as varchar), ");
		sql.append("	query ");
		sql.append(" from ");
		sql.append("	pg_stat_activity ");
		sql.append(" where state = 'active'");
		sql.append(" order by ");
		sql.append("	tempo_execucao_seg desc ");

		Query query = createSQLQuery(sql.toString(), params);

		return query.list();
	}

	public List<Object[]> findQuerysEmLock(){

		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();
		sql.append(" SELECT ");
		sql.append(" 	blockead.pid AS blocked_pid, ");
		sql.append("	blockead.usename AS blocked_user, ");
		sql.append("	EXTRACT(EPOCH FROM (now() - blockead.query_start)) as blocked_execucao_seg, ");
		sql.append(" 	blockead.query as blocked_query, ");
		sql.append("	blockinga.pid AS blocking_pid, ");
		sql.append("	blockinga.usename AS blocking_user, ");
		sql.append("	EXTRACT(EPOCH FROM (now() - blockinga.query_start)) as blocking_execucao_seg, ");
		sql.append("	blockinga.query as blocking_query ");
		sql.append(" from ");
		sql.append("	pg_catalog.pg_locks blockedl ");
		sql.append("	JOIN pg_stat_activity blockead ON blockedl.pid = blockead.pid ");
		sql.append("	JOIN pg_catalog.pg_locks blockingl ON (blockingl.transactionid = blockedl.transactionid AND blockedl.pid != blockingl.pid) ");
		sql.append("	JOIN pg_stat_activity blockinga ON blockingl.pid = blockinga.pid ");
		sql.append(" where NOT blockedl.granted AND blockinga.datname=current_database() ");
		sql.append(" order by blockinga.query_start, blockead.query_start ");

		Query query = createSQLQuery(sql.toString(), params);

		return query.list();
	}

	public List<Object[]> findServletPaths(String path, int size) {

		StringBuilder sql = new StringBuilder();

		sql.append(" select regexp_replace(servlet_path,'[[:digit:]]','','g') as sp, ");
		sql.append(" count(*) * 100.0 / ");
		sql.append(" ( ");
		sql.append("	select count(*) ");
		sql.append("	from log_acesso ");
		sql.append("	where servlet_path like '%").append(path).append("%' ");
		sql.append(" ), ");
		sql.append(" count(*) ");
		sql.append(" from log_acesso ");
		sql.append(" where servlet_path like '%").append(path).append("%' ");
		sql.append(" group by sp ");
		if (StringUtils.isNotBlank(path)){
			sql.append(" order by count(*) desc ");
		} else {
			sql.append(" order by sp ");
		}

		Session session = getSession();
		Query query = session.createSQLQuery(sql.toString());

		query.setFirstResult(0);
		query.setMaxResults(size);

		return query.list();
	}

	public Object[] findRegistroTotal() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	count(case when la.ajax = 'true' then 1 end), ");
		hql.append(" 	count (case when la.servletPath like '%Job%' then 1 end), ");
		hql.append(" 	count (case when la.servletPath like '%/%' ");
		hql.append(" 		 and la.servletPath not like '%rest%' then 1 end), ");
		hql.append(" 	count (case when la.servletPath  like '%rest%' then 1 end) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");

		Query query = createQuery(hql);
		return (Object[]) query.uniqueResult();
	}

	public Object[] findTempoMedio() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	avg(case when la.ajax = 'true' then la.tempo end), ");
		hql.append(" 	avg (case when la.servletPath like '%Job%' then la.tempo end), ");
		hql.append(" 	avg (case when la.servletPath like '%/%' ");
		hql.append(" 		 and la.servletPath not like '%rest%' then la.tempo end), ");
		hql.append(" 	avg (case when la.servletPath  like '%rest%' then la.tempo end) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");

		Query query = createQuery(hql);
		return (Object[]) query.uniqueResult();
	}

	public List<Object[]> findMethods() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select la.method, count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where la.status != 0 ");
		hql.append(" group by la.method ");
		hql.append(" order by la.method ");

		Query query = createQuery(hql);
		return query.list();
	}

	public List<Object[]> findStatusByMethod(String method) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select la.status/100, count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where la.status != 0 ");
		hql.append(" and la.method = :method ");
		params.put("method", method);
		hql.append(" group by la.status/100 ");
		hql.append(" order by count(*) desc ");

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<Object[]> findExceptions(int size) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select min(id) as id, ");
		hql.append(" la.exception, ");
		hql.append(" count(*) * 100.0 / ");
		hql.append(" ( ");
		hql.append("	select count(*) ");
		hql.append(" 	from ").append(clazz.getName()).append(" la2 ");
		hql.append(" 	where la2.exception != null ");
		hql.append(" ), ");
		hql.append(" count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where la.exception != null ");
		hql.append(" group by la.exception");
		hql.append(" order by count(*) desc ");

		Query query = createQuery(hql);

		query.setFirstResult(0);
		query.setMaxResults(size);

		return query.list();
	}

	public List<Object[]> findServletPathsByException(String exception) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select la.servletPath, ");
		hql.append(" count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where la.exception = :exception ");
		params.put("exception", exception);
		hql.append(" group by la.servletPath");
		hql.append(" order by count(*) desc ");

		Query query = createQuery(hql,params);

		return query.list();
	}

	public int countPathPorTempo(LogAcessoFiltro filtro) {
		StringBuilder sql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();
		Integer intervalo = filtro.getIntervalo();

		sql.append(" select count(*) from ( ");
		sql.append(" select ");
		sql.append("	servlet_path as path, ");
		sql.append("	to_char(date_trunc('hour', inicio) + cast(date_part('minute', inicio) as int)  / " + intervalo + " * interval '" + intervalo + " min', 'DD/MM HH24:MI') AS horaMinuto, ");
		sql.append("	round(avg(tempo)) as tempoMedio, ");
		sql.append("	round(sum(tempo)) as tempoTotal, ");
		sql.append("	sum(content_size) / 1024 as tamanhoTotal, ");
		sql.append("	count(*) as acessos ");
		sql.append(" from ");
		sql.append("	log_Acesso la ");
		sql.append(" where 1=1");

		makeWhere(filtro, sql, params);

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		if(dataInicio != null && dataFim != null) {
			sql.append(" and la.inicio between :dataInicio ");
			params.put("dataInicio", dataInicio);
			sql.append(" and :dataFim ");
			params.put("dataFim", dataFim);
		}

		sql.append(" group by ");
		sql.append("	path, ");
		sql.append("	horaMinuto ");
		sql.append(" order by ");
		sql.append("	horaMinuto, ");
		sql.append("	tempoTotal desc ");
		sql.append(" ) as count");

		Query query = createSQLQuery(sql.toString(), params);

		return ((BigInteger) query.uniqueResult()).intValue();
	}
	public LogAcesso getLastLogAcesso(Usuario usuario){
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select la ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where la.remoteUser = :usuarioLogin ");
		params.put("usuarioLogin", usuario.getLogin());
		hql.append(" order by la.id desc ");

		Query query = createQuery(hql,params);
		query.setMaxResults(1);

		return (LogAcesso) query.uniqueResult();
	}

	public LogAcesso getLastLogAcessoByData(Usuario usuario, Date data) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select l ").append(" from ").append(clazz.getName()).append(" l ");
		hql.append(" where l.id = ");
		hql.append(" 	(select max(la.id) ");
		hql.append(" 		from ").append(clazz.getName()).append(" la ");
		hql.append(" 			where la.remoteUser = :usuarioLogin ");
		hql.append(" 			and la.inicio < :data )");

		params.put("usuarioLogin", usuario.getLogin());
		params.put("data", data);
		Query query = createQuery(hql,params);

		return (LogAcesso) query.uniqueResult();
	}

	public int getIdExclusao(int numPreservar) {
		Session session = getSession();
		Query query = session.createSQLQuery(" select id from log_acesso where " + getFiltroExcluir() + " order by id desc offset " + numPreservar + " limit 1 ");
		query.setTimeout(GetdocConstants.QUERY_TIMEOUT);
		Object result = query.uniqueResult();
		return result != null ? ((Number) result).intValue() : 0;
	}
}
