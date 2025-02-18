package net.wasys.getdoc.domain.enumeration;

import org.apache.commons.lang.StringUtils;

public enum TipoProuni {
	FEDERAL("Federal"),
	RECIFE("Recife"),
	NULO(null);

	private String tipoProuni;

	TipoProuni(String tipoProuni){
		this.tipoProuni = tipoProuni;
	}

	public String getTipoProuni() {
		return tipoProuni;
	}

	public static TipoProuni getByTipoPruno(String tipoProuni){

		if(StringUtils.isBlank(tipoProuni)){
			return null;
		}

		TipoProuni[] listaTiposProuni = TipoProuni.values();
		for(TipoProuni key : listaTiposProuni){
			String keyTipoProuni = StringUtils.upperCase(key.getTipoProuni());
			if(StringUtils.upperCase(tipoProuni).equals(keyTipoProuni)) {
				return key;
			}
		}
		return null;
	}

}
