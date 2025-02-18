package net.wasys.getdoc.domain.enumeration;

import java.util.Arrays;
import java.util.List;

public enum PermissaoTP {

	ANALISTA,
	AREA,
	COMERCIAL,
	CLIENTE;

	public static List<PermissaoTP> getPermissoes(RoleGD roleGD) {

		if(roleGD == null) {
			return Arrays.asList(CLIENTE);
		}
		else if(RoleGD.GD_ADMIN.equals(roleGD) || RoleGD.GD_GESTOR.equals(roleGD)) {
			return null;
		}
		else if(RoleGD.GD_ANALISTA.equals(roleGD)) {
			return Arrays.asList(ANALISTA);
		}
		else if(RoleGD.GD_AREA.equals(roleGD)) {
			return Arrays.asList(AREA);
		}
		else if(RoleGD.GD_COMERCIAL.equals(roleGD)) {
			return Arrays.asList(COMERCIAL);
		}

		return null;
	}
}
