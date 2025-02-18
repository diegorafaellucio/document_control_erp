package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.ListaChamada;
import net.wasys.getdoc.domain.enumeration.TipoCalendario;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name="CALENDARIO_CRITERIO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"CALENDARIO_ID", "TIPO_CALENDARIO", "CHAMADA", "DATA_INICIO", "DATA_FIM"}))
public class CalendarioCriterio extends net.wasys.util.ddd.Entity {

	private Long id;
	private Calendario calendario;
	private TipoCalendario tipoCalendario;
	private ListaChamada chamada;
	private Date dataInicio;
	private Date dataFim;
	private boolean executado = false;
	private boolean ativo = true;
	private List<CalendarioCriterioSituacao> calendarioCriterioSituacoes = new ArrayList<>();

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CALENDARIO_ID", nullable=false)
	public Calendario getCalendario() {
		return this.calendario;
	}

	public void setCalendario(Calendario calendario) {
		this.calendario = calendario;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_CALENDARIO", length=50, nullable=false)
	public TipoCalendario getTipoCalendario() {
		return tipoCalendario;
	}

	public void setTipoCalendario(TipoCalendario tipoCalendario) {
		this.tipoCalendario = tipoCalendario;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="CHAMADA", length=50, nullable=false)
	public ListaChamada getChamada() {
		return chamada;
	}

	public void setChamada(ListaChamada chamada) {
		this.chamada = chamada;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_INICIO", nullable=false)
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FIM", nullable=false)
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Column(name="EXECUTADO", nullable=false)
	public boolean getExecutado() {
		return executado;
	}

	public void setExecutado(boolean executado) {
		this.executado = executado;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(mappedBy="calendarioCriterio", fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
	public List<CalendarioCriterioSituacao> getCalendarioCriterioSituacoes() {
		return calendarioCriterioSituacoes;
	}

	public void setCalendarioCriterioSituacoes(List<CalendarioCriterioSituacao> calendarioCriterioSituacoes) {
		this.calendarioCriterioSituacoes = calendarioCriterioSituacoes;
	}
}
