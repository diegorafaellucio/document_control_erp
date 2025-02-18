package net.wasys.getdoc.domain.vo;

import java.util.Map;

public class LinhaVO {

	private int numeroLinha;
	private String chaveUnicidade;
	private Map<String, String> colunaValor;

	public int getNumeroLinha() {
		return numeroLinha;
	}

	public void setNumeroLinha(int numeroLinha) {
		this.numeroLinha = numeroLinha;
	}

	public String getChaveUnicidade() {
		return chaveUnicidade;
	}

	public void setChaveUnicidade(String chaveUnicidade) {
		this.chaveUnicidade = chaveUnicidade;
	}

	public Map<String, String> getColunaValor() {
		return colunaValor;
	}

	public void setColunaValor(Map<String, String> colunaValor) {
		this.colunaValor = colunaValor;
	}
}