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
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.ExecutorShutdownException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FluxoAprovacaoTipificacaoJob extends TransactionWrapperJob {

	private static final long THREAD_TIMEOUT = 2 * 60000;
	private final int QTD_THREADS = 2;

	@Autowired private ProcessoService processoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipificacaoFluxoAprovacaoService tipificacaoFluxoAguardandoTipificacaoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ConfiguracaoOCRService configuracaoOCRService;

	@Override
	@Scheduled(cron = "17/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {
		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job TipificacaoJob " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;

		Stopwatch stopwatch = Stopwatch.createStarted();

		log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());
		List<Situacao> aguardandoTipificacao = situacaoService.findByNome(Situacao.AGUARDANDO_TIPIFICACAO);

		ProcessoFiltro processoFiltro = new ProcessoFiltro();
		processoFiltro.setSituacao(aguardandoTipificacao);
		processoFiltro.setOrdenar("processo.id", SortOrder.ASCENDING);
		List<Long> processosASeremTipificados = processoService.findIdsByFiltro(processoFiltro, null, null);

		systraceThread("Situações para Busca " + aguardandoTipificacao);
		systraceThread("Processos para Tipificação " + processosASeremTipificados);

		if(processosASeremTipificados.isEmpty()) {
			doFinally(inicio, log);
			return;
		}

		DummyUtils.addParameter(log, "Processos", processosASeremTipificados.size());

		int totalProcessos = processosASeremTipificados.size();
		long timeoutTotal = (totalProcessos * THREAD_TIMEOUT) / QTD_THREADS;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(QTD_THREADS);
		ExecutorTimeoutUtil executorTimeout = new ExecutorTimeoutUtil(executor, THREAD_TIMEOUT, timeoutTotal, QTD_THREADS);

		try {
			AtomicInteger erroProcessadoSemTrocaSituacao = new AtomicInteger();
			AtomicInteger erroGenericoProcessamento = new AtomicInteger();
			AtomicInteger erroGenericoProcessamentoTrocaSituacao = new AtomicInteger();
			AtomicInteger totalTipificacaoDesligada = new AtomicInteger();
			AtomicInteger processosSubmetidos = new AtomicInteger();

			for (Long processoId : processosASeremTipificados) {
				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					Processo processo = processoService.get(processoId);
					boolean tipificacaoAtiva = configuracaoOCRService.isFluxoAprovacaoTipificacaoAtivo(processoId, processo.getTipoProcesso().getId());
					if (!tipificacaoAtiva){
						totalTipificacaoDesligada.getAndIncrement();
						enviarEmAnalise(processo);
						return;
					}
					tipificacaoFluxoAguardandoTipificacaoService.tipificarDocumentosByProcesso(processo, false);
					Situacao situacao = processo.getSituacao();
					String situacaoNome = situacao.getNome();
					if (Situacao.AGUARDANDO_TIPIFICACAO.equals(situacaoNome)){
						enviarEmAnalise(processo);
						erroProcessadoSemTrocaSituacao.getAndIncrement();
					}
					processosSubmetidos.getAndIncrement();
				});
				tw.setExceptionHandler((Exception e, String stackTrace) -> {
					systraceThread( "### stack tipificacao _ " + stackTrace, LogLevel.FATAL);
					e.printStackTrace();
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
				DummyUtils.sleep(177);
			}

			executorTimeout.esperarTerminarFuturesOuCancelar(false);

			List<Object> referenciaThreadsTimeouts = executorTimeout.getReferenciaThreadsTimeouts();
			DummyUtils.addParameter(log, "timeouts", referenciaThreadsTimeouts.size());
			DummyUtils.addParameter(log, "totalTipificacaoDesligada", totalTipificacaoDesligada);
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
		catch (ExecutorShutdownException e) {
			handleException(log, e);
		}
		finally {
			executorTimeout.shutdown();
			executor.shutdown();
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
	}
}
