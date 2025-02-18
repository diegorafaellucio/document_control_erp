package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;

public class ExecucaoGeracaoRelatorioFiltro {

	private String ordem;
	private Date dataInicioMenorQue;

	public void setOrdem(String ordem) {
		this.ordem = ordem;
	}

	public String getOrdem() {
		return ordem;
	}

	public void setDataInicioMenorQue(Date dataInicioMenorQue) {
		this.dataInicioMenorQue = dataInicioMenorQue;
	}

	public Date getDataInicioMenorQue() {
		return dataInicioMenorQue;
	}
}
