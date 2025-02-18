package net.wasys.getdoc.domain.entity;

import java.util.*;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import net.wasys.getdoc.domain.enumeration.FarolRegra;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoSubRegra;

@Entity(name="SUB_REGRA")
public class SubRegra extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoSubRegra tipo;
	private String condicionalJs;
	private Boolean filhoSim;
	private FarolRegra farol;
	private TipoConsultaExterna consultaExterna;
	private String subConsultaExterna;
	private String observacao;
	private String varConsulta;

	private Regra regraFilha;
	private RegraLinha linha;
	private BaseInterna baseInterna;
	private Situacao situacaoDestino;

	private List<DeparaParam> deparaParams = new ArrayList<>();
	private List<DeparaRetorno> deparaRetornos = new ArrayList<>();
	private List<SubRegraAcao> subRegraAcoes = new ArrayList<>();

	@Id
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tipo", nullable=false)
	public TipoSubRegra getTipo() {
		return tipo;
	}

	public void setTipo(TipoSubRegra tipo) {
		this.tipo = tipo;
	}

	@Column(name="CONDICIONAL_JS")
	public String getCondicionalJs() {
		return condicionalJs;
	}

	public void setCondicionalJs(String condicionalJs) {
		this.condicionalJs = condicionalJs;
	}

	@Column(name="FILHO_SIM")
	public Boolean getFilhoSim() {
		return filhoSim;
	}

	public void setFilhoSim(Boolean filhoSim) {
		this.filhoSim = filhoSim;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="FAROL")
	public FarolRegra getFarol() {
		return farol;
	}

	public void setFarol(FarolRegra farol) {
		this.farol = farol;
	}

	@Column(name="VAR_CONSULTA", length=30)
	public String getVarConsulta() {
		return varConsulta;
	}

	public void setVarConsulta(String varConsulta) {
		this.varConsulta = varConsulta;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="CONSULTA_EXTERNA")
	public TipoConsultaExterna getConsultaExterna() {
		return consultaExterna;
	}

	public void setConsultaExterna(TipoConsultaExterna consultaExterna) {
		this.consultaExterna = consultaExterna;
	}

	@Column(name="OBSERVACAO")
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Column(name="SUB_CONSULTA_EXTERNA")
	public String getSubConsultaExterna() {
		return subConsultaExterna;
	}

	public void setSubConsultaExterna(String subConsultaExterna) {
		this.subConsultaExterna = subConsultaExterna;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGRA_LINHA_ID")
	public RegraLinha getLinha() {
		return linha;
	}

	public void setLinha(RegraLinha linha) {
		this.linha = linha;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGRA_FILHA_ID")
	public Regra getRegraFilha() {
		return regraFilha;
	}

	public void setRegraFilha(Regra regraFilha) {
		this.regraFilha = regraFilha;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID")
	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_DESTINO_ID")
	public Situacao getSituacaoDestino() {
		return situacaoDestino;
	}

	public void setSituacaoDestino(Situacao situacaoDestino) {
		this.situacaoDestino = situacaoDestino;
	}

	@OrderBy("ORIGEM")
	@OneToMany(mappedBy="subRegra", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	public List<DeparaParam> getDeparaParams() {
		return deparaParams;
	}

	public void setDeparaParams(List<DeparaParam> deparaParams) {
		this.deparaParams = deparaParams;
	}

	@OrderBy("ID")
	@OneToMany(mappedBy="subRegra", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	public List<DeparaRetorno> getDeparaRetornos() {
		return deparaRetornos;
	}

	public void setDeparaRetornos(List<DeparaRetorno> deparaRetornos) {
		this.deparaRetornos = deparaRetornos;
	}

	@OneToMany(mappedBy="subRegra", fetch=FetchType.EAGER)
	public List<SubRegraAcao> getSubRegraAcoes() {
		return subRegraAcoes;
	}

	public void setSubRegraAcoes(List<SubRegraAcao> subRegraAcoes) {
		this.subRegraAcoes = subRegraAcoes;
	}

	@Transient
	public boolean isPaiCondicional() {
		return linha.isPaiCondicional();
	}

	@Transient
	public boolean isTipoCondicional() {
		return TipoSubRegra.CONDICAO.equals(tipo);
	}

	@Transient
	public boolean isTipoBaseInterna() {
		return TipoSubRegra.BASE_INTERNA.equals(tipo);
	}

	@Transient
	public boolean isTipoConsultaExterna() {
		return TipoSubRegra.CONSULTA_EXTERNA.equals(tipo);
	}

	@Transient
	public boolean isTipoChamadaRegra() {
		return TipoSubRegra.CHAMADA_REGRA.equals(tipo);
	}

	@Transient
	public boolean isTipoFim() {
		return TipoSubRegra.FIM.equals(tipo);
	}

	@Transient
	public Set<SubRegra> getSubRegrasPai() {

		RegraLinha linhaPai = linha.getLinhaPai();
		if(linhaPai != null) {
			return linhaPai.getSubRegras();
		}

		return Collections.emptySet();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{tipo:" + getTipo() + "}";
	}
}