package net.wasys.getdoc.domain.vo;

public class ReaproveitamentoMembroFamiliarVO {

	private String nomeGrupo;
	private String nome;
	private String parentesco;
	private String dataNascimento;
	private String renda;

	public ReaproveitamentoMembroFamiliarVO() {
	}

	public ReaproveitamentoMembroFamiliarVO(String nomeGrupo, String nome, String parentesco, String dataNascimento, String renda) {
		this.nomeGrupo = nomeGrupo;
		this.nome = nome;
		this.parentesco = parentesco;
		this.dataNascimento = dataNascimento;
		this.renda = renda;
	}

	public String getNomeGrupo() {
		return nomeGrupo;
	}

	public void setNomeGrupo(String nomeGrupo) {
		this.nomeGrupo = nomeGrupo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getParentesco() {
		return parentesco;
	}

	public void setParentesco(String parentesco) {
		this.parentesco = parentesco;
	}

	public String getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getRenda() {
		return renda;
	}

	public void setRenda(String renda) {
		this.renda = renda;
	}

	@Override public String toString() {
		return "ReaproveitamentoMembroFamiliar{" +
				"nomeGrupo='" + nomeGrupo + '\'' +
				", nome='" + nome + '\'' +
				", parentesco='" + parentesco + '\'' +
				", dataNascimento=" + dataNascimento +
				", renda='" + renda + '\'' +
				'}';
	}
}
