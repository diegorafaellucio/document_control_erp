package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestCriarProcesso")
public class RequestCriarProcesso extends SuperVo {

    @ApiModelProperty(notes = "ID do tipo processo.")
    private Long tipoProcessoid;

    @ApiModelProperty(notes = "Campos do processo.")
    private List<RequestCampo> campos;

    @NotNull
    public Long getTipoProcessoid() {
        return tipoProcessoid;
    }

    public void setTipoProcessoid(Long tipoProcessoid) {
        this.tipoProcessoid = tipoProcessoid;
    }

    @NotNull
    public List<RequestCampo> getCampos() {
        return campos;
    }

    public void setCampos(List<RequestCampo> campos) {
        this.campos = campos;
    }
}
