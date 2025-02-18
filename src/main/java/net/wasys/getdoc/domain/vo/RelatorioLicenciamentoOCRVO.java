package net.wasys.getdoc.domain.vo;

import java.util.List;

public class RelatorioLicenciamentoOCRVO {

	private Long id;
	private String nome;
	private List<Long> qtdPorMes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<Long> getQtdPorMes() {
		return qtdPorMes;
	}

	public void setQtdPorMes(List<Long> qtdPorMes) {
		this.qtdPorMes = qtdPorMes;
	}
}

