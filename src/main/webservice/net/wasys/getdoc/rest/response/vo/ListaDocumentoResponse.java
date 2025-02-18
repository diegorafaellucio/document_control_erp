package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "ListaDocumentoResponse")
public class ListaDocumentoResponse extends SuperVo {

    @ApiModelProperty(notes = "Indica se possui documentos pendentes. Se for > 0, considerar o badge na aba.")
    private int badgeDocumentosPendente;

    @ApiModelProperty(notes = "Lista de documentos.")
    private List<DocumentoResponse> documentos;

    public int getBadgeDocumentosPendente() {
        return badgeDocumentosPendente;
    }

    public void setBadgeDocumentosPendente(int badgeDocumentosPendente) {
        this.badgeDocumentosPendente = badgeDocumentosPendente;
    }

    public List<DocumentoResponse> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentoResponse> documentos) {
        this.documentos = documentos;
    }
}