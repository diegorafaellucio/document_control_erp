package net.wasys.getdoc.domain.vo.filtro;

import java.util.*;

public class RelatorioLicenciamentoFiltro {
	private int ano;
	private int mes;
	private List<Date> meses;

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getAno() {
		return ano;
	}

	public void setAno(int ano) {
		this.ano = ano;
	}

	public List<Date> getMeses() {
		return meses;
	}

	public void setMeses(List<Date> meses) {
		this.meses = meses;
	}
}