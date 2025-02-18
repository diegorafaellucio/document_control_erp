package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.FuncoesService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FuncaoJob extends TransactionWrapperJob {

	@Autowired private FuncoesService funcoesService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0/15 1-6 * * *")//a cada 15 minutos entre 1:00 e 6:00
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		LogAcesso log = null;
		try {

			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			funcoesService.processarTodosProcessoParaRelatorioGeral();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
