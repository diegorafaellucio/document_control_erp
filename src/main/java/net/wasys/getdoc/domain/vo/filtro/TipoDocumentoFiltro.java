package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.Resposta;

public class TipoDocumentoFiltro {

    private Long id;
    private String nome;
    private String nomeLike;
    private Long tipoProcessoId;
    private Resposta obrigatorio = Resposta.TODOS;
    private Resposta ativo = Resposta.TODOS;
    private Long codOrigem;

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

    public String getNomeLike() {
        return nomeLike;
    }

    public void setNomeLike(String nomeLike) {
        this.nomeLike = nomeLike;
    }

    public Long getTipoProcessoId() {
        return tipoProcessoId;
    }

    public void setTipoProcessoId(Long tipoProcessoId) {
        this.tipoProcessoId = tipoProcessoId;
    }

    public Resposta getObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(Resposta obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public Resposta getAtivo() {
        return ativo;
    }

    public void setAtivo(Resposta ativo) {
        this.ativo = ativo;
    }

    public Long getCodOrigem() {
        return codOrigem;
    }

    public void setCodOrigem(Long codOrigem) {
        this.codOrigem = codOrigem;
    }
}
