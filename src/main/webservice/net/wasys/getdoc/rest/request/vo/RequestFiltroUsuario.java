package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.RoleGD;

/**
 *
 */
@ApiModel(value = "RequestFiltroUsuario")
public class RequestFiltroUsuario extends SuperVo {

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Login.")
    private String login;

    @ApiModelProperty(notes = "RoleGD.")
    private RoleGD roleGd;

    @ApiModelProperty(notes = "ID do Subperfil.")
    private Long subperfilId;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public RoleGD getRoleGd() {
        return roleGd;
    }

    public void setRoleGd(RoleGD roleGd) {
        this.roleGd = roleGd;
    }

    public Long getSubperfilId() {
        return subperfilId;
    }

    public void setSubperfilId(Long subperfilId) {
        this.subperfilId = subperfilId;
    }
}
