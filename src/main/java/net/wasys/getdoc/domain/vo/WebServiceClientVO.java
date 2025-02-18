package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public abstract class WebServiceClientVO {

	private Usuario usuario;
	private Processo processo;

	public abstract TipoConsultaExterna getTipoConsultaExterna();

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
}
