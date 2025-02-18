package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Set;

@ApiModel(value = "EmailRecebidoResponse")
public class EmailRecebidoResponse extends EmailResponse {

    @ApiModelProperty(value = "Quem enviou o email.")
    private String from;

    public EmailRecebidoResponse(){}

    public EmailRecebidoResponse(EmailRecebido emailRecebido) {
        setId(emailRecebido.getEmailEnviado().getId());
        setData(DummyUtils.formatDateTime(emailRecebido.getSentDate()) + "h.");
        this.from = emailRecebido.getEmailFrom();
        setAssunto(emailRecebido.getSubject());
        setObservacaoCurta(emailRecebido.getConteudoCurto());
        setConteudo(DummyUtils.stringToHTML(emailRecebido.getConteudoLong()));
        Set<EmailRecebidoAnexo> anexos = emailRecebido.getAnexos();
        if(CollectionUtils.isNotEmpty(anexos)){
            setAnexos(new ArrayList<>());

            for (EmailRecebidoAnexo anexo : anexos) {
                getAnexos().add(new ProcessoLogAnexoResponse(anexo));
            }
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}