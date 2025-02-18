package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "ListaModeloOcrResponse")
public class ListaModeloOcrResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Data de alteração.")
    private Date dataAlteracao;

    @ApiModelProperty(value = "Descrição.")
    private String descricao;

    @ApiModelProperty(value = "Dimensão.")
    private String dimensao;

    @ApiModelProperty(value = "Arquivo.")
    private String arquivo;

    @ApiModelProperty(value = "Ativo?.")
    private boolean ativo;


    public ListaModeloOcrResponse(){}


    public ListaModeloOcrResponse(ModeloOcr modeloOcr) {
        this.id = modeloOcr.getId();
        this.descricao = modeloOcr.getDescricao();
        this.arquivo = modeloOcr.getNomeArquivo();
        this.dataAlteracao = modeloOcr.getDataAlteracao();
        this.ativo = modeloOcr.getAtivo();
        this.dimensao = "Largura: " + modeloOcr.getLargura() + "px Altura: " + modeloOcr.getAltura() + "px";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Date dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDimensao() {
        return dimensao;
    }

    public void setDimensao(String dimensao) {
        this.dimensao = dimensao;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}