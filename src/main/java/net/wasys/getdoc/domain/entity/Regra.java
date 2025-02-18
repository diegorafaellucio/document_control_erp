package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;

import java.util.*;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"NOME"}))
public class Regra extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String descricao;
	private Date dataAlteracao;
	private boolean ativa = true;
	private Date inicioVigencia;
	private Date fimVigencia;
	private TipoExecucaoRegra tipoExecucao;
	private Integer decisaoFluxo;
	private boolean reprocessaEditarCampos = false;
	private boolean reprocessaAtualizarDocumentos = false;

	private Situacao situacao;

	private List<RegraRole> regraRoles  = new ArrayList<>(0);
	private Set<RegraTipoProcesso> tiposProcessos = new HashSet<>(0);

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

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="DESCRICAO", length=100)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAtualizacao) {
		this.dataAlteracao = dataAtualizacao;
	}

	@Column(name="ATIVA", nullable=false)
	public boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	@Column(name="INICIO_VIGENCIA")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getInicioVigencia() {
		return inicioVigencia;
	}

	public void setInicioVigencia(Date inicioVigencia) {
		this.inicioVigencia = inicioVigencia;
	}

	@Column(name="FIM_VIGENCIA")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFimVigencia() {
		return fimVigencia;
	}

	public void setFimVigencia(Date fimVigencia) {
		this.fimVigencia = fimVigencia;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_EXECUCAO")
	public TipoExecucaoRegra getTipoExecucao() {
		return tipoExecucao;
	}

	public void setTipoExecucao(TipoExecucaoRegra tipoExecucao) {
		this.tipoExecucao = tipoExecucao;
	}

	@Column(name="DECISAO_FLUXO")
	public Integer getDecisaoFluxo() {
		return decisaoFluxo;
	}

	public void setDecisaoFluxo(Integer decisaoFluxo) {
		this.decisaoFluxo = decisaoFluxo;
	}

	@Column(name="REPROCESSA_EDITAR_CAMPOS")
	public boolean isReprocessaEditarCampos() {
		return reprocessaEditarCampos;
	}

	public void setReprocessaEditarCampos(boolean reprocessaEditarCampos) {
		this.reprocessaEditarCampos = reprocessaEditarCampos;
	}

	@Column(name="REPROCESSA_ATUALIZAR_DOCUMENTOS")
	public boolean isReprocessaAtualizarDocumentos() {
		return reprocessaAtualizarDocumentos;
	}

	public void setReprocessaAtualizarDocumentos(boolean reprocessaAtualizarDocumentos) {
		this.reprocessaAtualizarDocumentos = reprocessaAtualizarDocumentos;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID", nullable=false)
	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="regra", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<RegraTipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(Set<RegraTipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="regra", cascade=CascadeType.ALL, orphanRemoval=true)
	public List<RegraRole> getRegraRoles() { return regraRoles; }

	public void setRegraRoles(List<RegraRole> regraRoles) { this.regraRoles = regraRoles; }

	@Transient
	public TipoProcesso getTipoProcesso() {
		Set<RegraTipoProcesso> tiposProcessos = getTiposProcessos();
		if(tiposProcessos == null || tiposProcessos.isEmpty()) {
			return null;
		}
		RegraTipoProcesso tipoProcesso = tiposProcessos.iterator().next();
		return tipoProcesso.getTipoProcesso();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + getNome() + "}";
	}
}
