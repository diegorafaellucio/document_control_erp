package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="TIPO_DOCUMENTO_CATEGORIA")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_DOCUMENTO_ID", "CATEGORIA_DOCUMENTO_ID"}))
public class TipoDocumentoCategoria extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoDocumento tipoDocumento;
	private CategoriaDocumento categoriaDocumento;

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
	@JoinColumn(name="TIPO_DOCUMENTO_ID", nullable=true)
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	@JoinColumn(name="Categoria_DOCUMENTO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public CategoriaDocumento getCategoriaDocumento() {
		return categoriaDocumento;
	}

	public void setCategoriaDocumento(CategoriaDocumento categoriaDocumento) {
		this.categoriaDocumento = categoriaDocumento;
	}
}
