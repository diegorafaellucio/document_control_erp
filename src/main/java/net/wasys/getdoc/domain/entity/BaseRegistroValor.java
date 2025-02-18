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

@Entity(name="BASE_REGISTRO_VALOR")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"BASE_REGISTRO_ID", "NOME"}))
public class BaseRegistroValor extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String valor;

	private BaseRegistro baseRegistro;

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
	@JoinColumn(name="BASE_REGISTRO_ID", nullable=false)
	public BaseRegistro getBaseRegistro() {
		return baseRegistro;
	}

	public void setBaseRegistro(BaseRegistro baseRegistro) {
		this.baseRegistro = baseRegistro;
	}

	@Column(name="NOME", length=100, nullable=false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="VALOR")
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{nome=" + getNome() + ",valor=" + getValor() + "}";
	}
}