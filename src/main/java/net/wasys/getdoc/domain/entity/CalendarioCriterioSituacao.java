package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CALENDARIO_CRITERIO_SITUACAO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"CALENDARIO_CRITERIO_ID"}))
public class CalendarioCriterioSituacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private CalendarioCriterio calendarioCriterio;
	private Situacao situacao;

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
	@JoinColumn(name="CALENDARIO_CRITERIO_ID", nullable=false)
	public CalendarioCriterio getCalendarioCriterio() {
		return calendarioCriterio;
	}

	public void setCalendarioCriterio(CalendarioCriterio calendarioCriterio) {
		this.calendarioCriterio = calendarioCriterio;
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
