package net.wasys.getdoc.domain.vo.filtro;

import org.primefaces.model.SortOrder;

public class AlunoFiltro {

	private String nome;
	private String cpf;
	private boolean isEstrangeiro;
	private String numInscricao;
	private String numCandidato;
	private String campoOrdem;
	private SortOrder ordem;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public boolean getEstrangeiro() {
		return isEstrangeiro;
	}

	public void setEstrangeiro(boolean estrangeiro) {
		isEstrangeiro = estrangeiro;
	}

	public void setNumInscricao(String numInscricao) { this.numInscricao = numInscricao; }

	public String getNumInscricao() {
		return numInscricao;
	}

	public String getNumCandidato() {
		return numCandidato;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public void setCampoOrdem(String campoOrdem) {
		this.campoOrdem = campoOrdem;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public void setOrdem(SortOrder ordem) {
		this.ordem = ordem;
	}

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}
}
