package net.wasys.getdoc.domain.vo;

public class ImagemVO {

	private Long id;
	private String nome;
	private String caminho;
	private Integer versao;
	private String hash;
	private Boolean existente;
	private RegistrosDeDigitalizacaoVO registrosDeDigitalizacao;

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

	public String getCaminho() {
		return caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
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

	public Boolean getExistente() {
		return existente;
	}

	public void setExistente(Boolean existente) {
		this.existente = existente;
	}

	public RegistrosDeDigitalizacaoVO getRegistrosDeDigitalizacao() {
		return registrosDeDigitalizacao;
	}

	public void setRegistrosDeDigitalizacao(RegistrosDeDigitalizacaoVO registrosDeDigitalizacao) {
		this.registrosDeDigitalizacao = registrosDeDigitalizacao;
	}
}
