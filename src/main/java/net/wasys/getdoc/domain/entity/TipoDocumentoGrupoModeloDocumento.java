package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="TIPO_DOCUMENTO_GRUPO_MODELO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_DOCUMENTO_ID", "GRUPO_MODELO_DOCUMENTO_ID"}))
public class TipoDocumentoGrupoModeloDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoDocumento tipoDocumento;
	private GrupoModeloDocumento grupoModeloDocumento;
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

	@JoinColumn(name="GRUPO_MODELO_DOCUMENTO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	@Column(name="REQUISITAR_DATA_VALIDADE_EXPIRACAO", nullable=false)
	public boolean getRequisitarDataValidadeExpiracao() {
		return requisitarDataValidadeExpiracao;
	}

	public void setRequisitarDataValidadeExpiracao(boolean requisitarDataValidadeExpiracao) {
		this.requisitarDataValidadeExpiracao = requisitarDataValidadeExpiracao;
	}

}
