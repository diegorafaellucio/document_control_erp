package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ExecucaoRegrasGrupo1Job extends TransactionWrapperJob {

	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private ApplicationContext applicationContext;
	private ExecutorService executorGrupo1 = Executors.newFixedThreadPool(5);

	@Override
	@Scheduled(cron="4/10 * * * * ?")//a cada 10 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			ProcessoFiltro filtro = new ProcessoFiltro();
			filtro.setRegrasExecutadas(false);
			List<Long> processosIds = processoService.findIdsByFiltro(filtro, 0, 30);

			List<ProcessoRegraWorkerThread> workers = new ArrayList<>();
			List<Future> futures = new ArrayList<>();
			for (int i = 0; i < processosIds.size(); i++) {
				Long processoId = processosIds.get(i);
				ProcessoRegraWorkerThread worker = new ProcessoRegraWorkerThread();
				worker.setApplicationContext(applicationContext);
				worker.setPossuiConsultaExterna(false);
				worker.setDecisaoFluxo(false);
				worker.setProcessoId(processoId);
				worker.setThreadId(i + 1);
				workers.add(worker);
				Future<?> future = executorGrupo1.submit(worker);
				futures.add(future);
				DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
			}

			long timeout = System.currentTimeMillis() + 3 * 60 * 1000;
			DummyUtils.checkTimeout(futures, timeout);

			StringBuilder exceptionsMessages = new StringBuilder();
			for (ProcessoRegraWorkerThread worker : workers) {
				Exception exception = worker.getException();
				if(exception == null) continue;

				Long processoId = worker.getProcessoId();
				String message = exceptionService.getMessage(exception);
				exceptionsMessages.append("ProcessoId: ").append(processoId).append(": ").append(message).append("\n");

				exceptionsMessages.append("\n");
			}

			if(exceptionsMessages.length() > 0) {
				emailSmtpService.enviarEmailException("Erros excecutando regras do Grupo 1: ", exceptionsMessages.toString(), "");
			}
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}