package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOG_ATENDIMENTO" )
public class LogAtendimento extends net.wasys.util.ddd.Entity {

	private Long id;
	private Usuario analista;
	private StatusLaboral statusLaboral;
	private Date inicio;
	private Date fim;
	private Long tempo;
	private Processo processo;
	private Situacao situacaoInicial;
	private Situacao situacaoFinal;
	private Date dataEmAnalise;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STATUS_LABORAL_ID")
	public StatusLaboral getStatusLaboral() {
		return statusLaboral;
	}

	public void setStatusLaboral(StatusLaboral statusLaboral) {
		this.statusLaboral = statusLaboral;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ANALISTA_ID")
	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "INICIO")
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FIM")
	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	@Column(name = "TEMPO")
	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESSO_ID")
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SITUACAO_INICIAL_ID")
	public Situacao getSituacaoInicial() {
		return situacaoInicial;
	}

	public void setSituacaoInicial(Situacao situacao) {
		this.situacaoInicial = situacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SITUACAO_FINAL_ID")
	public Situacao getSituacaoFinal() {
		return situacaoFinal;
	}

	public void setSituacaoFinal(Situacao situacaoFinal) {
		this.situacaoFinal = situacaoFinal;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_EM_ANALISE")
	public Date getDataEmAnalise() {
		return dataEmAnalise;
	}

	public void setDataEmAnalise(Date dataEmAnalise) {
		this.dataEmAnalise = dataEmAnalise;
	}
}
