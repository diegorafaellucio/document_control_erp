package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class NotificacaoAnaliseIsencaoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private EmailSmtpService emailSmtpService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	@Scheduled(cron="0 4/3 * * * ?")//a cada 3 minutos
	//@Scheduled(cron="0/10 * * * * ?")//a cada 10 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		String executar = System.getProperty("getdoc.executarSincronismo");
		if("false".equals(executar)) {
			DummyUtils.systraceThread("FIM (executarSincronismo false)");
			return;
		}

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			List<Long> notificarIsencaoDisciplinas = processoService.findProcessoAnaliseDeIsencaoParaEnvioDeEmail();

			do {
				List<Long> notificarAprovacaoIds2 = DummyUtils.removeItens(notificarIsencaoDisciplinas, 100);
				if(notificarAprovacaoIds2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Long processoId : notificarAprovacaoIds2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						emailEnviadoService.enviarNotificacaoAnaliseIsencao(processoId);
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(154);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!notificarIsencaoDisciplinas.isEmpty());
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
