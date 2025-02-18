package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(value = "RequestOpcoesFiltro")
public class RequestOpcoesFiltro {

	private List<String> criterios;

	private List<String> valoresPais;

	private List<Long> idsPais;

	private Long tipoCampoId;

	public List<String> getCriterios() { return criterios; }

	public void setCriterios(List<String> criterios) { this.criterios = criterios; }

	public Long getTipoCampoId() {
		return tipoCampoId;
	}

	public void setTipoCampoId(Long tipoCampoId) {
		this.tipoCampoId = tipoCampoId;
	}

	public List<String> getValoresPais() { return valoresPais; }

	public void setValoresPais(List<String> valoresPais) { this.valoresPais = valoresPais; }

	public List<Long> getIdsPais() { return idsPais; }

	public void setIdsPais(List<Long> idsPais) { this.idsPais = idsPais; }
}
