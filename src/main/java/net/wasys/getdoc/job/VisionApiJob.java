package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.ImagemService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class VisionApiJob extends TransactionWrapperJob {

	@Autowired private ImagemService imagemService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ResourceService resourceService;

	@Override
	//@Scheduled(cron="25 0/2 * * * ?")//a cada 2 minutos
	//@Scheduled(cron="3/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		if("true".equals(System.getProperty("getdoc_mesa.stopservices"))) {
			DummyUtils.systraceThread("job não habilitado para execução");
			return;
		}

		String visionKey = resourceService.getValue(ResourceService.VISION_PROCESSOR_KEY);
		if(StringUtils.isBlank(visionKey )) {
			DummyUtils.systraceThread("fim. vision não habilitado");
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(5);
		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());
			List<Long> ids = imagemService.findIdsToFullText();
			if(ids.isEmpty()) {
				return;
			}
			DummyUtils.systraceThread("visionApi... " + ids.size() + ", " + ids);

			do {
				List<Long> ids2 = DummyUtils.removeItens(ids, 50);
				if(ids2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Long imagemId : ids2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Imagem imagem = imagemService.get(imagemId);
						imagemService.getFullText(imagem);
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 5 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!ids.isEmpty());
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			executor.shutdown();
			doFinally(inicio, log);
		}
	}
}
