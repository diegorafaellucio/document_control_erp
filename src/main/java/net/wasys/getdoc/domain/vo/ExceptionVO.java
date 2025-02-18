package net.wasys.getdoc.domain.vo;

import java.util.List;

public class ExceptionVO {

    private Long id;
    private String nome;
    private String quantidade;
    private List<String> servletPaths;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public List<String> getServletPaths() {
        return servletPaths;
    }

    public void setServletPaths(List<String> servletPaths) {
        this.servletPaths = servletPaths;
    }
}
