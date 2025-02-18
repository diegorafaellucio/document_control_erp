package net.wasys.getdoc.restws.dto;

public class MonitoramentoSiaDTO {

	private ServicoDTO[] servicos;

	public ServicoDTO[] getServicos() {
		return servicos;
	}

	public void setServicos(ServicoDTO[] servicos) {
		this.servicos = servicos;
	}

	public static class ServicoDTO {

		private String nome;
		private Long quantidadeSucesso;
		private Long tempoMedioSucesso;
		private Long quantidadeErro;
		private Long tempoMedioErro;

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public Long getQuantidadeSucesso() {
			return quantidadeSucesso;
		}

		public void setQuantidadeSucesso(Long quantidadeSucesso) {
			this.quantidadeSucesso = quantidadeSucesso;
		}

		public Long getTempoMedioSucesso() {
			return tempoMedioSucesso;
		}

		public void setTempoMedioSucesso(Long tempoMedioSucesso) {
			this.tempoMedioSucesso = tempoMedioSucesso;
		}

		public Long getQuantidadeErro() {
			return quantidadeErro;
		}

		public void setQuantidadeErro(Long quantidadeErro) {
			this.quantidadeErro = quantidadeErro;
		}

		public Long getTempoMedioErro() {
			return tempoMedioErro;
		}

		public void setTempoMedioErro(Long tempoMedioErro) {
			this.tempoMedioErro = tempoMedioErro;
		}
	}
}
