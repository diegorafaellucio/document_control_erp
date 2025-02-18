package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfiguracoesWsDetranArnVO {

	private String endPointSP;
	private String endPointOutros;
	private String login;
	private String senha;
	private Integer validadeDetran;

	@JsonProperty("VALIDADE_DETRAN")
	public Integer getValidadeDetran() {
		return validadeDetran;
	}

	public void setValidadeDetran(Integer validadeDetran) {
		this.validadeDetran = validadeDetran;
	}

	@JsonProperty("END_POINT_SP")
	public String getEndPointSP() {
		return endPointSP;
	}

	public void setEndPointSP(String endPointSP) {
		this.endPointSP = endPointSP;
	}

	@JsonProperty("END_POINT_OUTROS")
	public String getEndPointOutros() {
		return endPointOutros;
	}

	public void setEndPointOutros(String endPointOutros) {
		this.endPointOutros = endPointOutros;
	}

	@JsonProperty("LOGIN")
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@JsonProperty("SENHA")
	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
}
