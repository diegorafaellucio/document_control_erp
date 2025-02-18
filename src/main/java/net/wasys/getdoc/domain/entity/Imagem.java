package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.Origem;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="CAMINHO"))
public class Imagem extends net.wasys.util.ddd.Entity {

	private Long id;
	private String caminho;
	private String hashChecksum;
	private Integer versao = 1;
	private String extensao;
	private Boolean preparada;
	private boolean aguardandoFulltext;
	private Origem origem;
	private BigDecimal similaridadeFacial;
	private String caminhoFacial;

	private Documento documento;
	private Imagem baseFacial;
	private Boolean existente;
	private ModeloDocumento modeloDocumento;

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

	@Column(name="CAMINHO")
	public String getCaminho() {
		return this.caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
	}

	@Column(name="CAMINHO_FACIAL")
	public String getCaminhoFacial() {
		return caminhoFacial;
	}

	public void setCaminhoFacial(String caminhoFacial) {
		this.caminhoFacial = caminhoFacial;
	}

	@Column(name="VERSAO")
	public Integer getVersao() {
		return versao;
	}

	public void setVersao(Integer versao) {
		this.versao = versao;
	}

	@Column(name="extensao", length=10)
	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}


	@Column(name="HASH_CHECKSUM", nullable=false)
	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	@Column(name="PREPARADA")
	public Boolean getPreparada() {
		return preparada;
	}

	public void setPreparada(Boolean preparada) {
		this.preparada = preparada;
	}

	@Column(name="SIMILARIDADE_FACIAL")
	public BigDecimal getSimilaridadeFacial() {
		return similaridadeFacial;
	}

	public void setSimilaridadeFacial(BigDecimal similaridadeFacial) {
		this.similaridadeFacial = similaridadeFacial;
	}

	@Column(name="AGUARDANDO_FULLTEXT")
	public boolean getAguardandoFulltext() {
		return aguardandoFulltext;
	}

	public void setAguardandoFulltext(boolean aguardandoFulltext) {
		this.aguardandoFulltext = aguardandoFulltext;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_FACIAL_ID")
	public Imagem getBaseFacial() {
		return baseFacial;
	}

	public void setBaseFacial(Imagem baseFacial) {
		this.baseFacial = baseFacial;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DOCUMENTO_ID", nullable=false)
	public Documento getDocumento() {
		return this.documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ORIGEM", length=30)
	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@Column(name="EXISTENTE")
	public Boolean isExistente() {
		return existente;
	}

	public void setExistente(Boolean existente) {
		this.existente = existente;
	}

	@Transient
	public static String gerarCaminho(String dir, Imagem imagem) {
		return gerarCaminho(dir, imagem, File.separator);
	}

	@Transient
	public static String gerarCaminho(String dir, Imagem imagem, String separador) {

		Long imagemId = imagem.getId();
		Documento documento = imagem.getDocumento();
		String extensao = imagem.getExtensao();
		Processo processo = documento.getProcesso();
		Date dataCriacao = processo.getDataCriacao();

		Calendar c = Calendar.getInstance();
		c.setTime(dataCriacao);
		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;
		String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
		int dia = c.get(Calendar.DAY_OF_MONTH);
		String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

		Long processoId = processo.getId();
		String processoIdStr = processoId.toString();
		int length = processoIdStr.length();
		String agrupador = processoIdStr.substring(length - 3, length);

		StringBuilder caminho = new StringBuilder(dir);
		caminho.append(ano).append(separador);
		caminho.append(mesStr).append(separador);
		caminho.append(diaStr).append(separador);
		caminho.append(agrupador).append(separador);

		caminho.append(processoId).append("_").append(imagemId).append(".").append(extensao);

		return caminho.toString();
	}

	@Transient
	public String getSimilaridadeFacialStr() {

		BigDecimal similaridadeFacial = getSimilaridadeFacial();
		if(similaridadeFacial == null) {
			return "";
		}

		similaridadeFacial = similaridadeFacial.multiply(new BigDecimal(100));
		return similaridadeFacial.intValue() + "%";
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MODELO_DOCUMENTO_ID")
	public ModeloDocumento getModeloDocumento() {
		return this.modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

}
