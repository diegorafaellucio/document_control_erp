package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfiguracoesWsCrivoVO {

	private String endPointCrivo;
	private String politica;
	private String login;
	private String senha;
	private Integer validadeCrivo;

	@JsonProperty("END_POINT_CRIVO")
	public String getEndPointCrivo() {
		return endPointCrivo;
	}

	public void setEndPointCrivo(String endPointCrivo) {
		this.endPointCrivo = endPointCrivo;
	}

	@JsonProperty("POLITICA")
	public String getPolitica() {
		return politica;
	}

	public void setPolitica(String politica) {
		this.politica = politica;
	}

	@JsonProperty("VALIDADE_CRIVO")
	public Integer getValidadeCrivo() {
		return validadeCrivo;
	}

	public void setValidadeCrivo(Integer validadeCrivo) {
		this.validadeCrivo = validadeCrivo;
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
