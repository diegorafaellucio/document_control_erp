package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CATEGORIA_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"CHAVE"}))
public class CategoriaDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private String chave;
	private String descricao;
	private boolean ativo;

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

	@Column(name="CHAVE", length=50)
	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	@Column(name="DESCRICAO", length=100)
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
}
