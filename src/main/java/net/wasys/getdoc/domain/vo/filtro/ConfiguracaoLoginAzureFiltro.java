package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.enumeration.RoleGD;

public class ConfiguracaoLoginAzureFiltro {

	private RoleGD roleGD;
	private Subperfil subperfil;
	private String grupo;

	public RoleGD getRoleGD() {
		return roleGD;
	}

	public void setRoleGD(RoleGD roleGD) {
		this.roleGD = roleGD;
	}

	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}
}
