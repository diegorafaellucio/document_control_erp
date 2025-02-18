package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name="PROCESSO_LOG")
public class ProcessoLog extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;
	private AcaoProcesso acao;
	private String observacao;
	private StatusProcesso statusProcesso;
	private Boolean lido;
	private BigDecimal horasPrazoSituacao;
	private Date prazoLimiteSituacao;
	private Date prazoLimiteEtapa;

	private Usuario usuario;
	private Processo processo;
	private EmailEnviado emailEnviado;
	private Solicitacao solicitacao;
	private ProcessoPendencia pendencia;
	private TipoEvidencia tipoEvidencia;
	private Situacao situacao;
	private Situacao situacaoAnterior;
	private LogImportacao logImportacao;
	private Etapa etapa;
	private BigDecimal horasPrazoEtapa;

	private Set<ProcessoLogAnexo> anexos = new HashSet<ProcessoLogAnexo>(0);

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

	@Enumerated(EnumType.STRING)
	@Column(name="ACAO", nullable=false)
	public AcaoProcesso getAcao() {
		return this.acao;
	}

	public void setAcao(AcaoProcesso acao) {
		this.acao = acao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name="observacao", length=3000)
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_PROCESSO")
	public StatusProcesso getStatusProcesso() {
		return statusProcesso;
	}

	public void setStatusProcesso(StatusProcesso statusProcesso) {
		this.statusProcesso = statusProcesso;
	}

	@Column(name="HORAS_PRAZO_SITUACAO")
	public BigDecimal getHorasPrazoSituacao() {
		return horasPrazoSituacao;
	}

	public void setHorasPrazoSituacao(BigDecimal horasPrazoSituacao) {
		this.horasPrazoSituacao = horasPrazoSituacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE_SITUACAO")
	public Date getPrazoLimiteSituacao() {
		return prazoLimiteSituacao;
	}

	public void setPrazoLimiteSituacao(Date prazoLimiteSituacao) {
		this.prazoLimiteSituacao = prazoLimiteSituacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE_ETAPA")
	public Date getPrazoLimiteEtapa() {
		return prazoLimiteEtapa;
	}

	public void setPrazoLimiteEtapa(Date prazoLimiteEtapa) {
		this.prazoLimiteEtapa = prazoLimiteEtapa;
	}

	@Column(name="LIDO")
	public Boolean getLido() {
		return lido;
	}

	public void setLido(Boolean lido) {
		this.lido = lido;
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
	@JoinColumn(name="TIPO_EVIDENCIA_ID")
	public TipoEvidencia getTipoEvidencia() {
		return tipoEvidencia;
	}

	public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {
		this.tipoEvidencia = tipoEvidencia;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
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
	@JoinColumn(name="SOLICITACAO_ID")
	public Solicitacao getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(Solicitacao solicitacao) {
		this.solicitacao = solicitacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_PENDENCIA_ID")
	public ProcessoPendencia getPendencia() {
		return pendencia;
	}

	public void setPendencia(ProcessoPendencia pendencia) {
		this.pendencia = pendencia;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EMAIL_ENVIADO_ID")
	public EmailEnviado getEmailEnviado() {
		return emailEnviado;
	}

	public void setEmailEnviado(EmailEnviado emailEnviado) {
		this.emailEnviado = emailEnviado;
	}

	@OrderBy("id")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="processoLog")
	public Set<ProcessoLogAnexo> getAnexos() {
		return this.anexos;
	}

	public void setAnexos(Set<ProcessoLogAnexo> anexos) {
		this.anexos = anexos;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ANTERIOR_ID")
	public Situacao getSituacaoAnterior() {
		return situacaoAnterior;
	}

	public void setSituacaoAnterior(Situacao situacaoAnterior) {
		this.situacaoAnterior = situacaoAnterior;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LOG_IMPORTACAO_ID")
	public LogImportacao getLogImportacao() {
		return logImportacao;
	}

	public void setLogImportacao(LogImportacao logImportacao) {
		this.logImportacao = logImportacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ETAPA_ID")
	public Etapa getEtapa() {
		return etapa;
	}

	public void setEtapa(Etapa etapa) {
		this.etapa = etapa;
	}

	@Column(name="HORAS_PRAZO_ETAPA")
	public BigDecimal getHorasPrazoEtapa() {
		return horasPrazoEtapa;
	}

	public void setHorasPrazoEtapa(BigDecimal horasPrazoEtapa) {
		this.horasPrazoEtapa = horasPrazoEtapa;
	}

	@Transient
	public String getObservacaoCurta() {
		return getObservacaoCurta2(70);
	}

	@Transient
	public String getObservacaoCurta2(int size) {

		String observacao = getObservacao();
		if(StringUtils.isBlank(observacao)) {
			return observacao;
		}

		observacao = observacao.replace("\t", " ");
		observacao = observacao.replace("\n", " ");
		observacao = observacao.replace("\r", " ");
		observacao = observacao.replace("&nbsp;", " ");
		observacao = observacao.replaceAll("\\<.*?>", " ");
		while(observacao.contains("  ")) {
			observacao = observacao.replace("  ", " ");
		}

		if(observacao.length() > size) {
			observacao = observacao.substring(0, (size - 5)) + "[...]";
		}

		return observacao;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{acao:" + getAcao() + ",data:" + DummyUtils.formatDateTime2(getData()) + "}";
	}
}
