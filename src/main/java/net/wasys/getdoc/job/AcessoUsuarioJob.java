package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AcessoUsuarioJob extends TransactionWrapperJob {

	@Autowired private UsuarioService usuarioService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0 1 * * ?")//uma hora da manhã
	//@Scheduled(cron="0 0/2 * * * ?")//a cada 2 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		DummyUtils.systraceThread("AcessoUsuarioJob.execute() iniciando job " + new Date());

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, LogAcessoJob.ACESSO_USUARIO_JOB);

			usuarioService.verificarAcessos();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
