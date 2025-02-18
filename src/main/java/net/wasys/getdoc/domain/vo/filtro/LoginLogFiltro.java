package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.RoleGD;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginLogFiltro {

	private Date dataInicio;
	private Date dataFim;
	private List<Long> usuarioIds = new ArrayList<>();
	private RoleGD roleGD;
	private boolean apenasAtivos = false;


	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public List<Long> getUsuarioIds() {
		return usuarioIds;
	}

	public void setUsuarioIds(List<Long> usuarioIds) {
		this.usuarioIds = usuarioIds;
	}

	public RoleGD getRoleGD() {
		return roleGD;
	}

	public void setRoleGD(RoleGD roleGD) {
		this.roleGD = roleGD;
	}

	public boolean isApenasAtivos() {
		return apenasAtivos;
	}

	public void setApenasAtivos(boolean apenasAtivos) {
		this.apenasAtivos = apenasAtivos;
	}

	@Override public String toString() {
		return "LoginLogFiltro{" +
				"dataInicio=" + dataInicio +
				", dataFim=" + dataFim +
				", usuarioIds=" + usuarioIds +
				", roleGD=" + roleGD +
				", apenasAtivos=" + apenasAtivos +
				'}';
	}
}
