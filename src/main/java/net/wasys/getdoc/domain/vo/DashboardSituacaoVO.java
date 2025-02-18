package net.wasys.getdoc.domain.vo;

import java.util.List;

public class DashboardSituacaoVO {

	List<Object[]> situacao;
	List<Object[]> situacaoCompara;

	public List<Object[]> getSituacao() {
		return situacao;
	}

	public void setSituacao(List<Object[]> situacao) {
		this.situacao = situacao;
	}

	public List<Object[]> getSituacaoCompara() {
		return situacaoCompara;
	}

	public void setSituacaoCompara(List<Object[]> situacaoCompara) {
		this.situacaoCompara = situacaoCompara;
	}
}
