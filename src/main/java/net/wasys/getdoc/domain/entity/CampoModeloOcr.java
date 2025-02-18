package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="CAMPO_MODELO_OCR")
public class CampoModeloOcr extends net.wasys.util.ddd.Entity implements Cloneable{

	public static final String CNH_LABEL_OCR_NOME = "nome";
	public static final String CNH_LABEL_OCR_CPF = "cpf";
	public static final String CNH_LABEL_OCR_DATA_NASCIMENTO = "data_nascimento";
	public static final String CNH_LABEL_OCR_RG = "rg";
	public static final String CNH_LABEL_OCR_RG_EMISSOR = "rg_emissor";
	public static final String CNH_LABEL_OCR_RG_UF = "rg_uf";
	public static final String CNH_LABEL_OCR_NUMERO_REGISTRO = "numero_registro";
	public static final String CNH_LABEL_OCR_CIDADE = "cidade";
	public static final String CNH_LABEL_OCR_UF = "uf";
	public static final String CNH_LABEL_OCR_PAI = "pai";
	public static final String CNH_LABEL_OCR_MAE = "mae";
	public static final String CNH_LABEL_OCR_DATA_EMISSAO = "data_emissao";
	public static final String CNH_LABEL_OCR_DATA_VALIDADE = "data_validade";

	public static final String RG_LABEL_OCR_RG = "rg";
	public static final String RG_LABEL_OCR_DATA_EXPEDICAO = "data_expedicao";
	public static final String RG_LABEL_OCR_NOME = "nome";
	public static final String RG_LABEL_OCR_NOME_MAE = "nome_mae";
	public static final String RG_LABEL_OCR_NOME_PAI = "nome_pai";
	public static final String RG_LABEL_OCR_DATA_NASC = "data_nasc";
	public static final String RG_LABEL_OCR_CPF = "cpf";
	public static final String RG_LABEL_OCR_CIDADE_ORIGEM = "cidade_origem";
	public static final String RG_LABEL_OCR_UF_ORIGEM = "uf_origem";

	public static final String ENEM_LABEL_OCR_CIENCINAS_HUMANAS = "ciencias_humanas";
	public static final String ENEM_LABEL_OCR_CIENCIAS_NATURAIS = "ciencias_naturais";
	public static final String ENEM_LABEL_OCR_LINGUAGEN = "linguagem";
	public static final String ENEM_LABEL_OCR_MATEMATICA = "matematica";
	public static final String ENEM_LABEL_OCR_REDACAO = "redacao";

	public static final String GENERAL_LABEL_OCR_DATA_VALIDADE = "data_validade";

	public static final String GENERAL_REGEX_OCR_DATA_VALIDADE_YEAR_4 = "\\d{2}/\\d{2}/\\d{4}";
	public static final String GENERAL_REGEX_OCR_DATA_VALIDADE_YEAR_2 = "\\d{2}/\\d{2}/\\d{2}";

	private String descricao;
	private String valorAprovacao;
	private String valorOperador;
	private String valorTipo;
	private String campoProcesso;

	private Long id;
	private String labelOcr;
	private String nome;
	private ModeloOcr modeloOcr;

	private boolean ativo;
	private boolean fulltext;

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

	@Column(name="LABEL_OCR", nullable=false)
	public String getLabelOcr() {
		return labelOcr;
	}

	public void setLabelOcr(String labelOcr) {
		this.labelOcr = labelOcr;
	}

	@Column(name="DESCRICAO", nullable=false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MODELO_OCR_ID", nullable=false)
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	@Override
	public CampoModeloOcr clone() {
		try {
			CampoModeloOcr clone = (CampoModeloOcr) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="VALOR_APROVACAO")
	public String getValorAprovacao() {
		return valorAprovacao;
	}

	public void setValorAprovacao(String valorAprovacao) {
		this.valorAprovacao = valorAprovacao;
	}

	@Column(name="VALOR_OPERADOR")
	public String getValorOperador() {
		return valorOperador;
	}

	public void setValorOperador(String valorOperador) {
		this.valorOperador = valorOperador;
	}

	@Column(name="VALOR_TIPO")
	public String getValorTipo() {
		return valorTipo;
	}

	public void setValorTipo(String valorTipo) {
		this.valorTipo = valorTipo;
	}

	@Column(name="CAMPO_PROCESSO")
	public String getCampoProcesso() {
		return campoProcesso;
	}

	@Column(name="ATIVO")
	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="FULL_TEXT")
	public boolean isFulltext() {
		return fulltext;
	}

	public void setFulltext(boolean fulltext) {
		this.fulltext = fulltext;
	}

	public void setCampoProcesso(String campoProcesso) {
		this.campoProcesso = campoProcesso;
	}

	public enum Operador {
		IGUAL("="),
		DIFERENTE("<>"),
		MAIOR(">"),
		MENOR("<"),
		MAIOR_IGUAL(">=");

		private String operador;

		Operador(String operador) {
			this.operador = operador;
		}

		public String getOperador() {
			return operador;
		}
	}

	public enum TipoComparacao {
		INTEGER("Número Inteiro"),
		DOUBLE("Número Decimal"),
		SIMILARIDADE("Similaridade"),
		BASE_INTERNA("Base Interna - Similaridade");

		private String tipoComparacao;

		TipoComparacao(String tipoComparacao) {
			this.tipoComparacao = tipoComparacao;
		}

		public String getTipoComparacao() {
			return tipoComparacao;
		}
	}

	public enum ValorComparacaoSimilaridade {
		NOME,
		CPF
	}

}
