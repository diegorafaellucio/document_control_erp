package net.wasys.getdoc.job;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FluxoAprovacaoAnaliseJob extends TransactionWrapperJob {

    private static final long THREAD_TIMEOUT = 2 * 60000;
    private final int QTD_THREADS = 2;

    @Autowired private LogAcessoService logAcessoService;
    @Autowired private ProcessoService processoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private SituacaoService situacaoService;
    @Autowired private FluxoAprovacaoAnaliseService fluxoAprovacaoAnaliseService;
    @Autowired private ConfiguracaoOCRService configuracaoOCRService;
    @Autowired private LogAnaliseIAService logAnaliseIAService;

    @Override
    @Scheduled(cron = "57/30 * * * * ?")//a cada 30 segundos
    public void run() {
        super.run();
    }

    @Override
    public void execute() {

        long inicio = System.currentTimeMillis();
        systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

        LogAcesso log = null;

        Stopwatch stopwatch = Stopwatch.createStarted();

        log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());
        ProcessoFiltro processoFiltro = new ProcessoFiltro();
        List<Situacao> situacoes = situacaoService.findByNome(Situacao.AGUARDANDO_ANALISE_IA);
        processoFiltro.setSituacao(situacoes);
        processoFiltro.setOrdenar("processo.id", SortOrder.ASCENDING);

        List<Long> ids = processoService.findIdsByFiltro(processoFiltro, null, null);

        if(ids.isEmpty()) {
            doFinally(inicio, log);
            return;
        }

        DummyUtils.addParameter(log, "Processos", ids);

        int totalProcessos = ids.size();
        long timeoutTotal = (totalProcessos * THREAD_TIMEOUT) / QTD_THREADS;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(QTD_THREADS);
        ExecutorTimeoutUtil executorTimeout = new ExecutorTimeoutUtil(executor, THREAD_TIMEOUT, timeoutTotal, QTD_THREADS);

        try {
            AtomicInteger erroProcessadoSemTrocaSituacao = new AtomicInteger();
            AtomicInteger erroGenericoProcessamento = new AtomicInteger();
            AtomicInteger erroGenericoProcessamentoTrocaSituacao = new AtomicInteger();
            AtomicInteger totalAnaliseIADesligada = new AtomicInteger();
            AtomicInteger processosSubmetidos = new AtomicInteger();

            for(Long processoId : ids) {
                TransactionWrapper tw = new TransactionWrapper(applicationContext);
                tw.setRunnable(() -> {
                    Processo processo = processoService.get(processoId);

                    boolean analiseIAAtiva = configuracaoOCRService.isFluxoAprovacaoIAAtivo(processoId, processo.getTipoProcesso().getId());
                    if (!analiseIAAtiva){
                        totalAnaliseIADesligada.getAndIncrement();
                        fluxoAprovacaoAnaliseService.relatarIAInativa(processo);
                        enviarEmAnalise(processo);
                        return;
                    }

                    fluxoAprovacaoAnaliseService.validarResultadoOCR(processo, false);
                    Situacao situacao = processo.getSituacao();
                    if (situacao.getNome().equals(Situacao.AGUARDANDO_ANALISE_IA)) {
                        enviarEmAnalise(processo);
                        erroProcessadoSemTrocaSituacao.getAndIncrement();
                    }

                    processosSubmetidos.getAndIncrement();
                });
                tw.setExceptionHandler((Exception e, String stackTrace) -> {
                    Processo processo = processoService.get(processoId);
                    try {
                        enviarEmAnalise(processo);
                        erroGenericoProcessamento.getAndIncrement();
                    } catch (Exception exception) {
                        erroGenericoProcessamentoTrocaSituacao.getAndIncrement();
                        exception.printStackTrace();
                    }
                });
                executorTimeout.submit(tw);
                DummyUtils.sleep(152);//sem isso o hibernate se perde nas sessions
            }

            executorTimeout.esperarTerminarFuturesOuCancelar(false);

            List<Object> referenciaThreadsTimeouts = executorTimeout.getReferenciaThreadsTimeouts();
            DummyUtils.addParameter(log, "timeouts", referenciaThreadsTimeouts.size());

            DummyUtils.addParameter(log, "totalAnaliseIADesligada", totalAnaliseIADesligada);
            DummyUtils.addParameter(log, "erroProcessadoSemTrocaSituacao", erroProcessadoSemTrocaSituacao);
            DummyUtils.addParameter(log, "erroGenericoProcessamento", erroGenericoProcessamento);
            DummyUtils.addParameter(log, "erroGenericoProcessamentoTrocaSituacao", erroGenericoProcessamentoTrocaSituacao);
            DummyUtils.addParameter(log, "totalProcessos", totalProcessos);
            DummyUtils.addParameter(log, "processosSubmetidos", processosSubmetidos);
            DummyUtils.addParameter(log, "tempo", stopwatch.elapsed(TimeUnit.MILLISECONDS));

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

    private void enviarEmAnalise(Processo processo) throws Exception {
        Usuario usuario = usuarioService.getByLogin(Usuario.LOGIN_ADMIN);
        SituacaoFiltro filtro = new SituacaoFiltro();
        filtro.setNome(Situacao.EM_ANALISE);
        filtro.setTipoProcessoId(processo.getTipoProcesso().getId());
        filtro.setAtiva(true);
        List<Situacao> situacaoList = situacaoService.findByFiltro(filtro, 0, 1);
        processoService.concluirSituacao(processo, usuario, situacaoList.get(0), null, null);
        logAnaliseIAService.updateStatusMotivoProcesso(processo, Situacao.EM_ANALISE, "Processo com documentos DIGITALIZADOS.");
    }
}
