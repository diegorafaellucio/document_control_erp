package net.wasys.util.ddd;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.internal.SemaphoreBulkhead;

import java.time.Duration;

/**
 * Bulkhead serve para limitar a quantidade de execuções em paralelo a algum método.
 * Esta classe é utilizada quando o analista busca o próximo processo. Como o lock é feito no lado do banco de dados,
 * é necessário algum mecanismo para evitar que muitas conexões fiquem em lock em casos de desastre (load muito alto,
 * excesso de conexões, problemas na rede etc), e que podem compremeter ainda mais a situação da aplicação.
 */
public class ProximoProcessoBulkhead extends SemaphoreBulkhead {

	public ProximoProcessoBulkhead() {
		super("bulkheadProximoProcesso");

		BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
				.maxConcurrentCalls(50)
				.maxWaitDuration(Duration.ofSeconds(20))
				.build();

		this.changeConfig(bulkheadConfig);
	}

	public String metricsToString() {

		Metrics metrics = getMetrics();
		return "AvailableConcurrentCalls=" + metrics.getAvailableConcurrentCalls() + ", MaxAllowedConcurrentCalls=" + metrics.getMaxAllowedConcurrentCalls();
	}
}
