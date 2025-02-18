package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="USUARIO_REGIONAL")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"USUARIO_ID", "REGIONAL_ID"}))
public class UsuarioRegional extends net.wasys.util.ddd.Entity {

	private Long id;

	private Usuario usuario;
	private BaseRegistro regional;

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
	@JoinColumn(name="REGIONAL_ID", nullable=false)
	public BaseRegistro getRegional() {
		return regional;
	}

	public void setRegional(BaseRegistro regional) {
		this.regional = regional;
	}
}
