package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CAMPO_OCR_TIPO_CAMPO")
public class CampoOcrTipoCampo extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoCampo tipoCampo;
	private ModeloOcr modeloOcr;
	private CampoModeloOcr campoModeloOcr;

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
	@JoinColumn(name="TIPO_CAMPO_ID", nullable=false)
	public TipoCampo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(TipoCampo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MODELO_OCR_ID", nullable=false)
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAMPO_MODELO_OCR_ID", nullable=false)
	public CampoModeloOcr getCampoModeloOcr() {
		return campoModeloOcr;
	}


	public void setCampoModeloOcr(CampoModeloOcr campoModeloOcr) {
		this.campoModeloOcr = campoModeloOcr;
	}
}
