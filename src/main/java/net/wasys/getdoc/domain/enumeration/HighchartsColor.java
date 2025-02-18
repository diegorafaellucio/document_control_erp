package net.wasys.getdoc.domain.enumeration;

public enum HighchartsColor {

	A("#90ed7d", "Vestibular Enem"),
	B("#7cb5ec", "Msv - Interna"),
	C("#f7a35c", "ProUni"),
	D("#91e8e1", "Transferência Externa"),
	E("#2b908f", "Msv - Externa"),
	F("#e4d354", "Vestibular"),
	G("#f15c80", "Pós Graduação"),
	H("#f45b5b", "---"),
	I("#91e8e1", "---"),
	J("#8085e9", "---"),
	;

	private String colorHex;
	private String tipoProcesso;

	private HighchartsColor(String colorHex, String tipoProcesso) {
		this.colorHex = colorHex;
		this.tipoProcesso = tipoProcesso;
	}

	public static String getColorForTipoProcesso(String nomeTipoProcesso) {

		HighchartsColor[] values = HighchartsColor.values();
		for (HighchartsColor color : values) {
			String var = color.tipoProcesso;

			if(var.equals(nomeTipoProcesso)) {
				return color.colorHex;
			}
		}
		return null;
	}
}