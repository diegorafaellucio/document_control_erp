package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "RequestProcessoSeguro")
public class RequestSegurosTransformacoes {

    @ApiModelProperty(value = "Lista de seguros de um processo.")
    private List<RequestProcessoSeguro> Seguros;

    @ApiModelProperty(value = "Lista de transformações de um processo.")
    private List<RequestProcessoTransformacao> transformacoes;

    public List<RequestProcessoSeguro> getSeguros() {
        return Seguros;
    }

    public void setSeguros(List<RequestProcessoSeguro> seguros) {
        this.Seguros = seguros;
    }

    public List<RequestProcessoTransformacao> getTransformacoes() {
        return transformacoes;
    }

    public void setTransformacoes(List<RequestProcessoTransformacao> transformacoes) {
        this.transformacoes = transformacoes;
    }
}
