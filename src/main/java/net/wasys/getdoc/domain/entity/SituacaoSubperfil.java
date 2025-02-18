package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="SITUACAO_SUBPERFIL")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"SITUACAO_ID", "SUBPERFIL_ID"}))
public class SituacaoSubperfil extends net.wasys.util.ddd.Entity {

	private Long id;
	private Situacao situacao;
	private Subperfil subperfil;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID", nullable=false)
	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBPERFIL_ID", nullable=false)
	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}
}
