package net.wasys.getdoc.domain.vo;

import java.util.List;

public class ColunaConfigVO {
	private String nome;
	private List<String> campos;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<String> getCampos() {
		return campos;
	}

	public void setCampos(List<String> campos) {
		this.campos = campos;
	}
}
