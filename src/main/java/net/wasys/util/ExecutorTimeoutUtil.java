package net.wasys.util;

import net.wasys.util.ddd.ExecutorShutdownException;
import net.wasys.util.other.Bolso;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutorTimeoutUtil {

	private final ThreadPoolExecutor executor;
	private long threadTimeout;
	private long executorTimeoutMillis;
	private int qtdeThreadsExecutor;
	private final Date dataCriacao;
	private long dataTimeoutExecutor;

	private long tempoEsperaAteExecucao = 0;
	private List<Bolso<Future>> bolsos = new ArrayList<>();
	private Map<Runnable, Object> referenciaRunnableMap = new HashMap<>();
	private Map<Future, Object> referenciaFutureMap = new HashMap<>();
	private List<Object> referenciaThreadsTimeouts = new ArrayList<>();
	private AtomicLong dataFinalizacaoPrimeiro = new AtomicLong(-1);

	public ExecutorTimeoutUtil(ThreadPoolExecutor executor) {
		this.executor = executor;
		dataCriacao = new Date();
	}

	public ExecutorTimeoutUtil(ThreadPoolExecutor executor, long threadTimeout, long executorTimeoutMillis, int qtdeThreadsExecutor) {
		this.executor = executor;
		this.threadTimeout = threadTimeout;
		this.executorTimeoutMillis = executorTimeoutMillis;
		this.qtdeThreadsExecutor = qtdeThreadsExecutor;
		dataCriacao = new Date();
	}

	public void configurar(long threadTimeout, int qtdeThreads, int qtdeRegistros) {

		qtdeThreads = Math.min(qtdeRegistros, qtdeThreads);
		Long timeoutTotal = (qtdeRegistros * threadTimeout) / qtdeThreads;

		this.threadTimeout = threadTimeout;
		this.executorTimeoutMillis = timeoutTotal;
		this.qtdeThreadsExecutor = qtdeThreads;
	}

	public void submit(Runnable runnable) {
		submit(runnable, null);
	}

	public void submit(Runnable runnable, Object referencia) {

		Future<?> future = executor.submit(runnable);

		if(referencia != null) {
			referenciaFutureMap.put(future, referencia);
			referenciaRunnableMap.put(runnable, referencia);
		}

		if (bolsos.size() > 0 && bolsos.size() % qtdeThreadsExecutor == 0) {
			tempoEsperaAteExecucao += threadTimeout;
		}

		long finalTime = System.currentTimeMillis() + threadTimeout + tempoEsperaAteExecucao;

		Bolso<Future> futureBolso = new Bolso<>(future, finalTime);
		bolsos.add(futureBolso);
	}

	public void esperarTerminarFuturesOuCancelar() {
		esperarTerminarFuturesOuCancelar(true);
	}

	public void esperarTerminarFuturesOuCancelar(boolean throwsIfTimeout) throws ExecutorShutdownException {

		try {
			dataTimeoutExecutor = System.currentTimeMillis() + executorTimeoutMillis;

			List<Bolso<Future>> bolsosProcessar;
			boolean executorAtingiuTempoLimite = false;

			do {
				bolsosProcessar = new ArrayList<>(bolsos);

				for (Bolso<Future> futureBolso : bolsosProcessar) {

					executorAtingiuTempoLimite = executorAtingiuTempoLimite();
					if (executorAtingiuTempoLimite) {
						break;
					}

					Future future = futureBolso.getObjeto();

					boolean expirou = futureBolso.expirou();
					boolean done = future.isDone();
					if (done || expirou) {
						try {
							future.get(threadTimeout, TimeUnit.MILLISECONDS);

							if (new Long(-1).equals(dataFinalizacaoPrimeiro.get())) {
								dataFinalizacaoPrimeiro.set(System.currentTimeMillis());
							}
						} catch (TimeoutException e) {
							e.printStackTrace();
							expirou = true;
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}

						if (!done && expirou) {
							boolean cancelou = future.cancel(true);
							//systraceThread("Future levou timeout por demorar mais de " + threadTimeout + "ms. Future cancelou? \"" + cancelou + "\"", LogLevel.ERROR);
							Object referencia = referenciaFutureMap.get(future);
							if (referencia != null) {
								referenciaThreadsTimeouts.add(referencia);
							}
						}

						bolsos.remove(futureBolso);
					}
				}

				if (executorAtingiuTempoLimite) break;

				if (bolsos.size() > 0) {
					DummyUtils.sleep(1000);
				}
			}
			while (bolsos.size() > 0);

			if (executorAtingiuTempoLimite && !bolsos.isEmpty() && (!executor.getQueue().isEmpty() || executor.getActiveCount() > 0)) {
				int tasksNaoExecutadas = cancelarFutures();
				if (throwsIfTimeout) {
					throw new ExecutorShutdownException("Executor levou timeout. Data criação=" + DummyUtils.formatDateTime2(dataCriacao) + ", Data limite=" + DummyUtils.formatDateTime2(new Date(dataTimeoutExecutor)) + ". Tasks não executadas: " + tasksNaoExecutadas);
				}
			}
		}
		finally {
			executor.shutdown();
		}
	}

	private int cancelarFutures() {

		List<Runnable> runnablesNaoExecutadas = executor.shutdownNow();

		for (Runnable runnable : runnablesNaoExecutadas) {
			Object referencia = referenciaRunnableMap.get(runnable);
			referenciaThreadsTimeouts.add(referencia);
		}

		int tasksNaoExecutadas = runnablesNaoExecutadas.size();
		return tasksNaoExecutadas;
	}

	private boolean executorAtingiuTempoLimite() {
		return System.currentTimeMillis() >= dataTimeoutExecutor;
	}

	public List<Object> getReferenciaThreadsTimeouts() {
		return referenciaThreadsTimeouts;
	}

	public Map<Runnable, Object> getReferenciaRunnableMap() {
		return referenciaRunnableMap;
	}

	public Timer aumentarThreadsPaulatinamente(int qtdeThreads, int delay) {

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long finalizacaoPrimeiro = dataFinalizacaoPrimeiro.get();
				if(new Long(-1).equals(dataFinalizacaoPrimeiro.get()) || finalizacaoPrimeiro + delay > System.currentTimeMillis()) {
					return;
				}

				int corePoolSize = executor.getCorePoolSize();
				if(corePoolSize < qtdeThreads) {
					int newPoolSize = corePoolSize + 1;
					executor.setCorePoolSize(newPoolSize);
					executor.setMaximumPoolSize(newPoolSize);
				} else {
					timer.cancel();
				}
			}
		}, delay, delay);

		return timer;
	}

	public int getQtdeThreadsExecutor() {
		return qtdeThreadsExecutor;
	}

	public long getDataTimeoutExecutor() {
		return dataTimeoutExecutor;
	}

	public void shutdown() {
		if(executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
	}
}