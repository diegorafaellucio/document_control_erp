package net.wasys.getdoc.restws.dto;

import java.io.Serializable;

public class UsuarioRegionalDTO implements Serializable {

	private Long id;
	private Long usuarioId;
	private Long codRegional;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public Long getCodRegional() {
		return codRegional;
	}

	public void setCodRegional(Long codRegional) {
		this.codRegional = codRegional;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{usuarioId:" + usuarioId + ", codRegional:" + codRegional + "}";
	}
}
