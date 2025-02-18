package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.EmailPop3Service;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.SpringJob;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ImportacaoPop3Job extends TransactionWrapperJob {

	@Autowired private EmailPop3Service emailPop3Service;
	@Autowired private LogAcessoService logAcessoService;

	@Override
//	@Scheduled(cron="0 0/2 * * * ?")//a cada 2 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			SpringJob job = new SpringJob() {
				public void execute() {
					emailPop3Service.importarEmails();
				}
			};
			job.setApplicationContext(applicationContext);
			Thread thread = new Thread(job);
			thread.start();
			thread.join(8 * 60 * 1000);
			if(thread.isAlive()) {
				DummyUtils.systraceThread("encerrando thread por timeout.");
				thread.interrupt();
			}

			Exception exception2 = job.getException();
			if(exception2 != null) {
				throw exception2;
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
