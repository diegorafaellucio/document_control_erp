package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "RelatorioAcompanhamentoResponse")
public class RelatorioAcompanhamentoResponse extends SuperVo {

    @ApiModelProperty(value = "Nome da linha.")
    private String nomeLinha;

    @ApiModelProperty(value = "Total.")
    private int total;

    @ApiModelProperty(value = "Atrasados.")
    private int atrasados;

    @ApiModelProperty(value = "Alertas.")
    private int alertas;

    @ApiModelProperty(value = "Ok.")
    private int ok;

    public RelatorioAcompanhamentoResponse(){}

    public RelatorioAcompanhamentoResponse(RelatorioAcompanhamentoVO rVo) {
        this.nomeLinha = rVo.getNomeLinha();
        this.total = rVo.getTotal();
        this.atrasados = rVo.getAtrasados();
        this.alertas = rVo.getAlertas();
        this.ok = rVo.getOk();
    }

    public String getNomeLinha() {
        return nomeLinha;
    }

    public void setNomeLinha(String nomeLinha) {
        this.nomeLinha = nomeLinha;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAtrasados() {
        return atrasados;
    }

    public void setAtrasados(int atrasados) {
        this.atrasados = atrasados;
    }

    public int getAlertas() {
        return alertas;
    }

    public void setAlertas(int alertas) {
        this.alertas = alertas;
    }

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }
}