package net.wasys.util.other;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyLock<T> implements Lock {

	private final static ConcurrentHashMap<Object, LockAndCounter> locksMap = new ConcurrentHashMap<>();

	private final T key;
	private LockAndCounter lastLock;

	public KeyLock(T lockKey) {
		this.key = lockKey;
	}

	private static class LockAndCounter {
		private final Lock lock = new ReentrantLock();
		private final AtomicInteger counter = new AtomicInteger(0);
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{counter:" + counter + "}";
		}
	}

	private LockAndCounter getLock() {
		return locksMap.compute(key, (key, lockAndCounterInner) -> {
			if (lockAndCounterInner == null) {
				lockAndCounterInner = new LockAndCounter();
			}
			lockAndCounterInner.counter.incrementAndGet();
			return lastLock = lockAndCounterInner;
		});
	}

	private void cleanupLock(LockAndCounter lockAndCounterOuter) {
		if (lockAndCounterOuter.counter.decrementAndGet() == 0) {
			locksMap.compute(key, (key, lockAndCounterInner) -> {
				if (lockAndCounterInner == null || lockAndCounterInner.counter.get() == 0) {
					return null;
				}
				return lockAndCounterInner;
			});
		}
	}

	public long lock2() {
		long inicioEspera = System.currentTimeMillis();
		lock();
		long tempoEspera = System.currentTimeMillis() - inicioEspera;
		return tempoEspera;
	}

	@Override
	public void lock() {
		LockAndCounter lockAndCounter = getLock();
		lockAndCounter.lock.lock();
	}

	@Override
	public void unlock() {
		LockAndCounter lockAndCounter = locksMap.get(key);
		lockAndCounter.lock.unlock();
		cleanupLock(lockAndCounter);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		LockAndCounter lockAndCounter = getLock();
		try {
			lockAndCounter.lock.lockInterruptibly();
		}
		catch (InterruptedException e) {
			cleanupLock(lockAndCounter);
			throw e;
		}
	}

	@Override
	public boolean tryLock() {
		LockAndCounter lockAndCounter = getLock();
		boolean acquired = lockAndCounter.lock.tryLock();
		if (!acquired) {
			cleanupLock(lockAndCounter);
		}
		return acquired;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		LockAndCounter lockAndCounter = getLock();
		boolean acquired;
		try {
			acquired = lockAndCounter.lock.tryLock(time, unit);
		}
		catch (InterruptedException e) {
			cleanupLock(lockAndCounter);
			throw e;
		}

		if (!acquired) {
			cleanupLock(lockAndCounter);
		}
		return acquired;
	}

	@Override
	public Condition newCondition() {
		LockAndCounter lockAndCounter = locksMap.get(key);
		return lockAndCounter.lock.newCondition();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{key:" + key + ",lock:" + lastLock + "}";
	}

	public static String locksMapToString() {
		return locksMap.toString();
	}

	public static class KeyLockTest {

		@Test
		public void testDifferentKeysDontLock() throws InterruptedException {
			KeyLock<Object> lock = new KeyLock<>("lock1");
			lock.lock();
			AtomicBoolean anotherThreadWasExecuted = new AtomicBoolean(false);
			try {
				new Thread(() -> {
					KeyLock<Object> anotherLock = new KeyLock<>("lock2");
					anotherLock.lock();
					try {
						anotherThreadWasExecuted.set(true);
					}
					finally {
						anotherLock.unlock();
					}
				}).start();
				Thread.sleep(100);
			}
			finally {
				Assert.assertTrue(anotherThreadWasExecuted.get());
				lock.unlock();
			}
		}

		@Test
		public void testSameKeysLock() throws InterruptedException {
			KeyLock<Object> lock = new KeyLock<>("chaveIgual");
			lock.lock();
			AtomicBoolean anotherThreadWasExecuted = new AtomicBoolean(false);
			try {
				new Thread(() -> {
					KeyLock<Object> anotherLock = new KeyLock<>("chaveIgual");
					anotherLock.lock();
					try {
						anotherThreadWasExecuted.set(true);
					}
					finally {
						anotherLock.unlock();
					}
				}).start();
				Thread.sleep(100);
			}
			finally {
				Assert.assertFalse(anotherThreadWasExecuted.get());
				lock.unlock();
			}
		}
	}
}
