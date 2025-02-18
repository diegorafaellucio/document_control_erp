package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.rest.annotations.NotNull;
import net.wasys.getdoc.rest.response.vo.SituacaoResponse;
import net.wasys.getdoc.rest.response.vo.StatusPrazoResponse;
import net.wasys.getdoc.rest.response.vo.StatusProcessoResponse;
import net.wasys.getdoc.rest.response.vo.TipoProcessoResponse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestTrocarAnalista")
public class RequestTrocarAnalista extends SuperVo {

    @ApiModelProperty(notes = "IDs dos processos.")
    private List<Long> processoId;

    @ApiModelProperty(notes = "ID do novo analista.")
    private Long analistaId;

    @NotNull
    public List<Long> getProcessoId() {
        return processoId;
    }

    public void setProcessoId(List<Long> processoId) {
        this.processoId = processoId;
    }

    @NotNull
    public Long getAnalistaId() {
        return analistaId;
    }

    public void setAnalistaId(Long analistaId) {
        this.analistaId = analistaId;
    }
}
