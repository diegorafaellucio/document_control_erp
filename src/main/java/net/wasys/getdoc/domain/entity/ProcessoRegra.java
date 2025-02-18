package net.wasys.getdoc.domain.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.wasys.getdoc.domain.enumeration.FarolRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;

@Entity(name="PROCESSO_REGRA")
public class ProcessoRegra extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date dataRegra;
	private Date dataExecucao;
	private FarolRegra farol;
	private StatusProcessoRegra status;
	private Long tempo;
	private String mensagem;
	private String stackTrace;
	private Long subRegraFinalId;

	private Regra regra;
	private Processo processo;
	private Situacao situacaoDestino;

	private List<ProcessoRegraLog> processoRegraLogs = new ArrayList<>(0);

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_REGRA")
	public Date getDataRegra() {
		return dataRegra;
	}

	public void setDataRegra(Date dataRegra) {
		this.dataRegra = dataRegra;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EXECUCAO")
	public Date getDataExecucao() {
		return dataExecucao;
	}

	public void setDataExecucao(Date dataExecucao) {
		this.dataExecucao = dataExecucao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="FAROL")
	public FarolRegra getFarol() {
		return farol;
	}

	public void setFarol(FarolRegra farol) {
		this.farol = farol;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS")
	public StatusProcessoRegra getStatus() {
		return status;
	}

	public void setStatus(StatusProcessoRegra status) {
		this.status = status;
	}

	@Column(name="TEMPO")
	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	@Column(name="MENSAGEM")
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@Column(name="STACKTRACE")
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	@Column(name="SUB_REGRA_FINAL_ID")
	public Long getSubRegraFinalId() {
		return subRegraFinalId;
	}

	public void setSubRegraFinalId(Long subRegraFinalId) {
		this.subRegraFinalId = subRegraFinalId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGRA_ID", nullable=false)
	public Regra getRegra() {
		return regra;
	}

	public void setRegra(Regra regra) {
		this.regra = regra;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_DESTINO_ID")
	public Situacao getSituacaoDestino() {
		return situacaoDestino;
	}

	public void setSituacaoDestino(Situacao situacaoDestino) {
		this.situacaoDestino = situacaoDestino;
	}

	@Transient
	public void setProcessoRegraLogs(List<ProcessoRegraLog> processoRegraLogs) {
		this.processoRegraLogs = processoRegraLogs;
	}

	@Transient
	public void addProcessoRegraLog(ProcessoRegraLog log) {
		processoRegraLogs.add(log);
	}

	@Transient
	public List<ProcessoRegraLog> getProcessoRegraLogs() {
		return processoRegraLogs;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{regra:" + regra + "}";
	}
}
