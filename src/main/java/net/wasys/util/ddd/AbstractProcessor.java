package net.wasys.util.ddd;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.LogAcessoProcessor;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;

import java.util.Date;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

public abstract class AbstractProcessor extends TransactionWrapper implements Runnable {

	private Thread thread;
	private boolean finalizado;
	private Exception exception;

	public AbstractProcessor() {
		setRunnable(() -> execute2());
	}

	protected abstract void execute2() throws Exception;

	public void start() {

		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String className = ste.getClassName();
		this.onThreadPrefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();

		thread = new Thread(this);
		int identityHashCode = System.identityHashCode(thread);
		thread.setName("thdap-" + onThreadPrefix + "-" + identityHashCode);
		thread.start();
	}

	public boolean isFinalizado() {
		return finalizado || thread == null || !thread.isAlive();
	}

	public void setFinalizado(boolean finalizado) {
		this.finalizado = finalizado;
	}

	public Exception getError() {
		if(exception != null) {
			return exception;
		}
		return super.getException();
	}

	protected LogAcessoProcessor getLogAcessoProcessor() {

		String simpleName = getClass().getSimpleName();
		LogAcessoProcessor[] values = LogAcessoProcessor.values();
		for (LogAcessoProcessor value : values) {
			if(simpleName.equals(value.getKey())) {
				return value;
			}
		}

		return null;
	}

	protected void doFinally(long inicio, LogAcesso log) {

		Date fim = new Date();
		long tempo = fim.getTime() - inicio;
		if (log != null) {
			log.setFim(fim);
			log.setTempo(tempo);

			LogAcessoService logAcessoService = applicationContext.getBean(LogAcessoService.class);
			logAcessoService.saveOrUpdateNewSession(log);
		}

		systraceThread(getClass().getSimpleName() + " exporter finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo + "ms.");
	}

	protected void handleException(LogAcesso log, Exception e) {
		handleException(log, e, null);
	}

	protected void handleException(LogAcesso log, Exception e, Map<String, String> toAlerta) {

		this.exception = e;

		String messageError = DummyUtils.getExceptionMessage(e);

		systraceThread(getClass().getSimpleName() + " erro: " + messageError, LogLevel.ERROR);
		e.printStackTrace();

		if (log != null) {
			log.setException(messageError);
		}

		String method = DummyUtils.getCurrentMethodName(3);

		EmailSmtpService emailSmtpService = applicationContext.getBean(EmailSmtpService.class);
		emailSmtpService.enviarEmailException(method, e);
	}
}
