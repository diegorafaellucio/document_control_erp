package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.service.*;
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

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EnvioProcessoProuniJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private CalendarioCriterioService calendarioCriterioService;
	@Autowired private EmailSmtpService emailSmtpService;

	@Override
	@Scheduled(cron="0 20/30 * * * ?")//a cada meia hora
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		Date dataAtual = new Date();
		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(dataAtual));

		LogAcesso log = null;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			Date dataEnvioProcesso = calendarioCriterioService.getFirstDataFimEmissaoTermo();

			if(dataEnvioProcesso == null) return;

			if(dataAtual.after(dataEnvioProcesso)) {

				List<CalendarioCriterio> calendarioCriterioList = calendarioCriterioService.findByDataFim(dataEnvioProcesso);

				for(CalendarioCriterio calendarioCriterio : calendarioCriterioList) {

					Calendario calendario = calendarioCriterio.getCalendario();
					TipoProcesso tipoProcesso = calendario.getTipoProcesso();
					Long tipoProcessoId = tipoProcesso.getId();

					List<Long> ids = processoService.findIdsEnvioProcessoProuni(calendarioCriterio);
					Long situacaoId;
					if (TipoProcesso.SIS_PROUNI.equals(tipoProcessoId)) {
						situacaoId = Situacao.SITUACAO_ENVIO_PARA_EMITIR_TR_SISPROUNI;
					} else {
						calendarioCriterio.setExecutado(true);
						calendarioCriterioService.saveOrUpdate(calendarioCriterio, null);
						continue;
					}

					Situacao situacao = situacaoService.get(situacaoId);
					ProcessoLog processoLog = new ProcessoLog();
					processoLog.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);

					int i = 1;
					List<Future> futures = new ArrayList<>();
					for (Long processoId : ids) {

						systraceThread("Enviando processo: " + processoId + " para emissao do termo de recusa... " + i + " de " + ids.size());

						TransactionWrapper tw = new TransactionWrapper(applicationContext);
						tw.setRunnable(() -> {
							Processo processo = processoService.get(processoId);
							processoService.alterarSituacaoMudancaDeFluxoRegra(processo, null, situacao, processoLog, null);
						});
						String currentMethodName = DummyUtils.getCurrentMethodName();
						tw.setExceptionHandler((e, stackTrace) -> {
							emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
						});
						Future<?> future = executor.submit(tw);
						futures.add(future);
						DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
						i++;
					}

					DummyUtils.checkTimeout(futures, 10 * 60 * 1000);

					//TODO: verificar se esse cara teve ser marcado como true mesmo que dê algum erro, ou que tenha algum timeout.
					calendarioCriterio.setExecutado(true);
					calendarioCriterioService.saveOrUpdate(calendarioCriterio, null);
				}
			}
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
