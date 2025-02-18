package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.TipoDocumento;

public class CampanhaTipoDocumentoVO {
	private TipoDocumento tipoDocumento;
	private Boolean obrigatorio;
	private Boolean exibirNoPortal;

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public Boolean getExibirNoPortal() {
		return exibirNoPortal;
	}

	public void setExibirNoPortal(Boolean exibirNoPortal) {
		this.exibirNoPortal = exibirNoPortal;
	}
}
