package net.wasys.getdoc.domain.vo;

public class SalarioMinimoVO {

	private String vigencia;
	private String multiplicador;
	private String salarioMensal;
	private String salarioMaximoPermitido;

	public SalarioMinimoVO(String vigencia, String multiplicador, String salarioMensal, String salarioMaximoPermitido) {
		this.vigencia = vigencia;
		this.multiplicador = multiplicador;
		this.salarioMensal = salarioMensal;
		this.salarioMaximoPermitido = salarioMaximoPermitido;
	}

	public String getVigencia() {
		return vigencia;
	}

	public void setVigencia(String vigencia) {
		this.vigencia = vigencia;
	}

	public String getMultiplicador() {
		return multiplicador;
	}

	public void setMultiplicador(String multiplicador) {
		this.multiplicador = multiplicador;
	}

	public String getSalarioMensal() {
		return salarioMensal;
	}

	public void setSalarioMensal(String salarioMensal) {
		this.salarioMensal = salarioMensal;
	}

	public String getSalarioMaximoPermitido() {
		return salarioMaximoPermitido;
	}

	public void setSalarioMaximoPermitido(String salarioMaximoPermitido) {
		this.salarioMaximoPermitido = salarioMaximoPermitido;
	}
}