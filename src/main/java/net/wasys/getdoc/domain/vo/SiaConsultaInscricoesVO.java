package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class SiaConsultaInscricoesVO extends WebServiceClientVO {

	private String numeroInscricao;
	private String numeroCandidato;
	private String cpfAluno;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.SIA_CONSULTA_INSCRICOES;
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

	public String getCpfAluno() {
		return cpfAluno;
	}

	public void setCpfAluno(String cpfAluno) {
		this.cpfAluno = cpfAluno;
	}
}
