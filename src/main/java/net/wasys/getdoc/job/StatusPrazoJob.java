package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StatusPrazoJob extends TransactionWrapperJob {

	private static boolean executando = false;

	@Autowired private ProcessoService processoService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 00 0/1 * * ?")//a cada 1 hora
	//@Scheduled(cron="0 1/2 * * * ?")//a cada 2 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		if(executando) {
			DummyUtils.systraceThread("FIM (ja estava executando)");
			return;
		}

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			processoService.atualizarStatusPrazo();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
