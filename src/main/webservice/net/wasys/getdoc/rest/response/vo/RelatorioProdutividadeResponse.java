package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "RelatorioProdutividadeResponse")
public class RelatorioProdutividadeResponse extends SuperVo {

    @ApiModelProperty(value = "Visão.")
    private String nomeLinha;

    @ApiModelProperty(value = "Quantidade de requisições.")
    private long requisicoes;

    @ApiModelProperty(value = "Quantidade de atividade.")
    private long atividades;

    @ApiModelProperty(value = "Quantidade de cadastro manual.")
    private long cadastroManual;

    @ApiModelProperty(value = "Quantidade de cadastro automático.")
    private long cadastroAutomatico;

    @ApiModelProperty(value = "Quantidade de cadastro total.")
    private long cadastroTotal;

    @ApiModelProperty(value = "Quantidade de acompanhamento.")
    private long acompanhamento;

    @ApiModelProperty(value = "Quantidade de finalizados em análise.")
    private long finalizadosAnalise;

    @ApiModelProperty(value = "Quantidade de finalizados em acompanhamento.")
    private long finalizadosAcompanhamento;

    @ApiModelProperty(value = "Quantidade de finalizados total.")
    private long finalizadosTotal;

    public RelatorioProdutividadeResponse(){}

    public RelatorioProdutividadeResponse(RelatorioProdutividadeVO rpvo) {
        this.nomeLinha = rpvo.getRegistroDescricao();
        this.acompanhamento = rpvo.getEmAcompanhamento();
        this.atividades = rpvo.getAtividades();
        this.cadastroAutomatico = rpvo.getCadastroAutomatio();
        this.cadastroManual = rpvo.getCadastroManual();
        this.cadastroTotal = rpvo.getCadastroTotal();
        this.finalizadosAcompanhamento = rpvo.getFinalizadosAcompanhamento();
        this.requisicoes = rpvo.getRequisicoes();
        this.finalizadosAnalise = rpvo.getFinalizadosAnalise();
        this.finalizadosTotal = rpvo.getFinalizadosTotal();
    }

    public String getNomeLinha() {
        return nomeLinha;
    }

    public void setNomeLinha(String nomeLinha) {
        this.nomeLinha = nomeLinha;
    }

    public long getRequisicoes() {
        return requisicoes;
    }

    public void setRequisicoes(long requisicoes) {
        this.requisicoes = requisicoes;
    }

    public long getAtividades() {
        return atividades;
    }

    public void setAtividades(long atividades) {
        this.atividades = atividades;
    }

    public long getCadastroManual() {
        return cadastroManual;
    }

    public void setCadastroManual(long cadastroManual) {
        this.cadastroManual = cadastroManual;
    }

    public long getCadastroAutomatico() {
        return cadastroAutomatico;
    }

    public void setCadastroAutomatico(long cadastroAutomatico) {
        this.cadastroAutomatico = cadastroAutomatico;
    }

    public long getCadastroTotal() {
        return cadastroTotal;
    }

    public void setCadastroTotal(long cadastroTotal) {
        this.cadastroTotal = cadastroTotal;
    }

    public long getAcompanhamento() {
        return acompanhamento;
    }

    public void setAcompanhamento(long acompanhamento) {
        this.acompanhamento = acompanhamento;
    }

    public long getFinalizadosAnalise() {
        return finalizadosAnalise;
    }

    public void setFinalizadosAnalise(long finalizadosAnalise) {
        this.finalizadosAnalise = finalizadosAnalise;
    }

    public long getFinalizadosAcompanhamento() {
        return finalizadosAcompanhamento;
    }

    public void setFinalizadosAcompanhamento(long finalizadosAcompanhamento) {
        this.finalizadosAcompanhamento = finalizadosAcompanhamento;
    }

    public long getFinalizadosTotal() {
        return finalizadosTotal;
    }

    public void setFinalizadosTotal(long finalizadosTotal) {
        this.finalizadosTotal = finalizadosTotal;
    }
}