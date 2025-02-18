package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsOcrSpaceVO {

	private String endpoint;
	private String apikey;
	private String idioma;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getIdioma() {	return idioma; }

	public void setIdioma(String idioma) { this.idioma = idioma; }
}
