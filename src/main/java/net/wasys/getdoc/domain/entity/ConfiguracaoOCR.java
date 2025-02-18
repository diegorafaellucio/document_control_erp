package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CONFIGURACAO_OCR")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"NOME"}))
public class ConfiguracaoOCR extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private boolean tipificacaoAtiva;
	private boolean analiseAtiva;
	private boolean aprovacaoAtiva;
	private boolean ocrAtivo;
	private TipoProcesso tipoProcesso;

	public ConfiguracaoOCR() {
	}

	public ConfiguracaoOCR(Long id) {
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

	@Column(name = "NOME", nullable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "TIPIFICACAO_ATIVA", nullable = false)
	public boolean isTipificacaoAtiva() {
		return tipificacaoAtiva;
	}

	public void setTipificacaoAtiva(boolean ativa) {
		this.tipificacaoAtiva = ativa;
	}

	@Column(name = "ANALISE_ATIVA", nullable = false)
	public boolean isAnaliseAtiva() {
		return analiseAtiva;
	}

	public void setAnaliseAtiva(boolean aprovarAutomaticamente) {
		this.analiseAtiva = aprovarAutomaticamente;
	}

	@Column(name = "OCR_ATIVO", nullable = false)
	public boolean isOcrAtivo() {
		return ocrAtivo;
	}

	public void setOcrAtivo(boolean ocrAtivo) {
		this.ocrAtivo = ocrAtivo;
	}

	@Column(name = "APROVACAO_ATIVA", nullable = false)
	public boolean isAprovacaoAtiva() {
		return aprovacaoAtiva;
	}

	public void setAprovacaoAtiva(boolean aprovacaoAtiva) {
		this.aprovacaoAtiva = aprovacaoAtiva;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID")
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Override
	public String toString() {
		return "ConfiguracaoOCR{" +
				"id=" + id +
				", nome='" + nome + '\'' +
				", tipificacaoAtiva=" + tipificacaoAtiva +
				", analiseAtiva=" + analiseAtiva +
				", ocrAtivo=" + ocrAtivo +
				", tipoProcesso=" + tipoProcesso +
				'}';
	}
}
