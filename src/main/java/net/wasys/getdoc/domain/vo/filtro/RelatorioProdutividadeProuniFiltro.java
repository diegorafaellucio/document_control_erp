package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;

public class RelatorioProdutividadeProuniFiltro {

	private Date dataInicio;
	private Date dataFim;

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Override public String toString() {
		return "RelatorioProdutividadeProuniFiltro{" +
				"dataInicio=" + dataInicio +
				", dataFim=" + dataFim +
				'}';
	}
}
