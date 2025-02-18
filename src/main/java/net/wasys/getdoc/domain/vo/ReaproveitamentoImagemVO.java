package net.wasys.getdoc.domain.vo;

public class ReaproveitamentoImagemVO {

	private String caminho;
	private String metadados;
	private Integer versao;
	private String hash;

	public ReaproveitamentoImagemVO() {
	}

	public ReaproveitamentoImagemVO(String caminho, String metadados) {
		this.caminho = caminho;
		this.metadados = metadados;
	}

	public String getCaminho() {
		return caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
	}

	public String getMetadados() {
		return metadados;
	}

	public void setMetadados(String metadados) {
		this.metadados = metadados;
	}

	public Integer getVersao() {
		return versao;
	}

	public void setVersao(Integer versao) {
		this.versao = versao;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override public String toString() {
		return "ReaproveitamentoImagemVO{" +
				"caminho='" + caminho + '\'' +
				", metadados='" + metadados + '\'' +
				'}';
	}
}
