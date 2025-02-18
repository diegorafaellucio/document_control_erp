package net.wasys.getdoc.domain.enumeration;

import java.math.BigDecimal;

public enum StatusPrazo {

	NORMAL(null),
	ADVERTIR(3),
	ALERTAR(0),
	;

	private BigDecimal horasRestantes;

	StatusPrazo(Integer horasRestantes) {
		this.horasRestantes = horasRestantes != null ? new BigDecimal(horasRestantes) : null;
	}

	public BigDecimal getHorasRestantes() {
		return horasRestantes;
	}

	public static StatusPrazo getByHorasRestantes(BigDecimal horas, BigDecimal horasPrazoAdvertir) {

		if(horas.compareTo(StatusPrazo.ALERTAR.getHorasRestantes()) <= 0) {
			return StatusPrazo.ALERTAR;
		}
		else if(horasPrazoAdvertir != null && horas.compareTo(horasPrazoAdvertir) <= 0) {
			return StatusPrazo.ADVERTIR;
		}
		return StatusPrazo.NORMAL;
	}
}
