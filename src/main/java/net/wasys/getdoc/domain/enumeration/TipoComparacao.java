package net.wasys.getdoc.domain.enumeration;

public enum TipoComparacao {

	IGUAL(true, false),
	MENOR_QUE(true, false),
	MAIOR_QUE(true, false),
	ESTA_APROVACO(false, true),
	ESTA_REPROVADO(false, true),
	ESTA_DIGITALIZADO(false, true);

	private boolean aplicaCampo;
	private boolean aplicaDocumento;

	private TipoComparacao(boolean aplicaCampo, boolean aplicaDocumento) {
		this.aplicaCampo = aplicaCampo;
		this.aplicaDocumento = aplicaDocumento;
	}

	public boolean getAplicaCampo() {
		return aplicaCampo;
	}

	public boolean getAplicaDocumento() {
		return aplicaDocumento;
	}
}
