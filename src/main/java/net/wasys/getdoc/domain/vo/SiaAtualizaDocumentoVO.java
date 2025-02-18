package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class SiaAtualizaDocumentoVO extends WebServiceClientVO {

	private String numeroInscricao;
	private String numeroCandidato;
	private String codOrigem;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.SIA_ATUALIZA_DOCUMENTO;
	}

	public String getNumeroInscricao() {
		return numeroInscricao;
	}

	public void setNumeroInscricao(String numeroInscricao) {
		this.numeroInscricao = numeroInscricao;
	}

	public String getNumeroCandidato() {
		return numeroCandidato;
	}

	public void setNumeroCandidato(String numeroCandidato) {
		this.numeroCandidato = numeroCandidato;
	}

	public String getCodOrigem() {
		return codOrigem;
	}

	public void setCodOrigem(String codOrigem) {
		this.codOrigem = codOrigem;
	}
}
