package net.wasys.getdoc.restws.dto;

import java.io.Serializable;

public class UsuarioCampusDTO implements Serializable {

	private Long id;
	private Long usuarioId;
	private Long codCampus;

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

	public Long getCodCampus() {
		return codCampus;
	}

	public void setCodCampus(Long codCampus) {
		this.codCampus = codCampus;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{usuarioId:" + usuarioId + ", codRegional:" + codCampus + "}";
	}
}
