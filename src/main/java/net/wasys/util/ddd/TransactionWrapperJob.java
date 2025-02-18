package net.wasys.util.ddd;

import com.ctc.wstx.util.ExceptionUtil;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TransactionWrapperJob extends TransactionWrapper {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ParametroService parametroService;

	private static ConcurrentHashMap<Class<?>, Boolean> executandoMap = new ConcurrentHashMap<>();

	public ThreadLocal<Boolean> habilitar = new ThreadLocal<>();

	public TransactionWrapperJob() {

		setRunnable(() -> executarJob());
	}

	private void executarJob() throws Exception {

		Thread thread = Thread.currentThread();
		int identityHashCode = System.identityHashCode(thread);
		thread.setName("thdtwjob-" + getClass().getSimpleName() + "-" + identityHashCode);

		Class<? extends TransactionWrapperJob> clazz = getClass();
		Boolean executando = isExecutando(clazz);

		//if(true) return;

		if(executando != null && executando) {
			DummyUtils.systraceThread(clazz.getSimpleName() + " > FIM (ja estava executando)");
			return;
		}

		//pequeno sleep porque se não a chamada a tabela de parametros pode acabar acontecendo antes da session estar realmetne disponível para essa thread ¯\_(ツ)_/¯
		DummyUtils.sleep(100);

		LogAcessoJob logAcessoJob = getLogAcessoJob();
		boolean isExecutarJob = parametroService.isExecutarJob(logAcessoJob);
		Boolean habilitar2 = habilitar.get();
		if(!isExecutarJob && (habilitar2 == null || !habilitar2)) {
			//String server = DummyUtils.getServer();
			//DummyUtils.systraceThread(clazz.getSimpleName() + " > FIM (não habilitado) " + logAcessoJob + " / " + server);
			return;
		}

		executandoMap.put(clazz, true);

		try {
			execute();
		}
		finally {
			executandoMap.put(clazz, false);
		}
	}

	public static Boolean isExecutando(Class<? extends TransactionWrapperJob> clazz) {
		return executandoMap.get(clazz);
	}

	protected abstract void execute() throws Exception;

	protected void doFinally(long inicio, LogAcesso log) {
		Date fim = new Date();
		long tempo = fim.getTime() - inicio;
		if(log != null) {
			log.setFim(fim);
			log.setTempo(tempo);
			logAcessoService.saveOrUpdateNewSession(log);
		}
		DummyUtils.systraceThread(getClass().getSimpleName() + " job finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
	}

	protected void handleException(LogAcesso log, Exception e) {
		String method = DummyUtils.getCurrentMethodName(3);
		DummyUtils.systraceThread("Erro inesperado em " + method + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
		e.printStackTrace();
		if(log != null) {

			String exception = log.getException();
			exception = exception != null ? exception + "\n\n" : "";

			String stackTrace = ExceptionUtils.getStackTrace(e);
			exception += stackTrace;
			log.setException(exception);
		}
		//emailSmtpService.enviarEmailException(method, e);
	}

	protected LogAcessoJob getLogAcessoJob() {

		String simpleName = getClass().getSimpleName();
		LogAcessoJob[] values = LogAcessoJob.values();
		for (LogAcessoJob value : values) {
			if(simpleName.equals(value.getKey())) {
				return value;
			}
		}

		return null;
	}
}
