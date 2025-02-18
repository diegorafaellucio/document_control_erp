package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(value = "RequestProcessoTransformacao")
public class RequestProcessoTransformacao {

    @ApiModelProperty(value = "id da transformação.")
    private Long id;

    @ApiModelProperty(value = "id da base transformação.")
    private String baseRegistroID;

    @ApiModelProperty(value = "id do processo.")
    private String processoID;

    @ApiModelProperty(value = "Valor da transformação.")
    private Float valor;

    @ApiModelProperty(value = "Detalhes da transformação.")
    private Map detalhesTransformacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessoID() {
        return processoID;
    }

    public void setProcessoID(String processoID) {
        this.processoID = processoID;
    }

    public String getBaseRegistroID() {
        return baseRegistroID;
    }

    public void setBaseRegistroID(String baseRegistroID) {
        this.baseRegistroID = baseRegistroID;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public Map getDetalhesTransformacao() {
        return detalhesTransformacao;
    }

    public void setDetalhesTransformacao(Map detalhesTransformacao) {
        this.detalhesTransformacao = detalhesTransformacao;
    }
}
