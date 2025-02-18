package net.wasys.getdoc.domain.vo.filtro;

import java.util.List;

public class TextoPadraoFiltro {

	private Long tipoProcessoId;
	private List<Long> textoPadraoIds;
	private Boolean ativo;

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public List<Long> getTextoPadraoIds() {
		return textoPadraoIds;
	}

	public void setTextoPadraoIds(List<Long> textoPadraoIds) {
		this.textoPadraoIds = textoPadraoIds;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
