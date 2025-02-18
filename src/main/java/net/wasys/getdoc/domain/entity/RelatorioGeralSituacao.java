package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.util.DummyUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name="RELATORIO_GERAL_SITUACAO")
public class RelatorioGeralSituacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long processoId;
	private Date data;
	private Date dataFim;
	private Date prazoLimite;
	private BigDecimal tempo;

	private Situacao situacao;
	private Situacao situacaoAnterior;
	private Usuario usuarioInicio;
	private Usuario usuarioFim;
	private StatusPrazo statusPrazoSituacao;
	private RelatorioGeral relatorioGeral;
	private Long tempoTratativa;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="PROCESSO_ID")
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA")
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID")
	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ANTERIOR_ID")
	public Situacao getSituacaoAnterior() {
		return situacaoAnterior;
	}

	public void setSituacaoAnterior(Situacao situacaoAnterior) {
		this.situacaoAnterior = situacaoAnterior;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FIM")
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Column(name="TEMPO")
	public BigDecimal getTempo() {
		return tempo;
	}

	public void setTempo(BigDecimal tempo) {
		this.tempo = tempo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RELATORIO_GERAL_ID")
	public RelatorioGeral getRelatorioGeral() {
		return relatorioGeral;
	}

	public void setRelatorioGeral(RelatorioGeral relatorioGeral) {
		this.relatorioGeral = relatorioGeral;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_INICIO_ID")
	public Usuario getUsuarioInicio() {
		return usuarioInicio;
	}

	public void setUsuarioInicio(Usuario usuarioInicio) {
		this.usuarioInicio = usuarioInicio;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_FIM_ID")
	public Usuario getUsuarioFim() {
		return usuarioFim;
	}

	public void setUsuarioFim(Usuario usuarioFim) {
		this.usuarioFim = usuarioFim;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_PRAZO_SITUACAO")
	public StatusPrazo getStatusPrazoSituacao() {
		return statusPrazoSituacao;
	}

	public void setStatusPrazoSituacao(StatusPrazo statusPrazoSituacao) {
		this.statusPrazoSituacao = statusPrazoSituacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE")
	public Date getPrazoLimite() { return prazoLimite; }

	public void setPrazoLimite(Date prazoLimite) { this.prazoLimite = prazoLimite; }

	@Column(name="TEMPO_TRATATIVA")
	public Long getTempoTratativa() {
		return tempoTratativa;
	}

	public void setTempoTratativa(Long tempoTratativa) {
		this.tempoTratativa = tempoTratativa;
	}

	@Override
	public String toString() {
		Situacao situacao = getSituacao();
		String situacaoNome = situacao != null ? situacao.getNome() : "";
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + situacaoNome + ",data:" + DummyUtils.formatDateTime2(getData()) + "}";
	}
}