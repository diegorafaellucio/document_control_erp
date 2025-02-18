package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;

public class TempoStatusVO {

	private int vezes = 0;
	private BigDecimal tempoTotal = new BigDecimal(0);

	public BigDecimal getTempoTotal() {
		return tempoTotal;
	}

	public int getVezes() {
		return vezes;
	}

	public void addTempo(BigDecimal tempo) {
		vezes++;
		tempoTotal = tempoTotal.add(tempo);
	}
}
