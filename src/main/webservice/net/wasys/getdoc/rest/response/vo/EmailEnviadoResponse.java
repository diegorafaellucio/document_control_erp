package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApiModel(value = "EmailEnviadoResponse")
public class EmailEnviadoResponse extends EmailResponse {

    @ApiModelProperty(value = "Email de resposta vinculado ao email enviado..")
    private List<EmailRecebidoResponse> emailsRecebidos;

    public EmailEnviadoResponse(){}


    public EmailEnviadoResponse(EmailVO emailVO) {
        super(emailVO);
        if(CollectionUtils.isNotEmpty(emailVO.getEmailsRecebidos())){
            this.emailsRecebidos = new ArrayList<>();

            for (EmailRecebido emailsRecebido : emailVO.getEmailsRecebidos()) {
                this.emailsRecebidos.add(new EmailRecebidoResponse(emailsRecebido));
            }
        }
    }

    public List<EmailRecebidoResponse> getEmailsRecebidos() {
        return emailsRecebidos;
    }

    public void setEmailsRecebidos(List<EmailRecebidoResponse> emailsRecebidos) {
        this.emailsRecebidos = emailsRecebidos;
    }
}