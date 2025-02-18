package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.EmailExchangeService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ImportacaoExchangeJob extends TransactionWrapperJob {

	@Autowired private EmailExchangeService emailExchangeService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="15 0/10 * * * ?")//a cada 10 minutos
//	@Scheduled(cron="15/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, LogAcessoJob.IMPORTACAO_EXCHANGE_JOB);

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() ->
				emailExchangeService.importarEmails()
			);
			tw.runNewThread(8 * 60 * 1000);
			tw.throwException();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
