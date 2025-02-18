package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.CampoMap;

import java.util.List;

public class CampoDinamicoVO {
	private String nomeCampo;
	private String nomeGrupo;
	private String valorCampo;
	private List<String> chavesUnicidade; //busca por campos provenientes de base interna;

	public CampoDinamicoVO() {}

	public CampoDinamicoVO(String nomeCampo, String valorCampo, String nomeGrupo) {
		this.nomeCampo = nomeCampo;
		this.nomeGrupo = nomeGrupo;
		this.valorCampo = valorCampo;
	}

	public CampoDinamicoVO(CampoMap.CampoEnum enumm, String valorCampo) {
		this.nomeCampo = enumm.getNome();
		this.nomeGrupo = enumm.getGrupo().getNome();
		this.valorCampo = valorCampo;
	}

	public String getNomeCampo() {
		return nomeCampo;
	}
	public void setNomeCampo(String nomeCampo) {
		this.nomeCampo = nomeCampo;
	}
	public String getNomeGrupo() {
		return nomeGrupo;
	}
	public void setNomeGrupo(String nomeGrupo) {
		this.nomeGrupo = nomeGrupo;
	}
	public String getValorCampo() {
		return valorCampo;
	}
	public void setValorCampo(String valorCampo) {
		this.valorCampo = valorCampo;
	}
	public List<String> getChavesUnicidade() {
		return chavesUnicidade;
	}
	public void setChavesUnicidade(List<String> chavesUnicidade) {
		this.chavesUnicidade = chavesUnicidade;
	}
}
