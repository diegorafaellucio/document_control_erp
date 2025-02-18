package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestCampo")
public class RequestCampo extends SuperVo {

    @ApiModelProperty(notes = "ID do tipo de campo.")
    private Long tipoCampoId;

    @ApiModelProperty(notes = "Valor do campo.")
    private String valor;

    @ApiModelProperty(notes = "Opcão ID.")
    private String opcaoId;


    public Long getTipoCampoId() {
        return tipoCampoId;
    }

    public void setTipoCampoId(Long tipoCampoId) {
        this.tipoCampoId = tipoCampoId;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getOpcaoId() {
        return opcaoId;
    }

    public void setOpcaoId(String opcaoId) {
        this.opcaoId = opcaoId;
    }
}
