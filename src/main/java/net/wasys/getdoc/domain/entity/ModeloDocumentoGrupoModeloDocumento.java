package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="MODELO_DOCUMENTO_GRUPO_MODELO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"GRUPO_MODELO_DOCUMENTO_ID", "MODELO_DOCUMENTO_ID"}))
public class ModeloDocumentoGrupoModeloDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private GrupoModeloDocumento grupoModeloDocumento;
	private ModeloDocumento modeloDocumento;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="GRUPO_MODELO_DOCUMENTO_ID", nullable=true)
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	@JoinColumn(name="MODELO_DOCUMENTO_ID")
	@ManyToOne(fetch= FetchType.LAZY)
	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}


	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}
}
