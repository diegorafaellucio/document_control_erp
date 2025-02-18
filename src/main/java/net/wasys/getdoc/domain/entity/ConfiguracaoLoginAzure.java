package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.RoleGD;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name="CONFIGURACAO_LOGIN_AZURE")
public class ConfiguracaoLoginAzure extends net.wasys.util.ddd.Entity {

	private Long id;
	private Subperfil subPerfil;
	private String grupo;
	private RoleGD role;

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
	@JoinColumn(name="SUBPERFIL_ID")
	public Subperfil getSubperfil() {
		return subPerfil;
	}

	public void setSubperfil(Subperfil subPerfil) {
		this.subPerfil = subPerfil;
	}

	@Column(name="GRUPO", nullable=false, length=100)
	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ROLE", nullable=false)
	public RoleGD getRole() {
		return role;
	}

	public void setRole(RoleGD role) {
		this.role = role;
	}
}