package net.wasys.getdoc.domain.vo;

import java.util.Map;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class CrivoConsultaVO extends WebServiceClientVO {

	public static final String POLITICA = "POLÍTICA";

	private String politica;
	private Map<String, String> parametros;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.CRIVO;
	}

	public String getPolitica() {
		return politica;
	}

	public void setPolitica(String politica) {
		this.politica = politica;
	}

	public Map<String, String> getParametros() {
		return parametros;
	}

	public void setParametros(Map<String, String> parametros) {
		this.parametros = parametros;
	}
}