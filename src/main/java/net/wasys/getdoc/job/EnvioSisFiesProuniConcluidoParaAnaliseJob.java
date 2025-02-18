package net.wasys.getdoc.job;

import net.bootsfaces.render.H;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.other.HorasUteisCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EnvioSisFiesProuniConcluidoParaAnaliseJob extends TransactionWrapperJob {

	@Autowired private ProcessoService processoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;

	public static final String observacao = "ATUALIZAÇÃO AUTOMÁTICA";

	@Override
	@Scheduled(cron="0 00 22 * * ?")//diariamente às 22h
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job EnvioSisFiesProuniConcluidoParaAnaliseJob " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		Date data = new Date();
		HorasUteisCalculator huc = processoService.buildHUC();
		boolean diaUtil = huc.isDiaUtil(data);
		if(!diaUtil) return;

		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			List<Long> ids = processoService.findIdsToEnvioParaAnaliseSisFiesProuniConcluido();
			if(ids.isEmpty()) {
				return;
			}
			systraceThread("enviando para analise... " + ids.size() + ", " + ids);

			do {
				List<Long> ids2 = DummyUtils.removeItens(ids, 50);
				if(ids2.isEmpty()) break;
				DummyUtils.addParameter(log, "Qtd processos:", ids2.size());
				AtomicInteger erros = new AtomicInteger();

				List<Future> futures = new ArrayList<>();
				for (Long processoId : ids2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Processo processo = processoService.get(processoId);
						ProcessoLog plog = processoLogService.criaLog(processo, null, AcaoProcesso.ENVIO_ANALISE, observacao);
						processoService.enviarParaAnalise(processo, null, plog, false);
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					LogAcesso logAcesso = log;
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
						erros.incrementAndGet();
						DummyUtils.addParameter(logAcesso, "Erro" + erros + ": ", stackTrace);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while(!ids.isEmpty());

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("job EnvioSisFiesProuniConcluidoParaAnaliseJob finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
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
