package net.wasys.getdoc.domain.vo;

import java.io.Serializable;

public class AlunoVO implements Serializable {

	private Long id;
	private String nomeAluno;
	private String cpf;
	private String matricula;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeAluno() {
		return nomeAluno;
	}

	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + getNomeAluno() + getCpf() + getMatricula();
	}
}
