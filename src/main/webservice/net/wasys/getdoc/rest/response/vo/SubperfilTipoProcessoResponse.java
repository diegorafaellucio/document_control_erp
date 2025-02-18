package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;
import java.util.Objects;

@ApiModel(value = "SubperfilTipoProcessoResponse")
public class SubperfilTipoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Nome")
    private String nome;

    @ApiModelProperty(value = "Situações")
    private List<SubperfilSituacaoResponse> situacoes;


    public SubperfilTipoProcessoResponse() {
    }

    public SubperfilTipoProcessoResponse(TipoProcesso tipoProcesso) {
        this.id = tipoProcesso.getId();
        this.nome = tipoProcesso.getNome();
    }

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

    public List<SubperfilSituacaoResponse> getSituacoes() {
        return situacoes;
    }

    public void setSituacoes(List<SubperfilSituacaoResponse> situacoes) {
        this.situacoes = situacoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubperfilTipoProcessoResponse)) return false;
        SubperfilTipoProcessoResponse that = (SubperfilTipoProcessoResponse) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}