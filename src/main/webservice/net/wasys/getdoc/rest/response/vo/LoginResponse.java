package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Role;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AbaPrincipal;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@ApiModel(value="LoginResponse")
public class LoginResponse extends SuperVo {

    @ApiModelProperty(notes = "Nome do usuário logado")
    private String nome;

    @ApiModelProperty(notes = "Email do usuário logado")
    private String email;

    @ApiModelProperty(notes = "Permissões")
    private List<RoleResponse> roles;

    @ApiModelProperty(notes = "RoleGD")
    private RoleGD roleGd;

    @ApiModelProperty(value = "A aba principal é a primeira a ser exibida na tela de detalhes do processo")
    private AbaPrincipal abaPrincipal;

    @ApiModelProperty(value = "Data de expiração da Senha")
    private Date dataExpiracaoSenha;

    public LoginResponse() {
    }

    public LoginResponse(SessaoHttpRequest sessaoHttpRequest) {
        Usuario usuario = sessaoHttpRequest.getUsuario();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.roleGd = usuario.getRoleGD();

        Set<Role> roles = usuario.getRoles();
        if (roles != null) {
            this.roles = new ArrayList<>();

            for (Role role : roles) {
                this.roles.add(new RoleResponse(role));
            }
        }

        Subperfil subperfil = usuario.getSubperfilAtivo();
        if(subperfil != null) {
            this.abaPrincipal = subperfil.getAbaPrincipal();
        }

        Date dataExpiracaoSenha = usuario.getDataExpiracaoSenha();
        if(dataExpiracaoSenha != null){
            this.dataExpiracaoSenha = dataExpiracaoSenha;
        }

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

    public Date getDataExpiracaoSenha() { return dataExpiracaoSenha; }

    public void setDataExpiracaoSenha(Date dataExpiracaoSenha) { this.dataExpiracaoSenha = dataExpiracaoSenha; }
}