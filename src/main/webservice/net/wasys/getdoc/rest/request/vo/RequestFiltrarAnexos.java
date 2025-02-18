package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
@ApiModel(value = "RequestFiltrarAnexos")
public class RequestFiltrarAnexos extends SuperVo {

    @ApiModelProperty(notes = "Data início.")
    private Date dataInicio;

    @ApiModelProperty(notes = "Data fim.")
    private Date dataFim;

    @ApiModelProperty(notes = "Texto de busca.")
    private String textoBusca;

    @ApiModelProperty(notes = "Ação.")
    private List<AcaoProcesso> acao;

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

    public String getTextoBusca() {
        return textoBusca;
    }

    public void setTextoBusca(String textoBusca) {
        this.textoBusca = textoBusca;
    }

    public List<AcaoProcesso> getAcao() {
        return acao;
    }

    public void setAcao(List<AcaoProcesso> acao) {
        this.acao = acao;
    }
}
