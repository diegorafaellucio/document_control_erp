package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

import java.io.File;
import java.util.Date;

public class DataValidBiometriaRequestVO extends DataValidRequestVO {

	public static final String FOTO = "FOTO";

	private File foto;

	public File getFoto() {
		return foto;
	}

	public void setFoto(File foto) {
		this.foto = foto;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.DATA_VALID_BIOMETRIA;
	}
}
