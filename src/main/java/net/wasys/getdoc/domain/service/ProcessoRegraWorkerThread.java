package net.wasys.getdoc.domain.service;

import java.util.Arrays;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;
import net.wasys.util.LogLevel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import org.springframework.beans.factory.annotation.Qualifier;

import static net.wasys.util.DummyUtils.systraceThread;

public class ProcessoRegraWorkerThread extends TransactionWrapper {

	private int threadId;
	private Exception exception;
	private Boolean possuiConsultaExterna;
	private Long processoId;
	private Boolean decisaoFluxo;

	public ProcessoRegraWorkerThread() {
		setRunnable(() -> process());

		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String className = ste.getClassName();
		onThreadStackTrace = ExceptionUtils.getStackTrace(new Throwable());
		onThreadStackTrace = onThreadStackTrace.substring(onThreadStackTrace.indexOf("\n") + 1, onThreadStackTrace.length());
		this.onThreadPrefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();
	}

	private void process() {

		SessionFactory sessionFactory = applicationContext.getBean("sessionFactory", SessionFactory.class);
		Session session = sessionFactory.getCurrentSession();
		String grupo = decisaoFluxo ? " grupo3" : (possuiConsultaExterna ? " grupo2" : " grupo1");

		//DummyUtils.systraceThread("iniciando thread#" + threadId + grupo + " processo#" + processoId + " session#" + System.identityHashCode(session));

		try {
			ProcessoRegraService processoRegraService = applicationContext.getBean(ProcessoRegraService.class);
			ProcessoService processoService = applicationContext.getBean(ProcessoService.class);

			if(decisaoFluxo != null && decisaoFluxo) {
				ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
				filtro.setProcessoId(processoId);
				filtro.setStatusList(Arrays.asList(StatusProcessoRegra.PENDENTE, StatusProcessoRegra.PROCESSANDO));
				filtro.setPossuiConsultaExterna(null);
				filtro.setDecisaoFluxo(false);
				List<ProcessoRegra> prs0 = processoRegraService.findLasts(filtro);
				if(!prs0.isEmpty()) {
					//se ainda tem regras que não são de decisão, não executa.
					return;
				}
			}

			ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
			filtro.setProcessoId(processoId);
			filtro.setStatusList(Arrays.asList(StatusProcessoRegra.PENDENTE));
			filtro.setPossuiConsultaExterna(possuiConsultaExterna);
			filtro.setDecisaoFluxo(decisaoFluxo);
			filtro.setTipoExecucao(TipoExecucaoRegra.AGENDADA);

			if(decisaoFluxo){
				filtro.setDesconsiderarProcessoComDocumentoTipificando(true);
			}

			List<ProcessoRegra> prs = processoRegraService.findLasts(filtro);

			if(!prs.isEmpty()) {
				String observacao = "Processamento de regras do " + grupo + ".";
				systraceThread(observacao + " thread#" + threadId + " processo#" + processoId);
				Processo processo = processoService.get(processoId);
				processoRegraService.processarRegras(processo, null, observacao, prs, false, true);
			}
		}
		catch (Exception e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("ERRO thread#" + threadId + grupo + " processo#" + processoId + " session#" + System.identityHashCode(session) + ": " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();
			exception = e;
		}

		//DummyUtils.systraceThread("finalizada thread#" + threadId + grupo + " processo#" + processoId + " session#" + System.identityHashCode(session));
	}

	public Exception getException() {
		return exception;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public void setPossuiConsultaExterna(Boolean possuiConsultaExterna) {
		this.possuiConsultaExterna = possuiConsultaExterna;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public void setDecisaoFluxo(Boolean decisaoFluxo) {
		this.decisaoFluxo = decisaoFluxo;
	}
}
