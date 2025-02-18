package net.wasys.getdoc.domain.enumeration;

public enum TipoTipificacao {

	DARKNET("Darknet"),
	VISION("Vision");

	private String varConsulta;

	private TipoTipificacao(String varConsulta) {
		this.varConsulta = varConsulta;
	}

	public String getVarConsulta() {
		return varConsulta;
	}

	public static TipoTipificacao getByVarConsulta(String var) {

		TipoTipificacao[] values = TipoTipificacao.values();
		for (TipoTipificacao tipoTipificacao : values) {
			String varConsulta = tipoTipificacao.getVarConsulta();
			if (varConsulta.equals(var)) {
				return tipoTipificacao;
			}
		}

		return null;
	}
}
