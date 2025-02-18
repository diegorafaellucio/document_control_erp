package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.*;

@Entity(name="MODELO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"DESCRICAO"}))
public class ModeloDocumento extends net.wasys.util.ddd.Entity {

	public static final String CONCLUSAO_ENSINO_MEDIO_LABEL_DARKNET = "comprovante_conclusao_ensino_medio";
	public static final String DIPLOMA_ENSINO_SUPERIOR_LABEL_DARKNET = "diploma_graduacao";

	private Long id;
	private String descricao;
	private String palavrasEsperadas;
	private String palavrasExcludentes;
	private boolean ativo;
	private Date dataAlteracao;
	private Integer percentualMininoTipificacao;
	private boolean darknetApiHabilitada;
	private boolean visionApiHabilitada;
	private String labelDarknet;
	private ModeloOcr modeloOcr;

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

	@Column(name="DESCRICAO", length=100)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	@Column(name="PALAVRAS_ESPERADAS", length=2000)
	public String getPalavrasEsperadas() {
		return palavrasEsperadas;
	}

	public void setPalavrasEsperadas(String palavrasEsperadas) {
		this.palavrasEsperadas = palavrasEsperadas;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="PALAVRAS_EXCLUDENTES", length=2000)
	public String getPalavrasExcludentes() {
		return palavrasExcludentes;
	}

	public void setPalavrasExcludentes(String palavrasExcludentes) {
		this.palavrasExcludentes = palavrasExcludentes;
	}

	@Column(name="PERCENTUAL_MINIMO_TIPIFICACAO")
	public Integer getPercentualMininoTipificacao() {
		return percentualMininoTipificacao;
	}

	public void setPercentualMininoTipificacao(Integer percentualMininoTipificacao) {
		this.percentualMininoTipificacao = percentualMininoTipificacao;
	}

	@Column(name="DARKNET_API_HABILITADA")
	public boolean getDarknetApiHabilitada() {
		return darknetApiHabilitada;
	}

	public void setDarknetApiHabilitada(boolean darknetApiHabilitada) {
		this.darknetApiHabilitada = darknetApiHabilitada;
	}

	@Column(name="VISION_API_HABILITADA")
	public boolean getVisionApiHabilitada() {
		return visionApiHabilitada;
	}

	public void setVisionApiHabilitada(boolean visionApiHabilitada) {
		this.visionApiHabilitada = visionApiHabilitada;
	}

	@Column(name="LABEL_DARKNET")
	public String getLabelDarknet() {
		return labelDarknet;
	}

	public void setLabelDarknet(String labelDarknet) {
		this.labelDarknet = labelDarknet;
	}

	@JoinColumn(name="MODELO_OCR_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}
}
