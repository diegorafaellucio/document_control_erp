package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "NovoProcessoResponse")
public class NovoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Status")
    private StatusProcesso status;

    public NovoProcessoResponse() {
    }

    public NovoProcessoResponse(Processo processo) {
        if(processo != null) {
            setProcesso(processo);
        }
    }

    public void setProcesso(Processo processo) {
        this.id = processo.getId();
        this.status = processo.getStatus();
    }

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
}