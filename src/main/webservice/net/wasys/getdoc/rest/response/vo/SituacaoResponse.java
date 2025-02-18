package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.collections.CollectionUtils;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApiModel(value = "SituacaoResponse")
public class SituacaoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Descrição")
    private String descricao;

    @ApiModelProperty(value = "Nome")
    private String nome;
//    private TipoProcessoResponse tipoProcesso;
//    private List<ProximaSituacaoResponse> proximas;

    public SituacaoResponse() {
    }

    public SituacaoResponse(Situacao situacao) {
        if (situacao != null) {
            this.id = situacao.getId();
            this.nome = situacao.getNome();
            this.descricao = situacao.getTipoProcesso().getNome() + " > " + this.nome;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

//    public List<ProximaSituacaoResponse> getProximas() {
//        return proximas;
//    }
//
//    public void setProximas(List<ProximaSituacaoResponse> proximas) {
//        this.proximas = proximas;
//    }
//
//    public TipoProcessoResponse getTipoProcesso() {
//        return tipoProcesso;
//    }
//
//    public void setTipoProcesso(TipoProcessoResponse tipoProcesso) {
//        this.tipoProcesso = tipoProcesso;
//    }
}