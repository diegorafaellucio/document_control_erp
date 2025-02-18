package net.wasys.getdoc.domain.enumeration;

public enum CamposMetadadosTipificacao {

	DN_TODAS_LABELS("dn_todas_labels"),
	DN_LABEL("dn_todas_labels"),
	DN_CROP("dn_crop"),
	GV_PERCENTUAL_ACERTO("gv_percentual_acerto"),
	DN_PERCENTUAL_ACERTO("dn_percentual_acerto"),
	DN_IMAGEM_TIPIFICADA("dn_imagem_tipificada"),
	GV_IMAGEM_TIPIFICADA("gv_imagem_tipificada"),
	REGISTRO_DE_DIGITALIZACAO("registros_de_digitalizacao");

	private String campo;

	CamposMetadadosTipificacao(String campo) {
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}
}
