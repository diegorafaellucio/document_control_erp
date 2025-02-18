package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "PermissaoTipoProcessoResponse")
public class PermissaoTipoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "Status")
    private PermissaoTP permissao;

    @ApiModelProperty
    private String descricao;

    public PermissaoTP getPermissao() {
        return permissao;
    }

    public void setPermissao(PermissaoTP permissao) {
        this.permissao = permissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}