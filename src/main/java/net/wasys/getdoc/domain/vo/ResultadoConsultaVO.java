package net.wasys.getdoc.domain.vo;

import java.io.Serializable;
import java.util.Map;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class ResultadoConsultaVO implements Serializable {

	private String nomeRegra;
	private String subRegraId;
	private TipoConsultaExterna tipoConsultaExterna;
	private Map<?, ?> valores;
	private Map<?, ?> parametros;

	public String getNomeRegra() {
		return nomeRegra;
	}

	public void setNomeRegra(String nomeRegra) {
		this.nomeRegra = nomeRegra;
	}

	public String getSubRegraId() {
		return subRegraId;
	}

	public void setSubRegraId(String subRegraId) {
		this.subRegraId = subRegraId;
	}

	public TipoConsultaExterna getTipoConsultaExterna() {
		return tipoConsultaExterna;
	}

	public void setTipoConsultaExterna(TipoConsultaExterna tipoConsultaExterna) {
		this.tipoConsultaExterna = tipoConsultaExterna;
	}

	public Map<?, ?> getValores() {
		return valores;
	}

	public void setValores(Map<?, ?> valores) {
		this.valores = valores;
	}

	public Map<?, ?> getParametros() {
		return parametros;
	}

	public void setParametros(Map<?, ?> parametros) {
		this.parametros = parametros;
	}
}
