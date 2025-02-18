package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "ModeloDocumentoResponse")
public class ModeloDocumentoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Ativo?")
    private boolean ativo;

    @ApiModelProperty(value = "Palavras esperadas")
    private String palavrasEsperadas;

    @ApiModelProperty(value = "Palavras excludentes")
    private String palavrasExcludentes;


    public ModeloDocumentoResponse(){}


    public ModeloDocumentoResponse(ModeloDocumento modeloDocumento) {
        this.id = modeloDocumento.getId();
        this.nome = modeloDocumento.getDescricao();
        this.ativo = modeloDocumento.getAtivo();
        this.palavrasEsperadas = modeloDocumento.getPalavrasEsperadas();
        this.palavrasExcludentes = modeloDocumento.getPalavrasExcludentes();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getPalavrasEsperadas() {
        return palavrasEsperadas;
    }

    public void setPalavrasEsperadas(String palavrasEsperadas) {
        this.palavrasEsperadas = palavrasEsperadas;
    }

    public String getPalavrasExcludentes() {
        return palavrasExcludentes;
    }

    public void setPalavrasExcludentes(String palavrasExcludentes) {
        this.palavrasExcludentes = palavrasExcludentes;
    }
}