package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class NotificaCandidatoFiesAndProuniJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TextoPadraoService textoPadraoService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	@Scheduled(cron="0 4/3 * * * ?")//a cada 3 minutos
//	@Scheduled(cron="0/10 * * * * ?")//a cada 10 segundos
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

			TextoPadrao textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_CANDIDATO_SISFIES_ID);
			boolean ativoSisFies = textoPadrao.getAtivo();

			textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_CANDIDATO_SISPROUNI_ID);
			boolean ativoSisProuni = textoPadrao.getAtivo();

			List<Long> tiposProcessosIds = new ArrayList<>();

			if(!ativoSisFies && !ativoSisProuni){
				DummyUtils.systraceThread("NotificaCandidatoFiesAndProuniJob Desativado nos Texto Padrões IDs: " + TextoPadrao.NOTIFICACAO_CANDIDATO_SISFIES_ID + " / " + TextoPadrao.NOTIFICACAO_CANDIDATO_SISPROUNI_ID);
				return;
			}

			if(!ativoSisFies) {
				DummyUtils.systraceThread("NotificaCandidatoFiesAndProuniJob Desativado SisFies nos Texto Padrões ID: " + TextoPadrao.NOTIFICACAO_CANDIDATO_SISFIES_ID);
			} else {
				tiposProcessosIds.add(TipoProcesso.SIS_FIES);
			}

			if(!ativoSisProuni) {
				DummyUtils.systraceThread("NotificaCandidatoFiesAndProuniJob Desativado SisProuni nos Texto Padrões ID: " + TextoPadrao.NOTIFICACAO_CANDIDATO_SISPROUNI_ID);
			} else {
				tiposProcessosIds.add(TipoProcesso.SIS_PROUNI);
			}

			List<Long> list = processoService.findIdsProcessoParaEnvioDeEmailSisFiesAndSisProuni(tiposProcessosIds);

			do {

				List<Long> list2 = DummyUtils.removeItens(list, 100);
				if(list2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for(Long processoId : list2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						processoService.iniciarNotificacaoCadidatoSisFiesAndSisProuni(processoId, null, null, true);
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
