package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;

import java.util.Date;

/**
 *
 */
@ApiModel(value = "RequestRelatorioGeral")
public class RequestRelatorioGeral extends SuperVo {

    @ApiModelProperty(notes = "Data início.")
    private Date dataInicio;

    @ApiModelProperty(notes = "Data fim.")
    private Date dataFim;

    @ApiModelProperty(notes = "Tipo.")
    private RelatorioGeralFiltro.Tipo tipo;

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public RelatorioGeralFiltro.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(RelatorioGeralFiltro.Tipo tipo) {
        this.tipo = tipo;
    }
}
