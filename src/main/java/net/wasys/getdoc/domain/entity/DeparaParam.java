package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name="DEPARA_PARAM")
public class DeparaParam extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nomeFonte;
	private String origem;
	private String destino;

	private SubRegra subRegra;
	private FonteExterna fonteExterna;
	private BaseInterna baseInterna;
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

	@Column(name="NOME_FONTE", length=30)
	public String getNomeFonte() {
		return nomeFonte;
	}

	public void setNomeFonte(String nomeFonte) {
		this.nomeFonte = nomeFonte;
	}

	@Column(name="ORIGEM")
	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	@Column(name="DESTINO", length=100)
	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUB_REGRA_ID", nullable=false)
	public SubRegra getSubRegra() {
		return subRegra;
	}

	public void setSubRegra(SubRegra regra) {
		this.subRegra = regra;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FONTE_EXTERNA_ID")
	public FonteExterna getFonteExterna() {
		return fonteExterna;
	}

	public void setFonteExterna(FonteExterna fonteExterna) {
		this.fonteExterna = fonteExterna;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID")
	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID")
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{origem:" + origem + ",destino:" + destino + "}";
	}
}
