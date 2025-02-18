package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LimpaLogAcessoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 15 01 * * ?")//1:15h
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minutos
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

			int deletados = logAcessoService.limpaLogAcesso();

			DummyUtils.addParameter(log, "deletados", deletados);

			systraceThread("finalizando job, " + deletados + " logs deletados.");
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
