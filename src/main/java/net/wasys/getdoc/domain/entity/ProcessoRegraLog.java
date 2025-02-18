package net.wasys.getdoc.domain.entity;

import java.util.Date;
import java.util.List;

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
import javax.persistence.Transient;

import net.wasys.getdoc.domain.enumeration.FarolRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoSubRegra;

@Entity(name="PROCESSO_REGRA_LOG")
public class ProcessoRegraLog extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;
	private StatusProcessoRegra status;
	private Long subRegraId;
	private TipoSubRegra tipo;
	private String condicionalJs;
	private FarolRegra farol;
	private Boolean filhoSim;
	private TipoConsultaExterna consultaExterna;
	private String varConsulta;
	private String observacao;
	private String params;
	private String result;
	private String stackTrace;
	private String js;

	private Regra regraFilha;
	private BaseInterna baseInterna;
	private ProcessoRegra processoRegra;
	private Situacao situacaoDestino;

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

	@Column(name="VAR_CONSULTA")
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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA")
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS")
	public StatusProcessoRegra getStatus() {
		return status;
	}

	public void setStatus(StatusProcessoRegra status) {
		this.status = status;
	}

	@Column(name="SUB_REGRA_ID")
	public Long getSubRegraId() {
		return subRegraId;
	}

	public void setSubRegraId(Long subRegraId) {
		this.subRegraId = subRegraId;
	}

	@Column(name="PARAMS")
	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	@Column(name="RESULT")
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Column(name="JS")
	public String getJs() {
		return js;
	}

	public void setJs(String js) {
		this.js = js;
	}

	@Column(name="STACK_TRACE")
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_REGRA_ID")
	public ProcessoRegra getProcessoRegra() {
		return processoRegra;
	}

	public void setProcessoRegra(ProcessoRegra processoRegra) {
		this.processoRegra = processoRegra;
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

	@Transient
	public boolean isPaiCondicional() {

		if(processoRegra == null || processoRegra.getProcessoRegraLogs().isEmpty()) {
			return false;
		}

		ProcessoRegraLog anterior = null;

		List<ProcessoRegraLog> logs = processoRegra.getProcessoRegraLogs();
		for (ProcessoRegraLog logAtual : logs) {
			if (anterior == null && logAtual.equals(this)) {
				return false;
			}
			if (anterior != null && logAtual.equals(this) && TipoSubRegra.CONDICAO.equals(anterior.getTipo())) {
				return true;
			}

			anterior = logAtual;
		}

		return false;
	}

	@Transient
	public boolean isFim() {
		return TipoSubRegra.FIM.equals(this.getTipo());
	}
}