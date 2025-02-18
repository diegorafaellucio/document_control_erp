package net.wasys.getdoc.domain.enumeration;

public enum DocumentoAssinatura {
	CET("1"),
	CCB_CAPA("2"),
	CCB_CLAUSULAS("3"),
	APROVACAO("5");

	private String cod;


	private DocumentoAssinatura(String cod) {
		this.cod = cod;

	}

	public String getCod() {
		return cod;
	}


}
