package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.TipoDocumento;

import java.util.List;

public class CampanhaEquivalenciaVO {

	private TipoDocumento documentoEquivalente;
	private List<TipoDocumento> documentosEquivalidos;

	public TipoDocumento getDocumentoEquivalente() {
		return documentoEquivalente;
	}

	public void setDocumentoEquivalente(TipoDocumento documentoEquivalente) {
		this.documentoEquivalente = documentoEquivalente;
	}

	public List<TipoDocumento> getDocumentosEquivalidos() {
		return documentosEquivalidos;
	}

	public void setDocumentosEquivalidos(List<TipoDocumento> documentosEquivalidos) {
		this.documentosEquivalidos = documentosEquivalidos;
	}
}
