package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsGetDocAlunoVO {

	private String endpointImgDoc;
	private String endpointLogin;
	private String endpointImagensDigitalizadas;
	private String endpointConsultaMatricula;
	private String login;
	private String senha;
	private String linkExtranet;

	public String getEndpointImagensDigitalizadas() {
		return endpointImagensDigitalizadas;
	}

	public void setEndpointImagensDigitalizadas(String endpointImagensDigitalizadas) {
		this.endpointImagensDigitalizadas = endpointImagensDigitalizadas;
	}

	public String getEndpointImgDoc() {
		return endpointImgDoc;
	}

	public void setEndpointImgDoc(String endpointImgDoc) {
		this.endpointImgDoc = endpointImgDoc;
	}

	public String getEndpointLogin() {
		return endpointLogin;
	}

	public void setEndpointLogin(String endpointLogin) {
		this.endpointLogin = endpointLogin;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getEndpointConsultaMatricula() {
		return endpointConsultaMatricula;
	}

	public void setEndpointConsultaMatricula(String endpointConsultaMatricula) {
		this.endpointConsultaMatricula = endpointConsultaMatricula;
	}

	public String getLinkExtranet() {
		return linkExtranet;
	}

	public void setLinkExtranet(String linkExtranet) {
		this.linkExtranet = linkExtranet;
	}
}
