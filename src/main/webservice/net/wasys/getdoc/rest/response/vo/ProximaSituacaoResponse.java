package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "ProximaSituacaoResponse")
public class ProximaSituacaoResponse extends SuperVo {


    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Situação atual")
    private String nome;


    public ProximaSituacaoResponse() {
    }

    public ProximaSituacaoResponse(ProximaSituacao proximaSituacao) {
        this.id = proximaSituacao.getId();
        this.nome = proximaSituacao.getProxima().getNome();
    }
    public ProximaSituacaoResponse(Situacao situacao) {
        this.id = situacao.getId();
        this.nome = situacao.getNome();
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
}