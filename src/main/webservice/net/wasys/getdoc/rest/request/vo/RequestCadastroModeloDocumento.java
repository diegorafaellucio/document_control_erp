package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

@ApiModel(value = "RequestCadastroModeloDocumento")
public class RequestCadastroModeloDocumento extends SuperVo {

    @ApiModelProperty(value = "Descrição.")
    private String descricao;

    @ApiModelProperty(value = "Palavras esperadas separadas pelo caráctere ;")
    private String palavrasEsperadas;

    @ApiModelProperty(value = "Palavras excludentes separadas pelo caráctere ;")
    private String palavrasExcludentes;

    @ApiModelProperty(value = "Ativo?")
    private boolean ativo;

    @NotNull
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @NotNull
    public String getPalavrasEsperadas() {
        return palavrasEsperadas;
    }

    public void setPalavrasEsperadas(String palavrasEsperadas) {
        this.palavrasEsperadas = palavrasEsperadas;
    }

    @NotNull
    public String getPalavrasExcludentes() {
        return palavrasExcludentes;
    }

    public void setPalavrasExcludentes(String palavrasExcludentes) {
        this.palavrasExcludentes = palavrasExcludentes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}