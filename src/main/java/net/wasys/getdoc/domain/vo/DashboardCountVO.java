package net.wasys.getdoc.domain.vo;

import java.math.BigInteger;
import java.util.Date;

public class DashboardCountVO {

	private Date periodo;
	private String formaIngresso;
	private BigInteger processos;
	private String tipo;

	public Date getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Date periodo) {
		this.periodo = periodo;
	}

	public String getFormaIngresso() {
		return formaIngresso;
	}

	public void setFormaIngresso(String formaIngresso) {
		this.formaIngresso = formaIngresso;
	}

	public BigInteger getProcessos() {
		return processos;
	}

	public void setProcessos(BigInteger count) {
		this.processos = count;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
