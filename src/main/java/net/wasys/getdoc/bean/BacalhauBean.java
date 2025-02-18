package net.wasys.getdoc.bean;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import net.wasys.getdoc.bean.datamodel.BacalhauDataModel;
import net.wasys.getdoc.bean.datamodel.BacalhauEmailDataModel;
import net.wasys.getdoc.bean.datamodel.BacalhauImagemPerdidaDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import net.wasys.getdoc.job.BacalhauDiarioJob;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Faces;
import org.primefaces.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

@ManagedBean
@ViewScoped
public class BacalhauBean extends AbstractBean {

	@Autowired private BacalhauService bacalhauService;
	@Autowired private BacalhauEmailService bacalhauEmailService;
	@Autowired private ParametroService parametroService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BacalhauImagemPerdidaService bacalhauImagemPerdidaService;
	@Autowired private ImagemService imagemService;
	@Autowired private ResourceService resourceService;
	@Autowired private LogAcessoService logAcessoService;

	private BacalhauDataModel dataModel;
	private BacalhauEmailDataModel emailDataModel;
	private BacalhauImagemPerdidaDataModel imagemPerdidaDataModel;

	private BacalhauFiltro filtro = new BacalhauFiltro();
	private Date dataExecIni;
	private Date dataExecFim;
	private BacalhauEmail email = new BacalhauEmail();
	private Boolean porPeriodo;
	private File excelFile;
	private Map<String, Integer> statusBacalhauGeralMap;
	private Map<String, Double> statusBacalhauGeralPercent;
	private Imagem ultimaImagemProcessadaParaRecuperar;
	private boolean bacalhauGeralAgendado;
	private ConfiguracoesJobsBean.JobVO jobDiarioVO;
	private ConfiguracoesJobsBean.JobVO jobGeralVO;

	protected void initBean() {

		dataModel = new BacalhauDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(bacalhauService);

		emailDataModel = new BacalhauEmailDataModel();
		emailDataModel.setService(bacalhauEmailService);

		filtro.setDataInicio(new Date());
		filtro.setDataFim(new Date());

		imagemPerdidaDataModel = new BacalhauImagemPerdidaDataModel();
		imagemPerdidaDataModel.setService(bacalhauImagemPerdidaService);
		imagemPerdidaDataModel.setFiltro(filtro);

		this.dataExecIni = new Date();
		this.dataExecFim = new Date();
		porPeriodo = false;

		String agendamentoBacalhauGeral = parametroService.getValor(ParametroService.P.AGENDAMENTO_BACALHAU_GERAL);
		if(agendamentoBacalhauGeral != null) {
			JSONObject agendamentoBacalhauGeralJson = new JSONObject(agendamentoBacalhauGeral);
			bacalhauGeralAgendado = !agendamentoBacalhauGeralJson.getBoolean(BacalhauImagemPerdida.AGENDAMENTO_FINALIZADO);
			long imagemId = agendamentoBacalhauGeralJson.getLong("ultimaImagemProcessadaId");
			this.ultimaImagemProcessadaParaRecuperar = imagemService.get(imagemId);

			carregarInformacoesBacalhauGeral();
		}

		String bacalhauDiarioJobKey = LogAcessoJob.BACALHAU_DIARIO_JOB.getKey();
		boolean jobDiarioAtivo = false;
		String jobDiarioAtivoServer = null;
		String bacalhauGeralJobKey = LogAcessoJob.BACALHAU_GERAL_AGENDADO_JOB.getKey();
		boolean jobGeralAtivo = false;
		String jobGeralAtivoServer = null;

		List<Parametro> configuracoesList = parametroService.findLikeChave(ParametroService.P.CONFIGURACOES_JOBS);
		for (Parametro parametro : configuracoesList) {

			String chave = parametro.getChave();
			String server = null;
			int i = chave.indexOf("-");
			if(i > 0) {
				server = chave.substring(i + 1);
			}
			String mode = DummyUtils.getMode();
			if((!"dev".equals(mode) && !"homolog".equals(mode)) && ("local".equals(server) || "localhost".equals(server) || "homolog".equals(server))) {
				continue;
			}

			String configuracoes = parametro.getValor();
			Map<?, ?> configuracoesMap = DummyUtils.jsonStringToMap(configuracoes);

			Object jobDiarioAtivoObj = configuracoesMap.get(bacalhauDiarioJobKey);
			boolean jobDiarioAtivoAux = jobDiarioAtivoObj != null ? Boolean.valueOf(String.valueOf(jobDiarioAtivoObj)) : false;
			if(jobDiarioAtivoAux) {
				jobDiarioAtivoServer = server + ", ";
			}
			jobDiarioAtivo |= jobDiarioAtivoAux;

			Object jobGeralAtivoObj = configuracoesMap.get(bacalhauGeralJobKey);
			boolean jobGeralAtivoAux = jobGeralAtivoObj != null ? Boolean.valueOf(String.valueOf(jobGeralAtivoObj)) : false;
			if(jobGeralAtivoAux) {
				jobGeralAtivoServer = server + ", ";
			}
			jobGeralAtivo |= jobGeralAtivoAux;
		}

		jobDiarioVO = new ConfiguracoesJobsBean.JobVO();
		jobDiarioVO.setNome(bacalhauDiarioJobKey);
		jobDiarioVO.setValor(jobDiarioAtivo);
		jobDiarioVO.setServer(jobDiarioAtivoServer);
		Class<? extends TransactionWrapperJob> jobDiarioClass = getJobClass(bacalhauDiarioJobKey);
		Method[] methodsDiario = jobDiarioClass.getMethods();
		for (Method method: methodsDiario) {
			Scheduled scheduled = method.getAnnotation(Scheduled.class);
			if (scheduled != null) {
				String cron = scheduled.cron();
				jobDiarioVO.setCron(cron);
				break;
			}
		}

		jobGeralVO = new ConfiguracoesJobsBean.JobVO();
		jobGeralVO.setNome(bacalhauGeralJobKey);
		jobGeralVO.setValor(jobGeralAtivo);
		jobGeralVO.setServer(jobGeralAtivoServer);
		Class<? extends TransactionWrapperJob> jobGeralClass = getJobClass(bacalhauGeralJobKey);
		Method[] methodsGeral = jobGeralClass.getMethods();
		for (Method method: methodsGeral) {
			Scheduled scheduled = method.getAnnotation(Scheduled.class);
			if (scheduled != null) {
				String cron = scheduled.cron();
				jobGeralVO.setCron(cron);
				break;
			}
		}

		if(jobDiarioAtivo) {
			String jobDiarioCronExpression = jobDiarioVO.getCron();
			Date proximaDataDiario = getProximaDataFromCronExpression(jobDiarioCronExpression);
			jobDiarioVO.setProximaDataExecucao(proximaDataDiario);
		}

		if(jobGeralAtivo) {
			String jobGeralCronExpression = jobGeralVO.getCron();
			Date proximaDataGeral = getProximaDataFromCronExpression(jobGeralCronExpression);
			jobGeralVO.setProximaDataExecucao(proximaDataGeral);
		}
	}

	private Date getProximaDataFromCronExpression(String jobDiarioCronExpression) {
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
		CronParser cronParser = new CronParser(cronDefinition);
		Cron jobDiarioCron = cronParser.parse(jobDiarioCronExpression);
		ExecutionTime executionTime = ExecutionTime.forCron(jobDiarioCron);
		ZonedDateTime now = ZonedDateTime.now();
		Optional<ZonedDateTime> lastExecution = executionTime.nextExecution(now);
		ZonedDateTime zonedDateTime = lastExecution.get();
		Instant instant = zonedDateTime.toInstant();
		Date lastExecutionDate = Date.from(instant);
		return lastExecutionDate;
	}

	public void carregarInformacoesBacalhauGeral() {

		statusBacalhauGeralMap = bacalhauImagemPerdidaService.verificarStatusBacalhauGeral();
		statusBacalhauGeralPercent = bacalhauService.carregarPercentualStatusBacalhauGeral(statusBacalhauGeralMap);
	}

	public void buscar() {
		filtro = getFiltro();
	}

	public void carregarExecucaoJob() {
		List<LogAcesso> lastsJobs = logAcessoService.findLastsJobs(Arrays.asList(LogAcessoJob.BACALHAU_DIARIO_JOB, LogAcessoJob.BACALHAU_GERAL_AGENDADO_JOB), null);
		for (LogAcesso lastsJob : lastsJobs) {
			String servletPath = lastsJob.getServletPath();
			if(LogAcessoJob.BACALHAU_DIARIO_JOB.getKey().equals(servletPath)) {
				Date dataUltimaExecucao = lastsJob.getInicio();
				jobDiarioVO.setDataUltimaExecucao(dataUltimaExecucao);
				String exception = lastsJob.getException();
				jobDiarioVO.setException(exception);
			}
			else if(LogAcessoJob.BACALHAU_GERAL_AGENDADO_JOB.getKey().equals(servletPath)) {
				Date dataUltimaExecucao = lastsJob.getInicio();
				jobGeralVO.setDataUltimaExecucao(dataUltimaExecucao);
				String exception = lastsJob.getException();
				jobGeralVO.setException(exception);
			}
		}
	}

	public String toJsonFormat(String str) {

		try {
			JSONObject json = new JSONObject(str);
			str = json.toString(4);
			str = DummyUtils.stringToHTML(str);
			return str;
		} catch (Exception e) {
			//e.printStackTrace();
		}

		return str;
	}

	public BacalhauFiltro getFiltro() {
		return filtro;
	}

	public void executarJob() {
		if (dataExecIni != null) {
			String dt = DummyUtils.format(dataExecIni, "yyyy-MM-dd");

			Map<String, String> emails = new LinkedHashMap<String, String>();

			List<BacalhauEmail> mails = bacalhauEmailService.list(0, 100);

			for (BacalhauEmail mail : mails) {
				emails.put(mail.getEmail(), mail.getNome());
			}


			logAcessoService.criaLogJob(System.currentTimeMillis(), LogAcessoJob.BACALHAU_DIARIO_JOB);

			if (porPeriodo && dataExecFim != null) {
				bacalhauService.executarBacalhau(TipoExecucaoBacalhau.MANUAL, emails, dataExecIni, dataExecFim, "dia-" + dt);
			} else {
				bacalhauService.executarBacalhau(TipoExecucaoBacalhau.MANUAL, emails, dataExecIni, dataExecIni, "dia-" + dt);
			}
		}
	}

	public void agendarBacalhauGeral() {

		try {
			bacalhauService.agendarBacalhauGeral();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excelExport() {

		try {
			excelFile = File.createTempFile("relatorio-imagens-perdidas_", ".csv");
			DummyUtils.deleteOnExitFile(excelFile);
			FileWriter fw = new FileWriter(excelFile);
			PrintWriter writer = new PrintWriter(fw);

			bacalhauImagemPerdidaService.exportar(writer, filtro);

			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excelDownload() {
		try {
			Faces.sendFile(excelFile, true);
			DummyUtils.deleteFile(excelFile);
		}
		catch (Exception e) {
			addMessageError(e);
		}

	}

	private TransactionWrapperJob getJobBean(String className) {
		Class<?> jobClass = getJobClass(className);
		return (TransactionWrapperJob) applicationContext.getBean(jobClass);
	}

	private Class<? extends TransactionWrapperJob> getJobClass(String className) {
		try {
			return (Class<? extends TransactionWrapperJob>) Class.forName("net.wasys.getdoc.job." + className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Date getDataExecIni() {
		return dataExecIni;
	}

	public void setDataExecIni(Date dataExecIni) {
		this.dataExecIni = dataExecIni;
	}

	public Date getDataExecFim() {
		return dataExecFim;
	}

	public void setDataExecFim(Date dataExecFim) {
		this.dataExecFim = dataExecFim;
	}

	public BacalhauDataModel getDataModel() {
		return dataModel;
	}

	public BacalhauEmailDataModel getEmailDataModel() {
		return emailDataModel;
	}

	public BacalhauImagemPerdidaDataModel getImagemPerdidaDataModel() {
		return imagemPerdidaDataModel;
	}

	public void excluirEmail(BacalhauEmail email) {
		bacalhauEmailService.deleteEmailById(email.getId());
	}

	public void gravarEmail() {
		bacalhauEmailService.saveOrUpdateEmail(email);
		email = new BacalhauEmail();
	}

	public BacalhauEmail getEmail() {
		return email;
	}

	public void setEmail(BacalhauEmail email) {
		this.email = email;
	}

	public Boolean getPorPeriodo() {
		return porPeriodo;
	}

	public void setPorPeriodo(Boolean porPeriodo) {
		this.porPeriodo = porPeriodo;
	}

	public Boolean getBacalhauGeralAgendado() {
		return bacalhauGeralAgendado;
	}

	public Map<String, Integer> getStatusBacalhauGeralMap() {
		return statusBacalhauGeralMap;
	}

	public Map<String, Double> getStatusBacalhauGeralPercent() {
		return statusBacalhauGeralPercent;
	}

	public Imagem getUltimaImagemProcessadaParaRecuperar() {
		return ultimaImagemProcessadaParaRecuperar;
	}

	public String getBacalhauDiarioPath() {
		String bacalhauDirStr = resourceService.getValue(ResourceService.BACALHAU_PATH);
		return bacalhauDirStr;
	}

	public ConfiguracoesJobsBean.JobVO getJobDiarioVO() {
		return jobDiarioVO;
	}

	public ConfiguracoesJobsBean.JobVO getJobGeralVO() {
		return jobGeralVO;
	}
}
