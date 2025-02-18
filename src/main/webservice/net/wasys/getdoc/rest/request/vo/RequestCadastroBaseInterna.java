package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestCadastroBaseInterna")
public class RequestCadastroBaseInterna {

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Descrição.")
    private String descricao;

    @ApiModelProperty(value = "Colunas de Unicidade.")
    private String colunasUnicidade;

    @ApiModelProperty(value = "Ativa.")
    private boolean ativa;

    @ApiModelProperty(value = "Colunas de label para o combo.")
    private String colunaLabel;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getColunasUnicidade() {
        return colunasUnicidade;
    }

    public void setColunasUnicidade(String colunasUnicidade) {
        this.colunasUnicidade = colunasUnicidade;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public String getColunaLabel() {
        return colunaLabel;
    }

    public void setColunaLabel(String colunaLabel) {
        this.colunaLabel = colunaLabel;
    }
}
