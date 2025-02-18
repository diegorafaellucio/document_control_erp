package net.wasys.getdoc.domain.enumeration;

import org.apache.commons.lang.StringUtils;

public enum TipoParceiro {
	POLO_PARCEIRO("Sim"),
	UNIDADE("Não"),
	NULO(null);

	private String ehPoloParceiro;

	TipoParceiro(String ehPoloParceiro){
		this.ehPoloParceiro = ehPoloParceiro;
	}

	public String getEhPoloParceiro() {
		return ehPoloParceiro;
	}

	public static TipoParceiro getByEhPoloParceiro(String ehPoloParceiro){

		if(StringUtils.isBlank(ehPoloParceiro)){
			return null;
		}

		TipoParceiro[] tipoParceiros = TipoParceiro.values();
		for(TipoParceiro key : tipoParceiros){
			String keyEhPoloParceiro = StringUtils.upperCase(key.getEhPoloParceiro());
			if(StringUtils.upperCase(ehPoloParceiro).equals(keyEhPoloParceiro)) {
				return key;
			}
		}
		return null;
	}
}
