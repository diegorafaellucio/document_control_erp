package net.wasys.util.ddd;

import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public abstract class SpringJob extends TimerTask {

	private String[] sessionFactoryName = {"sessionFactory"};
	protected ApplicationContext applicationContext;
	private Exception exception;

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setSessionFactoryName(String... sessionFactoryName) {
		this.sessionFactoryName = sessionFactoryName;
	}

	public void executeWithoutSession(){

		try {
			execute();
		}
		catch (Exception e) {

			logger.error(getClass().getName() + ".executeWithoutSession() > falha ao executar o job: " + e.getMessage(), e);
			exception = e;
		}
	}

	public void executeWithSessionManagement(){

		try {
			openSessions();
		}
		catch (RuntimeException e) {

			logger.error(getClass().getName() + ".executeWithSessionManagement() > falha ao executar o job, não foi possível criar a sessão: " + e.getMessage(), e);
			exception = e;
		}

		try {
			execute();
		}
		catch (Exception e) {

			logger.error(getClass().getName() + ".executeWithSessionManagement() > falha ao executar o job: " + e.getMessage(), e);
			exception = e;
		}
		finally {

			closeSessions();
		}
	}

	private void openSessions() {

		for (String sfn : sessionFactoryName) {

			SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean(sfn, org.hibernate.SessionFactory.class);
			Session session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.MANUAL);

			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}
	}

	private void closeSessions() {

		for (String sfn : sessionFactoryName) {

			SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean(sfn, org.hibernate.SessionFactory.class);
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);

			SessionFactoryUtils.closeSession(sessionHolder.getSession());
		}
	}

	public abstract void execute() throws Exception;

	@Override
	public void run() {
		executeWithSessionManagement();
	}

	public Exception getException() {
		return exception;
	}
}
