package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.LogVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

@ApiModel(value = "AcompanhamentoResponse")
public class AcompanhamentoResponse extends SuperVo {

    @ApiModelProperty(value = "ID do Log do Processo.")
    private Long processoLogId;

    @ApiModelProperty(value = "Data.")
    private Date data;

    @ApiModelProperty(value = "Usuário.")
    private String usuario;

    @ApiModelProperty(value = "Ação.")
    private String acao;

    @ApiModelProperty(value = "Observação para ser exibida na lista.")
    private String observacao;

    @ApiModelProperty(value = "Observação para ser exibida no alerta.")
    private String observacaoHtml;

    @ApiModelProperty(value = "Flag para indicar que tem anexos.")
    private boolean possuiAnexos;

    public AcompanhamentoResponse() {
    }

    public AcompanhamentoResponse(LogVO log) {
        if(log.getProcessoLog() != null) {
            this.processoLogId = log.getProcessoLog().getId();
        }
        this.data = log.getData();
        if(log.getUsuario() != null) {
            this.usuario = log.getUsuario().getNome();
        }
        this.observacaoHtml = log.getObservacaoFull();
        if(StringUtils.isNotEmpty(log.getObservacaoFull())) {
            this.observacao = DummyUtils.htmlToString(log.getObservacaoFull());
        }
        this.possuiAnexos = log.getTemAnexo();
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getObservacaoHtml() {
        return observacaoHtml;
    }

    public void setObservacaoHtml(String observacaoHtml) {
        this.observacaoHtml = observacaoHtml;
    }

    public boolean isPossuiAnexos() {
        return possuiAnexos;
    }

    public void setPossuiAnexos(boolean possuiAnexos) {
        this.possuiAnexos = possuiAnexos;
    }

    public Long getProcessoLogId() {
        return processoLogId;
    }

    public void setProcessoLogId(Long processoLogId) {
        this.processoLogId = processoLogId;
    }
}