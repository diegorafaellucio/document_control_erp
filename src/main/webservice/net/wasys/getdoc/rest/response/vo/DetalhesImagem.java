package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "DetalhesImagem")
public class DetalhesImagem {

	@ApiModelProperty(notes = "Caminho no file system")
	private String caminho;

	@ApiModelProperty(notes = "Metadados da imagem")
	private String metadados;

	public String getCaminho() {
		return caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
	}

	public String getMetadados() {
		return metadados;
	}

	public void setMetadados(String metadados) {
		this.metadados = metadados;
	}
}

