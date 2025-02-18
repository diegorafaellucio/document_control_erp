package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Role;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AbaPrincipal;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;


@ApiModel(value = "UsuarioResponse")
public class UsuarioResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "nome")
    private String nome;

    @ApiModelProperty(value = "email")
    private String email;

    @ApiModelProperty(value = "RoleResponse")
    private List<RoleResponse> roles;

    @ApiModelProperty(value = "RoleGD")
    private RoleGD roleGd;

    @ApiModelProperty(value = "A aba principal é a primeira a ser exibida na tela de detalhes do processo")
    private AbaPrincipal abaPrincipal;

    public UsuarioResponse(){}

    public UsuarioResponse(Usuario usuario){
        if(usuario != null) {
            this.id = usuario.getId();
            this.nome = usuario.getNome();
            this.email = usuario.getEmail();
            this.roleGd = usuario.getRoleGD();

            if (usuario.getRoles() != null) {
                this.roles = new ArrayList<>();

                for (Role role : usuario.getRoles()) {
                    this.roles.add(new RoleResponse(role));
                }
            }

            Subperfil subperfil = usuario.getSubperfilAtivo();
            if(subperfil != null) {
                this.abaPrincipal = subperfil.getAbaPrincipal();
            }
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleResponse> roles) {
        this.roles = roles;
    }

    public RoleGD getRoleGd() {
        return roleGd;
    }

    public void setRoleGd(RoleGD roleGd) {
        this.roleGd = roleGd;
    }

    public AbaPrincipal getAbaPrincipal() {
        return abaPrincipal;
    }

    public void setAbaPrincipal(AbaPrincipal abaPrincipal) {
        this.abaPrincipal = abaPrincipal;
    }
}