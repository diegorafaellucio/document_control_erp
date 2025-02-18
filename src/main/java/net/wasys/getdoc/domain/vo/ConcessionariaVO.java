package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.BaseRegistro;

public class ConcessionariaVO {

    private BaseRegistro baseRegistro;
    private String nome;
    private String razaoSocial;
    private String cnpj;
    private String codigoTab;
    private String codigoFilial;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BaseRegistro getBaseRegistro() {
        return baseRegistro;
    }

    public void setBaseRegistro(BaseRegistro baseRegistro) {
        this.baseRegistro = baseRegistro;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCodigoTab() {
        return codigoTab;
    }

    public void setCodigoTab(String codigoTab) {
        this.codigoTab = codigoTab;
    }

    public String getCodigoFilial() {
        return codigoFilial;
    }

    public void setCodigoFilial(String codigoFilial) {
        this.codigoFilial = codigoFilial;
    }
}
