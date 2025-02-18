package net.wasys.getdoc.bean;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

@ManagedBean
@ViewScoped
public class ConfiguracoesJobsBean extends AbstractBean {

	@Autowired private ParametroService parametroService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private LogAcessoService logAcessoService;

	private Map<String, JobVO> map;
	private Map<String, String> cronMap = new LinkedHashMap<>();
	private Map<String, String> dataMap = new LinkedHashMap<>();
	private List<String> keys1;
	private List<String> keys2;
	private String jobSelecionado;
	private List<LogAcessoJob> jobsAtivos = new ArrayList<>();

	public static class JobVO {
		private String nome;
		private Date dataUltimaExecucao;
		private String exception;
		private boolean valor;
		private boolean executando;
		private String server;
		private String cron;
		private Date proximaDataExecucao;
		public String getNome() { return nome; }
		public void setNome(String nome) { this.nome = nome; }
		public Date getDataUltimaExecucao() { return dataUltimaExecucao; }
		public void setDataUltimaExecucao(Date dataUltimaExecucao) { this.dataUltimaExecucao = dataUltimaExecucao; }
		public String getException() { return exception; }
		public void setException(String exception) { this.exception = exception; }
		public boolean getValor() { return valor; }
		public void setValor(boolean valor) { this.valor = valor; }
		public boolean isExecutando() { return executando; }
		public void setExecutando(boolean executando) { this.executando = executando; }

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public Date getProximaDataExecucao() {
			return proximaDataExecucao;
		}

		public void setProximaDataExecucao(Date proximaDataExecucao) {
			this.proximaDataExecucao = proximaDataExecucao;
		}
	}

	protected void initBean() {

		map = new TreeMap<>();

		List<LogAcessoJob> jobsAtivos = parametroService.getJobsAtivos();
		LogAcessoJob[] values = LogAcessoJob.values();
		for (LogAcessoJob job : values) {
			String key = job.getKey();
			Class<? extends TransactionWrapperJob> jobClass = getJobClass(key);
			Boolean executando = TransactionWrapperJob.isExecutando(jobClass);
			executando = executando != null ? executando : false;

			JobVO vo = new JobVO();
			vo.setNome(key);
			vo.setDataUltimaExecucao(null);
			vo.setException(null);
			boolean isAtivo = jobsAtivos.contains(job);
			vo.setValor(isAtivo);
			vo.setExecutando(executando);
			map.put(key, vo);

			String cron = getScheduledCron(jobClass);
			if(StringUtils.isNotBlank(cron)) {
				cronMap.put(key, cron);
				Date proximaData = getProximaDataFromCron(cron);
				dataMap.put(key, DummyUtils.formatDateTime2(proximaData));
			}
		}

		int size = map.size();
		int meio = (int) Math.ceil(size / 2d);
		Set<String> keySet = map.keySet();
		List<String> keyList = new ArrayList<>(keySet);

		keys1 = keyList.subList(0, meio);
		keys2 = keyList.subList(meio, size);
	}

	public static String getScheduledCron(Class<? extends TransactionWrapperJob> jobClass) {
		Method[] methodsDiario = jobClass.getMethods();
		for (Method method: methodsDiario) {
			Scheduled scheduled = method.getAnnotation(Scheduled.class);
			if (scheduled != null) {
				String cron = scheduled.cron();
				return cron;
			}
		}
		return null;
	}

	public static Date getProximaDataFromCron(String cron) {
		Cron jobDiarioCron = getCron(cron);
		ExecutionTime executionTime = ExecutionTime.forCron(jobDiarioCron);
		ZonedDateTime now = ZonedDateTime.now();
		Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
		ZonedDateTime zonedDateTime = nextExecution.get();
		Instant instant = zonedDateTime.toInstant();
		Date nextExecutionDate = Date.from(instant);
		return nextExecutionDate;
	}

	public static Date getUltimaDataFromCron(String cron) {
		Cron jobDiarioCron = getCron(cron);
		ExecutionTime executionTime = ExecutionTime.forCron(jobDiarioCron);
		ZonedDateTime now = ZonedDateTime.now();
		Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
		ZonedDateTime zonedDateTime = lastExecution.get();
		Instant instant = zonedDateTime.toInstant();
		Date nextExecutionDate = Date.from(instant);
		return nextExecutionDate;
	}

	private static Cron getCron(String cron) {
		try {
			return getCron(cron, CronType.QUARTZ);
		} catch (IllegalArgumentException e) {
			try {
				return getCron(cron, CronType.SPRING);
			} catch (IllegalArgumentException e2) {
				throw e2;
			}
		}
	}

	private static Cron getCron(String cron, CronType cronType) {
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
		CronParser cronParser = new CronParser(cronDefinition);
		return cronParser.parse(cron);
	}

	public void carregarUltimasExecucoes() {

		if(!jobsAtivos.isEmpty()) {
			String server = DummyUtils.getServer();
			List<LogAcesso> lastsJobs = logAcessoService.findLastsJobs(null, server);
			for (LogAcesso lastsJob : lastsJobs) {
				String servletPath = lastsJob.getServletPath();
				JobVO jobVO = map.get(servletPath);
				if(jobVO != null) {
					Date dataUltimaExecucao = lastsJob.getInicio();
					jobVO.setDataUltimaExecucao(dataUltimaExecucao);
					String exception = lastsJob.getException();
					jobVO.setException(exception);
				}
			}
		}
	}

	public void salvar() {

		try {
			Map<String, Boolean> valoresMap = new LinkedHashMap<>();
			for (String key : map.keySet()) {
				JobVO jobVO = map.get(key);
				Boolean valor = jobVO.getValor();
				valoresMap.put(key, valor);
			}

			parametroService.salvarConfiguracoesJobs(valoresMap);
			addMessage("registroAlterado.sucesso");
			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void executar() {

		try {
			TransactionWrapperJob job = getJobBean(jobSelecionado);
			Thread thread = new Thread(() -> {
				job.habilitar.set(true);
				job.run();
			});
			thread.start();

			//esse sleep é necessário... mas se eu coloco ele a pagina não funciona.
			//DummyUtils.sleep(500);

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Job " + jobSelecionado + " executando... acompanhe os logs do sistema para mais detalhes.", null);
			jobSelecionado = null;
			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private TransactionWrapperJob getJobBean(String className) {
		Class<?> jobClass = getJobClass(className);
		return (TransactionWrapperJob) applicationContext.getBean(jobClass);
	}

	public static Class<? extends TransactionWrapperJob> getJobClass(String className) {
		try {
			return (Class<? extends TransactionWrapperJob>) Class.forName("net.wasys.getdoc.job." + className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getKeys1() {
		return keys1;
	}

	public List<String> getKeys2() {
		return keys2;
	}

	public Map<String, JobVO> getMap() {
		return map;
	}

	public void setJobSelecionado(String jobSelecionado) {
		this.jobSelecionado = jobSelecionado;
	}

	public Map<String, String> getCronMap() {
		return cronMap;
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}
}
