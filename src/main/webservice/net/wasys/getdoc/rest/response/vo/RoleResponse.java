package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Role;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Objects;
import java.util.Set;

@ApiModel(value = "RoleResponse")
public class RoleResponse extends SuperVo {

    @ApiModelProperty(value = "Nome")
    private String nome;

    public RoleResponse(){}

    public RoleResponse(Role role){
        this.nome = role.getNome();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleResponse)) return false;
        RoleResponse that = (RoleResponse) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nome);
    }
}