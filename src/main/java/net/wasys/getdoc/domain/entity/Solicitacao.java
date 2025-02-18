package net.wasys.getdoc.domain.entity;

import java.util.Date;

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

import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;

@Entity
public class Solicitacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date dataCriacao;
	private Date dataResposta;
	private Date dataFinalizacao;
	private Integer horasPrazo;
	private StatusSolicitacao status;
	private Date prazoLimite;

	private Processo processo;
	private Subarea subarea;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDataCriacao(Date data) {
		this.dataCriacao = data;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CRIACAO", nullable=false)
	public Date getDataCriacao() {
		return dataCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_RESPOSTA")
	public Date getDataResposta() {
		return dataResposta;
	}

	public void setDataResposta(Date dataResposta) {
		this.dataResposta = dataResposta;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO")
	public Date getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(Date dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@Column(name="HORAS_PRAZO")
	public Integer getHorasPrazo() {
		return horasPrazo;
	}

	public void setHorasPrazo(Integer horasPrazo) {
		this.horasPrazo = horasPrazo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS")
	public StatusSolicitacao getStatus() {
		return status;
	}

	public void setStatus(StatusSolicitacao statusSolicitacao) {
		this.status = statusSolicitacao;
	}

	@Column(name="PRAZO_LIMITE")
	public Date getPrazoLimite() {
		return prazoLimite;
	}

	public void setPrazoLimite(Date prazoLimite) {
		this.prazoLimite = prazoLimite;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBAREA_ID", nullable=false)
	public Subarea getSubarea() {
		return subarea;
	}

	public void setSubarea(Subarea subarea) {
		this.subarea = subarea;
	}
}
