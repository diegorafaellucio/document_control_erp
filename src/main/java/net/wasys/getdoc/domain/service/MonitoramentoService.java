package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import net.wasys.getdoc.bean.ConfiguracoesJobsBean;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.vo.StatusWSVO;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.util.DummyUtils;
import net.wasys.util.process.ProcessUtils;
import net.wasys.util.rest.dto.MonitoramentoDTO;
import net.wasys.util.rest.dto.MonitoramentoDTO.LogJobDTO;
import net.wasys.util.rest.dto.MonitoramentoDTO.LogWsDTO;
import net.wasys.util.rest.dto.MonitoramentoDTO.ParametroDTO;

@Service
public class MonitoramentoService {

	private static String currentPostgresVersion;

	@Autowired private ProcessoService processoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ResourceService resourceService;
	@Autowired private LoginLogService loginLogService;
	@Autowired private ParametroService parametroService;
	@Autowired private ConsultaExternaService consultaExternaService;

	public Map<String, BigDecimal> getParametros(ServletContext servletContext) {

		int countTotal = 0;
		Map<String, Integer> usuarioCount = Usuario.getCountMap(servletContext);
		Map<String, BigDecimal> parametros = new LinkedHashMap<>();
		Set<String> keySet = usuarioCount.keySet();
		for(String key : keySet) {
			int countRole = usuarioCount.get(key);
			if(!StringUtils.equals(RoleGD.GD_API.name(), key)) {
				countTotal += countRole;
			}
			parametros.put("Usuários " + key + " logados", new BigDecimal(countRole));
		}

		parametros.put("Usuários logados", new BigDecimal(countTotal));

		int ativosHoje = loginLogService.getAtivos(new Date(), new Date());
		parametros.put("Usuários Ativos Hoje", new BigDecimal(ativosHoje));

		int mediaRequisicoes = logAcessoService.getAcessosMinuto();
		parametros.put("Requisições Minuto", new BigDecimal(mediaRequisicoes));

		BigDecimal temposMinuto = logAcessoService.getTemposMinuto();
		parametros.put("Média Tempo Requisição (ms)", temposMinuto);

		BigDecimal[] dadosMinuto = logAcessoService.getDadosMinuto();
		BigDecimal dadosUp = dadosMinuto[0];
		BigDecimal dadosDown = dadosMinuto[1];
		parametros.put("Tráfego Upload (MB)", dadosUp);
		parametros.put("Tráfego Download (MB)", dadosDown);

		long inicio = System.currentTimeMillis();
		processoService.testeCemProcessos();
		long fim = System.currentTimeMillis();
		parametros.put("Tempo 100 Processos (ms)", new BigDecimal(fim - inicio));

		Map<String, BigDecimal> parametrosFilesystem = getParametrosDisco();
		parametros.putAll(parametrosFilesystem);

		Map<String, BigDecimal> parametrosMaiorTempoQuery = getParametrosMaiorTempoQuery();
		parametros.putAll(parametrosMaiorTempoQuery);

		Map<String, BigDecimal> parametrosNumeroConexoesBanco = getParametrosNumeroConexoesBanco();
		parametros.putAll(parametrosNumeroConexoesBanco);

		Map<String, BigDecimal> parametrosMemoriaJvm = getParametrosMemoriaJvm();
		parametros.putAll(parametrosMemoriaJvm);

		try {
			BigDecimal cpuUsage = ProcessUtils.getCpuUsage();
			parametros.put("Uso CPU (%)", cpuUsage);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, BigDecimal> parametrosThreads = getParametrosThreads();
		parametros.putAll(parametrosThreads);

		try {
			BigDecimal load = ProcessUtils.getLoad();
			parametros.put("Load average", load);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return parametros;
	}

	private Map<String, BigDecimal> getParametrosThreads() {

		Map<String, BigDecimal> parametrosThreads = new LinkedHashMap<>();

		for (Thread.State state : Thread.State.values()) {
			parametrosThreads.put("Threads " + state.name(), new BigDecimal(0));
		}

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		long[] allThreadIds = threadMXBean.getAllThreadIds();
		ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(allThreadIds);
		if(threadInfo != null) {
			for (ThreadInfo ti : threadInfo) {
				if(ti != null) {
					Thread.State threadState = ti.getThreadState();
					String key = "Threads " + threadState.name();
					BigDecimal count = parametrosThreads.get(key);
					count = count.add(new BigDecimal(1));
					parametrosThreads.put(key, count);
				}
			}
			parametrosThreads.put("Threads", new BigDecimal(threadInfo.length));
		}

		return parametrosThreads;
	}

	private Map<String, BigDecimal> getParametrosDisco() {

		Map<String, BigDecimal> map = new LinkedHashMap<>();
		Map<String, Long> map1 = null;

		if("dev".equals(DummyUtils.getMode())) {
			map1 = new HashMap<>();
			map1.put("C:", 90L);
		} else {
			map1 = ProcessUtils.getEspacoUtilizadoDiscoPercent(null);
		}

		for (Map.Entry<String, Long> entry : map1.entrySet()) {
			Long value1 = entry.getValue();

			BigDecimal value2 = new BigDecimal(value1);
			String key = entry.getKey();
			key = key.replaceAll("\\\\$", "");
			map.put("Espaço utilizado em disco " + key + " (%)", value2);
		}

		return map;
	}

	public MonitoramentoDTO getDTO(ServletContext servletContext) {

		Map<String, BigDecimal> parametros = getParametros(servletContext);
		List<ParametroDTO> parametros2 = new ArrayList<>();
		for (String nome : parametros.keySet()) {
			BigDecimal valor = parametros.get(nome);
			ParametroDTO parametroDTO = new ParametroDTO();
			parametroDTO.setNome(nome);
			parametroDTO.setValor(valor);
			parametros2.add(parametroDTO);
		}
		ParametroDTO[] parametrosArray = new ParametroDTO[parametros.size()];
		parametros2.toArray(parametrosArray);
		MonitoramentoDTO dto = new MonitoramentoDTO();
		dto.setParametros(parametrosArray);

		//TODO criar um rest no monitorador especifico pra isso
//		List<LogJobDTO> logsJobs = findLastsJobs();
//		LogJobDTO[] logsJobsArray = new LogJobDTO[logsJobs.size()];
//		logsJobs.toArray(logsJobsArray);
//		dto.setLogsJobs(logsJobs);

		//TODO criar um rest no monitorador especifico pra isso
//		List<LogWsDTO> logsWSs = findLastsWSs();
//		LogWsDTO[] logsWSsArray = new LogWsDTO[logsWSs.size()];
//		logsWSs.toArray(logsWSsArray);
//		dto.setLogsWs(logsWSs);

		return dto;
	}

	public List<LogWsDTO> findLastsWSs() {
		List<StatusWSVO> wsStatus = consultaExternaService.findWsStatus();
		List<LogWsDTO> list = new ArrayList<>();
		for (StatusWSVO status : wsStatus) {
			String tipo = status.getTipo();
			String mensagemErro = status.getMensagemErro();
			boolean sucesso = StringUtils.isBlank(mensagemErro);
			LogWsDTO vo = new LogWsDTO();
			vo.setNome(tipo);
			vo.setSucesso(sucesso);
			list.add(vo);
		}
		return list;
	}

	public List<LogJobDTO> findLastsJobs() {

		LogAcessoJob[] jobs = LogAcessoJob.values();
		List<LogAcessoJob> jobs2 = Arrays.asList(jobs);
		String server = DummyUtils.getServer();
		List<LogAcesso> lastsJobs = logAcessoService.findLastsJobs(null, server);
		List<LogJobDTO> logsJobs = new ArrayList<>();
		for (LogAcesso logAcesso : lastsJobs) {

			String nome = logAcesso.getServletPath();
			Date inicio = logAcesso.getInicio();
			Date dataInicio = new Date(inicio.getTime() + 50);//50ms de margem de erro
			String exception = logAcesso.getException();
			boolean sucesso = StringUtils.isEmpty(exception);

			LogJobDTO logJobDTO = new LogJobDTO();
			logJobDTO.setNome(nome);
			logJobDTO.setDataUltimaExecucao(dataInicio);
			logJobDTO.setSucesso(sucesso);

			logsJobs.add(logJobDTO);
		}
		return logsJobs;
	}

	@SuppressWarnings("unchecked")
	private Map<String, BigDecimal> getParametrosMaiorTempoQuery() {

		Session session = sessionFactory.getCurrentSession();

		StringBuilder sql = new StringBuilder();

		if(currentPostgresVersion == null) {
			SQLQuery query = session.createSQLQuery(" select version() ");
			currentPostgresVersion = (String) query.uniqueResult();
		}

		if(currentPostgresVersion.matches(".* 9\\.(1|2|3).*")) {
			sql.append(" SELECT max(EXTRACT(EPOCH FROM (now() - query_start))) as tempo_execucao_seg ");
			sql.append(" FROM pg_stat_activity ");
			sql.append(" WHERE current_query <> '<IDLE>' and current_query <> '<IDLE> in transaction' "); 
		} else {
			sql.append(" SELECT max(EXTRACT(EPOCH FROM (now() - query_start))) as tempo_execucao_seg ");
			sql.append(" FROM pg_stat_activity ");
			sql.append(" WHERE state = 'active' "); 
		}
		SQLQuery query = session.createSQLQuery(sql.toString());

		Map<String, BigDecimal> map = new LinkedHashMap<>();
		List<Double> list = query.list();
		for (Double tempoExecucaoSeg : list) {
			BigDecimal tempoExecucaoSegBD = new BigDecimal(tempoExecucaoSeg);
			tempoExecucaoSegBD = tempoExecucaoSegBD.setScale(3, RoundingMode.CEILING);
			tempoExecucaoSegBD = tempoExecucaoSegBD.compareTo(new BigDecimal(0)) < 0 ? new BigDecimal(0) : tempoExecucaoSegBD;
			map.put("Maior Tempo Query", tempoExecucaoSegBD);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private Map<String, BigDecimal> getParametrosNumeroConexoesBanco() {

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT count(*) ");
		sql.append(" FROM pg_stat_activity ");

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql.toString());

		Map<String, BigDecimal> map = new LinkedHashMap<>();
		List<BigInteger> list = query.list();
		BigDecimal countTotal = new BigDecimal(0);
		for (BigInteger count : list) {
			BigDecimal countBD = new BigDecimal(count);
			countTotal = countTotal.add(countBD);
			map.put("Número de Conexões", countTotal);
		}

		return map;
	}

	private Map<String, BigDecimal> getParametrosMemoriaJvm() {

		BigDecimal bd1024 = new BigDecimal(1024);
		Map<String, BigDecimal> map = new LinkedHashMap<>();

		long totalBytes = Runtime.getRuntime().totalMemory();
		BigDecimal totalKilobytes = new BigDecimal(totalBytes).divide(bd1024, 3, RoundingMode.CEILING);
		BigDecimal totalMegabytes = totalKilobytes.divide(bd1024, 3, RoundingMode.CEILING);
		totalMegabytes = totalMegabytes.setScale(3, RoundingMode.CEILING);
		map.put("Memória Total JVM (MB)", totalMegabytes);

		long utilizadaBytes = totalBytes - Runtime.getRuntime().freeMemory();
		BigDecimal utilizadaKilobytes = new BigDecimal(utilizadaBytes).divide(bd1024, 3, RoundingMode.CEILING);
		BigDecimal utilizadaMegabytes = utilizadaKilobytes.divide(bd1024, 3, RoundingMode.CEILING);
		utilizadaMegabytes = utilizadaMegabytes.setScale(3, RoundingMode.CEILING);
		map.put("Memória Utilizada JVM (MB)", utilizadaMegabytes);

		long maxBytes = Runtime.getRuntime().maxMemory();
		double percentUtilizada = ((double) utilizadaBytes / (double) maxBytes) * 100;
		BigDecimal percentUtilizadaBD = new BigDecimal(percentUtilizada);
		percentUtilizadaBD = percentUtilizadaBD.setScale(2, RoundingMode.CEILING);
		map.put("Memória Utilizada JVM (%)", percentUtilizadaBD);

		return map;
	}

	public MonitoramentoDTO getDTOJobs(ServletContext servletContext) {

		List<LogAcessoJob> jobsAtivos = parametroService.getJobsAtivos();
		if(jobsAtivos.isEmpty()) {
			return new MonitoramentoDTO();
		}

		List<LogJobDTO> logsJobs = new ArrayList<>();
		for (LogAcessoJob job : jobsAtivos) {

			String key = job.getKey();
			Class<? extends TransactionWrapperJob> jobClass = ConfiguracoesJobsBean.getJobClass(key);
			String cron = ConfiguracoesJobsBean.getScheduledCron(jobClass);
			if(StringUtils.isBlank(cron)) {
				continue;
			}

			LogAcessoFiltro filtro = new LogAcessoFiltro();
			String server = DummyUtils.getServer();
			filtro.setServer(server);
			filtro.setServletPath(key);
			List<LogAcesso> list = logAcessoService.findByFiltro(filtro, 0, 1);
			LogAcesso la = list.isEmpty() ? null : list.get(0);

			Date inicio = null;
			Boolean sucesso = null;
			if(la != null) {
				inicio = la.getInicio();
				String exception = la.getException();
				sucesso = StringUtils.isEmpty(exception);
			}
			else {
				inicio = ConfiguracoesJobsBean.getUltimaDataFromCron(cron);
			}

			LogJobDTO logJobDTO = new LogJobDTO();
			logJobDTO.setNome(key);
			logJobDTO.setDataUltimaExecucao(inicio);
			logJobDTO.setSucesso(sucesso);

			logsJobs.add(logJobDTO);
		}

		MonitoramentoDTO dto = new MonitoramentoDTO();
		dto.setLogsJobs(logsJobs);
		return dto;
	}
}
