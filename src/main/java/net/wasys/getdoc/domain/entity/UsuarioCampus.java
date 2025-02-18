package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="USUARIO_CAMPUS")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"USUARIO_ID", "CAMPUS_ID"}))
public class UsuarioCampus extends net.wasys.util.ddd.Entity {

	private Long id;

	private Usuario usuario;
	private BaseRegistro campus;

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
	@JoinColumn(name="USUARIO_ID", nullable=false)
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAMPUS_ID", nullable=false)
	public BaseRegistro getCampus() {
		return campus;
	}

	public void setCampus(BaseRegistro campus) {
		this.campus = campus;
	}
}
