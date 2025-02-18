package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="RequestListaDocumentosGraduacao")
public class RequestListaDocumentosProxyGraduacao {

    @ApiModelProperty(notes="Processo ID.", required = true)
    private Long processoId;

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}
}
