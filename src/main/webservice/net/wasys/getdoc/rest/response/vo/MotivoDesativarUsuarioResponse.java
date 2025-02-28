package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.MotivoDesativacaoUsuario;
import net.wasys.getdoc.rest.request.vo.SuperVo;


@ApiModel(value = "MotivoDesativarUsuarioResponse")
public class MotivoDesativarUsuarioResponse extends SuperVo {

    @ApiModelProperty(value = "Motivo")
    private MotivoDesativacaoUsuario motivo;

    @ApiModelProperty(value = "Descrição")
    private String descricao;

    public MotivoDesativarUsuarioResponse(MotivoDesativacaoUsuario motivo, String descricao) {
        this.motivo = motivo;
        this.descricao = descricao;
    }

    public MotivoDesativacaoUsuario getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoDesativacaoUsuario motivo) {
        this.motivo = motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}