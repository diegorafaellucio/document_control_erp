package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jpedal.parser.shape.S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class Tipificacao2Job extends TransactionWrapperJob {

	@Autowired private DocumentoService documentoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	private ExecutorService executor = Executors.newFixedThreadPool(5);

	@Override
	@Scheduled(cron="17 0/1 * * * ?")//a cada 1 minuto
	//@Scheduled(cron="17/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + getClass().getSimpleName() + " " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			MultiValueMap<Long, Long> docsIds = documentoService.findIdsToTipificacao2();
			List<Long> ids = new ArrayList<>(docsIds.keySet());
			if(ids.isEmpty()) {
				return;
			}
			DummyUtils.systraceThread("tipificando... " + ids.size() + ", " + ids);

			Map<Long, TransactionWrapper> tws = new LinkedHashMap<>();
			do {
				List<Long> ids2 = DummyUtils.removeItens(ids, 50);
				if(ids2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();

				for (Long documentoId : ids2) {
					List<Long> imagensIds = docsIds.get(documentoId);
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {

						DummyUtils.mudarNomeThread("thdpool-tipificacao2");

						Documento documento = documentoService.get(documentoId);
						documentoService.tipificar2(documento, imagensIds);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					tws.put(documentoId, tw);
					DummyUtils.sleep(151);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!ids.isEmpty());

			handleExceptions(log, tws);

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			DummyUtils.systraceThread("job finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void handleExceptions(LogAcesso log, Map<Long, TransactionWrapper> tws) {

		StringBuilder erros = new StringBuilder();
		StringBuilder exceptions = new StringBuilder();
		for (Map.Entry<Long, TransactionWrapper> entry : tws.entrySet()) {
			Long documentoId = entry.getKey();
			TransactionWrapper tw = entry.getValue();
			Exception exception = tw.getException();
			if(exception != null) {
				String exceptionMessage = DummyUtils.getExceptionMessage(exception);
				String stackTrace = ExceptionUtils.getStackTrace(exception);
				erros.append("DocumentoID: ").append(documentoId).append(", erro: ").append(exceptionMessage).append("\n");
				exceptions.append("DocumentoID: ").append(documentoId).append(", stackTrace: ").append(stackTrace).append("\n\n");
			}
		}
		if(erros.length() > 0) {
			String exception = log.getException();
			exception = exception != null ? exception + "\n\n" : "";
			exception += erros + "\n\n\n" + exceptions;
			log.setException(exception);
		}
	}
}
