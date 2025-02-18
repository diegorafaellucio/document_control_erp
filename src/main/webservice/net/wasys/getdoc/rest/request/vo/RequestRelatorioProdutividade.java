package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;

import java.util.Date;

/**
 *
 */
@ApiModel(value = "RequestRelatorioProdutividade")
public class RequestRelatorioProdutividade extends SuperVo {

    @ApiModelProperty(notes = "Data início.")
    private Date dataInicio;

    @ApiModelProperty(notes = "Data fim.")
    private Date dataFim;

    @ApiModelProperty(notes = "Tipo.")
    private RelatorioProdutividadeFiltro.Tipo tipo;

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

    public RelatorioProdutividadeFiltro.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(RelatorioProdutividadeFiltro.Tipo tipo) {
        this.tipo = tipo;
    }
}
