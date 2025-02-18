package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RequestNotificarReaproveitamento")
public class RequestNotificarReaproveitamento {

	@ApiModelProperty(notes = "Número do Candidato ou Número de Inscrição.")
	private String numCandidato;

	@ApiModelProperty(notes = "Número de Inscrição.")
	private String numInscricao;

	@ApiModelProperty(notes = "Matrícula.")
	private String matricula;

	@ApiModelProperty(notes = "Processo ID no getdoc_aluno.")
	private Long processoId;

	@ApiModelProperty(notes = "Processo ID para  bloquear.")
	private Long processoCaptacaoId;

	@ApiModelProperty(notes = "Pode filtrar processos de isenção de disciplinas?")
	private boolean filtrarIsencaoDisciplinas;

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

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public Long getProcessoCaptacaoId() {
		return processoCaptacaoId;
	}

	public void setProcessoCaptacaoId(Long processoCaptacaoId) {
		this.processoCaptacaoId = processoCaptacaoId;
	}

	public boolean isFiltrarIsencaoDisciplinas() {
		return filtrarIsencaoDisciplinas;
	}

	public void setFiltrarIsencaoDisciplinas(boolean filtrarIsencaoDisciplinas) {
		this.filtrarIsencaoDisciplinas = filtrarIsencaoDisciplinas;
	}

	@Override public String toString() {
		return "RequestNotificarReaproveitamento{" +
				"numCandidato='" + numCandidato + '\'' +
				", numInscricao='" + numInscricao + '\'' +
				", matricula='" + matricula + '\'' +
				", processoId=" + processoId +
				", isFiltrarIsencaoDisciplinas=" + filtrarIsencaoDisciplinas +
				'}';
	}
}
