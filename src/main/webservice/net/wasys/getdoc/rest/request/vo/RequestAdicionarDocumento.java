package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

/**
 *
 */
@ApiModel(value = "RequestAdicionarDocumento")
public class RequestAdicionarDocumento extends SuperVo {

    @ApiModelProperty(notes = "ID do documento.")
    private Long documentoId;

    @NotNull
    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }
}
