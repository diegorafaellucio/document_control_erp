package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="DEPARA_RETORNO")
public class DeparaRetorno extends net.wasys.util.ddd.Entity {

	private Long id;
	private String origem;
	private Boolean sobrescreverValor;
	private TipoCampo tipoCampo;

	private SubRegra subRegra;

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

	@Column(name="ORIGEM")
	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
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
	@JoinColumn(name="TIPO_CAMPO_ID", nullable=false)
	public TipoCampo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(TipoCampo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}

	@Column(name="SOBRESCREVER_VALOR")
	public Boolean getSobrescreverValor() { return sobrescreverValor; }

	public void setSobrescreverValor(Boolean sobrescreverValor) { this.sobrescreverValor = sobrescreverValor; }
}
