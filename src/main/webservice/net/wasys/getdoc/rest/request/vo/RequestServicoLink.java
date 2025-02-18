package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 */
@ApiModel(value = "RequestServicoLink")
public class RequestServicoLink extends SuperVo {

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "codigo.")
    private String codigo;

    @ApiModelProperty(notes = "Numero.")
    private String numero;


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
