package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsAtilaVO {

	private String endpointAutenticacao;
	private String grantType;
	private String clientId;
	private String clientSecret;
	private String accountId;
	private String endpointNotificacao;

	public String getEndpointAutenticacao() {
		return endpointAutenticacao;
	}

	public void setEndpointAutenticacao(String endpointAutenticacao) {
		this.endpointAutenticacao = endpointAutenticacao;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
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

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getEndpointNotificacao() {
		return endpointNotificacao;
	}

	public void setEndpointNotificacao(String endpointNotificacao) {
		this.endpointNotificacao = endpointNotificacao;
	}
}
