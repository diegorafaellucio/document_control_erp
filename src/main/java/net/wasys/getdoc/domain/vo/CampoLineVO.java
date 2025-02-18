package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Campo;

public class CampoLineVO {

	private Campo campo1;
	private Campo campo2;
	private boolean colunaUnica;

	public Campo getCampo1() {
		return campo1;
	}

	public void setCampo1(Campo campo1) {
		this.campo1 = campo1;
	}

	public Campo getCampo2() {
		return campo2;
	}

	public void setCampo2(Campo campo2) {
		this.campo2 = campo2;
	}

	public boolean isColunaUnica() {
		return colunaUnica;
	}

	public void setColunaUnica(boolean colunaUnica) {
		this.colunaUnica = colunaUnica;
	}
}
