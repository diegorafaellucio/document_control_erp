package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "AreaResponse")
public class SubAreaResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Emails.")
    private String emails;

    @ApiModelProperty(notes = "Ativo.")
    private boolean ativo;

    @ApiModelProperty(notes = "Data atualização.")
    private Date dataAtualizacao;

    public SubAreaResponse() {
    }

    public SubAreaResponse(Subarea subarea) {
        this.id = subarea.getId();
        this.descricao = subarea.getDescricao();
        this.ativo = subarea.getAtivo();
        this.emails = subarea.getEmails();
        this.dataAtualizacao = subarea.getDataAtualizacao();
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

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
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