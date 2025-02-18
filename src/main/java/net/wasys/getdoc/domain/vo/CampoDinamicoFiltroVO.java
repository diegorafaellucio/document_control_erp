package net.wasys.getdoc.domain.vo;

import java.util.List;

public class CampoDinamicoFiltroVO {

	private List<String> criterios;
	private List<Long> idsPais;
	private List<String> valoresPais;
	private Long tipoCampoId;

	public List<String> getCriterios() { return criterios; }

	public void setCriterios(List<String> criterios) { this.criterios = criterios; }

	public List<Long> getIdsPais() { return idsPais; }

	public void setIdsPais(List<Long> idsPais) { this.idsPais = idsPais; 	}

	public List<String> getValoresPais() { return valoresPais; }

	public void setValoresPais(List<String> valoresPais) { this.valoresPais = valoresPais; }

	public Long getTipoCampoId() { return tipoCampoId; }

	public void setTipoCampoId(Long tipoCampoId) { this.tipoCampoId = tipoCampoId; }
}
