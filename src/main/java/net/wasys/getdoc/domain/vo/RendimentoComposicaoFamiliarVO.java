package net.wasys.getdoc.domain.vo;

import java.util.ArrayList;
import java.util.List;

public class RendimentoComposicaoFamiliarVO {

	private String vigenciaSalarioMinimo = "";
	private String salarioMinimoMensal = "";
	private String salarioMaximoPermitido = "";
	private List<RendimentoMembroFamiliarVO> rendimentos = new ArrayList<>(0);
	private String rendaTotal = "";
	private String rendaPerCapta = "";
	private String qtdeSalariosMinimos = "";
	private boolean validador;

	public RendimentoComposicaoFamiliarVO() {
	}

	public RendimentoComposicaoFamiliarVO(List<RendimentoMembroFamiliarVO> rendimentos) {
		this.rendimentos = rendimentos;
	}

	public String getVigenciaSalarioMinimo() {
		return vigenciaSalarioMinimo;
	}

	public void setVigenciaSalarioMinimo(String vigenciaSalarioMinimo) {
		this.vigenciaSalarioMinimo = vigenciaSalarioMinimo;
	}

	public String getSalarioMinimoMensal() {
		return salarioMinimoMensal;
	}

	public void setSalarioMinimoMensal(String salarioMinimoMensal) {
		this.salarioMinimoMensal = salarioMinimoMensal;
	}

	public List<RendimentoMembroFamiliarVO> getRendimentos() {
		return rendimentos;
	}

	public void setRendimentos(List<RendimentoMembroFamiliarVO> rendimentos) {
		this.rendimentos = rendimentos;
	}

	public String getRendaTotal() {
		return rendaTotal;
	}

	public void setRendaTotal(String rendaTotal) {
		this.rendaTotal = rendaTotal;
	}

	public String getRendaPerCapta() {
		return rendaPerCapta;
	}

	public void setRendaPerCapta(String rendaPerCapta) {
		this.rendaPerCapta = rendaPerCapta;
	}

	public String getQtdeSalariosMinimos() {
		return qtdeSalariosMinimos;
	}

	public void setQtdeSalariosMinimos(String qtdeSalariosMinimos) {
		this.qtdeSalariosMinimos = qtdeSalariosMinimos;
	}

	public void setSalarioMaximoPermitido(String salarioMaximoPermitido) {
		this.salarioMaximoPermitido = salarioMaximoPermitido;
	}

	public boolean isValidador() {
		return validador;
	}

	public void setValidador(boolean validador) {
		this.validador = validador;
	}

	@Override public String toString() {
		return "RendimentoComposicaoFamiliarVO{" +
				"vigenciaSalarioMinimo='" + vigenciaSalarioMinimo + '\'' +
				", salarioMinimoMensal='" + salarioMinimoMensal + '\'' +
				", salarioMaximoPermitido='" + salarioMaximoPermitido + '\'' +
				", rendimentos=" + rendimentos +
				", rendaTotal='" + rendaTotal + '\'' +
				", rendaPerCapta='" + rendaPerCapta + '\'' +
				", qtdeSalariosMinimos='" + qtdeSalariosMinimos + '\'' +
				", validador=" + validador +
				'}';
	}

	public String getSalarioMaximoPermitido() {
		return salarioMaximoPermitido;
	}
}
