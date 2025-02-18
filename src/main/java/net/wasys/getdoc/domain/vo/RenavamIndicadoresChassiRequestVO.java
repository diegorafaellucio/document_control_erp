package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class RenavamIndicadoresChassiRequestVO extends WebServiceClientVO {

	public static final String CHASSI = "CHASSI";

	private String chassi;

	public String getChassi() {
		return chassi;
	}

	public void setChassi(String chassi) {
		this.chassi = chassi;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.RENAVAM_INDICADORES_CHASSI;
	}
}
