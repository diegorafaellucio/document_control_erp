package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApiModel(value = "EmailEnviadoResponse")
public class EmailResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Data.")
    private String data;

    @ApiModelProperty(value = "Lista de destinatários.")
    private List<String> destinatariosList;

    @ApiModelProperty(value = "Assunto.")
    private String assunto;

    @ApiModelProperty(value = "Assunto.")
    private String destinatarios;

    @ApiModelProperty(value = "Conteúdo resumido.")
    private String observacaoCurta;

    @ApiModelProperty(value = "Conteudo.")
    private String conteudo;

    @ApiModelProperty(value = "Anexos.")
    private List<ProcessoLogAnexoResponse> anexos;

    public EmailResponse(){}


    public EmailResponse(EmailVO emailVO) {
        this.id = emailVO.getEmailEnviado().getId();
        this.data = "Email enviado " + DummyUtils.formatDateTime(emailVO.getEmailEnviado().getDataEnvio()) + "h.";
        this.destinatariosList = emailVO.getEmailEnviado().getDestinatariosList();
        this.destinatarios = emailVO.getEmailEnviado().getDestinatarios();
        this.assunto = emailVO.getEmailEnviado().getCodigo() + " " + emailVO.getEmailEnviado().getAssunto();
        this.observacaoCurta = emailVO.getLogCriacao().getObservacaoCurta();
        this.conteudo = DummyUtils.stringToHTML(emailVO.getLogCriacao().getObservacao());
        Set<ProcessoLogAnexo> anexos = emailVO.getLogCriacao().getAnexos();
        if(CollectionUtils.isNotEmpty(anexos)){
            this.anexos = new ArrayList<>();

            for (ProcessoLogAnexo anexo : anexos) {
                this.anexos.add(new ProcessoLogAnexoResponse(anexo));
            }

        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getDestinatariosList() {
        return destinatariosList;
    }

    public void setDestinatariosList(List<String> destinatariosList) {
        this.destinatariosList = destinatariosList;
    }

    public String getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(String destinatarios) {
        this.destinatarios = destinatarios;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getObservacaoCurta() {
        return observacaoCurta;
    }

    public void setObservacaoCurta(String observacaoCurta) {
        this.observacaoCurta = observacaoCurta;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public List<ProcessoLogAnexoResponse> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<ProcessoLogAnexoResponse> anexos) {
        this.anexos = anexos;
    }
}