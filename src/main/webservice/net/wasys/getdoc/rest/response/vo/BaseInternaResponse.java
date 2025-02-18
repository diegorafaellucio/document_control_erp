package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "BaseInternaResponse")
public class BaseInternaResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Descrição.")
    private String descricao;

    @ApiModelProperty(value = "Colunas de Unicidade.")
    private String colunasUnicidade;

    @ApiModelProperty(value = "Ativa.")
    private boolean ativa;

    @ApiModelProperty(value = "Data de alteração.")
    private Date dataAlteracao;

    @ApiModelProperty(value = "Colunas de label.")
    private String colunaLabel;

    public BaseInternaResponse() {
    }

    public BaseInternaResponse(BaseInterna baseInterna) {
        this.id = baseInterna.getId();
        this.nome = baseInterna.getNome();
        this.descricao = baseInterna.getDescricao();
        this.ativa = baseInterna.getAtiva();
        this.colunasUnicidade = baseInterna.getColunasUnicidade();
        this.colunaLabel = baseInterna.getColunaLabel();
        this.dataAlteracao = baseInterna.getDataAlteracao();
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

    public Date getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Date dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getColunaLabel() {
        return colunaLabel;
    }

    public void setColunaLabel(String colunaLabel) {
        this.colunaLabel = colunaLabel;
    }
}
