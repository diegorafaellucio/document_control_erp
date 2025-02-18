package net.wasys.util.ocrws.dto.callback;

public class ModeloOcrDTO {

	private Long idOrigem;
	private String descricao;
	private String hashChecksum;
	private Integer altura;
	private Integer largura;
	private String pathOrigem;
	private String arquivo;

	public Long getIdOrigem() {
		return idOrigem;
	}

	public void setIdOrigem(Long idOrigem) {
		this.idOrigem = idOrigem;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	public Integer getAltura() {
		return altura;
	}

	public void setAltura(Integer altura) {
		this.altura = altura;
	}

	public Integer getLargura() {
		return largura;
	}

	public void setLargura(Integer largura) {
		this.largura = largura;
	}

	public String getPathOrigem() {
		return pathOrigem;
	}

	public void setPathOrigem(String pathOrigem) {
		this.pathOrigem = pathOrigem;
	}

	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}

	public String getArquivo() {
		return arquivo;
	}
}