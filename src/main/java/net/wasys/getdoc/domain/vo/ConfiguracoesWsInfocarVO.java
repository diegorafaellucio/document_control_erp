package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfiguracoesWsInfocarVO {

	private String endPointLeilao;
	private String endPointDecode;
	private String key;
	private Integer validadeLeilao;
	private Integer validadeDecode;

	@JsonProperty("END_POINT_LEILAO")
	public String getEndPointLeilao() {
		return endPointLeilao;
	}

	public void setEndPointLeilao(String endPointLeilao) {
		this.endPointLeilao = endPointLeilao;
	}

	@JsonProperty("END_POINT_DECODE")
	public String getEndPointDecode() {
		return endPointDecode;
	}

	public void setEndPointDecode(String endPointDecode) {
		this.endPointDecode = endPointDecode;
	}

	@JsonProperty("KEY")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@JsonProperty("VALIDADE_LEILAO")
	public Integer getValidadeLeilao() {
		return validadeLeilao;
	}

	public void setValidadeLeilao(Integer validadeLeilao) {
		this.validadeLeilao = validadeLeilao;
	}

	@JsonProperty("VALIDADE_DECODE")
	public Integer getValidadeDecode() {
		return validadeDecode;
	}

	public void setValidadeDecode(Integer validadeDecode) {
		this.validadeDecode = validadeDecode;
	}
}
