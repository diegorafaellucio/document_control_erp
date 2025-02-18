package net.wasys.util.rest.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MonitoramentoDTO {

	private ParametroDTO[] parametros;
	private List<LogJobDTO> logsJobs;
	private List<LogWsDTO> logsWs;

	public ParametroDTO[] getParametros() {
		return parametros;
	}

	public void setParametros(ParametroDTO[] parametros) {
		this.parametros = parametros;
	}

	public List<LogJobDTO> getLogsJobs() {
		return logsJobs;
	}

	public void setLogsJobs(List<LogJobDTO> logsJobs) {
		this.logsJobs = logsJobs;
	}

	public List<LogWsDTO> getLogsWs() {
		return logsWs;
	}

	public void setLogsWs(List<LogWsDTO> logsWs) {
		this.logsWs = logsWs;
	}

	public static class LogJobDTO {

		private String nome;
		private Boolean sucesso;
		private Date dataUltimaExecucao;

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public Boolean getSucesso() {
			return sucesso;
		}

		public void setSucesso(Boolean sucesso) {
			this.sucesso = sucesso;
		}

		public Date getDataUltimaExecucao() {
			return dataUltimaExecucao;
		}

		public void setDataUltimaExecucao(Date dataUltimaExecucao) {
			this.dataUltimaExecucao = dataUltimaExecucao;
		}
	}

	public static class LogWsDTO {

		private String nome;
		private boolean sucesso;

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public boolean getSucesso() {
			return sucesso;
		}

		public void setSucesso(boolean sucesso) {
			this.sucesso = sucesso;
		}
	}

	public static class ParametroDTO {

		private String nome;
		private BigDecimal valor;

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public BigDecimal getValor() {
			return valor;
		}

		public void setValor(BigDecimal valor) {
			this.valor = valor;
		}
	}
}
