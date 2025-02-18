package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DashboardDiarioVO {

	private SituacaoPorHora inscritoPorHora = new SituacaoPorHora();
	private SituacaoPorHora emConferenciaPorHora = new SituacaoPorHora();
	private SituacaoPorHora pendentePorHora = new SituacaoPorHora();
	private SituacaoPorHora aprovadoPorHora = new SituacaoPorHora();
	private TempoOperacao tempoOperacao = new TempoOperacao();
	private List<Pizza> inscritoTipoProcesso = new ArrayList<>();
	private List<Pizza> digitalizadoTipoProcesso = new ArrayList<>();
	private List<Pizza> conferidoTipoProcesso = new ArrayList<>();
	private List<Pizza> pendenciaPorDocumento = new ArrayList<>();
	private List<Pizza> pendenciaPorIrregularidade = new ArrayList<>();

	public SituacaoPorHora getInscritoPorHora() {
		return inscritoPorHora;
	}

	public void setInscritoPorHora(SituacaoPorHora inscritoPorHora) {
		this.inscritoPorHora = inscritoPorHora;
	}

	public SituacaoPorHora getEmConferenciaPorHora() {
		return emConferenciaPorHora;
	}

	public void setEmConferenciaPorHora(SituacaoPorHora emConferenciaPorHora) {
		this.emConferenciaPorHora = emConferenciaPorHora;
	}

	public SituacaoPorHora getAprovadoPorHora() {
		return aprovadoPorHora;
	}

	public void setAprovadoPorHora(SituacaoPorHora aprovadoPorHora) {
		this.aprovadoPorHora = aprovadoPorHora;
	}

	public SituacaoPorHora getPendentePorHora() {
		return pendentePorHora;
	}

	public void setPendentePorHora(SituacaoPorHora pendentePorHora) {
		this.pendentePorHora = pendentePorHora;
	}

	public TempoOperacao getTempoOperacao() {
		return tempoOperacao;
	}

	public void setTempoOperacao(TempoOperacao tempoOperacao) {
		this.tempoOperacao = tempoOperacao;
	}

	public List<Pizza> getInscritoTipoProcesso()	 {
		return inscritoTipoProcesso;
	}

	public void setInscritoTipoProcesso(List<Pizza> inscritoTipoProcesso) {
		this.inscritoTipoProcesso = inscritoTipoProcesso;
	}

	public List<Pizza> getDigitalizadoTipoProcesso() {
		return digitalizadoTipoProcesso;
	}

	public void setDigitalizadoTipoProcesso(List<Pizza> digitalizadoTipoProcesso) {
		this.digitalizadoTipoProcesso = digitalizadoTipoProcesso;
	}

	public List<Pizza> getConferidoTipoProcesso() {
		return conferidoTipoProcesso;
	}

	public void setConferidoTipoProcesso(List<Pizza> conferidoTipoProcesso) {
		this.conferidoTipoProcesso = conferidoTipoProcesso;
	}

	public List<Pizza> getPendenciaPorDocumento() {
		return pendenciaPorDocumento;
	}

	public void setPendenciaPorDocumento(List<Pizza> pendenciaPorDocumento) {
		this.pendenciaPorDocumento = pendenciaPorDocumento;
	}

	public List<Pizza> getPendenciaPorIrregularidade() {
		return pendenciaPorIrregularidade;
	}

	public void setPendenciaPorIrregularidade(List<Pizza> pendenciaPorIrregularidade) {
		this.pendenciaPorIrregularidade = pendenciaPorIrregularidade;
	}

	public static class SituacaoPorHora {
		private List<Long> qtdPorHora;
		private List<Double> qtdPorHoraMediaMensal;
		private long totalDia;
		private long totalMediaMensal;

		public List<Long> getQtdPorHora() {
			return qtdPorHora;
		}
		public void setQtdPorHora(List<Long> qtdPorHora) {
			this.qtdPorHora = qtdPorHora;
		}
		public List<Double> getQtdPorHoraMediaMensal() {
			return qtdPorHoraMediaMensal;
		}
		public void setQtdPorHoraMediaMensal(List<Double> qtdPorHoraMediaMensal) {
			this.qtdPorHoraMediaMensal = qtdPorHoraMediaMensal;
		}
		public long getTotalDia() {
			return totalDia;
		}
		public void setTotalDia(long totalDia) {
			this.totalDia = totalDia;
		}
		public long getTotalMediaMensal() {
			return totalMediaMensal;
		}
		public void setTotalMediaMensal(long totalMediaMensal) {
			this.totalMediaMensal = totalMediaMensal;
		}
	}

	public static class TempoOperacao {
		private List<Double> digitalizado;
		private List<Double> conferido;

		public List<Double> getDigitalizado() {
			return digitalizado;
		}
		public void setDigitalizado(List<Double> digitalizado) {
			this.digitalizado = digitalizado;
		}
		public List<Double> getConferido() {
			return conferido;
		}
		public void setConferido(List<Double> conferido) {
			this.conferido = conferido;
		}
	}

	public static class Pizza {
		private String nome;
		private Long quantidade;

		@JsonProperty("name")
		public String getNome() {
			return nome;
		}
		public void setNome(String nome) {
			this.nome = nome;
		}
		@JsonProperty("data")
		public Long getQuantidade() {
			return quantidade;
		}
		public void setQuantidade(Long quantidade) {
			this.quantidade = quantidade;
		}
	}
}
