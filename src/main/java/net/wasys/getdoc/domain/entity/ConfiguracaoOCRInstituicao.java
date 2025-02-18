package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CONFIGURACAO_OCR_INSTITUICAO")
public class ConfiguracaoOCRInstituicao extends net.wasys.util.ddd.Entity {

	private Long id;
	private ConfiguracaoOCR configuracaoOCR;
	private Long codigoInstituicao;
	private String nomeInstituicao;

	public ConfiguracaoOCRInstituicao() {
	}

	public ConfiguracaoOCRInstituicao(Long id) {
		this.id = id;
	}

	@Id
	@Override
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CONFIGURACAO_OCR_ID")
	public ConfiguracaoOCR getConfiguracaoOCR() {
		return configuracaoOCR;
	}

	public void setConfiguracaoOCR(ConfiguracaoOCR configuracaoOCR) {
		this.configuracaoOCR = configuracaoOCR;
	}

	@Column(name="CODIGO_INSTITUICAO", nullable=false)
	public Long getCodigoInstituicao() {
		return codigoInstituicao;
	}

	public void setCodigoInstituicao(Long codigoInstituicao) {
		this.codigoInstituicao = codigoInstituicao;
	}

	@Column(name="NOME_INSTITUICAO", nullable=false)
	public String getNomeInstituicao() {
		return nomeInstituicao;
	}

	public void setNomeInstituicao(String nomeInstituicao) {
		this.nomeInstituicao = nomeInstituicao;
	}
}
