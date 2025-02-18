package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestPesquisaRelatorioAcompanhamentoAberto")
public class RequestPesquisaRelatorioAcompanhamentoAberto extends SuperVo {

    @ApiModelProperty(notes = "Lista de ID tipos de processo.")
    private List<Long> tipoProcesso;

    public List<Long> getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(List<Long> tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }
}
