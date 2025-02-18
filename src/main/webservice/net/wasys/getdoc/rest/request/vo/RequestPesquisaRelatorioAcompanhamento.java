package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.bean.RelatorioAcompanhamentoBean;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestPesquisaRelatorioAcompanhamento")
public class RequestPesquisaRelatorioAcompanhamento extends SuperVo {

    @ApiModelProperty(notes = "Padrão linha")
    private RelatorioAcompanhamentoBean.PadraoLinha padraoLinha;

    @ApiModelProperty(notes = "Lista de ID tipos de processo.")
    private List<Long> tipoProcesso;

    public RelatorioAcompanhamentoBean.PadraoLinha getPadraoLinha() {
        return padraoLinha;
    }

    public void setPadraoLinha(RelatorioAcompanhamentoBean.PadraoLinha padraoLinha) {
        this.padraoLinha = padraoLinha;
    }

    public List<Long> getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(List<Long> tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }
}
