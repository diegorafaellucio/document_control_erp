package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

@ApiModel(value = "RequestHelloCinq")
public class RequestHelloCinq extends SuperVo {

	@ApiModelProperty(notes = "nome")
	private String nome;

	@NotNull(messageKey = "request.required.parameter", nomeCampo="nome")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

}
