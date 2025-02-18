package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.Date;

@ApiModel(value = "RequestCadastroFeriado")
public class RequestCadastroFeriado extends SuperVo {

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Data.")
    private Date data;

    @NotNull
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @NotNull
    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}

