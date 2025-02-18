package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="CAMPANHA")
public class Campanha extends net.wasys.util.ddd.Entity {

	private Long id;
	private String descricao;
	private Date inicioVigencia;
	private Date fimVigencia;
	private String equivalencias;
	private String tipoDocumentoObrigatorioIds;
	private String exibirNoPortalIds;
	private String cursos;
	private String instituicoes;
	private String campus;
	private boolean padrao;

	private TipoProcesso tipoProcesso;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Column(name="EQUIVALENCIAS")
	public String getEquivalencias() {
		return equivalencias;
	}

	public void setEquivalencias(String equivalencias) {
		this.equivalencias = equivalencias;
	}

	@Column(name="TIPO_DOCUMENTO_OBRIGATORIO_IDS")
	public String getTipoDocumentoObrigatorioIds() {
		return tipoDocumentoObrigatorioIds;
	}

	public void setTipoDocumentoObrigatorioIds(String tipoDocumentoObrigatorioIds) {
		this.tipoDocumentoObrigatorioIds = tipoDocumentoObrigatorioIds;
	}

	@Column(name="EXIBIR_NO_PORTAL_IDS")
	public String getExibirNoPortalIds() {
		return exibirNoPortalIds;
	}

	public void setExibirNoPortalIds(String exibirNoPortalIds) {
		this.exibirNoPortalIds = exibirNoPortalIds;
	}

	@Column(name="CURSOS")
	public String getCursos() {
		return this.cursos;
	}

	public void setCursos(String cursos) {
		this.cursos = cursos;
	}

	@Column(name="INSTITUICOES")
	public String getInstituicoes() {
		return this.instituicoes;
	}

	public void setInstituicoes(String instituicoes) {
		this.instituicoes = instituicoes;
	}

	@Column(name="CAMPUS")
	public String getCampus() {
		return this.campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	@Column(name="PADRAO", nullable=false)
	public boolean isPadrao() {
		return padrao;
	}

	public void setPadrao(boolean padrao) {
		this.padrao = padrao;
	}

}
