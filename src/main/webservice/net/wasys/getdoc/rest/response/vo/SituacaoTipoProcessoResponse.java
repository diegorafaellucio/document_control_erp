package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "SituacaoTipoProcessoResponse")
public class SituacaoTipoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Nome")
    private String nome;


    public SituacaoTipoProcessoResponse() {
    }

    public SituacaoTipoProcessoResponse(TipoProcesso tipoProcesso) {
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
}