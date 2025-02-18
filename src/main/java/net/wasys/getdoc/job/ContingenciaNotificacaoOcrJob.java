package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.StatusLogOcr;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.LogOcrService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.filtro.LogOcrFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

/**
 * Job de contingencia. O OCR é agendado em runtime ao digitalizar a imagem, mas caso dê algum erro, esse job tenta novamente 
 */
@Service
public class ContingenciaNotificacaoOcrJob extends TransactionWrapperJob {

	@Autowired private LogOcrService logOcrService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ResourceService resourceService;

	@Override
	@Scheduled(cron="5 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		String executarOcr = System.getProperty("getdoc.executarOcr");
		if("false".equals(executarOcr)) {
			systraceThread("FIM (executarOcr false)");
			return;
		}

		Long sistemaId = resourceService.getValue(ResourceService.OCR_WS_ID_SISTEMA, Long.class);
		if(sistemaId == null) {
			systraceThread("OCR desabilitado, idSistema não configurado");
			return;
		}

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			LogOcrFiltro filtro = new LogOcrFiltro();
			filtro.setStatus(StatusLogOcr.AGENDADO);
			filtro.setDataFim(DateUtils.addMinutes(new Date(), -1));
			List<Long> ids = logOcrService.findIdsByFiltro(filtro);

			if(ids.isEmpty()) {
				return;
			}

			systraceThread("verificando... " + ids.size() + ": " + ids);

			do {
				final List<Long> ids2 = new ArrayList<Long>();
				for (int i = 0; i < 10 && !ids.isEmpty(); i++) {
					Long id = ids.remove(0);
					ids2.add(id);
				}

				if(ids2.isEmpty()) {
					break;
				}

				processar(ids2);
			}
			while(!ids.isEmpty());

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("job finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void processar(List<Long> ids) {

		List<Thread> threads = new ArrayList<>();
		final String currentMethodName = DummyUtils.getCurrentMethodName();

		for (final Long logOcrId : ids) {

			TransactionWrapper job = new TransactionWrapper(applicationContext);
			job.setRunnable(() -> {
				logOcrService.verificar(logOcrId);
			});
			job.setExceptionHandler((e, stackTrace) -> {
				emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
			});

			Thread thread = job.startThread();
			threads.add(thread);
		}

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
