package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.BacalhauService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BacalhauReversoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private BacalhauService bacalhauService;

	@Override
	@Scheduled(cron="0 30 02 * * ?")//02:30h
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			bacalhauService.executarBacalhauReverso();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
