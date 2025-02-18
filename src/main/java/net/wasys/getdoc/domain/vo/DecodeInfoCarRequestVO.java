package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class DecodeInfoCarRequestVO extends WebServiceClientVO {

	public static final String CHASSI = "CHASSI";
	public static final String PLACA = "PLACA";

	private String chassi;
	//private String placa;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.DECODE;
	}

	/*public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}*/

	public String getChassi() {
		return chassi;
	}

	public void setChassi(String chassi) {
		this.chassi = chassi;
	}
}
