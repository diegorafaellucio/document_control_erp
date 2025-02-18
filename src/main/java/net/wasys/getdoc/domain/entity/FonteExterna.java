package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

@Entity(name="FONTE_EXTERNA")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"campos_resultado"}))
public class FonteExterna extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoConsultaExterna nome;
	private String camposResultado;

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

	@Enumerated(EnumType.STRING)
	@Column(name="NOME", length=30)
	public TipoConsultaExterna getNome() {
		return nome;
	}

	public void setNome(TipoConsultaExterna nome) {
		this.nome = nome;
	}

	@Column(name="CAMPOS_RESULTADO")
	public String getCamposResultado() {
		return camposResultado;
	}

	public void setCamposResultado(String camposResultado) {
		this.camposResultado = camposResultado;
	}
}
