package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.TextoPadraoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
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
public class NotificaPendenciaProuniJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TextoPadraoService textoPadraoService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
//	@Scheduled(cron="0 4/3 * * * ?")//a cada 3 minutos
	@Scheduled(cron="0/10 * * * * ?")//a cada 10 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		String executarOcr = System.getProperty("getdoc.executarSincronismo");
		if("false".equals(executarOcr)) {
			DummyUtils.systraceThread("FIM (executarSincronismo false)");
			return;
		}

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			TextoPadrao textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_PENDENCIA_CANDIDATO_SISPROUNI_ID);
			boolean ativo = textoPadrao.getAtivo();

			if(!ativo) {
				DummyUtils.systraceThread("NotificaPendenciaProuniJob Desativado nos Texto Padrões ID: " + TextoPadrao.NOTIFICACAO_PENDENCIA_CANDIDATO_SISPROUNI_ID);
				return;
			}

			List<Long> list = processoService.findIdsProcessoParaEnvioDeEmailPendenteSisProuni();

			do {

				List<Long> list2 = DummyUtils.removeItens(list, 100);
				if(list.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for(Long processoId : list2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						processoService.iniciarNotificacaoPendenciaCadidatoSisProuni(processoId, null, null, true);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(154);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!list.isEmpty());
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
