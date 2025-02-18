package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.BacalhauImagemPerdida;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.BacalhauService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class BacalhauGeralAgendadoJob extends TransactionWrapperJob {

	@Autowired private BacalhauService bacalhauService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0/7 0-6 * * ?")//a cada 7 minutos entre 00:00 e 6:00
	//@Scheduled(cron="0 0 0 ? * 1") // todos os domingos meia noite
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

			bacalhauService.executarBacalhauGeral();

			bacalhauService.recuperarImagensPerdidas();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
