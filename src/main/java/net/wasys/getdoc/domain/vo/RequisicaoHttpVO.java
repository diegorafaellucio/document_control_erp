package net.wasys.getdoc.domain.vo;

import java.util.Map;

public class RequisicaoHttpVO {

    String method;
    Long quantidade;
    Map<String, Long> respostasHttp;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public Map<String, Long> getRespostasHttp() {
        return respostasHttp;
    }

    public void setRespostasHttp(Map<String, Long> respostasHttp) {
        this.respostasHttp = respostasHttp;
    }
}
