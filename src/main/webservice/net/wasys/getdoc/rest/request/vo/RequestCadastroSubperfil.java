package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestCadastroSubperfil")
public class RequestCadastroSubperfil extends SuperVo {

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Lista dos ID das situações.")
    private List<Long> situacoesId;

    @NotNull
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @NotNull
    public List<Long> getSituacoesId() {
        return situacoesId;
    }

    public void setSituacoesId(List<Long> situacoesId) {
        this.situacoesId = situacoesId;
    }
}

