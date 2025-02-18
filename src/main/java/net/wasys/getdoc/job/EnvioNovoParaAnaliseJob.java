package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
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
public class EnvioNovoParaAnaliseJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private EmailSmtpService emailSmtpService;

	@Override
	@Scheduled(cron="0 00 0/1 * * ?")//a cada 1 hora
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		List<Long> processosEnviados = new ArrayList<>();
		List<Long> processosErros = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			List<Long> ids = processoService.findIdsProcessosGraduacaoComDocumentosDigitalizado();
			if(!ids.isEmpty()) {
				systraceThread(ids.size() + " processos serao enviados para analise pois possuem documentos digitalizados nao conferidos...");
			}

			int i = 1;
			List<Future> futures = new ArrayList<>();
			for(Long processoId : ids) {

				systraceThread("Enviando processo: " + processoId + " para conferencia... " + i + " de " + ids.size());

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					processoService.enviarProcessoGraduacaoParaConferencia(processoId);
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
				i ++;
			}

			DummyUtils.checkTimeout(futures, 10 * 60 * 1000);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			executor.shutdown();
			emailSmtpService.enviarNotificacaoEnviosAnaliseJob(LogAcessoJob.ENVIO_PARA_ANALISE_GRADUACAO_JOB.getKey(), processosEnviados, processosErros);
			doFinally(inicio, log);
		}
	}
}
