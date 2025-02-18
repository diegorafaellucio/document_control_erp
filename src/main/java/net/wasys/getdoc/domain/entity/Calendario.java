package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoParceiro;
import net.wasys.getdoc.domain.enumeration.TipoProuni;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name="CALENDARIO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_PROCESSO_ID", "PERIODO_INGRESSO", "TIPO_PARCEIRO", "TIPO_PROUNI",}))
public class Calendario extends net.wasys.util.ddd.Entity {

	private Long id;
	private String descricao;
	private String periodoIngresso;
	private TipoParceiro tipoParceiro;
	private TipoProuni tipoProuni;
	private TipoProcesso tipoProcesso;
	private Date dataInicio;
	private Date dataFim;
	private List<CalendarioCriterio> criterios = new ArrayList<>();
	private boolean ativo = true;

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

	@Column(name="DESCRICAO", length=100, nullable=false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="PERIODO_INGRESSO", length=10, nullable=false)
	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public void setPeriodoIngresso(String periodoIngresso) {
		this.periodoIngresso = periodoIngresso;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PARCEIRO", length=50, nullable=false)
	public TipoParceiro getTipoParceiro() {
		return tipoParceiro;
	}

	public void setTipoParceiro(TipoParceiro tipoParceiro) {
		this.tipoParceiro = tipoParceiro;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PROUNI", length=50, nullable=false)
	public TipoProuni getTipoProuni() {
		return tipoProuni;
	}

	public void setTipoProuni(TipoProuni tipoProuni) {
		this.tipoProuni = tipoProuni;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
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

	@OrderBy("dataInicio")
	@OneToMany(mappedBy="calendario", fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
	public List<CalendarioCriterio> getCriterios() {
		return criterios;
	}

	public void setCriterios(List<CalendarioCriterio> criterios) {
		this.criterios = criterios;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

}
