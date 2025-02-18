package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CATEGORIA_GRUPO_MODELO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"DESCRICAO"}))
public class CategoriaGrupoModeloDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private String descricao;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String nome) {
		this.descricao = nome;
	}

}
