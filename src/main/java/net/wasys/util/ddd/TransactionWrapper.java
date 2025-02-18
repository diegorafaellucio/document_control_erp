package net.wasys.util.ddd;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.other.Bolso;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import net.wasys.util.other.ExceptionHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.wasys.util.DummyUtils.systraceThread;

public class TransactionWrapper implements Runnable {

	private static final List<Bolso<Thread>> sessionThreadList = new CopyOnWriteArrayList<>();

	protected ApplicationContext applicationContext;
	private String[] sessionFactoryName = {"sessionFactory", "sessionFactoryBi"};
	private Exception exception;
	private Thread thread;
	private TransactionRunnable runnable;
	private ExceptionHandler exceptionHandler;
	protected String onThreadPrefix;
	protected String onThreadStackTrace;
	private Bolso<Thread> bolso;
	private LogAcesso logAcesso;

	public TransactionWrapper() {
		this.logAcesso = LogAcessoFilter.getLogAcesso();
	}

	public TransactionWrapper(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.logAcesso = LogAcessoFilter.getLogAcesso();
	}

	public static List<Bolso<Thread>> getSessionThreadList() {
		return sessionThreadList;
	}

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setSessionFactoryName(String... sessionFactoryName) {
		this.sessionFactoryName = sessionFactoryName;
	}

	public void setRunnable(TransactionRunnable runnable) {
		this.runnable = runnable;
	}

	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	private void executeWithSessionManagement(){

		String prefix = getClass().getSimpleName() + ".executeWithSessionManagement() > ";
		if(onThreadPrefix != null) {
			prefix = onThreadPrefix + " > " + prefix;
		}

		try {
			openSessions();
		}
		catch (RuntimeException e) {
			systraceThread(prefix + "falha na execucao, não foi possível criar a sessão: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			e.printStackTrace();
			exception = e;
			if(exceptionHandler != null) {
				String stackTrace = getStackTrace(e);
				exceptionHandler.handle(e, stackTrace);
			}
		}

		try {
			runnable.run();
		}
		catch (Exception e) {
			systraceThread(prefix + "falha na execucao: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			e.printStackTrace();
			exception = e;
			if(exceptionHandler != null) {
				String stackTrace = getStackTrace(e);
				exceptionHandler.handle(e, stackTrace);
			}
		}
		finally {
			try {
				closeSessions();
			}
			catch (Exception e) {
				systraceThread(prefix + "falha ao fechar session: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
				e.printStackTrace();
				exception = e;
				if(exceptionHandler != null) {
					String stackTrace = getStackTrace(e);
					exceptionHandler.handle(e, stackTrace);
				}
			}
		}
	}

	private String getStackTrace(Exception e) {
		String stackTrace = ExceptionUtils.getStackTrace(e);
		String onThreadStackTrace = getOnThreadStackTrace();
		if(StringUtils.isNotBlank(onThreadStackTrace)) {
			stackTrace += "\nThread StackTrace: \n" + onThreadStackTrace;
		}
		return stackTrace;
	}

	private void openSessions() {
		for (String sfn : sessionFactoryName) {
			SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean(sfn, org.hibernate.SessionFactory.class);
			Session session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}

		Thread thread = Thread.currentThread();
		bolso = new Bolso<>();
		bolso.setObjeto(thread);
		bolso.setStartTime(System.currentTimeMillis());
		sessionThreadList.add(bolso);
	}

	private void closeSessions() {
		sessionThreadList.remove(bolso);
		for (String sfn : sessionFactoryName) {
			SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean(sfn, org.hibernate.SessionFactory.class);
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
			SessionFactoryUtils.closeSession(sessionHolder.getSession());
		}
	}

	@Override
	public void run() {
		LogAcessoFilter.setLogAcesso(logAcesso);
		executeWithSessionManagement();
	}

	public Exception getException() {
		return exception;
	}

	public String getOnThreadPrefix() {
		return onThreadPrefix;
	}

	public String getOnThreadStackTrace() {
		return onThreadStackTrace;
	}

	public Thread startThread() {

		exception = null;
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String className = ste.getClassName();
		this.onThreadPrefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();
		onThreadStackTrace = ExceptionUtils.getStackTrace(new Throwable());
		onThreadStackTrace = onThreadStackTrace.substring(onThreadStackTrace.indexOf("\n") + 1, onThreadStackTrace.length());

		thread = new Thread(this);
		int identityHashCode = System.identityHashCode(thread);
		thread.setName("thdtw-start-" + onThreadPrefix + "-" + identityHashCode);
		thread.start();
		return thread;
	}

	public void join() {
		join(null);
	}

	public void join(Long timeout) {
		try {
			if(timeout != null) {
				thread.join(timeout);
				if(thread.isAlive()) {
					thread.interrupt();
				}
			} else {
				thread.join();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/** Se rodar em outra thread, usa outra session do hibernate; e se usar outra session, uma exception não interfere no commit de outras transações
	 */
	public void runNewThread() {
		startThread();

		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String className = ste.getClassName();
		this.onThreadPrefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();
		onThreadStackTrace = ExceptionUtils.getStackTrace(new Throwable());
		onThreadStackTrace = onThreadStackTrace.substring(onThreadStackTrace.indexOf("\n") + 1, onThreadStackTrace.length());
		int identityHashCode = System.identityHashCode(thread);
		thread.setName("thdtw-run-" + onThreadPrefix + "-" + identityHashCode);

		join(null);
	}

	public void runNewThread(long timeout) {
		startThread();

		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String className = ste.getClassName();
		this.onThreadPrefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();
		onThreadStackTrace = ExceptionUtils.getStackTrace(new Throwable());
		onThreadStackTrace = onThreadStackTrace.substring(onThreadStackTrace.indexOf("\n") + 1, onThreadStackTrace.length());
		int identityHashCode = System.identityHashCode(thread);
		thread.setName("thdtw-run-" + onThreadPrefix + "-" + identityHashCode);

		join(timeout);
	}

	public void throwException() throws Exception {
		Exception exception = getException();
		if(exception != null) throw exception;
	}
}
