package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Usuario;

import java.util.List;
import java.util.Map;

public class EditarProcessoVO {

	private Long processoId;
	private Usuario usuario;
	private Map<Long, String> valores;
	private boolean validarCampos;
	private List<CampoGrupo> grupos;

	public Long getProcessoId() { return processoId; }

	public void setProcessoId(Long processoId) { this.processoId = processoId; }

	public Usuario getUsuario() { return usuario; }

	public void setUsuario(Usuario usuario) { this.usuario = usuario; }

	public Map<Long, String> getValores() { return valores; }

	public void setValores(Map<Long, String> valores) { this.valores = valores; }

	public boolean getValidarCampos() {
		return validarCampos;
	}

	public void setValidarCampos(boolean validarCampos) {
		this.validarCampos = validarCampos;
	}

	public List<CampoGrupo> getGrupos() {
		return grupos;
	}

	public void setGrupos(List<CampoGrupo> grupos) {
		this.grupos = grupos;
	}
}
