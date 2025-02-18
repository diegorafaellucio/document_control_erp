package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "TipoEvidenciaResponse")
public class TipoEvidenciaResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Ativa ou não.")
    private boolean ativo;

    public TipoEvidenciaResponse() {
    }

    public TipoEvidenciaResponse(TipoEvidencia tipoEvidencia) {
        this.id = tipoEvidencia.getId();
        this.descricao = tipoEvidencia.getDescricao();
        this.ativo = tipoEvidencia.getAtivo();
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}