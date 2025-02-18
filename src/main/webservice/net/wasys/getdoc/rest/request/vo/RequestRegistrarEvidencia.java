package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "RequestRegistrarEvidencia")
public class RequestRegistrarEvidencia extends SuperVo {

    @ApiModelProperty(notes = "ID do TipoEvidencia")
    private Long tipoEvidenciaId;

    @ApiModelProperty(notes = "Observação.")
    private String observacao;

    @ApiModelProperty(notes = "Lista de checksum dos arquivos a serem anexados.")
    private List<String> anexos;

    @ApiModelProperty(notes = "Lista de destinatários.")
    private List<String> destinatarios;

    @NotNull
    public Long getTipoEvidenciaId() {
        return tipoEvidenciaId;
    }

    public void setTipoEvidenciaId(Long tipoEvidenciaId) {
        this.tipoEvidenciaId = tipoEvidenciaId;
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

    public List<String> getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(List<String> destinatarios) {
        this.destinatarios = destinatarios;
    }
}
