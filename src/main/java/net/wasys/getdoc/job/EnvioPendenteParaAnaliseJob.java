package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoService;
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

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EnvioPendenteParaAnaliseJob extends TransactionWrapperJob {

	@Autowired private ProcessoService processoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;

	@Override
	@Scheduled(cron="0 00 0/1 * * ?")//a cada 1 hora
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job EnvioParaAnaliseJob " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		List<Long> processosEnviados = new ArrayList<>();
		List<Long> processosErros = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			List<Long> ids = processoService.findIdsToEnvioParaAnalise();
			if(ids.isEmpty()) {
				return;
			}
			systraceThread("enviando para analise... " + ids.size() + ", " + ids);

			do {
				List<Long> ids2 = DummyUtils.removeItens(ids, 50);
				if(ids2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Long processoId : ids2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Processo processo = processoService.get(processoId);
						processoService.enviarParaAnalise(processo, null, null, false);
						processosEnviados.add(processoId);
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName + "(" + processoId + ")", e, stackTrace);
						processosErros.add(processoId);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!ids.isEmpty());

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("job EnviarParaAnalise finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
            handleException(log, e);
		}
		finally {
			executor.shutdown();
			emailSmtpService.enviarNotificacaoEnviosAnaliseJob(LogAcessoJob.ENVIO_PARA_ANALISE_JOB.getKey(), processosEnviados, processosErros);
			doFinally(inicio, log);
		}
	}
}
