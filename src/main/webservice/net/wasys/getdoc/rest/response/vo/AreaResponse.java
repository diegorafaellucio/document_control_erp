package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "AreaResponse")
public class AreaResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Horas prazo.")
    private Integer horasPrazo;

    @ApiModelProperty(notes = "Indica se está ativo ou não.")
    private boolean ativo;

    @ApiModelProperty(notes = "Data da atualização.")
    private Date dataAtualizacao;

    public AreaResponse() {
    }

    public AreaResponse(Area area) {
        this.id = area.getId();
        this.descricao = area.getDescricao();
        this.horasPrazo = area.getHorasPrazo();
        this.ativo = area.getAtivo();
        this.dataAtualizacao = area.getDataAtualizacao();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getHorasPrazo() {
        return horasPrazo;
    }

    public void setHorasPrazo(Integer horasPrazo) {
        this.horasPrazo = horasPrazo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}