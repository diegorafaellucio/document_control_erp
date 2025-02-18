package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EnvioEmailNotificacaoAlunoJob extends TransactionWrapperJob {

    private static final String dataCorteAprovados = "01/05/2020 00:00";

    @Autowired private LogAcessoService logAcessoService;
    @Autowired private ProcessoService processoService;
    @Autowired private EmailSmtpService emailSmtpService;
    @Autowired private EmailEnviadoService emailEnviadoService;
    @Autowired private TextoPadraoService textoPadraoService;

    @Override
    @Scheduled(cron = "0 1/30 * * * ?")//a cada meia hora
    public void run() {
        super.run();
    }

    @Override
    public void execute() {

        long inicio = System.currentTimeMillis();
        systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

        LogAcesso log = null;
        try {
            log = logAcessoService.criaLogJob(inicio, LogAcessoJob.ENVIO_EMAIL_NOTIFICACAO_ALUNO_JOB);

			Map<Long, TextoPadrao> textoPadraoMap = textoPadraoService.findByIds(Arrays.asList(TextoPadrao.NOTIFICACAO_CANDIDATO_REPROVACAO_ID, TextoPadrao.NOTIFICACAO_CANDIDATO_RASCUNHO_ID, TextoPadrao.NOTIFICACAO_CANDIDATO_APROVACAO_ID));

			//enviarNotificacoesEnvioAnalise();

			TextoPadrao reprovacao = textoPadraoMap.get(TextoPadrao.NOTIFICACAO_CANDIDATO_REPROVACAO_ID);
			if(reprovacao != null && reprovacao.getAtivo()) {
				enviarNotificacoesReprovacao();
			}

			TextoPadrao rascunho = textoPadraoMap.get(TextoPadrao.NOTIFICACAO_CANDIDATO_RASCUNHO_ID);
			if(rascunho != null && rascunho.getAtivo()) {
				enviarNotificacoesRascunhos();
			}

			TextoPadrao aprovacao = textoPadraoMap.get(TextoPadrao.NOTIFICACAO_CANDIDATO_APROVACAO_ID);
			if(aprovacao != null && aprovacao.getAtivo()) {
				enviarNotificacoesAprovacao();
			}
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

    private void enviarNotificacoesAprovacao() {

        Date dataCorte = DummyUtils.parseDateTime(dataCorteAprovados);
        List<Long> notificarAprovacaoIds = processoService.fintToNotificarAprovacao(dataCorte);
        int total = notificarAprovacaoIds.size();
        AtomicInteger notificados = new AtomicInteger();

        if (total > 0) {
            systraceThread("[notificarAprovação] Total: " + total);
            String currentMethodName = DummyUtils.getCurrentMethodName();
            do {
                List<Long> notificarAprovacaoIds2 = DummyUtils.removeItens(notificarAprovacaoIds, 100);
                systraceThread("[notificarAprovação] Restando: " + notificarAprovacaoIds.size() + " de: " + total);

                for (Long processoId : notificarAprovacaoIds2) {
                    TransactionWrapper tw = new TransactionWrapper(applicationContext);
                    tw.setRunnable(() -> {
                        emailEnviadoService.enviarNotificacaoAprovacao(processoId, null, null);
                        notificados.getAndIncrement();
                    });
                    tw.setExceptionHandler((e, stackTrace) -> {
                        emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
                    });
                    tw.runNewThread();
                    DummyUtils.sleep(300);//sem isso o hibernate se perde nas sessions
                }
            }
            while (!notificarAprovacaoIds.isEmpty());
            systraceThread("[notificarAprovação] notificados: " + notificados + " de: " + total + " [FIM]");
        }
    }

    private void enviarNotificacoesReprovacao() {

        List<Long> notificarPendenciaIds = processoService.findToNotificarPendencia();
        int total = notificarPendenciaIds.size();
        AtomicInteger notificados = new AtomicInteger();

        if (total > 0) {
            systraceThread("[notificarReprovação] Total: " + total);
            String currentMethodName = DummyUtils.getCurrentMethodName();
            do {
                List<Long> notificarPendenciaIds2 = DummyUtils.removeItens(notificarPendenciaIds, 100);
                systraceThread("[notificarReprovação] Restando: " + notificarPendenciaIds.size() + " de: " + total);
                for (Long processoId : notificarPendenciaIds2) {
                    TransactionWrapper tw = new TransactionWrapper(applicationContext);
                    tw.setRunnable(() -> {
                        emailEnviadoService.enviarNotificacaoPendencia(processoId, null, null, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_PENDENCIA);
                        notificados.getAndIncrement();
                    });
                    tw.setExceptionHandler((e, stackTrace) -> {
                        emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
                    });
                    tw.runNewThread();
                    DummyUtils.sleep(300);//sem isso o hibernate se perde nas sessions

                }
            }
            while (!notificarPendenciaIds.isEmpty());
            systraceThread("[notificarReprovação] notificados: " + notificados + " de: " + total + " [FIM]");
        }
    }

    private void enviarNotificacoesRascunhos() {

        Date dataCorte = DateUtils.addHourOfDay(new Date(), -24);
        List<Long> notificarPendenciaRascunhoIds = processoService.findToNotificarPendenciaRascunho(dataCorte);
        int total = notificarPendenciaRascunhoIds.size();
        AtomicInteger notificados = new AtomicInteger();

        if (total > 0) {
            systraceThread("[notificarRascunho] Total: " + total);
            String currentMethodName = DummyUtils.getCurrentMethodName();
            do {
                List<Long> notificarPendenciaRascunhoIds2 = DummyUtils.removeItens(notificarPendenciaRascunhoIds, 100);
                systraceThread("[notificarRascunho] Restando: " + notificarPendenciaRascunhoIds.size() + " de: " + total);

                for (Long processoId : notificarPendenciaRascunhoIds2) {
                    TransactionWrapper tw = new TransactionWrapper(applicationContext);
                    tw.setRunnable(() -> {
                        emailEnviadoService.enviarNotificacaoPendencia(processoId, null, null, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_RASCUNHO);
                        notificados.getAndIncrement();
                    });
                    tw.setExceptionHandler((e, stackTrace) -> {
                        emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
                    });
                    tw.runNewThread();
                    DummyUtils.sleep(300);//sem isso o hibernate se perde nas sessions
                }
            }
            while (!notificarPendenciaRascunhoIds.isEmpty());
            systraceThread("[notificarRascunho] notificados: " + notificados + " de: " + total + " [FIM]");
        }
    }
}
