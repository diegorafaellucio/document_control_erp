package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="TIPO_DOCUMENTO_MODELO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_DOCUMENTO_ID", "MODELO_DOCUMENTO_ID"}))
public class TipoDocumentoModelo extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoDocumento tipoDocumento;
	private ModeloDocumento modeloDocumento;
	private boolean requisitarDataValidadeExpiracao;

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

	@JoinColumn(name="MODELO_DOCUMENTO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@Column(name="REQUISITAR_DATA_VALIDADE_EXPIRACAO", nullable=false)
	public boolean getRequisitarDataValidadeExpiracao() {
		return requisitarDataValidadeExpiracao;
	}

	public void setRequisitarDataValidadeExpiracao(boolean requisitarDataValidadeExpiracao) {
		this.requisitarDataValidadeExpiracao = requisitarDataValidadeExpiracao;
	}
}
