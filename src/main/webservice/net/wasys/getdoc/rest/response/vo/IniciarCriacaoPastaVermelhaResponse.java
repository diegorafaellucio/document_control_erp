package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "IniciarCriacaoPastaVermelhaResponse")
public class IniciarCriacaoPastaVermelhaResponse {

	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
