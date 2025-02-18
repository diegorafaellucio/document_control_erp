package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

@Entity(name="CAMPO_OCR")
public class CampoOcr extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String valor;
	private int positionTop;
	private int positionLeft;
	private int positionRight;
	private int positionBottom;
	private String recognizedValue;
	private String suspiciousSymbols;
	private String blockRef;
	private String errorRef;
	private String fieldId;
	private String erros;
	private String valorEditado;
	private Integer nivelConfianca;
	private Boolean valorIgualProcesso;
	private Integer similaridade;

	private Imagem imagem;
	private Usuario usuarioChecagem;
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
	@JoinColumn(name="IMAGEM_ID", nullable=false)
	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_CHECAGEM_ID")
	public Usuario getUsuarioChecagem() {
		return usuarioChecagem;
	}

	public void setUsuarioChecagem(Usuario usuarioChecagem) {
		this.usuarioChecagem = usuarioChecagem;
	}

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="VALOR")
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Column(name="POSITION_TOP")
	public int getPositionTop() {
		return positionTop;
	}

	public void setPositionTop(int positionTop) {
		this.positionTop = positionTop;
	}

	@Column(name="POSITION_LEFT")
	public int getPositionLeft() {
		return positionLeft;
	}

	public void setPositionLeft(int positionLeft) {
		this.positionLeft = positionLeft;
	}

	@Column(name="POSITION_RIGHT")
	public int getPositionRight() {
		return positionRight;
	}

	public void setPositionRight(int positionRight) {
		this.positionRight = positionRight;
	}

	@Column(name="POSITION_BOTTOM")
	public int getPositionBottom() {
		return positionBottom;
	}

	public void setPositionBottom(int positionBottom) {
		this.positionBottom = positionBottom;
	}

	@Column(name="RECOGNIZED_VALUE")
	public String getRecognizedValue() {
		return recognizedValue;
	}

	public void setRecognizedValue(String recognizedValue) {
		this.recognizedValue = recognizedValue;
	}

	@Column(name="SUSPICIOUS_SYMBOLS")
	public String getSuspiciousSymbols() {
		return suspiciousSymbols;
	}

	public void setSuspiciousSymbols(String suspiciousSymbols) {
		this.suspiciousSymbols = suspiciousSymbols;
	}

	@Column(name="BLOCK_REF")
	public String getBlockRef() {
		return blockRef;
	}

	public void setBlockRef(String blockRef) {
		this.blockRef = blockRef;
	}

	@Column(name="ERROR_REF")
	public String getErrorRef() {
		return errorRef;
	}

	public void setErrorRef(String errorRef) {
		this.errorRef = errorRef;
	}

	@Column(name="FIELD_ID")
	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	@Column(name="ERROS")
	public String getErros() {
		return erros;
	}

	public void setErros(String erros) {
		this.erros = erros;
	}

	@Column(name="VALOR_EDITADO")
	public String getValorEditado() {
		return valorEditado;
	}

	public void setValorEditado(String valorEditado) {
		this.valorEditado = valorEditado;
	}

	@Column(name="NIVEL_CONFIANCA")
	public Integer getNivelConfianca() {
		return nivelConfianca;
	}

	public void setNivelConfianca(Integer nivelConfianca) {
		this.nivelConfianca = nivelConfianca;
	}

	@Column(name="VALOR_IGUAL_PROCESSO")
	public Boolean getValorIgualProcesso() {
		return valorIgualProcesso;
	}

	public void setValorIgualProcesso(Boolean valorIgualProcesso) {
		this.valorIgualProcesso = valorIgualProcesso;
	}

	@Column(name="SIMILARIDADE")
	public Integer getSimilaridade() {
		return similaridade;
	}

	public void setSimilaridade(Integer similaridade) {
		this.similaridade = similaridade;
	}

	@Transient
	public String getValorOriginal() {

		String recognizedValue = getRecognizedValue();
		if(StringUtils.isNotBlank(recognizedValue)) {
			return recognizedValue;
		}

		return getValor();
	}

	@Transient
	public String getValorFinal() {

		String valorEditado = getValorEditado();
		if(StringUtils.isNotBlank(valorEditado)) {
			return valorEditado;
		}

		return getValorOriginal();
	}

	@Override
	public String toString() {
		Long id = getId();
		String nome = getNome();
		String valorFinal = getValorFinal();
		Boolean valorIgualProcesso = getValorIgualProcesso();
		Integer similaridade = getSimilaridade();
		Integer nivelConfianca = getNivelConfianca();
		return "#" + id + nome + "=" + valorFinal + "-vi " + valorIgualProcesso + "-s " + similaridade + "-nc " + nivelConfianca;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAMPO_MODELO_OCR_ID", nullable=false)
	public CampoModeloOcr getCampoModeloOcr() {
		return campoModeloOcr;
	}

	public void setCampoModeloOcr(CampoModeloOcr campoModeloOcr) {
		this.campoModeloOcr = campoModeloOcr;
	}

	public static class ResultadoComparacaoOCR {
		private boolean aprovado;
		private String nomeCampo;
		private Object percentualValidacao;
		private Object valorOCR;
		private Object valorProcesso;

		public boolean isAprovado() {
			return aprovado;
		}

		public void setAprovado(boolean aprovado) {
			this.aprovado = aprovado;
		}

		public String getNomeCampo() {
			return nomeCampo;
		}

		public void setNomeCampo(String nomeCampo) {
			this.nomeCampo = nomeCampo;
		}

		public Object getPercentualValidacao() {
			return percentualValidacao;
		}

		public void setPercentualValidacao(Object percentualValidacao) {
			this.percentualValidacao = percentualValidacao;
		}

		public Object getValorOCR() {
			return valorOCR;
		}

		public void setValorOCR(Object valorOCR) {
			this.valorOCR = valorOCR;
		}

		public Object getValorProcesso() {
			return valorProcesso;
		}

		public void setValorProcesso(Object valorProcesso) {
			this.valorProcesso = valorProcesso;
		}

		public ResultadoComparacaoOCR() {
		}


	}
}
