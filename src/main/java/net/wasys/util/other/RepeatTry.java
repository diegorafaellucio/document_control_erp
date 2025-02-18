package net.wasys.util.other;

import lombok.extern.slf4j.Slf4j;

import net.wasys.util.DummyUtils;
import net.wasys.util.rest.RestClient;

public class RepeatTry<T> {

	private ToTry<T> toTry;
	private long interval = 5000;
	private int repeatTimes;

	public RepeatTry(int repeatTimes, long interval) {
		this.repeatTimes = repeatTimes;
		this.interval = interval;
	}

	public void setToTry(ToTry<T> toTry) {
		this.toTry = toTry;
	}

	public T execute() throws Exception {

		int tentativas = 0;
		T result = null;
		Exception exception = null;
		do {
			try {
				result = toTry.toTry();
				exception = null;
			}
			catch (Exception e) {
				tentativas++;
				e.printStackTrace();
				if(exception == null) {
					exception = e;
				}

				String messageError = DummyUtils.getExceptionMessage(e);
				StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
				String className = ste.getClassName();
				String prefix = className.substring(className.lastIndexOf(".") + 1, className.length()) + ":" + ste.getLineNumber();
				DummyUtils.systraceThread(prefix + " > Erro na " + tentativas + "ª tentativa: " + messageError);

				if(tentativas < repeatTimes) {
					DummyUtils.sleep(interval);
				}
			}
		}
		while(tentativas < repeatTimes && exception != null && !(exception instanceof RestClient.RestException));

		if(exception != null) {
			throw exception;
		}

		return result;
	}
}
