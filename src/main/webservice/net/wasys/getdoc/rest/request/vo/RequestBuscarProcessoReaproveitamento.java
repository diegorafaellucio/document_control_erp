package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestBuscarProcessoReaproveitamento")
public class RequestBuscarProcessoReaproveitamento {

	@ApiModelProperty(notes = "Número do Candidato ou Número de Inscrição.")
	private String numCandidato;

	@ApiModelProperty(notes = "Número de Inscrição.")
	private String numInscricao;

	@ApiModelProperty(notes = "Número do CPF.")
	private String cpf;

	@ApiModelProperty(notes = "Precisa validar Situacao Anterior de Aluno?")
	private Boolean validarSituacaoAnterior = true;

	public String getNumCandidato() {
		return numCandidato;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}

	public String getNumInscricao() {
		return numInscricao;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public Boolean getValidarSituacaoAnterior() {
		return validarSituacaoAnterior;
	}

	public void setValidarSituacaoAnterior(Boolean validarSituacaoAnterior) {
		this.validarSituacaoAnterior = validarSituacaoAnterior;
	}

	@Override public String toString() {
		return "RequestBuscarProcessoReaproveitamento{" +
				"numCandidato='" + numCandidato + '\'' +
				", numInscricao='" + numInscricao + '\'' +
				'}';
	}
}
