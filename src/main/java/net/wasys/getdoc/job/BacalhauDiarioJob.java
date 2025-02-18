package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.BacalhauEmail;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import net.wasys.getdoc.domain.service.BacalhauEmailService;
import net.wasys.getdoc.domain.service.BacalhauService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class BacalhauDiarioJob extends TransactionWrapperJob {

	@Autowired private BacalhauService bacalhauService;
	@Autowired private BacalhauEmailService bacalhauEmailService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 15 02 * * ?")
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

			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -1);
			Date dataInicio = c.getTime();
			Date dataFim = c.getTime();

			String dt = DummyUtils.format(dataFim, "yyyy-MM-dd");

			//c.add(Calendar.DAY_OF_MONTH, -10);
			//dataInicio = c.getTime();

			Map<String, String> emails =  new LinkedHashMap<>();

			List<BacalhauEmail> mails = bacalhauEmailService.list(0, 100);

			for(BacalhauEmail mail : mails){
				emails.put(mail.getEmail(), mail.getNome());
			}

			//bacalhauService.gerarRelatorio(dataInicio, dataFim, "dia-" + dt);

//			bacalhauService.gerarRelatorio(TipoExecucaoBacalhau.AUTOMATICO, emails, dataInicio, dataFim, "dia-" + dt);

			bacalhauService.executarBacalhau(TipoExecucaoBacalhau.AUTOMATICO, emails, dataInicio, dataFim, "dia-" + dt);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
