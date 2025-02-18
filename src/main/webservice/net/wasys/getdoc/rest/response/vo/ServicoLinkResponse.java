package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "ServicoLinkResponse")
public class ServicoLinkResponse extends SuperVo {

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "codigo.")
    private String codigo;

    @ApiModelProperty(notes = "Numero.")
    private String numero;

    public ServicoLinkResponse() {
    }

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