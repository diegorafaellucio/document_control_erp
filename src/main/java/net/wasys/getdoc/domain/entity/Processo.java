package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Processo extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date dataCriacao;
	private Date dataEnvioAnalise;
	private Date dataFinalizacao;
	private BigDecimal horasPrazoAnalise;
	private Date prazoLimiteAnalise;
	private Date prazoLimiteEmAcompanhamento;
	private Date dataUltimaAcaoAnalista;
	private Date dataFinalizacaoAnalise;
	private Integer nivelPrioridade;
	private Long ultimaAjudaId;
	private StatusProcesso status;
	private Date prazoLimitePendente;
	private Date prazoLimiteSituacao;
	private BigDecimal horasPrazoSituacao;
	private StatusPrazo statusPrazo;
	private StatusOcr statusOcr;
	private Origem origem;
	private String numCandidato;
	private String numInscricao;
	private Long alunoProcessoId;
	private Boolean usaTermo;
	private Boolean eliminadoOrCancelado = false;

	private MotivoCancelamento motivoCancelamento;
	private TipoProcesso tipoProcesso;
	private Usuario analista;
	private Usuario autor;
	private Situacao situacao;
	private Aluno aluno;
	private Campanha campanha;
	private Processo processoOriginal;
	private Date dataUltimaAtualizacao;

	private Set<CampoGrupo> gruposCampos = new HashSet<CampoGrupo>(0);

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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CRIACAO", nullable=false)
	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ENVIO_ANALISE")
	public Date getDataEnvioAnalise() {
		return dataEnvioAnalise;
	}

	public void setDataEnvioAnalise(Date dataEnvioAnalise) {
		this.dataEnvioAnalise = dataEnvioAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO")
	public Date getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(Date dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	@Column(name="PRAZO_LIMITE_ANALISE")
	public Date getPrazoLimiteAnalise() {
		return prazoLimiteAnalise;
	}

	public void setPrazoLimiteAnalise(Date prazoAnalise) {
		this.prazoLimiteAnalise = prazoAnalise;
	}

	@Column(name="PRAZO_LIMITE_EM_ACOMPANHAMENTO")
	public Date getPrazoLimiteEmAcompanhamento() {
		return prazoLimiteEmAcompanhamento;
	}

	public void setPrazoLimiteEmAcompanhamento(Date prazoLimiteEmAcompanhamento) {
		this.prazoLimiteEmAcompanhamento = prazoLimiteEmAcompanhamento;
	}

	@Column(name="HORAS_PRAZO_ANALISE")
	public BigDecimal getHorasPrazoAnalise() {
		return horasPrazoAnalise;
	}

	public void setHorasPrazoAnalise(BigDecimal horasPrazoAnalise) {
		this.horasPrazoAnalise = horasPrazoAnalise;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable=false)
	public StatusProcesso getStatus() {
		return status;
	}

	public void setStatus(StatusProcesso status) {
		this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMA_ACAO_ANALISTA")
	public Date getDataUltimaAcaoAnalista() {
		return dataUltimaAcaoAnalista;
	}

	public void setDataUltimaAcaoAnalista(Date dataUltimaAcaoAnalista) {
		this.dataUltimaAcaoAnalista = dataUltimaAcaoAnalista;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO_ANALISE")
	public Date getDataFinalizacaoAnalise() {
		return dataFinalizacaoAnalise;
	}

	public void setDataFinalizacaoAnalise(Date dataFinalizacaoAnalise) {
		this.dataFinalizacaoAnalise = dataFinalizacaoAnalise;
	}

	@Column(name="NIVEL_PRIORIDADE")
	public Integer getNivelPrioridade() {
		return nivelPrioridade;
	}

	public void setNivelPrioridade(Integer nivelPrioridade) {
		this.nivelPrioridade = nivelPrioridade;
	}

	@Column(name="ULTIMA_AJUDA_ID")
	public Long getUltimaAjudaId() {
		return ultimaAjudaId;
	}

	public void setUltimaAjudaId(Long ultimaAjudaId) {
		this.ultimaAjudaId = ultimaAjudaId;
	}

	@Column(name="PROXIMO_PRAZO")
	public Date getProximoPrazo() {

		switch (getStatus()) {
		case AGUARDANDO_ANALISE:
			return getPrazoLimiteAnalise();
		case EM_ANALISE:
			return getPrazoLimiteAnalise();
		case EM_ACOMPANHAMENTO:
			return getPrazoLimiteEmAcompanhamento();
		case PENDENTE:
			return getPrazoLimitePendente();
		default:
			return null;
		}
	}

	@Deprecated
	public void setProximoPrazo(Date proximoPrazo) {}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ANALISTA_ID")
	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="AUTOR_ID")
	public Usuario getAutor() {
		return autor;
	}

	public void setAutor(Usuario autor) {
		this.autor = autor;
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
	@JoinColumn(name="MOTIVO_CANCELAMENTO_ID")
	public MotivoCancelamento getMotivoCancelamento() {
		return motivoCancelamento;
	}

	public void setMotivoCancelamento(MotivoCancelamento motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}

	@OrderBy("ordem")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="processo")
	public Set<CampoGrupo> getGruposCampos() {
		return this.gruposCampos;
	}

	public void setGruposCampos(Set<CampoGrupo> gruposCampos) {
		this.gruposCampos = gruposCampos;
	}

	@Column(name="PRAZO_LIMITE_PENDENTE")
	public Date getPrazoLimitePendente() {
		return prazoLimitePendente;
	}

	public void setPrazoLimitePendente(Date prazoLimitePendente) {
		this.prazoLimitePendente = prazoLimitePendente;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE_SITUACAO")
	public Date getPrazoLimiteSituacao() {
		return prazoLimiteSituacao;
	}

	public void setPrazoLimiteSituacao(Date prazoLimiteSituacao) {
		this.prazoLimiteSituacao = prazoLimiteSituacao;
	}

	@Column(name="HORAS_PRAZO_SITUACAO")
	public BigDecimal getHorasPrazoSituacao() {
		return horasPrazoSituacao;
	}

	public void setHorasPrazoSituacao(BigDecimal horasPrazoSituacao) {
		this.horasPrazoSituacao = horasPrazoSituacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_PRAZO")
	public StatusPrazo getStatusPrazo() {
		return statusPrazo;
	}

	public void setStatusPrazo(StatusPrazo statusPrazo) {
		this.statusPrazo = statusPrazo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_OCR", length=20)
	public StatusOcr getStatusOcr() {
		return statusOcr;
	}

	public void setStatusOcr(StatusOcr statusOcr) {
		this.statusOcr = statusOcr;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ORIGEM", length=20)
	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ALUNO_ID")
	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAMPANHA_ID")
	public Campanha getCampanha() {
		return campanha;
	}

	public void setCampanha(Campanha campanha) {
		this.campanha = campanha;
	}

	@Column(name="USA_TERMO")
	public Boolean getUsaTermo() {
		return usaTermo;
	}

	public void setUsaTermo(Boolean usaTermo) {
		this.usaTermo = usaTermo;
	}

	@Column(name="ELIMINADO_OU_CANCELADO")
	public Boolean getEliminadoOrCancelado() {
		return eliminadoOrCancelado;
	}

	public void setEliminadoOrCancelado(Boolean eliminadoOrCancelado) {
		this.eliminadoOrCancelado = eliminadoOrCancelado;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ORIGINAL_ID")
	public Processo getProcessoOriginal() {
		return processoOriginal;
	}

	public void setProcessoOriginal(Processo processoOriginal) {
		this.processoOriginal = processoOriginal;
	}

	@Transient
	public boolean isMestradoDoutoradoMedicina() {

		Long tipoProcessoId = tipoProcesso.getId();
		if(Arrays.asList(TipoProcesso.PS_DOUTORADO,TipoProcesso.PS_MESTRADO, TipoProcesso.PS_POS_DOUTORADO,
				TipoProcesso.DI_DOUTORADO, TipoProcesso.DI_MESTRADO,
				TipoProcesso.MEDICINA_ENEM, TipoProcesso.MEDICINA_TE, TipoProcesso.MEDICINA_VESTIBULAR).contains(tipoProcessoId)) {
			return true;
		}

		return false;
	}

	@Transient
	public boolean isSisFiesOrSisProuni() {

		Long tipoProcessoId = tipoProcesso.getId();
		if(Arrays.asList(TipoProcesso.SIS_FIES, TipoProcesso.SIS_PROUNI, TipoProcesso.TE_PROUNI, TipoProcesso.TE_FIES).contains(tipoProcessoId)) {
			return true;
		}

		return false;
	}

	@Column(name="NUM_CANDIDATO")
	public String getNumCandidato() {
		return numCandidato;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}

	@Column(name="NUM_INSCRICAO")
	public String getNumInscricao() {
		return numInscricao;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
	}

	@Column(name="ALUNO_PROCESSO_ID")
	public Long getAlunoProcessoId() {
		return alunoProcessoId;
	}

	public void setAlunoProcessoId(Long alunoProcessoId) {
		this.alunoProcessoId = alunoProcessoId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMA_ATUALIZACAO")
	public Date getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}

	public void setDataUltimaAtualizacao(Date dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
}
