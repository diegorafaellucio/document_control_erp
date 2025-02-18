package net.wasys.getdoc.domain.entity;

import net.wasys.util.DummyUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name="RELATORIO_GERAL_ETAPA")
public class RelatorioGeralEtapa extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date dataInicio;
	private Date dataFim;
	private Date prazoLimite;
	private BigDecimal tempo;
	private Boolean prazoOk;
	private int tratativasNaoContada;
	private Long processoLogInicialId;
	private String usuarios;

	private Etapa etapa;
	private Situacao situacaoDe;
	private Situacao situacaoPara;
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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_INICIO")
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date data) {
		this.dataInicio = data;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FIM")
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ETAPA_ID")
	public Etapa getEtapa() {
		return etapa;
	}

	public void setEtapa(Etapa etapa) {
		this.etapa = etapa;
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

	@Column(name="PRAZO_OK")
	public Boolean getPrazoOk() {
		return prazoOk;
	}

	public void setPrazoOk(Boolean prazoOk) {
		this.prazoOk = prazoOk;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="PRAZO_LIMITE")
	public Date getPrazoLimite() { return prazoLimite; }

	public void setPrazoLimite(Date prazoLimite) { this.prazoLimite = prazoLimite; }

	@Column(name = "TRATATIVAS_NAO_CONTADA")
	public int getTratativasNaoContada() {
		return tratativasNaoContada;
	}

	public void setTratativasNaoContada(int tratativasNaoContada) {
		this.tratativasNaoContada = tratativasNaoContada;
	}

	@Column(name = "PROCESSO_LOG_INICIAL_ID")
	public Long getProcessoLogInicialId() {
		return processoLogInicialId;
	}

	public void setProcessoLogInicialId(Long processoLogInicialId) {
		this.processoLogInicialId = processoLogInicialId;
	}

	@Type(type="StringJsonObject")
	@Column(name = "USUARIOS")
	public String getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(String usuarios) {
		this.usuarios = usuarios;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_DE_ID")
	public Situacao getSituacaoDe() {
		return situacaoDe;
	}

	public void setSituacaoDe(Situacao situacao) {
		this.situacaoDe = situacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_PARA_ID")
	public Situacao getSituacaoPara() {
		return situacaoPara;
	}

	public void setSituacaoPara(Situacao situacao) {
		this.situacaoPara = situacao;
	}

	@Override
	public String toString() {
		Etapa etapa = getEtapa();
		String etapaNome = etapa != null ? etapa.getNome() : "";
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + etapaNome + ",data:" + DummyUtils.formatDateTime2(getDataInicio()) + "}";
	}
}