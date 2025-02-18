package net.wasys.getdoc.restws.dto;

import java.io.Serializable;
import java.util.Date;

public class DataUltimoAcessoDTO implements Serializable {

	private Long usuarioId;
	private Date data;

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}
}
