package net.wasys.util.other;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class BolsoCache<T> {

	private static ConcurrentHashMap<BolsoCache, BolsoCacheCallback> cacheMap = new ConcurrentHashMap<>();

	static {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {

				Thread currentThread = Thread.currentThread();
				currentThread.setName("thdtmr-BolsoCache-" + sdf.format(new Date()));

				for (BolsoCache bolsoCache : cacheMap.keySet()) {
					if(bolsoCache.isExpirado()) {
						BolsoCacheCallback timeoutCallback = cacheMap.get(bolsoCache);
						timeoutCallback.run(bolsoCache);
						cacheMap.remove(bolsoCache);
					}
				}
			}
		}, 60 * 1000, 60 * 1000);
	}

	private T objeto;
	private long timeout;
	private long finalTime;

	public BolsoCache(T objeto, long timeout, BolsoCacheCallback timeoutCallback) {
		this.objeto = objeto;
		this.timeout = timeout;
		this.finalTime = System.currentTimeMillis() + timeout;
		cacheMap.put(this, timeoutCallback);
	}

	public void setObjeto(T objeto) {
		this.objeto = objeto;
	}

	public T getObjeto() {
		return objeto;
	}

	public boolean isExpirado() {
		return System.currentTimeMillis() > this.finalTime;
	}

	public interface BolsoCacheCallback {
		void run(BolsoCache bc);
	}
}