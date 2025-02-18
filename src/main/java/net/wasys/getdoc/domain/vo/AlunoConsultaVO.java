package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

import java.util.List;

public class AlunoConsultaVO extends WebServiceClientVO {

	private List<Long> codOrigens;
	private String cpf;
	private String numCandidato;
	private String numInscricao;
	private String matricula;
	private int quantidade;
	private TipoConsultaExterna tipoConsultaExterna;

	public AlunoConsultaVO(TipoConsultaExterna tipoConsultaExterna) {
		this.tipoConsultaExterna = tipoConsultaExterna;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return tipoConsultaExterna;
	}

	public List<Long> getCodOrigens() {
		return codOrigens;
	}

	public void setCodOrigens(List<Long> codOrigens) {
		this.codOrigens = codOrigens;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

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

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}
}