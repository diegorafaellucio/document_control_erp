package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.request.vo.SuperVo;


@ApiModel(value = "FiltroUsuarioResponse")
public class FiltroUsuarioResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Login.")
    private String login;

    @ApiModelProperty(notes = "RoleGD.")
    private RoleGD roleGd;

    @ApiModelProperty(notes = "Nome do Subperfil.")
    private String subperfil;

    @ApiModelProperty(notes = "Área.")
    private String area;

    public FiltroUsuarioResponse() {
    }

    public FiltroUsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.roleGd = usuario.getRoleGD();
        this.subperfil= usuario.getSubperfilAtivo() != null ? usuario.getSubperfilAtivo().getDescricao() : null;

        if(usuario.getArea() != null) {
            this.area = usuario.getArea().getDescricao();
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSubperfil() {
        return subperfil;
    }

    public void setSubperfil(String subperfil) {
        this.subperfil = subperfil;
    }
}