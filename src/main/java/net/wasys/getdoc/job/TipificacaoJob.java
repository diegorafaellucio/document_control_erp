package net.wasys.getdoc.job;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

@Service
public class TipificacaoJob extends TransactionWrapperJob {

	@Autowired private DocumentoService documentoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	//@Scheduled(cron="17 0/1 * * * ?")//a cada 1 minuto
	@Scheduled(cron="17/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job TipificacaoJob " + DummyUtils.formatDateTime(new Date()));

		ConcurrentHashMap<Long, Long> tempos = new ConcurrentHashMap<>();
		int countTimeouts = 0;
		Timer timer = new Timer();

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			LogAcesso log2 = log;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					atualizarParametrosLogs(tempos, log2, false);
					logAcessoService.saveOrUpdateNewSession(log2);
				}
			}, 5000, 5000);

			List<Long> ids = documentoService.findIdsToTipificacao();

			DummyUtils.addParameter(log, "documentos", ids.size());

			if(ids.isEmpty()) {
				return;
			}

			Map<Long, TransactionWrapper> tws = new LinkedHashMap<>();
			do {
				List<Long> ids2 = DummyUtils.removeItens(ids, 50);
				if(ids2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Long documentoId : ids2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Stopwatch sw = Stopwatch.createStarted();
						tempos.put(documentoId, -1L);

						Documento documento = documentoService.get(documentoId);
						documentoService.tipificar(documento);

						long tempo = sw.elapsed(TimeUnit.SECONDS);
						tempos.put(documentoId, tempo);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					tws.put(documentoId, tw);
					DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				List<Future> timeouts = DummyUtils.checkTimeout(futures, timeout);
				countTimeouts += timeouts.size();

				DummyUtils.addParameter(log, "timeouts", countTimeouts);

				if(!timeouts.isEmpty()) {
					return;
				}
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

			timer.cancel();

			atualizarParametrosLogs(tempos, log, true);

			doFinally(inicio, log);
		}
	}

	private void atualizarParametrosLogs(ConcurrentHashMap<Long, Long> tempos, LogAcesso log, boolean logarTimeouts) {

		Collection<Long> valores = tempos.values();
		BigDecimal media = DummyUtils.getMedia(valores, 2);

		BigDecimal max = DummyUtils.getMax(valores);

		List<Long> documentosIdsTimeouts = new ArrayList<>();
		Long documentoIdTempoMax = null;
		for (Map.Entry<Long, Long> entry : tempos.entrySet()) {
			Long value = entry.getValue();
			if(value < 0) {
				documentosIdsTimeouts.add(entry.getKey());
			}
			else if(value.equals(max)) {
				documentoIdTempoMax = entry.getKey();
			}
		}

		DummyUtils.addParameter(log, "processados", tempos.size());
		DummyUtils.addParameter(log, "tempoMedio", media);
		DummyUtils.addParameter(log, "tempoMax", max);
		DummyUtils.addParameter(log, "documentoIdTempoMax", documentoIdTempoMax);
		if(logarTimeouts) {
			DummyUtils.addParameter(log, "documentosIdsTimeouts", documentosIdsTimeouts);
		}
	}

	private void handleExceptions(LogAcesso log, Map<Long, TransactionWrapper> tws) {

		int countErros = 0;
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
				countErros++;
			}
		}
		if(erros.length() > 0) {
			String exception = log.getException();
			exception = exception != null ? exception + "\n\n" : "";
			exception += erros + "\n\n\n" + exceptions;
			log.setException(exception);
		}
		DummyUtils.addParameter(log, "erros", countErros);
	}
}
