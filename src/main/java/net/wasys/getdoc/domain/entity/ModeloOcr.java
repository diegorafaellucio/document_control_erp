package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.wasys.getdoc.GetdocConstants;

@Entity(name="MODELO_OCR")
public class ModeloOcr extends net.wasys.util.ddd.Entity {

	private Long id;
	private String descricao;
	private boolean ativo;
	private String pathModelo;
	private long sizeModelo;
	private Integer largura;
	private Integer altura;
	private String nomeArquivo;
	private String hashChecksum;
	private Date dataAlteracao;

	private String endpointModeloOcr;
	private boolean extrairFullText;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="PATH_MODELO", nullable=false)
	public String getPathModelo() {
		return pathModelo;
	}

	public void setPathModelo(String pathModelo) {
		this.pathModelo = pathModelo;
	}

	@Column(name="SIZE_MODELO", nullable=false)
	public long getSizeModelo() {
		return sizeModelo;
	}

	public void setSizeModelo(long sizeModelo) {
		this.sizeModelo = sizeModelo;
	}

	@Column(name="LARGURA")
	public Integer getLargura() {
		return largura;
	}

	public void setLargura(Integer largura) {
		this.largura = largura;
	}

	@Column(name="ALTURA")
	public Integer getAltura() {
		return altura;
	}

	public void setAltura(Integer altura) {
		this.altura = altura;
	}

	@Column(name="NOME_ARQUIVO", length=100)
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	@Column(name="HASH_CHECKSUM", length=50)
	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	public String criaCaminho(String modelosPath) {
		String separador = System.getProperty("file.separator");
		return criaCaminho(modelosPath, separador);
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Transient
	public String criaCaminho(String dir, String separador) {

		StringBuilder caminho = new StringBuilder(dir);

		Long id = getId();

		caminho.append(id).append("_").append(id).append(".").append(GetdocConstants.EXTENSAO_DEFINICAO_DOCUMENTO);

		return caminho.toString();
	}

	@Column(name="ENDPOINT_MODELO_OCR", length=300)
	public String getEndpointModeloOcr() {
		return endpointModeloOcr;
	}

	public void setEndpointModeloOcr(String endpointModeloOcr) {
		this.endpointModeloOcr = endpointModeloOcr;
	}

	@Column(name="EXTRAIR_FULL_TEXT", nullable = false)
	public boolean isExtrairFullText() {
		return extrairFullText;
	}

	public void setExtrairFullText(boolean extrairFullText) {
		this.extrairFullText = extrairFullText;
	}
}