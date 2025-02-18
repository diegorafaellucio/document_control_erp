package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.FaseEtapa;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity(name="ETAPA")
@Table(uniqueConstraints=@UniqueConstraint(columnNames="NOME"))
public class Etapa extends net.wasys.util.ddd.Entity {

	public static final List<Long> CONFERENCIA_BKO = Arrays.asList(63L, 66L, 151L, 145L, 68L, 4L, 60l, 61l, 62l, 59l, 69l, 70l, 72l, 73l, 74l, 75l, 76l, 78l);


	private Long id;
	private String nome;
	private TipoProcesso tipoProcesso;
	private BigDecimal horasPrazo;
	private TipoPrazo tipoPrazo;
	private boolean ativo = true;
	private String dica;
	private BigDecimal horasPrazoAdvertir;
	private TipoPrazo tipoPrazoAdvertir;
	private FaseEtapa fase;
	private boolean etapaFinal = false;
	private boolean considerarFinalDoDia = false;
	private boolean recalcularFinalDeSemana = false;

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

	@Column(name="NOME", length=100, nullable=false)
	public String getNome() {
		return this.nome;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID")
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="HORAS_PRAZO")
	public BigDecimal getHorasPrazo() {
		return horasPrazo;
	}

	public void setHorasPrazo(BigDecimal horasPrazo) {
		this.horasPrazo = horasPrazo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PRAZO")
	public TipoPrazo getTipoPrazo() {
		return tipoPrazo;
	}

	public void setTipoPrazo(TipoPrazo tipoPrazo) {
		this.tipoPrazo = tipoPrazo;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="DICA")
	public String getDica() {
		return dica;
	}

	public void setDica(String dica) {
		this.dica = dica;
	}

	@Column(name="HORAS_PRAZO_ADVERTIR")
	public BigDecimal getHorasPrazoAdvertir() {
		return horasPrazoAdvertir;
	}

	public void setHorasPrazoAdvertir(BigDecimal horasPrazoAdvertir) {
		this.horasPrazoAdvertir = horasPrazoAdvertir;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PRAZO_ADVERTIR")
	public TipoPrazo getTipoPrazoAdvertir() {
		return tipoPrazoAdvertir;
	}

	public void setTipoPrazoAdvertir(TipoPrazo tipoPrazoAdvertir) {
		this.tipoPrazoAdvertir = tipoPrazoAdvertir;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="FASE")
	public FaseEtapa getFase() {
		return fase;
	}

	public void setFase(FaseEtapa fase) {
		this.fase = fase;
	}

	@Column(name = "ETAPA_FINAL")
	public boolean getEtapaFinal() {
		return etapaFinal;
	}

	public void setEtapaFinal(boolean etapaFinal) {
		this.etapaFinal = etapaFinal;
	}

	@Column(name = "CONSIDERAR_FINAL_DIA")
	public boolean getConsiderarFinalDoDia() {
		return considerarFinalDoDia;
	}

	public void setConsiderarFinalDoDia(boolean considerarFinalDoDia) {
		this.considerarFinalDoDia = considerarFinalDoDia;
	}

	@Column(name = "RECALCULAR_FINAL_DE_SEMANA")
	public boolean getRecalcularFinalDeSemana() {
		return recalcularFinalDeSemana;
	}

	public void setRecalcularFinalDeSemana(boolean recalcularFinalDeSemana) {
		this.recalcularFinalDeSemana = recalcularFinalDeSemana;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + getNome() + "}";
	}
}
