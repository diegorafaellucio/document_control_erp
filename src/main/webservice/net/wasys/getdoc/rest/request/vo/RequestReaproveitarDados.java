package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestReaproveitarDados")
public class RequestReaproveitarDados {

	@ApiModelProperty(notes = "Número do Candidato ou Número de Inscrição.")
	private String numCandidato;

	@ApiModelProperty(notes = "Número de Inscrição.")
	private String numInscricao;

	@ApiModelProperty(notes = "Número do CPF.")
	private String cpf;

	@ApiModelProperty(notes = "Flag que sinaliza que os arquivos não serão reaproveitados.")
	private Boolean ignorarArquivos;

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

	public void setIgnorarArquivos(Boolean ignorarArquivos) {
		this.ignorarArquivos = ignorarArquivos;
	}

	public Boolean getIgnorarArquivos() {
		return ignorarArquivos;
	}

	@Override public String toString() {
		return "RequestReaproveitarDados{" +
				", numCandidato='" + numCandidato + '\'' +
				", numInscricao='" + numInscricao + '\'' +
				", cpf='" + cpf + '\'' +
				", ignorarArquivos='" + ignorarArquivos + '\'' +
				'}';
	}
}
