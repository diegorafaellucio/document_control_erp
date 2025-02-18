package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

import java.io.File;
import java.util.Date;

public class DataValidRequestVO extends WebServiceClientVO {

	public static final String CPF = "CPF";
	public static final String DATA_VALIDADE_CNH = "DATA_VALIDADE_CNH";
	public static final String NOME = "NOME";
	public static final String DATA_NASCIMENTO = "DATA_NASCIMENTO";
	public static final String NOME_MAE = "NOME_MAE";

	private String cpf;
	private Date dataValidadeCnh;
	private String nome;
	private Date dataNascimento;
	private String nomeMae;

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public Date getDataValidadeCnh() {
		return dataValidadeCnh;
	}

	public void setDataValidadeCnh(Date dataValidadeCnh) {
		this.dataValidadeCnh = dataValidadeCnh;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getNomeMae() {
		return nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.DATA_VALID;
	}
}
