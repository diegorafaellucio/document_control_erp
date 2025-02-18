package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "StatusProcessoGraduacaoResponse")
public class StatusProcessoGraduacaoResponse {

    @ApiModelProperty(value = "Processo ID")
    private Long id;

    @ApiModelProperty(value = "Status do Processo")
    private StatusProcesso status;

    @ApiModelProperty(value = "Lista de documentos possíveis para o inscrito.")
    private List<DocumentoGraduacaoResponse> documentos;

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

    public List<DocumentoGraduacaoResponse> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentoGraduacaoResponse> documentos) {
        this.documentos = documentos;
    }
}