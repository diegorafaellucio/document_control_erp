package net.wasys.getdoc.domain.vo;

public class ReaproveitamentoCampoVO {

    private String grupo;
    private String campo;
    private String valor;

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "ReaproveitamentoCampoVO{" +
                "grupo='" + grupo + '\'' +
                ", campo='" + campo + '\'' +
                ", valor='" + valor + '\'' +
                '}';
    }
}
