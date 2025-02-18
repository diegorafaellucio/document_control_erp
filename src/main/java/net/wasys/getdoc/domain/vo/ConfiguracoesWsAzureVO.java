package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsAzureVO {

	private String linkAutenticacao;
	private String memberOfEndpoint;
	private String usersEndpoint;
	private String tokenEndpoint;
	private String clientId;
	private String clientSecret;
	private String logoffEndPoint;

	public String getLinkAutenticacao() {
		return linkAutenticacao;
	}

	public void setLinkAutenticacao(String linkAutenticacao) {
		this.linkAutenticacao = linkAutenticacao;
	}

	public String getMemberOfEndpoint() {
		return memberOfEndpoint;
	}

	public void setMemberOfEndpoint(String memberOfEndpoint) {
		this.memberOfEndpoint = memberOfEndpoint;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getLogoffEndPoint() {
		return logoffEndPoint;
	}

	public void setLogoffEndPoint(String logoffEndPoint) {
		this.logoffEndPoint = logoffEndPoint;
	}

	public String getUsersEndpoint() {
		return usersEndpoint;
	}

	public void setUsersEndpoint(String usersEndpoint) {
		this.usersEndpoint = usersEndpoint;
	}
}
