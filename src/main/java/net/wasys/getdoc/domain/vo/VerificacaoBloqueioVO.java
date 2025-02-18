package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.BloqueioProcesso;
import net.wasys.getdoc.domain.entity.Processo;

public class VerificacaoBloqueioVO {

	private Long processoId;
	private String usuarioNome;
	private BloqueioProcesso bloqueio;

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public String getUsuarioNome() {
		return usuarioNome;
	}

	public void setUsuarioNome(String usuarioNome) {
		this.usuarioNome = usuarioNome;
	}

	public BloqueioProcesso getBloqueio() {
		return bloqueio;
	}

	public void setBloqueio(BloqueioProcesso bloqueio) {
		this.bloqueio = bloqueio;
	}
}
