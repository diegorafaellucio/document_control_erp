package net.wasys.getdoc.domain.entity;

import java.math.BigDecimal;
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

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;

@Entity(name="RELATORIO_GERAL_SOLICITACAO")
public class RelatorioGeralSolicitacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long processoId;
	private Long solicitacaoId;
	private Date dataSolicitacao;
	private Date prazoLimite;
	private Date dataFinalizacao;
	private Integer horasPrazo;
	private BigDecimal tempoComAnalista;
	private BigDecimal tempoComArea;
	private BigDecimal tempoAteFimSolicitacao;
	private StatusSolicitacao status;
	private Integer numeroRetrabalhos = 0;
	private AcaoProcesso acao;

	private Area area;
	private Usuario analistaSolicitante;
	private Usuario analistaArea;
	private RelatorioGeral relatorioGeral;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="SOLICITACAO_ID")
	public Long getSolicitacaoId() {
		return solicitacaoId;
	}

	public void setSolicitacaoId(Long solicitacaoId) {
		this.solicitacaoId = solicitacaoId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="AREA_ID")
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ANALISTA_SOLICITANTE_ID")
	public Usuario getAnalistaSolicitante() {
		return analistaSolicitante;
	}

	public void setAnalistaSolicitante(Usuario analistaSolicitante) {
		this.analistaSolicitante = analistaSolicitante;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_SOLICITACAO")
	public Date getDataSolicitacao() {
		return dataSolicitacao;
	}

	public void setDataSolicitacao(Date dataSolicitacao) {
		this.dataSolicitacao = dataSolicitacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="prazo_limite")
	public Date getPrazoLimite() {
		return prazoLimite;
	}

	public void setPrazoLimite(Date prazoLimite) {
		this.prazoLimite = prazoLimite;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RELATORIO_GERAL_ID")
	public RelatorioGeral getRelatorioGeral() {
		return relatorioGeral;
	}

	public void setRelatorioGeral(RelatorioGeral relatorioGeral) {
		this.relatorioGeral = relatorioGeral;
	}

	@Column(name="PROCESSO_ID", nullable=false)
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO")
	public Date getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(Date dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	@Column(name="HORAS_PRAZO")
	public Integer getHorasPrazo() {
		return horasPrazo;
	}

	public void setHorasPrazo(Integer horasPrazo) {
		this.horasPrazo = horasPrazo;
	}

	@Column(name="TEMPO_COM_ANALISTA")
	public BigDecimal getTempoComAnalista() {
		return tempoComAnalista;
	}

	public void setTempoComAnalista(BigDecimal tempoComAnalista) {
		this.tempoComAnalista = tempoComAnalista;
	}

	@Column(name="TEMPO_COM_AREA")
	public BigDecimal getTempoComArea() {
		return tempoComArea;
	}

	public void setTempoComArea(BigDecimal tempoComArea) {
		this.tempoComArea = tempoComArea;
	}

	@Column(name="TEMPO_ATE_FIM_SOLICITACAO")
	public BigDecimal getTempoAteFimSolicitacao() {
		return tempoAteFimSolicitacao;
	}

	public void setTempoAteFimSolicitacao(BigDecimal tempoAteFimSolicitacao) {
		this.tempoAteFimSolicitacao = tempoAteFimSolicitacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS")
	public StatusSolicitacao getStatus() {
		return status;
	}

	public void setStatus(StatusSolicitacao status) {
		this.status = status;
	}

	@Column(name="NUMERO_RETRABALHOS")
	public Integer getNumeroRetrabalhos() {
		return numeroRetrabalhos;
	}

	public void setNumeroRetrabalhos(Integer numeroRetrabalhos) {
		this.numeroRetrabalhos = numeroRetrabalhos;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ANALISTA_AREA_ID")
	public Usuario getAnalistaArea() {
		return analistaArea;
	}

	public void setAnalistaArea(Usuario analistaArea) {
		this.analistaArea = analistaArea;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ACAO")
	public AcaoProcesso getAcao() {
		return acao;
	}

	public void setAcao(AcaoProcesso acao) {
		this.acao = acao;
	}
}