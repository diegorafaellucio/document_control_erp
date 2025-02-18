package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestConcluirProcessos")
public class RequestConcluirProcessos extends SuperVo {

    @ApiModelProperty(notes = "IDs dos processos.")
    private List<Long> processosIds;

    @ApiModelProperty(notes = "ID da nova situação.")
    private Long situacaoId;

    @ApiModelProperty(notes = "Campo de observação.")
    private String observacao;

    @NotNull
    public List<Long> getProcessosIds() {
        return processosIds;
    }

    public void setProcessosIds(List<Long> processosIds) {
        this.processosIds = processosIds;
    }

    @NotNull
    public Long getSituacaoId() {
        return situacaoId;
    }

    public void setSituacaoId(Long situacaoId) {
        this.situacaoId = situacaoId;
    }

    public String getObservacao() { return observacao; }

    public void setObservacao(String observacao) { this.observacao = observacao; }
}
