package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestRespostaSolicitacao")
public class RequestRespostaSolicitacao extends SuperVo {

    @ApiModelProperty(notes = "Observação.")
    private String observacao;

    @ApiModelProperty(notes = "Lista de checksum dos arquivos a serem anexados.")
    private List<String> anexos;

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
