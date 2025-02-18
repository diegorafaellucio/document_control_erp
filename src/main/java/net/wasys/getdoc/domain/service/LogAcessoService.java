package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.enumeration.LogAcessoProcessor;
import net.wasys.getdoc.domain.repository.LogAcessoRepository;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.BolsoCache;
import net.wasys.util.servlet.HttpServletRequestLogAcesso;
import net.wasys.util.servlet.HttpServletResponseLogAcesso;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class LogAcessoService {

	public static final int EXCEPTION_SIZE_LIMIT = 30000;
	private static final Pattern PARANS_REST_PATTERN = Pattern.compile("/([\\d_]+)");

	private static ConcurrentLinkedQueue<BolsoCache<Integer>> historicoAcessosMinuto = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<BolsoCache<Long>> historicoTemposMinuto = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<BolsoCache<Integer[]>> historicoDadosMinuto = new ConcurrentLinkedQueue<>();

	@Autowired private LogAcessoRepository logAcessoRepository;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private EmailSmtpService emailSmtpService;

	public LogAcesso get(Long id) {
		return logAcessoRepository.get(id);
	}

	public int countByFiltro(LogAcessoFiltro filtro) {
		return logAcessoRepository.countByFiltro(filtro);
	}

	public List<LogAcesso> findByFiltro(LogAcessoFiltro filtro, int first, int pageSize) {
		return logAcessoRepository.findByFiltro(filtro, first, pageSize);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(LogAcesso logAcesso) {

		String exception = logAcesso.getException();
		if(exception != null && exception.length() > EXCEPTION_SIZE_LIMIT) {
			exception = exception.substring(0, EXCEPTION_SIZE_LIMIT);
			logAcesso.setException(exception);
		}

		try {
			logAcessoRepository.saveOrUpdate(logAcesso);
		}
		catch (Exception e) {
			systraceThread("Erro ao salvar logAcesso: " + DummyUtils.getExceptionMessage(e));
			systraceThread("id: " + logAcesso.getId());
			systraceThread("servletPath: " + logAcesso.getServletPath());
			systraceThread("contentType: " + logAcesso.getContentType());
			systraceThread("headers: " + logAcesso.getHeaders());
			systraceThread("method: " + logAcesso.getMethod());
			systraceThread("server: " + logAcesso.getServer());
			systraceThread("locale: " + logAcesso.getLocale());
			systraceThread("scheme: " + logAcesso.getScheme());
			systraceThread("protocol: " + logAcesso.getProtocol());
			systraceThread("remoteHost: " + logAcesso.getRemoteHost());
			systraceThread("serverName: " + logAcesso.getServerName());
			systraceThread("serverPort: " + logAcesso.getServerPort());
			systraceThread("contextPath: " + logAcesso.getContextPath());
			systraceThread("remoteUser: " + logAcesso.getRemoteUser());
			systraceThread("exception: " + logAcesso.getException());
			systraceThread("forward: " + logAcesso.getForward());
			systraceThread("redirect: " + logAcesso.getRedirect());
			systraceThread("status: " + logAcesso.getStatus());
			systraceThread("inicio: " + logAcesso.getInicio());
			systraceThread("ajax: " + logAcesso.getAjax());
			systraceThread("fim: " + logAcesso.getFim());
			systraceThread("tempo: " + logAcesso.getTempo());
			systraceThread("userAgent: " + logAcesso.getUserAgent());
			systraceThread("contentSize: " + logAcesso.getContentSize());
			systraceThread("contentLength: " + logAcesso.getContentLength());
			systraceThread("parameters: " + logAcesso.getParameters());
			e.printStackTrace();
		}
	}

	public void saveOrUpdateNewSession(LogAcesso log) {

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			DummyUtils.mudarNomeThread("thd-log-acesso-saveorupdate");
			LogAcessoService logAcessoService = applicationContext.getBean(LogAcessoService.class);
			logAcessoService.saveOrUpdate(log);
		});

		Thread ct = Thread.currentThread();
		StackTraceElement[] stArray = ct.getStackTrace();
		StackTraceElement st = null;
		for (int i = 2; i < stArray.length; i++) {
			st = stArray[i];
			String className = st.getClassName();
			if(!className.contains("$") && className.contains("getdoc")) {
				break;
			}
		}
		StackTraceElement st2 = st;
		tw.setExceptionHandler((e, stackTrace) -> {
			String message = DummyUtils.getExceptionMessage(e);
			String methodName2 = st2.toString();
			systraceThread("erro ao salvar logAcesso em " + methodName2 + ": " + message, LogLevel.ERROR);
		});

		tw.runNewThread();
	}

	public LogAcesso criaLogJob(long inicio, LogAcessoJob job) {

		final LogAcesso log = new LogAcesso();
		String server = DummyUtils.getServer();
		log.setServer(server);
		log.setServerName(server);
		log.setServletPath(job.getKey());

		Date dataInicio = new Date(inicio);
		log.setInicio(dataInicio);

		saveOrUpdateNewSession(log);

		LogAcessoFilter.setLogAcesso(log);

		return log;
	}

	public LogAcesso criaLogProcessor(long inicio, LogAcessoProcessor exporter, Object filtro, Usuario usuario) {

		final LogAcesso log = new LogAcesso();
		String server = DummyUtils.getServer();
		log.setServer(server);
		log.setServerName(server);
		log.setServletPath(exporter.getKey());

		if (usuario != null) {
			log.setRemoteUser(usuario.getLogin());
		}

		if (filtro != null) {

			try {

				String filtroStr = filtro.toString(); // TODO trocar para objeto json. o DummyUtils.objectToJson dá pau por causa do hibernate

				filtroStr = isNotBlank(filtroStr) && filtroStr.length() > 13000 ? filtroStr.substring(0, 13000) : filtroStr;
				log.setParameters(filtroStr);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		Date dataInicio = new Date(inicio);
		log.setInicio(dataInicio);

		saveOrUpdateNewSession(log);

		return log;
	}

	public LogAcesso criaLogAcesso(HttpServletRequest request) {

		int timeout = 1000 * 60;
		historicoAcessosMinuto.add(new BolsoCache<>(null, timeout, (bc) -> historicoAcessosMinuto.remove(bc)));

		LogAcesso log = new LogAcesso();

		long inicio = System.currentTimeMillis();
		Date dataInicio = new Date(inicio);
		log.setInicio(dataInicio);

		int contentLength = request.getContentLength();
		log.setContentLength(contentLength);

		boolean logarAcesso = DummyUtils.getLogarAcesso();
		String servletPath = request.getServletPath();

		String server = DummyUtils.getServer();
		log.setServer(server);

		boolean ajax = request.getHeader("X-Requested-With") != null;
		log.setAjax(ajax);

		Locale locale = request.getLocale();
		String localeStr = locale != null ? locale.toString() : null;
		log.setLocale(localeStr);

		String method = request.getMethod();
		log.setMethod(method);

		String userAgent = request.getHeader("User-Agent");
		log.setUserAgent(userAgent);

		String protocol = request.getProtocol();
		log.setProtocol(protocol);

		String remoteHost = request.getRemoteHost();
		log.setRemoteHost(remoteHost);

		String scheme = request.getScheme();
		log.setScheme(scheme);

		String serverName = request.getServerName();
		log.setServerName(serverName);

		int serverPort = request.getServerPort();
		log.setServerPort(serverPort);

		String contextPath = request.getContextPath();
		log.setContextPath(contextPath);

		Enumeration<String> headerNames = request.getHeaderNames();
		Map<String, String> headersMap = new LinkedHashMap<>();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String value = request.getHeader(name);
			if(StringUtils.isNotBlank(value)) {
				value = value.replace("\"", "'");
			}
			headersMap.put(name, value);
		}
		String headers = DummyUtils.objectToJson(headersMap);
		log.setHeaders(headers);

		String requestURI = request.getRequestURI();
		servletPath = requestURI.replace(contextPath, "");

		Map<String, String> paramsMap = new LinkedHashMap();
		Matcher matcher = PARANS_REST_PATTERN.matcher(servletPath);
		int i = 0;
		while(matcher.find()) {
			String param = matcher.group(1);
			paramsMap.put("param" + i++, param);
		}

		for (i = 0; i < paramsMap.size(); i++) {
			servletPath = servletPath.replaceFirst(PARANS_REST_PATTERN.pattern(), "/{param" + i + "}");
		}

		if(!paramsMap.isEmpty()) {
			String parameters = DummyUtils.objectToJson(paramsMap);
			log.setParameters(parameters);
		}
		log.setServletPath(servletPath);

		Thread thread = Thread.currentThread();
		int identityHashCode = System.identityHashCode(thread);
		String threadName = "thdreq-" + servletPath + "-" + identityHashCode;
		log.setThreadName(threadName);

		if(logarAcesso || "/rest".equals(servletPath)) {
			saveOrUpdateNewSession(log);
		}

		threadName = threadName + "-LA" + log.getId();
		log.setThreadName(threadName);

		return log;
	}

	public void finalizaLogAcesso(LogAcesso log, HttpServletRequestLogAcesso request, HttpServletResponseLogAcesso response) {

		String remoteUser = request.getRemoteUser();
		log.setRemoteUser(remoteUser);

		int contentSize = response.getSizeCount();
		log.setContentSize(contentSize);

		String contentType = response.getContentType();
		log.setContentType(contentType);

		int status = response.getStatus();
		log.setStatus(status);

		String forward = request.getForward();
		log.setForward(forward);

		String redirect = response.getRedirect();
		log.setRedirect(redirect);

		String servletPath = request.getServletPath();
		String method = log.getMethod();
		String headers = log.getHeaders();
		if("/rest".equals(servletPath)
				&& ("POST".equals(method) || "PUT".equals(method) || "OPTIONS".equals(method))
				&& !StringUtils.contains(headers, "multipart")//pra não salvar imagens
		) {
			byte[] contentAsByteArray = request.getContentAsByteArray();
			try {
				String body = new String(contentAsByteArray, "UTF-8");

				String parameters = log.getParameters();
				if(StringUtils.isNotBlank(parameters)) {
					Map<Object, Object> paramsMap = (Map<Object, Object>) DummyUtils.jsonStringToMap(parameters);
					paramsMap = paramsMap != null ? paramsMap : new LinkedHashMap<>();
					Map<?, ?> bodyMap = DummyUtils.jsonStringToMap(body);
					if(bodyMap != null) {
						paramsMap.put("body", bodyMap);
					} else {
						paramsMap.put("body", body);
					}
					String json = DummyUtils.objectToJson(paramsMap);
					log.setParameters(json);
				}
				else {
					log.setParameters(body);
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		else {
			Enumeration<String> parameterNames = request.getParameterNames();
			Map<String, String> parametersMap = new LinkedHashMap<>();
			while (parameterNames.hasMoreElements()) {
				String name = parameterNames.nextElement();
				String value = request.getParameter(name);
				if(StringUtils.isNotBlank(value)) {
					parametersMap.put(name, value);
				}
			}
			if(!parametersMap.isEmpty()) {
				String parameters = log.getParameters();
				Map<Object, Object> paramsMap = (Map<Object, Object>) DummyUtils.jsonStringToMap(parameters);
				paramsMap = paramsMap != null ? paramsMap : new LinkedHashMap<>();
				paramsMap.putAll(parametersMap);
				String json = DummyUtils.objectToJson(paramsMap);
				log.setParameters(json);
			}
		}

		long fim = System.currentTimeMillis();
		Date dataFim = new Date(fim);
		log.setFim(dataFim);

		Date inicio = log.getInicio();
		long tempo = fim - inicio.getTime();
		log.setTempo(tempo);

		boolean logarAcesso = DummyUtils.getLogarAcesso();
		String exception = log.getException();
		if(logarAcesso || "/rest".equals(servletPath) || StringUtils.isNotBlank(exception)) {
			saveOrUpdateNewSession(log);
		}

		int timeout = 1000 * 60;
		historicoTemposMinuto.add(new BolsoCache<>(tempo, timeout, (bc) -> historicoTemposMinuto.remove(bc)));

		Integer contentSizeDown = log.getContentSize();
		Integer contentLengthUp = log.getContentLength();
		historicoDadosMinuto.add(new BolsoCache<>(new Integer[] {contentLengthUp, contentSizeDown}, timeout, (bc) -> historicoDadosMinuto.remove(bc)));
	}

	public BigDecimal getTemposMinuto() {

		BigDecimal total = new BigDecimal(0);

		for (BolsoCache<Long> bolso : historicoTemposMinuto) {
			if (bolso.isExpirado()) {
				historicoTemposMinuto.remove(bolso);
				continue;
			}

			Long tempo = bolso.getObjeto();
			total = total.add(new BigDecimal(tempo));
		}
		int size = historicoTemposMinuto.size();
		if (size == 0) {
			return new BigDecimal(-1);
		}

		BigDecimal media = total.divide(new BigDecimal(size), 3, RoundingMode.CEILING);
		return media;
	}

	public BigDecimal[] getDadosMinuto() {

		BigDecimal totalUp = new BigDecimal(0);
		BigDecimal totalDown = new BigDecimal(0);

		for (BolsoCache<Integer[]> bolso : historicoDadosMinuto) {
			if (bolso.isExpirado()) {
				historicoDadosMinuto.remove(bolso);
				continue;
			}

			Integer[] objeto = bolso.getObjeto();
			Integer up = objeto[0];
			Integer down = objeto[1];
			if(up > 0) {
				totalUp = totalUp.add(new BigDecimal(up));
			}
			if(down > 0) {
				totalDown = totalDown.add(new BigDecimal(down));
			}
		}

		BigDecimal bd1024 = new BigDecimal(1024);
		BigDecimal totalUpKB = totalUp.divide(bd1024, 3, RoundingMode.CEILING);
		BigDecimal totalUpMB = totalUpKB.divide(bd1024, 3, RoundingMode.CEILING);
		BigDecimal totalDownKB = totalDown.divide(bd1024, 3, RoundingMode.CEILING);
		BigDecimal totalDownMB = totalDownKB.divide(bd1024, 3, RoundingMode.CEILING);

		return new BigDecimal[] { totalUpMB, totalDownMB };
	}

	public int getAcessosMinuto() {

		for (BolsoCache<Integer> bolso : historicoAcessosMinuto) {
			if (bolso.isExpirado()) {
				historicoAcessosMinuto.remove(bolso);
				continue;
			}
		}

		return historicoAcessosMinuto.size();
	}

	@Transactional(rollbackFor=Exception.class)
	public int limpaLogAcesso() throws Exception {

		int numPreservar = 5000000;
		LogAcesso log = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(log, "numPreservar", numPreservar);

		int idInicio = getIdExclusao(numPreservar);
		DummyUtils.addParameter(log, "idExclusao", idInicio);

		if(idInicio == 0) {
			systraceThread("LimpaLogAcessoJob, não vai deletar nenhum log, total é menor que o número preservado: " + numPreservar + ".");
			return 0;
		} else {
			systraceThread("LimpaLogAcessoJob, idExclusao: " + idInicio + ".");
		}

		AtomicInteger deletados = new AtomicInteger();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			LogAcessoService logAcessoService = applicationContext.getBean(LogAcessoService.class);
			int i = logAcessoService.excluirAnteriorA(idInicio);
			deletados.set(i);
		});
		tw.startThread();
		tw.join();

		if(tw.getException() != null) {
			throw tw.getException();
		}

		DummyUtils.addParameter(log, "deletados", deletados);
		systraceThread("" + deletados + " logs de acesso deletados.");
		return deletados.get();
	}

	@Transactional(rollbackFor=Exception.class)
	public int excluirAnteriorA(int idInicio) {
		return logAcessoRepository.excluirAnteriorA(idInicio);
	}

	public List<LogAcesso> findLastsJobs(List<LogAcessoJob> jobs, String server) {
		return logAcessoRepository.findLastsJobs(jobs, server);
	}

	public Map<String, String> findServletPaths(String path, int size){

		Map<String, String> map = new LinkedHashMap<>();
		DecimalFormat formater = new DecimalFormat("##0.00");
		List<Object[]> list =  logAcessoRepository.findServletPaths(path, size);

		for (Object[] objects : list) {
			String servletPath = (String) objects[0];
			BigDecimal rate = (BigDecimal) objects[1];
			BigInteger count = (BigInteger) objects[2];
			String qtd = count + " (" + formater.format(rate) +"%)";
			map.put(servletPath, qtd);
		}
		return map;
	}

	public List<RequisicaoHttpVO> findRequisicoesHttp(){

		List<Object[]> list = logAcessoRepository.findMethods();
		List<RequisicaoHttpVO> result = new ArrayList<>();

		for (Object[] items : list) {

			String method = (String) items[0];
			Long methodQtd = (Long) items[1];
			Map<String, Long> map = new LinkedHashMap<>();

			List<Object[]> list2 = logAcessoRepository.findStatusByMethod(method);
			for (Object[] items2 : list2) {

				int status = (Integer) items2[0];
				long statusQtd = (Long) items2[1];
				String respostaHttp = "";

				switch (status) {
					case 1:
						respostaHttp = "Respostas Informativas (1xx)";
						break;
					case 2:
						respostaHttp = "Respostas de Sucesso (2xx)";
						break;
					case 3:
						respostaHttp = "Mensagens de Redirecionamento (3xx)";
						break;
					case 4:
						respostaHttp = "Respostas de Erro do Cliente (4xx)";
						break;
					case 5:
						respostaHttp = "Respostas de Erro do Servidor (5xx)";
						break;
				}
				map.put(respostaHttp, statusQtd);
			}


			RequisicaoHttpVO vo = new RequisicaoHttpVO();
			vo.setMethod(method);
			vo.setQuantidade(methodQtd);
			vo.setRespostasHttp(map);

			result.add(vo);

		}
		return  result;
	}

	public List<ExceptionVO> findExceptions(int size){

		List<ExceptionVO> result = new ArrayList<>();
		List<Object[]> list =  logAcessoRepository.findExceptions(size);
		DecimalFormat formater = new DecimalFormat("##0.00");

		for (Object[] items : list) {
			Long id = (Long) items[0];
			String nome = (String) items[1];
			Double rate = (Double) items[2];
			Long count = (Long) items[3];

			String qtd = count + " (" + formater.format(rate) +"%)";

			List<Object[]> list2 =  logAcessoRepository.findServletPathsByException(nome);
			List<String> servletPaths =  new ArrayList<>();
			for (Object[] items2 : list2) {
				String nomeServletPath = (String) items2[0];
				Long countServletPath = (Long) items2[1];
				servletPaths.add(nomeServletPath + " - " + countServletPath );
			}

			ExceptionVO vo = new ExceptionVO();
			vo.setId(id);
			vo.setNome(nome);
			vo.setQuantidade(qtd);
			vo.setServletPaths(servletPaths);

			result.add(vo);
		}
		return result;
	}

	public List<LogTimeExecutionQueryVO> findTempoExecucaoQuery(){

		List<Object[]> timeExecutionQuery = logAcessoRepository.findTempoExecucaoQuery();
		List<LogTimeExecutionQueryVO> logTimeExecutionQueryVOS = new ArrayList<>();

		for (Object[] objects : timeExecutionQuery) {

			Integer pid = (Integer) objects[0];
			String datname = (String) objects[1];
			BigDecimal tempoExecusaoSeg = BigDecimal.valueOf((Double) objects[2]);
			String clientAddr = (String) objects[3];
			String query = (String) objects[4];

			LogTimeExecutionQueryVO logTimeExecutionQueryVO = new LogTimeExecutionQueryVO(pid, datname, tempoExecusaoSeg, clientAddr, query);

			logTimeExecutionQueryVOS.add(logTimeExecutionQueryVO);

		}

		return logTimeExecutionQueryVOS;
	}

	public List<QueryOnLockVO> findQuerysEmLock() {

		List<Object[]> queryOnLockQuery = logAcessoRepository.findQuerysEmLock();
		List<QueryOnLockVO> queryOnLockVOS = new ArrayList<>();

		for (Object[] objects : queryOnLockQuery) {

			Integer blockedPid = (Integer) objects[0];
			String blockedUser = (String) objects[1];
			BigDecimal blockedExecusaoSeg = BigDecimal.valueOf((Double) objects[2]);
			String blockedQuery = (String) objects[3];
			Integer blockingPid = (Integer) objects[4];
			String blockingUser = (String) objects[5];
			BigDecimal blockingExecusaoSeg = BigDecimal.valueOf((Double) objects[6]);
			String blockingQuery = (String) objects[7];

			QueryOnLockVO queryOnLockVO = new QueryOnLockVO(blockedPid, blockedUser, blockedExecusaoSeg, blockedQuery, blockingPid, blockingUser, blockingExecusaoSeg, blockingQuery);

			queryOnLockVOS.add(queryOnLockVO);
		}

		return queryOnLockVOS;
	}

	public Map<String, Map<String, Long>> findInfoTipoRegistro(){

		Map<String, Map<String, Long>> map = new HashMap<>();
		Map<String, Long> map2 = new HashMap<>();

		Object[] items = logAcessoRepository.findRegistroTotal();

		Long ajax = (Long) items[0];
		Long job = (Long) items[1];
		Long normal = (Long) items[2];
		Long rest = (Long) items[3];
		map2.put("ajax", ajax);
		map2.put("job", job);
		map2.put("normal", normal);
		map2.put("rest", rest);
		map.put("registro total", map2);

		items = logAcessoRepository.findTempoMedio();
		map2 = new HashMap<>();

		ajax = Math.round((Double) items[0]);
		job = Math.round((Double) items[1]);
		normal = Math.round((Double) items[2]);
		rest = Math.round((Double) items[3]);
		map2.put("ajax", ajax);
		map2.put("job", job);
		map2.put("normal", normal);
		map2.put("rest", rest);
		map.put("tempo medio", map2);

		return map;
	}

	public List<LogPorTempoVO> findPathPorTempo(LogAcessoFiltro filtro, int first, int pageSize){

		List<Object[]> pathForTime = logAcessoRepository.findPathPorTempo(filtro, first, pageSize);

		List<LogPorTempoVO> logPorTempoVOList = new ArrayList<>();

		for (Object[] objects : pathForTime) {

			String path = (String) objects[0];
			String horaMin = (String) objects[1];
			Long tempoMedio = objects[2] == null ? null : ((BigDecimal) objects[2]).longValue();
			Long tempoTotal = objects[3] == null ? null : ((BigDecimal) objects[3]).longValue();
			Long tamanhoTotal = objects[4] == null ? null : ((BigInteger) objects[4]).longValue();
			Long acessos =  objects[5] == null ? null : ((BigInteger) objects[5]).longValue();

			LogPorTempoVO logPorTempoVO = new LogPorTempoVO(path, horaMin, tempoMedio, tempoTotal, tamanhoTotal, acessos);

			logPorTempoVOList.add(logPorTempoVO);
		}

		return logPorTempoVOList;
	}

	public int countPathPorTime(LogAcessoFiltro filtro) {
		return logAcessoRepository.countPathPorTempo(filtro);
	}

	public String getBodyExemplo(Long logAcessoId){

		String nomeTemplate = "log-acesso-exemplo.htm";

		LogAcesso logAcesso = get(logAcessoId);
		String userAgent = DummyUtils.stringToHTML(logAcesso.getUserAgent());
		String parameters = DummyUtils.toJsonFormat(logAcesso.getParameters());
		String headers = DummyUtils.toJsonFormat(logAcesso.getHeaders());
		String erro = logAcesso.getException() != null ? logAcesso.getException() : "";
		String inicio = DummyUtils.formatDateTime2(logAcesso.getInicio());
		String fim = DummyUtils.formatDateTime2(logAcesso.getFim());
		String servletPath = logAcesso.getServletPath();
		String ajax = logAcesso.getAjax() ? "Sim" : "Não";
		Integer status = logAcesso.getStatus();
		String remoteUser = logAcesso.getRemoteUser();
		String method = logAcesso.getMethod();
		Long tempo = logAcesso.getTempo();
		String up = DummyUtils.toFileSize(Long.valueOf(logAcesso.getContentLength()));
		String down = DummyUtils.toFileSize(Long.valueOf(logAcesso.getContentSize()));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("id", logAcessoId);
		model.put("userAgent", userAgent);
		model.put("parameters", parameters);
		model.put("headers", headers);
		model.put("erro", erro);
		model.put("inicio", inicio);
		model.put("fim", fim);
		model.put("ajax", ajax);
		model.put("status", status);
		model.put("servletPath", servletPath);
		model.put("remoteUser", remoteUser);
		model.put("method", method);
		model.put("tempo", tempo);
		model.put("up", up);
		model.put("down", down);

		Map<String, File> attachments = new HashMap<>();

		return emailSmtpService.getBody(nomeTemplate, model, attachments);
	}

	public LogAcesso getLastLogAcesso(Usuario usuario) {
		return logAcessoRepository.getLastLogAcesso(usuario);
	}

	public LogAcesso getLastLogAcessoByData(Usuario usuario, Date data) {
		return logAcessoRepository.getLastLogAcessoByData(usuario, data);
	}

	public int getIdExclusao(int numPreservar) {
		return logAcessoRepository.getIdExclusao(numPreservar);
	}
}
