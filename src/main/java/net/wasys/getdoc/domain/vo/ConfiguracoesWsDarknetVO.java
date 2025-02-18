package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsDarknetVO {

	private String endpoint;
	private String endpointTipificacao;
	private String endpointFullText;
	private String endpointTipificacaoJobOCR;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpointTipificacao() {
		return endpointTipificacao;
	}

	public void setEndpointTipificacao(String endpointTipificacao) {
		this.endpointTipificacao = endpointTipificacao;
	}

	public String getEndpointFullText() {
		return endpointFullText;
	}

	public void setEndpointFullText(String endpointFullText) {
		this.endpointFullText = endpointFullText;
	}

	public String getEndpointTipificacaoJobOCR() {
		return endpointTipificacaoJobOCR;
	}

	public void setEndpointTipificacaoJobOCR(String endpointTipificacaoJobOCR) {
		this.endpointTipificacaoJobOCR = endpointTipificacaoJobOCR;
	}
}
