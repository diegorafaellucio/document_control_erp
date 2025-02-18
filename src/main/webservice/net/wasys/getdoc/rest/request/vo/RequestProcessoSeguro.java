package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(value = "RequestProcessoSeguro")
public class RequestProcessoSeguro {

    @ApiModelProperty(value = "id do seguro.")
    private Long id;

    @ApiModelProperty(value = "id da base registro.")
    private String baseRegistroID;

    @ApiModelProperty(value = "id do processo.")
    private String processoID;

    @ApiModelProperty(value = "Valor do seguro.")
    private Float valor;

    @ApiModelProperty(value = "Anos do seguro.")
    private String anos;

    @ApiModelProperty(value = "Quilometragem do seguro.")
    private String quilometragem;


    @ApiModelProperty(value = "Detalhes do seguro.")
    private Map detalhesSeguro;

    @ApiModelProperty(value = "Fornecedores dos seguros.")
    private Map<String, String> fornecedor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessoID() {
        return processoID;
    }

    public void setProcessoID(String processoID) {
        this.processoID = processoID;
    }

    public String getBaseRegistroID() {
        return baseRegistroID;
    }

    public void setBaseRegistroID(String baseRegistroID) {
        this.baseRegistroID = baseRegistroID;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public String getAnos() {
        return anos;
    }

    public void setAnos(String anos) {
        this.anos = anos;
    }

    public String getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(String quilometragem) {
        this.quilometragem = quilometragem;
    }

    public Map getDetalhesSeguro() { return detalhesSeguro; }

    public void setDetalhesSeguro(Map detalhesSeguro) {
        this.detalhesSeguro = detalhesSeguro;
    }

    public Map<String, String> getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Map<String, String> fornecedor) {
        this.fornecedor = fornecedor;
    }
}
