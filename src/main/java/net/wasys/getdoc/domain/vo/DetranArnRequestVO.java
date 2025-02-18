package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class DetranArnRequestVO extends WebServiceClientVO {

	public static final String CHASSI = "CHASSI";
	public static final String PLACA = "PLACA";
	public static final String UF = "UF";

	private String uf;
	private String placa;
	private String chassi;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.DETRAN_ARN;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getChassi() {
		return chassi;
	}

	public void setChassi(String chassi) {
		this.chassi = chassi;
	}
}
