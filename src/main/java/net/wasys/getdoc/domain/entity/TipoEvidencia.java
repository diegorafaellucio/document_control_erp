package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.RoleGD;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name="TIPO_EVIDENCIA")
public class TipoEvidencia extends net.wasys.util.ddd.Entity {

	public static final Long EVIDENCIA_PROUNI = 119L;

	private Long id;
	private String descricao;
	private boolean ativo;

    private List<TipoEvidenciaRole> tiposEvidenciasRoles = new ArrayList<>();

    @Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(fetch=FetchType.EAGER, mappedBy="tipoEvidencia")
	public List<TipoEvidenciaRole> getTiposEvidenciasRoles() {
		return tiposEvidenciasRoles;
	}

	public void setTiposEvidenciasRoles(List<TipoEvidenciaRole> tiposEvidenciasRoles) {
		this.tiposEvidenciasRoles = tiposEvidenciasRoles;
	}
}