package net.wasys.getdoc.domain.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardIntradayVO {

	private TipoProcessoPorDia tipoProcessoPorDia = new TipoProcessoPorDia();
	private TipoProcessoPorDia tipoProcessoPorDiaTratado = new TipoProcessoPorDia();
	private TempoOperacao tempoOperacao = new TempoOperacao();
	private List<Long> produtividadePorDia = new ArrayList<>();

	public TipoProcessoPorDia getTipoProcessoPorDia() {
		return tipoProcessoPorDia;
	}

	public void setTipoProcessoPorDia(TipoProcessoPorDia tipoProcessoPorDia) {
		this.tipoProcessoPorDia = tipoProcessoPorDia;
	}

	public TipoProcessoPorDia getTipoProcessoPorDiaTratado() {
		return tipoProcessoPorDiaTratado;
	}

	public void setTipoProcessoPorDiaTratado(TipoProcessoPorDia tipoProcessoPorDiaTratado) {
		this.tipoProcessoPorDiaTratado = tipoProcessoPorDiaTratado;
	}

	public TempoOperacao getTempoOperacao() {
		return tempoOperacao;
	}

	public void setTempoOperacao(TempoOperacao tempoOperacao) {
		this.tempoOperacao = tempoOperacao;
	}

	public List<Long> getProdutividadePorDia() {
		return produtividadePorDia;
	}

	public void setProdutividadePorDia(List<Long> produtividadePorDia) {
		this.produtividadePorDia = produtividadePorDia;
	}

	public static class TipoProcessoPorDia {
		private Map<String, List<Long>> qtdPorDia;
		private List<Long> totalPorDia;
		private long totalPeriodo;

		public Map<String, List<Long>> getQtdPorDia() {
			return qtdPorDia;
		}
		public void setQtdPorDia(Map<String, List<Long>> qtdPorDia) {
			this.qtdPorDia = qtdPorDia;
		}
		public List<Long> getTotalPorDia() {
			return totalPorDia;
		}
		public void setTotalPorDia(List<Long> totalPorDia) {
			this.totalPorDia = totalPorDia;
		}
		public long getTotalPeriodo() {
			return totalPeriodo;
		}
		public void setTotalPeriodo(long totalPeriodo) {
			this.totalPeriodo = totalPeriodo;
		}
	}

	public static class TempoOperacao {
		private List<Double> conferido;

		public List<Double> getConferido() {
			return conferido;
		}
		public void setConferido(List<Double> conferido) {
			this.conferido = conferido;
		}
	}
}
