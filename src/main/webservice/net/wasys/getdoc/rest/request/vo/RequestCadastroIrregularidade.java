package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

@ApiModel(value = "RequestCadastroIrregularidade")
public class RequestCadastroIrregularidade extends SuperVo {

    @ApiModelProperty(notes = "Descrição")
    private String descricao;

    @ApiModelProperty(notes = "Ativo")
    private boolean ativo;

    @NotNull
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
