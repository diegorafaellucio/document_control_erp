package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestCadastroModeloOcr")
public class RequestCadastroModeloOcr extends SuperVo {

    @ApiModelProperty(value = "Descrição.")
    private String descricao;

    @ApiModelProperty(value = "Largura em px")
    private int largura;

    @ApiModelProperty(value = "Altura em px")
    private int altura;

    @ApiModelProperty(value = "Ativo?")
    private boolean ativo;

    @ApiModelProperty(notes = "Lista de checksum dos arquivos a serem anexados.")
    private List<String> anexos;

    @NotNull
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getLargura() {
        return largura;
    }

    public void setLargura(int largura) {
        this.largura = largura;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<String> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }
}