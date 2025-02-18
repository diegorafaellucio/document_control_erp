package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestDocumentoServico")
public class RequestDocumentoServico extends SuperVo {

    @ApiModelProperty(notes = "Regional.")
    private String regional;

    @ApiModelProperty(notes = "Id Concessionária.")
    private String concessionaria;

    @ApiModelProperty(notes = "Numero do Orcamento.")
    private String numeroOrcamento;

    public String getRegional() {
        return regional;
    }

    public void setRegional(String regional) {
        this.regional = regional;
    }

    public String getConcessionaria() {
        return concessionaria;
    }

    public void setConcessionaria(String concessionaria) {
        this.concessionaria = concessionaria;
    }

    public String getNumeroOrcamento() {
        return numeroOrcamento;
    }

    public void setNumeroOrcamento(String numeroOrcamento) {
        this.numeroOrcamento = numeroOrcamento;
    }
}
