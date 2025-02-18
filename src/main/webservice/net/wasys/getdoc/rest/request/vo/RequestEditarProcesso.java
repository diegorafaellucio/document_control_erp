package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 *
 */
@ApiModel(value = "RequestEditarProcesso")
public class RequestEditarProcesso extends SuperVo {

    @ApiModelProperty(notes = "Map contendo o ID do Campo e seu novo valor.")
    private Map<Long, String> valorCampo;

    @ApiModelProperty(notes = "Lista com seguros de um processo.")
    public List<RequestProcessoSeguro> seguros;

    @ApiModelProperty(notes = "Lista com transformações de um processo.")
    public List<RequestProcessoTransformacao> transformacoes;

    @NotNull
    public Map<Long, String> getValorCampo() {
        return valorCampo;
    }

    public void setValorCampo(Map<Long, String> valorCampo) {
        this.valorCampo = valorCampo;
    }

    public List<RequestProcessoSeguro> getSeguros() {
        return seguros;
    }

    public void setSeguros(List<RequestProcessoSeguro> seguros) {
        this.seguros = seguros;
    }

    public List<RequestProcessoTransformacao> getTransformacoes() {
        return transformacoes;
    }

    public void setTransformacoes(List<RequestProcessoTransformacao> transformacoes) {
        this.transformacoes = transformacoes;
    }
}
