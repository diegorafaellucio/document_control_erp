package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.response.vo.agenteCertificadoResponse.AgentesCertificadosItem;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestDadosGarantia")
public class RequestDadosGarantia extends SuperVo {

    @ApiModelProperty(notes = "Numero do Orcamento.")
    private String numeroOrcamento;

    @ApiModelProperty(notes = "Id Concessionária.")
    private String concessionaria;

    @ApiModelProperty(notes = "codigoFipe.")
    private String codigoFipe;

    @ApiModelProperty(notes = "Codigo Estado Licenciamento.")
    private String codigoEstadoLicenciamento;

    @ApiModelProperty(notes = "Codigo Estado Placa.")
    private String codigoEstadoPlaca;

    @ApiModelProperty(notes = "Codigo Garantia")
    private String codigoGarantia;

    @ApiModelProperty(notes = "Codigo Obj Financiado")
    private String codigoObjFinanciado;

    @ApiModelProperty(notes = "Codigo Renavam")
    private String codigoRenavam;

    @ApiModelProperty(notes = "Desc Obj Financiado")
    private String descObjFinanciado;

    @ApiModelProperty(notes = "Desc Proc Veiculo")
    private String descProcVeiculo;

    @ApiModelProperty(notes = "get Descricao Chassi")
    private String getDescricaoChassi;

    @ApiModelProperty(notes = "Descricao Cor")
    private String descricaoCor;

    @ApiModelProperty(notes = "Descricao Marca")
    private String descricaoMarca;

    @ApiModelProperty(notes = "Descricao Modelo")
    private String descricaoModelo;

    @ApiModelProperty(notes = "Descricao Placa")
    private String descricaoPlaca;

    @ApiModelProperty(notes = "Desc Tipo Combustivel")
    private String descTipoCombustivel;

    @ApiModelProperty(notes = "Agente Certificado")
    private AgentesCertificadosItem agentesCertificados;

    public RequestDadosGarantia() {
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

    public String getCodigoFipe() {
        return codigoFipe;
    }

    public void setCodigoFipe(String codigoFipe) {
        this.codigoFipe = codigoFipe;
    }

    public String getCodigoEstadoLicenciamento() {
        return codigoEstadoLicenciamento;
    }

    public void setCodigoEstadoLicenciamento(String codigoEstadoLicenciamento) {
        this.codigoEstadoLicenciamento = codigoEstadoLicenciamento;
    }

    public String getCodigoEstadoPlaca() {
        return codigoEstadoPlaca;
    }

    public void setCodigoEstadoPlaca(String codigoEstadoPlaca) {
        this.codigoEstadoPlaca = codigoEstadoPlaca;
    }

    public String getCodigoGarantia() {
        return codigoGarantia;
    }

    public void setCodigoGarantia(String codigoGarantia) {
        this.codigoGarantia = codigoGarantia;
    }

    public String getCodigoObjFinanciado() {
        return codigoObjFinanciado;
    }

    public void setCodigoObjFinanciado(String codigoObjFinanciado) {
        this.codigoObjFinanciado = codigoObjFinanciado;
    }

    public String getCodigoRenavam() {
        return codigoRenavam;
    }

    public void setCodigoRenavam(String codigoRenavam) {
        this.codigoRenavam = codigoRenavam;
    }

    public String getDescObjFinanciado() {
        return descObjFinanciado;
    }

    public void setDescObjFinanciado(String descObjFinanciado) {
        this.descObjFinanciado = descObjFinanciado;
    }

    public String getDescProcVeiculo() {
        return descProcVeiculo;
    }

    public void setDescProcVeiculo(String descProcVeiculo) {
        this.descProcVeiculo = descProcVeiculo;
    }

    public String getGetDescricaoChassi() {
        return getDescricaoChassi;
    }

    public void setGetDescricaoChassi(String getDescricaoChassi) {
        this.getDescricaoChassi = getDescricaoChassi;
    }

    public String getDescricaoCor() {
        return descricaoCor;
    }

    public void setDescricaoCor(String descricaoCor) {
        this.descricaoCor = descricaoCor;
    }

    public String getDescricaoMarca() {
        return descricaoMarca;
    }

    public void setDescricaoMarca(String descricaoMarca) {
        this.descricaoMarca = descricaoMarca;
    }

    public String getDescricaoModelo() {
        return descricaoModelo;
    }

    public void setDescricaoModelo(String descricaoModelo) {
        this.descricaoModelo = descricaoModelo;
    }

    public String getDescricaoPlaca() {
        return descricaoPlaca;
    }

    public void setDescricaoPlaca(String descricaoPlaca) {
        this.descricaoPlaca = descricaoPlaca;
    }

    public String getDescTipoCombustivel() {
        return descTipoCombustivel;
    }

    public void setDescTipoCombustivel(String descTipoCombustivel) {
        this.descTipoCombustivel = descTipoCombustivel;
    }

    public AgentesCertificadosItem getAgentesCertificados() {
        return agentesCertificados;
    }

    public void setAgentesCertificados(AgentesCertificadosItem agentesCertificados) {
        this.agentesCertificados = agentesCertificados;
    }
}
