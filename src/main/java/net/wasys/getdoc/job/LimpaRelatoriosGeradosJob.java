package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.ExecucaoGeracaoRelatorioService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LimpaRelatoriosGeradosJob extends TransactionWrapperJob {

	private static final int QNT_DIAS_MANTER = 7;

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ExecucaoGeracaoRelatorioService execucaoGeracaoRelatorioService;

	@Override
	@Scheduled(cron="0 00 02 * * ?")//2:00h
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

			Calendar cal = Calendar.getInstance();
			cal = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
			cal.add(Calendar.DAY_OF_YEAR, - QNT_DIAS_MANTER);

			int deletados = execucaoGeracaoRelatorioService.excluirAnteriores(cal.getTime());

			systraceThread("finalizando job, " + deletados + " registros deletados.");
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
