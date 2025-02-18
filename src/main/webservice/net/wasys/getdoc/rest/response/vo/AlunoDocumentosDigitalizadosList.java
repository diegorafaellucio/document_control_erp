package net.wasys.getdoc.rest.response.vo;

import java.util.List;

public class AlunoDocumentosDigitalizadosList {

	private List<AlunoDocumentosDigitalizados> documentos;

	public List<AlunoDocumentosDigitalizados> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<AlunoDocumentosDigitalizados> documentos) {
		this.documentos = documentos;
	}

	@Override
	public String toString() {
		return "{ \"Total de Documentos Reaproveitados\": " + documentos.size() + " }";
	}
}