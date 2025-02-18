package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="TEXTO_PADRAO_TIPO_PROCESSO")
public class TextoPadraoTipoProcesso extends net.wasys.util.ddd.Entity {

	private Long id;

	private TextoPadrao textoPadrao;
	private TipoProcesso tipoProcesso;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TEXTO_PADRAO_ID", nullable=false)
	public TextoPadrao getTextoPadrao() {
		return textoPadrao;
	}

	public void setTextoPadrao(TextoPadrao textoPadrao) {
		this.textoPadrao = textoPadrao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
}
