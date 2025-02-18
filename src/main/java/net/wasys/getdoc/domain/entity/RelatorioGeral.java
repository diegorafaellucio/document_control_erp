package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.Estado;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.util.postgre.StringJsonUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name="RELATORIO_GERAL")
@TypeDefs({@TypeDef(name="StringJsonObject", typeClass=StringJsonUserType.class)})
public class RelatorioGeral extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long processoId;
	private Date dataCriacao;
	private Date dataEnvioAnalise;
	private Date dataEmAnalise;
	private Date dataEmAcompanhamento;
	private Date dataFinalizacao;
	private Date dataUltimaTratativa;
	private StatusProcesso status;
	private Integer nivelPrioridade;
	private BigDecimal horasPrazoAnalise;
	private Date prazoLimiteAnalise;
	private Date prazoLimiteEmAcompanhamento;
	private Date dataFinalizacaoAnalise;
	private Date dataFinalizacaoEmAcompanhamento;
	private Date dataUltimaAtualizacao;
	private StatusPrazo statusPrazoSituacao;

	private String numero;
	private String cpfCnpj;
	private String nome;
	private String nomePai;
	private String nomeMae;
	private Date dataEmissao;
	private String orgaoEmissor;
	private String identidade;
	private Estado identidadeUf;
	private String passaporte;
	private String nomeSocial;

	private BigDecimal tempoRascunho;
	private Integer vezesAguardandoAnalise;
	private BigDecimal tempoAguardandoAnalise;
	private Integer vezesPendente;
	private BigDecimal tempoPendente;
	private Integer vezesEmAnalise;
	private Integer vezesEmAcompanhamento;
	private BigDecimal tempoEmAnalise;
	private BigDecimal tempoAteFinalizacao;
	private BigDecimal tempoEmAcompanhamento;
	private BigDecimal tempoAteFinalizacaoAnalise;
	private BigDecimal tempoSlaCriacao;
	private BigDecimal tempoSlaTratativa;
	private BigDecimal horasExpediente;
	private String camposDinamicos;
	private Origem origem;

	private Situacao situacao;
	private Usuario autor;
	private Usuario analista;
	private TipoProcesso tipoProcesso;
	private String irregularidades;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="PROCESSO_ID", nullable=false)
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
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
	@Column(name="DATA_FINALIZACAO")
	public Date getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(Date dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMA_TRATATIVA")
	public Date getDataUltimaTratativa() {
		return dataUltimaTratativa;
	}

	public void setDataUltimaTratativa(Date dataUltimaTratativa) {
		this.dataUltimaTratativa = dataUltimaTratativa;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable=false)
	public StatusProcesso getStatus() {
		return status;
	}

	public void setStatus(StatusProcesso status) {
		this.status = status;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Column(name="NUMERO")
	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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
	@Column(name="DATA_EM_ANALISE")
	public Date getDataEmAnalise() {
		return dataEmAnalise;
	}

	public void setDataEmAnalise(Date dataEmAnalise) {
		this.dataEmAnalise = dataEmAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EM_ACOMPANHAMENTO")
	public Date getDataEmAcompanhamento() {
		return dataEmAcompanhamento;
	}

	public void setDataEmAcompanhamento(Date dataEmAcompanhamento) {
		this.dataEmAcompanhamento = dataEmAcompanhamento;
	}

	@Column(name="NIVEL_PRIORIDADE")
	public Integer getNivelPrioridade() {
		return nivelPrioridade;
	}

	public void setNivelPrioridade(Integer nivelPrioridade) {
		this.nivelPrioridade = nivelPrioridade;
	}

	@Column(name="HORAS_PRAZO_ANALISE")
	public BigDecimal getHorasPrazoAnalise() {
		return horasPrazoAnalise;
	}

	public void setHorasPrazoAnalise(BigDecimal horasPrazoAnalise) {
		this.horasPrazoAnalise = horasPrazoAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE_ANALISE")
	public Date getPrazoLimiteAnalise() {
		return prazoLimiteAnalise;
	}

	public void setPrazoLimiteAnalise(Date prazoAnalise) {
		this.prazoLimiteAnalise = prazoAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE_EM_ACOMPANHAMENTO")
	public Date getPrazoLimiteEmAcompanhamento() {
		return prazoLimiteEmAcompanhamento;
	}

	public void setPrazoLimiteEmAcompanhamento(Date prazoLimiteEmAcompanhamento) {
		this.prazoLimiteEmAcompanhamento = prazoLimiteEmAcompanhamento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO_ANALISE")
	public Date getDataFinalizacaoAnalise() {
		return dataFinalizacaoAnalise;
	}

	public void setDataFinalizacaoAnalise(Date dataFinalizacaoAnalise) {
		this.dataFinalizacaoAnalise = dataFinalizacaoAnalise;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO_ACOMPANHAMENTO")
	public Date getDataFinalizacaoEmAcompanhamento() {
		return dataFinalizacaoEmAcompanhamento;
	}

	public void setDataFinalizacaoEmAcompanhamento(Date dataFinalizacaoEmAcompanhamento) {
		this.dataFinalizacaoEmAcompanhamento = dataFinalizacaoEmAcompanhamento;
	}

	@Version
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMA_ATUALIZACAO")
	public Date getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}

	public void setDataUltimaAtualizacao(Date dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}

	@Column(name="CPF_CNPJ")
	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ANALISTA_ID")
	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	@Column(name="TEMPO_RASCUNHO")
	public BigDecimal getTempoRascunho() {
		return tempoRascunho;
	}

	public void setTempoRascunho(BigDecimal tempoRascunho) {
		this.tempoRascunho = tempoRascunho;
	}

	@Column(name="VEZES_AGUARDANDO_ANALISE")
	public Integer getVezesAguardandoAnalise() {
		return vezesAguardandoAnalise;
	}

	public void setVezesAguardandoAnalise(Integer vezesAguardandoAnalise) {
		this.vezesAguardandoAnalise = vezesAguardandoAnalise;
	}

	@Column(name="TEMPO_AGUARDANDO_ANALISE")
	public BigDecimal getTempoAguardandoAnalise() {
		return tempoAguardandoAnalise;
	}

	public void setTempoAguardandoAnalise(BigDecimal tempoAguardandoAnalise) {
		this.tempoAguardandoAnalise = tempoAguardandoAnalise;
	}

	@Column(name="TEMPO_PENDENTE")
	public BigDecimal getTempoPendente() {
		return tempoPendente;
	}

	public void setTempoPendente(BigDecimal tempoPendente) {
		this.tempoPendente = tempoPendente;
	}

	@Column(name="VEZES_PENDENTE")
	public Integer getVezesPendente() {
		return vezesPendente;
	}

	public void setVezesPendente(Integer vezesPendente) {
		this.vezesPendente = vezesPendente;
	}

	@Column(name="TEMPO_EM_ANALISE")
	public BigDecimal getTempoEmAnalise() {
		return tempoEmAnalise;
	}

	public void setTempoEmAnalise(BigDecimal tempoEmAnalise) {
		this.tempoEmAnalise = tempoEmAnalise;
	}

	@Column(name="VEZES_EM_ANALISE")
	public Integer getVezesEmAnalise() {
		return vezesEmAnalise;
	}

	public void setVezesEmAnalise(Integer vezesEmAnalise) {
		this.vezesEmAnalise = vezesEmAnalise;
	}

	@Column(name="TEMPO_ATE_FINALIZACAO")
	public BigDecimal getTempoAteFinalizacao() {
		return tempoAteFinalizacao;
	}

	public void setTempoAteFinalizacao(BigDecimal tempoAteFinalizacao) {
		this.tempoAteFinalizacao = tempoAteFinalizacao;
	}

	@Column(name="TEMPO_EM_ACOMPANHAMENTO")
	public BigDecimal getTempoEmAcompanhamento() {
		return tempoEmAcompanhamento;
	}

	public void setTempoEmAcompanhamento(BigDecimal tempoEmAcompanhamento) {
		this.tempoEmAcompanhamento = tempoEmAcompanhamento;
	}

	@Column(name="VEZES_EM_ACOMPANHAMENTO")
	public Integer getVezesEmAcompanhamento() {
		return vezesEmAcompanhamento;
	}

	public void setVezesEmAcompanhamento(Integer vezesEmAcompanhamento) {
		this.vezesEmAcompanhamento = vezesEmAcompanhamento;
	}

	@Column(name="TEMPO_SLA_CRIACAO")
	public BigDecimal getTempoSlaCriacao() {
		return tempoSlaCriacao;
	}

	public void setTempoSlaCriacao(BigDecimal tempoSlaCriacao) {
		this.tempoSlaCriacao = tempoSlaCriacao;
	}

	@Column(name="TEMPO_SLA_TRATATIVA")	
	public BigDecimal getTempoSlaTratativa() {
		return tempoSlaTratativa;
	}

	public void setTempoSlaTratativa(BigDecimal tempoSlaTratativa) {
		this.tempoSlaTratativa = tempoSlaTratativa;
	}

	@Column(name="HORAS_EXPEDIENTE")
	public BigDecimal getHorasExpediente() {
		return horasExpediente;
	}

	public void setHorasExpediente(BigDecimal horasExpediente) {
		this.horasExpediente = horasExpediente;
	}

	@Column(name="TEMPO_ATE_FINALIZACAO_ANALISE")
	public BigDecimal getTempoAteFinalizacaoAnalise() {
		return tempoAteFinalizacaoAnalise;
	}

	public void setTempoAteFinalizacaoAnalise(BigDecimal tempoAteFinalizacaoAnalise) {
		this.tempoAteFinalizacaoAnalise = tempoAteFinalizacaoAnalise;
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

	@Type(type="StringJsonObject")
	@Column(name="CAMPOS_DINAMICOS")
	public String getCamposDinamicos() {
		return camposDinamicos;
	}

	public void setCamposDinamicos(String camposDinamicos) {
		this.camposDinamicos = camposDinamicos;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ORIGEM", length=20)
	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@Column(name="IRREGULARIDADES")
	public String getIrregularidades() {
		return irregularidades;
	}

	public void setIrregularidades(String irregularidades) {
		this.irregularidades = irregularidades;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_PRAZO_SITUACAO")
	public StatusPrazo getStatusPrazoSituacao() { return statusPrazoSituacao; }

	public void setStatusPrazoSituacao(StatusPrazo statusPrazoSituacao) { this.statusPrazoSituacao = statusPrazoSituacao; }

	@Column(name="NOME_SOCIAL")
	public String getNomeSocial() {
		return nomeSocial;
	}

	public void setNomeSocial(String nomeSocial) {
		this.nomeSocial = nomeSocial;
	}

	@Column(name="PASSAPORTE")
	public String getPassaporte() {
		return passaporte;
	}

	public void setPassaporte(String passaporte) {
		this.passaporte = passaporte;
	}

	@Column(name="IDENTIDADE")
	public String getIdentidade() {
		return identidade;
	}

	public void setIdentidade(String identidade) {
		this.identidade = identidade;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="IDENTIDADE_UF")
	public Estado getIdentidadeUf() {
		return identidadeUf;
	}

	public void setIdentidadeUf(Estado identidadeUf) {
		this.identidadeUf = identidadeUf;
	}

	@Column(name="ORGAO_EMISSOR")
	public String getOrgaoEmissor() {
		return orgaoEmissor;
	}

	public void setOrgaoEmissor(String orgaoEmissor) {
		this.orgaoEmissor = orgaoEmissor;
	}

	@Column(name="DATA_EMISSAO")
	public Date getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(Date dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	@Column(name="NOME_MAE")
	public String getNomeMae() {
		return nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	@Column(name="NOME_PAI")
	public String getNomePai() {
		return nomePai;
	}

	public void setNomePai(String nomePai) {
		this.nomePai = nomePai;
	}
}