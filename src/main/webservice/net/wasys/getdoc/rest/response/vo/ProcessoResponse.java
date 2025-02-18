package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "ProcessoResponse")
public class ProcessoResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Status.")
    private StatusProcesso status;

    @ApiModelProperty(notes = "Número.")
    private String numero;

    @ApiModelProperty(notes = "Tipo.")
    private String tipo;

    @ApiModelProperty(notes = "Situação.")
    private String situacao;

    @ApiModelProperty(notes = "Data de envio.")
    private Date dataEnvio;

    @ApiModelProperty(notes = "Autor.")
    private String autor;

    @ApiModelProperty(notes = "Analista.")
    private String analista;

    @ApiModelProperty(notes = "Nome completo")
    private String nomeCompleto;

    @ApiModelProperty(notes = "Status OCR")
    private StatusOcr statusOcr;

    @ApiModelProperty(notes = "Orçamento/Aprovação")
    private String orcamentoAprovacao;

    @ApiModelProperty(notes = "Nome concessionária")
    private String nomeConcessionaria;

    @ApiModelProperty(notes = "Evidência não lida")
    private Boolean evidenciaNaoLida;

    @ApiModelProperty(notes = "Reenvio não lido")
    private Boolean reenvioNaoLido;

    @ApiModelProperty(notes = "E-mail não lido")
    private Boolean possuiEmailNaoLido;

    public ProcessoResponse() {
    }

    public ProcessoResponse(ProcessoVO processoVO) {
        Processo processo = processoVO.getProcesso();
        TipoProcesso tipoProcesso = processo.getTipoProcesso();
        Situacao situacao = processo.getSituacao();
        Date dataEnvioAnalise = processo.getDataEnvioAnalise();
        Usuario autor = processo.getAutor();
        RoleGD roleGD = autor.getRoleGD();
        Usuario analista = processo.getAnalista();

        this.id = processo.getId();
        this.status = processo.getStatus();
        this.numero = processoVO.getNumero();
        this.tipo = tipoProcesso.getNome();

        if(situacao != null){
            this.situacao = situacao.getNome();
        }

        if(dataEnvioAnalise != null){
            this.dataEnvio = dataEnvioAnalise;
        }

        if(roleGD != null){
            this.autor = roleGD.name();
        }

        if(analista != null){
            this.analista = analista.getNome();
        }

        //this.nomeCompleto = processoVO.getNome();
        this.statusOcr = processoVO.getStatusOcr();
        //this.orcamentoAprovacao = processoVO.getOrcamento();
        this.reenvioNaoLido = processoVO.getReenvioNaoLido();
        this.evidenciaNaoLida = processoVO.getEvidenciaNaoLida();
        this.possuiEmailNaoLido = processoVO.getPossuiEmailNaoLido();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusProcesso status) {
        this.status = status;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getAnalista() {
        return analista;
    }

    public void setAnalista(String analista) {
        this.analista = analista;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public StatusOcr getStatusOcr() { return statusOcr; }

    public void setStatusOcr(StatusOcr statusOcr) { this.statusOcr = statusOcr; }

    public String getOrcamentoAprovacao() { return orcamentoAprovacao; }

    public void setOrcamentoAprovacao(String orcamentoAprovacao) { this.orcamentoAprovacao = orcamentoAprovacao; }

    public String getNomeConcessionaria() { return nomeConcessionaria; }

    public void setNomeConcessionaria(String nomeConcessionaria) { this.nomeConcessionaria = nomeConcessionaria; }

    public Boolean getEvidenciaNaoLida() { return evidenciaNaoLida; }

    public void setEvidenciaNaoLida(Boolean evidenciaNaoLida) { this.evidenciaNaoLida = evidenciaNaoLida; }

    public Boolean getReenvioNaoLido() { return reenvioNaoLido; }

    public void setReenvioNaoLido(Boolean reenvioNaoLido) { this.reenvioNaoLido = reenvioNaoLido; }

    public Boolean getPossuiEmailNaoLido() { return possuiEmailNaoLido; }

    public void setPossuiEmailNaoLido(Boolean possuiEmailNaoLido) { this.possuiEmailNaoLido = possuiEmailNaoLido; }
}