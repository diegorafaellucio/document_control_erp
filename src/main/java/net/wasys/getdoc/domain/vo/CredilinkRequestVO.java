package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class CredilinkRequestVO extends WebServiceClientVO {

	public static final String CAMPO_CPF_CNPJ = "CPFCNPJ";
	public static final String CAMPO_NOME = "NOME";
	public static final String CAMPO_TELEFONE = "TELEFONE";
	private String cpfCnpj;
	private String nome;
	private String telefone;

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.CREDILINK;
	}
}
