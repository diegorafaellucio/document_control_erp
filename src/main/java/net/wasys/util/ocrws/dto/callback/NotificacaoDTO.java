package net.wasys.util.ocrws.dto.callback;

import java.util.Date;

public class NotificacaoDTO {

	private Long agendamentoId;
	private Long idRegistro;
	private String resultado;
	private String tipoErro;
	private String mensagemErro;
	private String stackTrace;
	private StatusAgendamento statusAgendamento;
	private Date inicioOcr;
	private Date fimOcr;
	private Long tempoOcr;
	private Date inicioPreAgendamento;
	private Date fimPreAgendamento;
	private Long tempoPreAgendamento;
	private Date inicioPreparacaoImagem;
	private Date fimPreparacaoImagem;
	private Long tempoPreparacaoImagem;
	private CampoOcrDTO[] camposOcr;

	public Long getAgendamentoId() {
		return agendamentoId;
	}

	public void setAgendamentoId(Long agendamentoId) {
		this.agendamentoId = agendamentoId;
	}

	public Long getIdRegistro() {
		return idRegistro;
	}

	public void setIdRegistro(Long idRegistro) {
		this.idRegistro = idRegistro;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getTipoErro() {
		return tipoErro;
	}

	public void setTipoErro(String tipoErro) {
		this.tipoErro = tipoErro;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public StatusAgendamento getStatusAgendamento() {
		return statusAgendamento;
	}

	public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
		this.statusAgendamento = statusAgendamento;
	}

	public Date getInicioOcr() {
		return inicioOcr;
	}

	public void setInicioOcr(Date inicioOcr) {
		this.inicioOcr = inicioOcr;
	}

	public Date getFimOcr() {
		return fimOcr;
	}

	public void setFimOcr(Date fimOcr) {
		this.fimOcr = fimOcr;
	}

	public Long getTempoOcr() {
		return tempoOcr;
	}

	public void setTempoOcr(Long tempoOcr) {
		this.tempoOcr = tempoOcr;
	}

	public Date getInicioPreAgendamento() {
		return inicioPreAgendamento;
	}

	public void setInicioPreAgendamento(Date inicioPreAgendamento) {
		this.inicioPreAgendamento = inicioPreAgendamento;
	}

	public Date getFimPreAgendamento() {
		return fimPreAgendamento;
	}

	public void setFimPreAgendamento(Date fimPreAgendamento) {
		this.fimPreAgendamento = fimPreAgendamento;
	}

	public Long getTempoPreAgendamento() {
		return tempoPreAgendamento;
	}

	public void setTempoPreAgendamento(Long tempoPreAgendamento) {
		this.tempoPreAgendamento = tempoPreAgendamento;
	}

	public Date getInicioPreparacaoImagem() {
		return inicioPreparacaoImagem;
	}

	public void setInicioPreparacaoImagem(Date inicioPreparacaoImagem) {
		this.inicioPreparacaoImagem = inicioPreparacaoImagem;
	}

	public Date getFimPreparacaoImagem() {
		return fimPreparacaoImagem;
	}

	public void setFimPreparacaoImagem(Date fimPreparacaoImagem) {
		this.fimPreparacaoImagem = fimPreparacaoImagem;
	}

	public Long getTempoPreparacaoImagem() {
		return tempoPreparacaoImagem;
	}

	public void setTempoPreparacaoImagem(Long tempoPreparacaoImagem) {
		this.tempoPreparacaoImagem = tempoPreparacaoImagem;
	}

	public CampoOcrDTO[] getCamposOcr() {
		return camposOcr;
	}

	public void setCamposOcr(CampoOcrDTO[] camposOcr) {
		this.camposOcr = camposOcr;
	}
}
