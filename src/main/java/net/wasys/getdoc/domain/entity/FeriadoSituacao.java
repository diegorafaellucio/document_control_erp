package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="FERIADO_SITUACAO")
public class FeriadoSituacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Feriado feriado;
	private Situacao situacao;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FERIADO_ID", nullable=false)
	public Feriado getFeriado() {
		return feriado;
	}

	public void setFeriado(Feriado feriado) {
		this.feriado = feriado;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID", nullable=false)
	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

}