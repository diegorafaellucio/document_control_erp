package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "ListaEmailResponse")
public class ListaEmailResponse extends SuperVo {

    @ApiModelProperty(notes = "Indica se possui emails pendentes. Se for > 0, considerar o badge na aba.")
    private int badgeEmailsPendente;

    @ApiModelProperty(notes = "Lista de emails.")
    private List<EmailEnviadoResponse> emails;

    public int getBadgeEmailsPendente() {
        return badgeEmailsPendente;
    }

    public void setBadgeEmailsPendente(int badgeEmailsPendente) {
        this.badgeEmailsPendente = badgeEmailsPendente;
    }

    public List<EmailEnviadoResponse> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailEnviadoResponse> emails) {
        this.emails = emails;
    }
}