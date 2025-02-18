package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "OpcoesBaseInternaResponse")
public class OpcoesBaseInternaResponse {

    @ApiModelProperty(value = "ID.")
    private String id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public OpcoesBaseInternaResponse() {
    }

    public OpcoesBaseInternaResponse(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
