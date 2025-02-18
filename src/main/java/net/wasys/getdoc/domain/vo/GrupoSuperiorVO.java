package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.CampoGrupo;

import java.util.List;

public class GrupoSuperiorVO {

	private Long tipoCampoGrupoId;
	private CampoGrupo grupoSuperior;
	private List<CampoGrupo> gruposFilhos;
	private Boolean abertoPadrao;
	private boolean grupoPai;

	public CampoGrupo getGrupoSuperior() {
		return grupoSuperior;
	}

	public void setGrupoSuperior(CampoGrupo grupoSuperior) {
		this.grupoSuperior = grupoSuperior;
	}

	public Long getTipoCampoGrupoId() {
		return tipoCampoGrupoId;
	}

	public void setTipoCampoGrupoId(Long tipoCampoGrupoId) {
		this.tipoCampoGrupoId = tipoCampoGrupoId;
	}

	public List<CampoGrupo> getGruposFilhos() {
		return gruposFilhos;
	}

	public void setGruposFilhos(List<CampoGrupo> gruposFilhos) {
		this.gruposFilhos = gruposFilhos;
	}

	public void setAbertoPadrao(Boolean abertoPadrao) {
		this.abertoPadrao = abertoPadrao;
	}

	public Boolean getAbertoPadrao() {
		return abertoPadrao;
	}

	public void setGrupoPai(boolean grupoPai) {
		this.grupoPai = grupoPai;
	}

	public boolean getGrupoPai() {
		return grupoPai;
	}
}