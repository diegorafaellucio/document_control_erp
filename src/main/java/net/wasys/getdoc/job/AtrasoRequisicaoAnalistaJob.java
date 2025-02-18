package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class AtrasoRequisicaoAnalistaJob extends TransactionWrapperJob {

	@Autowired private ProcessoService processoService;
	@Autowired private ParametroService parametroService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 30 0/1 * * ?")//a cada 1 hora, aos 30 minutos
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		systraceThread("iniciando job " + new Date());

		if(!isHorarioExpediente()) {
			systraceThread("FIM (não é horário de expediente)");
			return;
		}

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			processoService.notificarAtrasosAnalistas();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private boolean isHorarioExpediente() {

		String[] expedienteArray = parametroService.getExpediente();
		Expediente expediente = new Expediente(expedienteArray);

		return expediente.isHorarioExpediente(new Date());
	}
}
