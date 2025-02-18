package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;
import java.util.Map;

@ApiModel(value = "DetalhesSubPerfilResponse")
public class DetalhesSubPerfilResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Tipo de processo.")
    private List<SubperfilTipoProcessoResponse> listTipoProcesso;

    public DetalhesSubPerfilResponse() {
    }

    public DetalhesSubPerfilResponse(Subperfil subperfil) {
        this.id = subperfil.getId();
        this.descricao = subperfil.getDescricao();
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

    public List<SubperfilTipoProcessoResponse> getListTipoProcesso() {
        return listTipoProcesso;
    }

    public void setListTipoProcesso(List<SubperfilTipoProcessoResponse> listTipoProcesso) {
        this.listTipoProcesso = listTipoProcesso;
    }
}