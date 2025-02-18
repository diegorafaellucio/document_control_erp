package net.wasys.getdoc.rest.response.vo;


import net.wasys.getdoc.rest.request.vo.SuperVo;

/**
 * Centraliza as informações que são necessárias para montar o HttResponse.
 */
public class ArquivoDownload extends SuperVo {

    private Long id;
    private Long tamanho;
    private String nome;
    private String path;
    private String extensao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public void setTamanho(Long tamanho) {
        this.tamanho = tamanho;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getExtensao() {
        return extensao;
    }

    public void setExtensao(String extensao) {
        this.extensao = extensao;
    }
}