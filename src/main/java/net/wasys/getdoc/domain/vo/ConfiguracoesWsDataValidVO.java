package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsDataValidVO {

	private String endpointLogin;
	private String endpoint;
	private String endpointFace;
	private String consumerKey;
	private String consumerSecret;
	private Integer validadeDataValid;

	public String getEndpointLogin() {
		return endpointLogin;
	}

	public void setEndpointLogin(String endpointLogin) {
		this.endpointLogin = endpointLogin;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpointFace() {
		return endpointFace;
	}

	public void setEndpointFace(String endpointFace) {
		this.endpointFace = endpointFace;
	}

	public Integer getValidadeDataValid() {
		return validadeDataValid;
	}

	public void setValidadeDataValid(Integer validadeDataValid) {
		this.validadeDataValid = validadeDataValid;
	}
}