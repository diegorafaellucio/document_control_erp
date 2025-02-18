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
public class ExecucaoRegrasGrupo2Job extends TransactionWrapperJob {

	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private ApplicationContext applicationContext;
	private ExecutorService executorGrupo2 = Executors.newFixedThreadPool(20);

	@Override
	@Scheduled(cron="7/10 * * * * ?")//a cada 10 segundos
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
				worker.setPossuiConsultaExterna(true);
				worker.setProcessoId(processoId);
				worker.setDecisaoFluxo(false);
				worker.setThreadId(i + 1);
				workers.add(worker);
				Future<?> future = executorGrupo2.submit(worker);
				futures.add(future);
				DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
			}

			long timeout = System.currentTimeMillis() + 5 * 60 * 1000;
			DummyUtils.checkTimeout(futures, timeout);

			StringBuilder exceptionsMessages = new StringBuilder();
			for (ProcessoRegraWorkerThread worker : workers) {
				Exception exception = worker.getException();
				if(exception == null) continue;

				String message = exceptionService.getMessage(exception);
				Long processoId = worker.getProcessoId();
				exceptionsMessages.append("ProcessoId: ").append(processoId).append(": ").append(message).append("\n");

				exceptionsMessages.append("\n");
			}

			if(exceptionsMessages.length() > 0) {
				emailSmtpService.enviarEmailException("Erros excecutando regras do Grupo 2: ", exceptionsMessages.toString(), "");
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