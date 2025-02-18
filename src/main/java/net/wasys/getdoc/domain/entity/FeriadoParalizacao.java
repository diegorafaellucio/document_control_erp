package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="FERIADO_PARALIZACAO")
public class FeriadoParalizacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Feriado feriado;
	private Date data;

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

	@Temporal(TemporalType.DATE)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

}