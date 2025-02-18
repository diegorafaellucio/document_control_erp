package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class SiaConsultaComprovanteInscricaoVO extends WebServiceClientVO {

	private String numeroInscricao;
	private String numeroCandidato;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.SIA_CONSULTA_COMPROVANTE_INSCRICAO;
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
}
