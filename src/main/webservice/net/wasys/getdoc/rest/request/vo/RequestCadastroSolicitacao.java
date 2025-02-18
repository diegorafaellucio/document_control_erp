package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestCadastroSolicitacao")
public class RequestCadastroSolicitacao extends SuperVo {

    @ApiModelProperty(notes = "ID da subÁrea")
    private Long subAreaId;

    @ApiModelProperty(notes = "Observação.")
    private String observacao;

    @ApiModelProperty(notes = "Lista de checksum dos arquivos a serem anexados.")
    private List<String> anexos;

    @NotNull
    public Long getSubAreaId() {
        return subAreaId;
    }

    public void setSubAreaId(Long subAreaId) {
        this.subAreaId = subAreaId;
    }

    @NotNull
    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<String> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }
}
