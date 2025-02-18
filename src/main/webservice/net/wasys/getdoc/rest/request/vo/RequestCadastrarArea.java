package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestCadastrarArea")
public class RequestCadastrarArea extends SuperVo {

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Prazo.")
    private int prazoHoras;

    @ApiModelProperty(notes = "Ativa.")
    private boolean ativa;

    @ApiModelProperty(notes = "Sub áreas.")
    private List<RequestCadastrarSubArea> subAreas;

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @NotNull
    public int getPrazoHoras() {
        return prazoHoras;
    }

    public void setPrazoHoras(int prazoHoras) {
        this.prazoHoras = prazoHoras;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public List<RequestCadastrarSubArea> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(List<RequestCadastrarSubArea> subAreas) {
        this.subAreas = subAreas;
    }
}