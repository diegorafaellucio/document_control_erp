package net.wasys.getdoc.domain.vo;

import java.util.*;

public class RelatorioIsencaoDisciplinasVO {

	private Map<String, SituacaoPorDia> areaSituacaoPorDia = new HashMap<>();

	private List<Date> datas = new ArrayList<>();

	private Map<Date, Integer> totalPorData = new HashMap<>();

	public Map<String, SituacaoPorDia> getAreaSituacaoPorDia() {
		return areaSituacaoPorDia;
	}

	public void setAreaSituacaoPorDia(Map<String, SituacaoPorDia> areaSituacaoPorDia) {
		this.areaSituacaoPorDia = areaSituacaoPorDia;
	}

	public List<Date> getDatas() {
		return datas;
	}

	public void setDatas(List<Date> datas) {
		this.datas = datas;
	}

	public Map<Date, Integer> getTotalPorData() {
		return totalPorData;
	}

	public void setTotalPorData(Map<Date, Integer> totalPorData) {
		this.totalPorData = totalPorData;
	}

	public static class SituacaoPorDia {
		private Map<String, Map<Date, Integer>> qtdPorDia;
		private Long totalPeriodo;

		public  Map<String, Map<Date, Integer>> getQtdPorDia() {
			return qtdPorDia;
		}
		public void setQtdPorDia( Map<String, Map<Date, Integer>> qtdPorDia) {
			this.qtdPorDia = qtdPorDia;
		}
		public Long getTotalPeriodo() {
			return totalPeriodo;
		}
		public void setTotalPeriodo(Long totalPeriodo) {
			this.totalPeriodo = totalPeriodo;
		}
	}

}
