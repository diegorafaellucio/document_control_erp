package net.wasys.getdoc.domain.enumeration;

public enum Origem {
	ANDROID(true),
	IOS(true),
	WEB(false),
	SERVICE(false),
	EMAIL(false),
	PORTAL_GRADUCAO(false),
	PORTAL_POS_GRADUACAO(false),
	PORTAL_CVC(false),
	GETDOC_ALUNO(false);

	private boolean mobile;

	Origem(boolean mobile) {
		this.mobile = mobile;
	}

	public boolean isMobile() {
		return mobile;
	}
}
