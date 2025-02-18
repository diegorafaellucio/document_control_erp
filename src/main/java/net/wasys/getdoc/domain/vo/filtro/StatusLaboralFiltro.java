package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.StatusAtendimento;

import java.util.List;

public class StatusLaboralFiltro {

	private Boolean ativa;
	private List<StatusAtendimento> statusAtendimentoList;
	private Boolean fixo;

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public List<StatusAtendimento> getStatusAtendimentoList() {
		return statusAtendimentoList;
	}

	public void setStatusAtendimentoList(List<StatusAtendimento> statusAtendimentoList) {
		this.statusAtendimentoList = statusAtendimentoList;
	}

	public Boolean getFixo() {
		return fixo;
	}

	public void setFixo(Boolean fixo) {
		this.fixo = fixo;
	}
}
