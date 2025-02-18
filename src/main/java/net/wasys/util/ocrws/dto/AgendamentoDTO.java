package net.wasys.util.ocrws.dto;

public class AgendamentoDTO {

	private Long idSistema;
	private Long idRegistro;
	private Long idModeloOcr;
	private String pathImagem;
	private String hashChecksumModeloOcr;
	private String extensaoImagem;
	private Boolean resize = true;
	private Boolean deskew = true;
	private String[] pathsImagensSecundarias;
	private String imagem;

	public Long getIdSistema() {
		return idSistema;
	}

	public void setIdSistema(Long idSistema) {
		this.idSistema = idSistema;
	}

	public Long getIdRegistro() {
		return idRegistro;
	}

	public void setIdRegistro(Long idRegistro) {
		this.idRegistro = idRegistro;
	}

	public Long getIdModeloOcr() {
		return idModeloOcr;
	}

	public void setIdModeloOcr(Long idModeloOcr) {
		this.idModeloOcr = idModeloOcr;
	}

	public String getPathImagem() {
		return pathImagem;
	}

	public void setPathImagem(String pathImagem) {
		this.pathImagem = pathImagem;
	}

	public String getHashChecksumModeloOcr() {
		return hashChecksumModeloOcr;
	}

	public void setHashChecksumModeloOcr(String hashChecksumModeloOcr) {
		this.hashChecksumModeloOcr = hashChecksumModeloOcr;
	}

	public String getExtensaoImagem() {
		return extensaoImagem;
	}

	public void setExtensaoImagem(String extensaoImagem) {
		this.extensaoImagem = extensaoImagem;
	}

	public void setResize(Boolean resize) {
		this.resize = resize;
	}

	public Boolean getResize() {
		return resize;
	}

	public void setDeskew(Boolean deskew) {
		this.deskew = deskew;
	}

	public Boolean getDeskew() {
		return deskew;
	}

	public String[] getPathsImagensSecundarias() {
		return pathsImagensSecundarias;
	}

	public void setPathsImagensSecundarias(String[] pathsImagensSecundarias) {
		this.pathsImagensSecundarias = pathsImagensSecundarias;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}
}
