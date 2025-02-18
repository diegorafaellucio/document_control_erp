package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.response.vo.DocumentoResponse;

import java.util.List;

@ApiModel(value = "RequestConcluirTarefa")
public class RequestConcluirTarefa extends RequestSolicitacaoProcesso {
    @ApiModelProperty(notes = "Documentos do processo.")
    private List<DocumentoResponse> listaDocumentos;

    public List<DocumentoResponse> getListaDocumentos() {
        return listaDocumentos;
    }

    public void setListaDocumentos(List<DocumentoResponse> listaDocumentos) {
        this.listaDocumentos = listaDocumentos;
    }
}
