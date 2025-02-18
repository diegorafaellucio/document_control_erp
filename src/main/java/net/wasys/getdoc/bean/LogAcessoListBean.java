package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.LogAcessoDataModel;
import net.wasys.getdoc.bean.datamodel.PathPorTempoDataModel;
import net.wasys.getdoc.bean.datamodel.WSPorTempoDataModel;
import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.MonitoramentoService;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.CustomOpenSessionInViewFilter;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.Bolso;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.shaded.json.JSONObject;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.ServletContext;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@ManagedBean
@ViewScoped
public class LogAcessoListBean extends AbstractBean {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private MonitoramentoService monitoramentoService;

	private LogAcessoDataModel logAcessoDataModel;
	private PathPorTempoDataModel pathPorTempoDataModel;
	private WSPorTempoDataModel wsPorTempoDataModel;
	private LogAcessoFiltro filtro = new LogAcessoFiltro();

	private Map<String, String> servletPathsJob;
	private Map<String, String> servletPathsNormal;
	private Map<String, String> jvmParams;
	private List<RequisicaoHttpVO> requisicoesHttpVO;
	private List<ExceptionVO> exceptionVO;
	private Map<String, Map<String, Long>> infoTipoRegistro;
	private String monitoramentoErro;
	private Map<String, BigDecimal> monitoramentoUsuarios;
	private Map<String, BigDecimal> monitoramentoGeral;
	private List<LogTimeExecutionQueryVO> logTimeExecutionQueryVOS;
	private List<QueryOnLockVO> queryOnLockVOS;
	private String query;
	private Integer tabSelected = 0;
	private String lastHoraMinuto;
	private List<StatusWSVO> statusWsVos = new ArrayList<>();
	private List<Map<String, String>> conexoesAbertas;

	protected void initBean() {

		Date dataInicio = DateUtils.getFirstTimeOfDay(new Date());
		Date dataFim = DateUtils.getLastTimeOfDay(new Date());
		filtro.setDataInicio(dataInicio);
		filtro.setDataFim(dataFim);
	}

	public void buscar() {
		filtro = getFiltro();
		filtro.setOrdenar(null, null);

		if(tabSelected == 0) {
			carregarLogAcessos();
		} else if(tabSelected == 1) {
			carregarInfoLogs();
		} else if (tabSelected == 2) {
			carregarMonitoramento();
		} else if (tabSelected == 3) {
			carregarPathPorTempo();
		} else if (tabSelected == 4) {
			carregarWsPorTempo();
		} else if (tabSelected == 5) {
			carregarTempoExecucaoQuery();
		} else if (tabSelected == 6) {
			carregarConexoesAbertas();
		} else if (tabSelected == 7) {
			carregarQueryEmLock();
		}
	}

	private void carregarConexoesAbertas() {

		conexoesAbertas = new ArrayList<>();

		List<Bolso<Thread>> sessionThreadList = TransactionWrapper.getSessionThreadList();

		MultiValueMap<String, Long> auxMap = new LinkedMultiValueMap<>();
		if(sessionThreadList != null && !sessionThreadList.isEmpty()) {
			for (Bolso<Thread> bolso : sessionThreadList) {
				Thread thread = bolso.getObjeto();
				String name = thread.getName();
				name = name.replaceAll("-\\d+$", "");
				if(!thread.isAlive()) {
					//apenas para ter certeza que não tem thread morta
					name = "!!MORTA!!-" + name;
				}
				name = name + " " + thread.getState().name().toLowerCase();
				long startTime = bolso.getStartTime();
				long time = System.currentTimeMillis() - startTime;
				auxMap.add(name, time);
			}
		}

		List<Bolso<Thread>> svThreads = CustomOpenSessionInViewFilter.getSessionThreadList();
		if(svThreads != null && !svThreads.isEmpty()) {
			for (Bolso<Thread> bolso : svThreads) {
				Thread thread = bolso.getObjeto();
				String name = thread.getName();
				name = name.replaceAll("-\\d+$", "");
				if(!thread.isAlive()) {
					//apenas para ter certeza que não tem thread morta
					name = "!!MORTA!!-" + name;
				}
				name = name + " " + thread.getState().name().toLowerCase();
				long startTime = bolso.getStartTime();
				long time = System.currentTimeMillis() - startTime;
				auxMap.add(name, time);
			}
		}

		Set<String> auxSet = new HashSet<>(auxMap.keySet());
		for (String name : auxSet) {
			List<Long> tempos = auxMap.get(name);
			int count = tempos.size();
			long tempoTotal = auxMap.entrySet().stream().filter(e -> e.getKey().equals(name)).mapToLong(e2 -> e2.getValue().stream().mapToLong(e3 -> e3.longValue()).sum()).sum();
			long tempoMedio = tempoTotal / count;
			Map<String, String> map = new HashMap<>();
			map.put("nome", name);
			map.put("count", String.valueOf(count));
			map.put("tempoTotal", DummyUtils.formatNumber(new BigDecimal(tempoTotal)));
			map.put("tempoMedio", DummyUtils.formatNumber(new BigDecimal(tempoMedio)));
			map.put("order", String.valueOf(tempoTotal));
			conexoesAbertas.add(map);
		}
		Collections.sort(conexoesAbertas, (o1, o2) -> {
			Long count1 = new Long(o1.get("count"));
			Long count2 = new Long(o2.get("count"));
			int compareTo = count2.compareTo(count1);
			if(compareTo == 0) {
				Long tempo1 = new Long(o1.get("order"));
				Long tempo2 = new Long(o2.get("order"));
				compareTo = tempo2.compareTo(tempo1);
			}
			return compareTo;
		});
	}

	public void tabUpdate(Integer num){
		this.tabSelected = num;
	}

	public String toJsonFormat(String str) {

		try {
			JSONObject json = new JSONObject(str) ;
			str = json.toString(4);
			str = DummyUtils.stringToHTML(str);
			return str;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return str;
	}

	public void carregarLogAcessos() {
		logAcessoDataModel = new LogAcessoDataModel();
		logAcessoDataModel.setFiltro(filtro);
		logAcessoDataModel.setService(logAcessoService);
	}

	public void carregarInfoLogs() {
		servletPathsNormal = logAcessoService.findServletPaths("/",12);
		servletPathsJob = logAcessoService.findServletPaths("Job",5);
		infoTipoRegistro = logAcessoService.findInfoTipoRegistro();
		requisicoesHttpVO = logAcessoService.findRequisicoesHttp();
		exceptionVO = logAcessoService.findExceptions(5);
		buildJvmParams();
	}

	public void buildJvmParams() {
		Map<String, String> map = new TreeMap<>();
		System.getProperties().forEach((parametro, valor) -> {
			map.put((String) parametro, (String) valor);
		});

		jvmParams = map;
	}

	public void carregarPathPorTempo() {
		pathPorTempoDataModel = new PathPorTempoDataModel();
		pathPorTempoDataModel.setFiltro(filtro);
		pathPorTempoDataModel.setService(logAcessoService);
	}

	public void carregarWsPorTempo() {
		statusWsVos = consultaExternaService.findWsStatus();
		wsPorTempoDataModel = new WSPorTempoDataModel();
		wsPorTempoDataModel.setFiltro(filtro);
		wsPorTempoDataModel.setService(consultaExternaService);
	}

	public void carregarTempoExecucaoQuery() {
		logTimeExecutionQueryVOS = logAcessoService.findTempoExecucaoQuery();
	}

	private void carregarQueryEmLock() {
		queryOnLockVOS = logAcessoService.findQuerysEmLock();
	}

	public void carregarMonitoramento() {
		Map<String, BigDecimal> parametros;
		try {
			ServletContext servletContext = getServletContext();
			parametros = monitoramentoService.getParametros(servletContext);
		}
		catch (RuntimeException e) {

			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter ps = new PrintWriter(sw);
			e.printStackTrace(ps);

			this.monitoramentoErro = sw.getBuffer().toString();
			monitoramentoErro = StringEscapeUtils.escapeXml(monitoramentoErro);
			return;
		}

		this.monitoramentoUsuarios = new TreeMap<>();
		this.monitoramentoGeral = new TreeMap<>();
		BigDecimal usuariosLogados = null;
		BigDecimal usuariosAtivos = null;

		for (Map.Entry<String, BigDecimal> entry : parametros.entrySet()) {

			String parametroNome = entry.getKey();
			BigDecimal parametroValor = entry.getValue();

			if (parametroNome.equalsIgnoreCase("Usuários logados")) {
				usuariosLogados = parametroValor;
			}
			else if (parametroNome.equalsIgnoreCase("Usuários Ativos Hoje")) {
				usuariosAtivos = parametroValor;
			}
			else if (parametroNome.startsWith("Usuários")) {
				parametroNome = parametroNome.replace("Usuários","");
				parametroNome = parametroNome.replace("logados", "");
				parametroNome = StringUtils.trim(parametroNome);
				parametroNome = getMessage("RoleGD." + parametroNome + ".label");
				monitoramentoUsuarios.put(parametroNome, parametroValor);
			}
			else {
				monitoramentoGeral.put(parametroNome, parametroValor);
				continue;
			}
		}

		monitoramentoUsuarios = new LinkedHashMap<>(monitoramentoUsuarios);
		monitoramentoUsuarios.put("Usuários Ativos Hoje", usuariosAtivos);
		monitoramentoUsuarios.put("Todal Usuários Logados", usuariosLogados);
	}

	public String exceptionCut(String exception) {
		if (exception.length() > 35) {
			exception = exception.substring(0, 35) + "...";
		}
		return exception;
	}

	public LogAcessoDataModel getLogAcessoDataModel() {
		return logAcessoDataModel;
	}

	public PathPorTempoDataModel getPathPorTempoDataModel() {
		return pathPorTempoDataModel;
	}

	public WSPorTempoDataModel getWsPorTempoDataModel() {
		return wsPorTempoDataModel;
	}

	public LogAcessoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(LogAcessoFiltro filtro) {
		this.filtro = filtro;
	}

	public Map<String, String> getServletPathsJob() {
		return servletPathsJob;
	}

	public Map<String, String> getServletPathsNormal() {
		return servletPathsNormal;
	}

	public Map<String, Map<String, Long>> getInfoTipoRegistro() {
		return infoTipoRegistro;
	}

	public List<RequisicaoHttpVO> getRequisicoesHttpVO() {
		return requisicoesHttpVO;
	}

	public void setRequisicoesHttpVO(List<RequisicaoHttpVO> requisicoesHttpVO) {
		this.requisicoesHttpVO = requisicoesHttpVO;
	}

	public List<ExceptionVO> getExceptionVO() {
		return exceptionVO;
	}

	public void setExceptionVO(List<ExceptionVO> exceptionVO) {
		this.exceptionVO = exceptionVO;
	}

	public String getMonitoramentoErro() {
		return monitoramentoErro;
	}

	public Map<String, BigDecimal> getMonitoramentoUsuarios() {
		return monitoramentoUsuarios;
	}

	public Map<String, BigDecimal> getMonitoramentoGeral() {
		return monitoramentoGeral;
	}

	public List<LogTimeExecutionQueryVO> getLogTimeExecutionQueryVOS() {
		return logTimeExecutionQueryVOS;
	}

	public List<QueryOnLockVO> getQueryOnLockVOS() {
		return queryOnLockVOS;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<StatusWSVO> getStatusWsVos() {
		return statusWsVos;
	}

	public String getCssPorTempo(String horaMinuto) {
		try {
			if(!StringUtils.equals(horaMinuto, lastHoraMinuto)) {
				return "bg-info";
			}
			return "";
		}
		finally {
			lastHoraMinuto = horaMinuto;
		}
	}

	public StreamedContent baixarThreadInfo(){
		try {
			File tmp = DummyUtils.gerarThreadDump();
			InputStream stream = new FileInputStream(tmp);
			DefaultStreamedContent dsc  = DefaultStreamedContent.builder()
					.contentType("text/plain")
					.name("dump.txt")
					.stream(() -> stream)
					.build();
			return dsc;
		}
		catch (IOException e) {
			addMessageError(e);
		}
		return null;
	}

	public StreamedContent downloadArquivoVisualizacao(Long logAcessoId) {
		try {
			File bodyFile = File.createTempFile("arquivo-body", ".html");
			String bodyExemplo = logAcessoService.getBodyExemplo(logAcessoId);
			DummyUtils.escrever(bodyFile, bodyExemplo);

			FileInputStream fis = new FileInputStream(bodyFile);

			String name = "log-acesso-" + logAcessoId + ".html";
			DefaultStreamedContent dsc  = DefaultStreamedContent.builder()
					.contentType("text/html")
					.name(name)
					.stream(() -> fis)
					.build();

			return dsc;
		}
		catch (Exception e) {
			addMessageError(e);
			return null;
		}
	}

	public List<Map<String, String>> getConexoesAbertas() {
		return conexoesAbertas;
	}

	public Map<String, String> getJvmParams() {
		return jvmParams;
	}
}