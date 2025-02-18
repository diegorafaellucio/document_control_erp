package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.MotivoDesativacaoUsuario;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestDesativarUsuario")
public class RequestDesativarUsuario extends SuperVo {

    @ApiModelProperty(notes = "Motivo da desativação do usuário.")
    private MotivoDesativacaoUsuario motivo;

    @NotNull
    public MotivoDesativacaoUsuario getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoDesativacaoUsuario motivo) {
        this.motivo = motivo;
    }
}
