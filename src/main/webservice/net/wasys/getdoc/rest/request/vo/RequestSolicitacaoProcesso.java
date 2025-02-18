package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestSolicitacaoProcesso")
public class RequestSolicitacaoProcesso extends SuperVo {

    @ApiModelProperty(notes = "ID da situação.")
    private Long situacaoId;

    @ApiModelProperty(notes = "Lista de emails que serão notificados.")
    private List<String> emails;

    @ApiModelProperty(notes = "Observação da solicitação.")
    private String observacao;

    @ApiModelProperty(notes = "Lista de checksum dos arquivos a serem anexados.")
    private List<String> anexos;

    @ApiModelProperty(notes = "Lista de destinatários.")
    private List<String> destinatarios;

    @ApiModelProperty(notes = "Campos da solicitacao caso tenha.")
    private List<RequestCampo> campos;

    @NotNull
    public Long getSituacaoId() {
        return situacaoId;
    }

    public void setSituacaoId(Long situacaoId) {
        this.situacaoId = situacaoId;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<String> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }

    public List<String> getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(List<String> destinatarios) {
        this.destinatarios = destinatarios;
    }

    public List<RequestCampo> getCampos() {
        return campos;
    }

    public void setCampos(List<RequestCampo> campos) {
        this.campos = campos;
    }
}
