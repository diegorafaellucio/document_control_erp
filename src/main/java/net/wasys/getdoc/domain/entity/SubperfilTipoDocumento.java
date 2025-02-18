package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name="SUBPERFIL_TIPO_DOCUMENTO")
public class SubperfilTipoDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private Subperfil subperfil;
	private TipoDocumento tipoDocumento;

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
	@JoinColumn(name="SUBPERFIL_ID", nullable=false)	
	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_DOCUMENTO_ID", nullable=false)
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

}