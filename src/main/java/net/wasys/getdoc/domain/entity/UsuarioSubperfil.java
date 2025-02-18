package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name="USUARIO_SUBPERFIL")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"USUARIO_ID", "SUBPERFIL_ID"}))
public class UsuarioSubperfil extends net.wasys.util.ddd.Entity {

	private Long id;
	private Integer nivel;

	private Usuario usuario;
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

	@Column(name="NIVEL")
	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
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
	@JoinColumn(name="SUBPERFIL_ID", nullable=false)
	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}
}
