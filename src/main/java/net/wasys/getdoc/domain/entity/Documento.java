package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Entity(name="DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"PROCESSO_ID", "NOME"}))
public class Documento extends net.wasys.util.ddd.Entity {

	public static final String NOME_ASSINATURA = "ASSINATURA";
	public static final String NOME_OUTROS = "OUTROS";
	public static final String NOME_FOTOS = "FOTOS";
	public static final String NOME_TIFICANDO = "TIPIFICANDO...";

	public static final String ID = "ID";
	public static final String OBRIGATORIO = "OBRIGATÓRIO";
	public static final String STATUS = "STATUS";
	public static final String DATA_DIGITALIZACAO = "DATA DIGITALIZAÇÃO";
	public static final String VERSAO_ATUAL = "VERSÃO ATUAL";
	public static final String STATUS_ORC = "STATUS ORC";
	public static final String PORCENTAGEM_RECONHECIMENTO_FACIAL = "PORCENTAGEM RECONHECIMENTO FACIAL";
	public static final String STATUS_RECONHECIMENTO_FACIAL = "STATUS RECONHECIMENTO FACIAL";
	public static final String IRREGULARIDADE_ID = "IRREGULARIDADE ID";

	public static final String POSFIX_MEMBRO_FAMILIAR = "MEMBRO FAMILIAR";

	private Long id;
	private String nome;
	private boolean obrigatorio;
	private Integer ordem;
	private short taxaCompressao = 100;
	private StatusDocumento status;
	private Date dataDigitalizacao;
	private Integer versaoAtual = 0;
	private StatusOcr statusOcr;
	private boolean reconhecimentoFacial;
	private StatusFacial statusFacial;
	private boolean aguardandoReconhecimentoFacial;
	private BigDecimal similaridadeFacial;
	private String metaDados;
	private Origem origem;
	private boolean notificadoSia;
	private boolean exibirNoPortal;
	private String hashJson;
	private Date validadeExpiracao;
	private Date dataEmissao;

	private ModeloOcr modeloOcr;
	private Processo processo;
	private TipoDocumento tipoDocumento;
	private ModeloDocumento modeloDocumento;
	private CampoGrupo grupoRelacionado;

	private Advertencia advertencia;

	private GrupoModeloDocumento grupoModeloDocumento;

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

	@Column(name="NOME", length=100, nullable=false)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = StringUtils.upperCase(nome);
	}

	@Column(name="OBRIGATORIO", nullable=false)
	public boolean getObrigatorio() {
		return this.obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	@Column(name="RECONHECIMENTO_FACIAL", nullable=false)
	public boolean getReconhecimentoFacial() {
		return reconhecimentoFacial;
	}

	public void setReconhecimentoFacial(boolean reconhecimentoFacial) {
		this.reconhecimentoFacial = reconhecimentoFacial;
	}

	@Column(name="ORDEM", nullable=false)
	public Integer getOrdem() {
		return this.ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@JoinColumn(name="MODELO_OCR_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return this.processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@Column(name="TAXA_COMPRESSAO", nullable=false)
	public short getTaxaCompressao() {
		return taxaCompressao;
	}

	public void setTaxaCompressao(short taxaCompressao) {
		this.taxaCompressao = taxaCompressao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_DIGITALIZACAO")
	public Date getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public void setDataDigitalizacao(Date dataDigitalizacao) {
		this.dataDigitalizacao = dataDigitalizacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable=false)
	public StatusDocumento getStatus() {
		return this.status;
	}

	public void setStatus(StatusDocumento status) {
		this.status = status;
	}

	@Column(name="VERSAO_ATUAL")
	public Integer getVersaoAtual() {
		return versaoAtual;
	}

	public void setVersaoAtual(Integer versaoAtual) {
		this.versaoAtual = versaoAtual;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_OCR", length=20)
	public StatusOcr getStatusOcr() {
		return statusOcr;
	}

	public void setStatusOcr(StatusOcr statusOcr) {
		this.statusOcr = statusOcr;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_FACIAL", length=20)
	public StatusFacial getStatusFacial() {
		return statusFacial;
	}

	public void setStatusFacial(StatusFacial statusFacial) {
		this.statusFacial = statusFacial;
	}

	@Column(name="AGUARDANDO_RECONHECIMENTO_FACIAL")
	public boolean getAguardandoReconhecimentoFacial() {
		return aguardandoReconhecimentoFacial;
	}

	public void setAguardandoReconhecimentoFacial(boolean aguardandoReconhecimentoFacial) {
		this.aguardandoReconhecimentoFacial = aguardandoReconhecimentoFacial;
	}

	@Column(name="SIMILARIDADE_FACIAL")
	public BigDecimal getSimilaridadeFacial() {
		return similaridadeFacial;
	}

	public void setSimilaridadeFacial(BigDecimal similaridadeFacial) {
		this.similaridadeFacial = similaridadeFacial;
	}

	@Column(name="METADADOS")
	public String getMetaDados() { return metaDados; }

	public void setMetaDados(String metaDados) { this.metaDados = metaDados; }

	@Column(name="NOTIFICADO_SIA")
	public boolean getNotificadoSia() {
		return notificadoSia;
	}

	public void setNotificadoSia(boolean notificarSia) {
		this.notificadoSia = notificarSia;
	}

	@Column(name="EXIBIR_NO_PORTAL", nullable=false)
	public boolean getExibirNoPortal() {
		return exibirNoPortal;
	}

	public void setExibirNoPortal(boolean exibirNoPortal) {
		this.exibirNoPortal = exibirNoPortal;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_DOCUMENTO_ID", nullable=true)
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ORIGEM", length=30)
	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@Transient
	public boolean isPendente() {
		return StatusDocumento.PENDENTE.equals(getStatus());
	}

	@Transient
	public boolean isOutros() {
		return tipoDocumento == null && NOME_OUTROS.equals(nome.toUpperCase());
	}

	@JoinColumn(name="MODELO_DOCUMENTO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@Override
	public String toString() {
		return super.toString() + "#" + getNome();
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="GRUPO_RELACIONADO_ID")
	public CampoGrupo getGrupoRelacionado() {
		return grupoRelacionado;
	}

	public void setGrupoRelacionado(CampoGrupo campoGrupo) {
		this.grupoRelacionado = campoGrupo;
	}

	@Column(name="HASH_JSON", length=480)
	public String getHashJson() {
		return this.hashJson;
	}

	public void setHashJson(String hashJson) {
		this.hashJson = hashJson;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="VALIDADE_EXPIRACAO")
	public Date getValidadeExpiracao() {
		return validadeExpiracao;
	}

	public void setValidadeExpiracao(Date validadeExpiracao) {
		this.validadeExpiracao = validadeExpiracao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EMISSAO")
	public Date getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(Date dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ADVERTENCIA_ID")
	public Advertencia getAdvertencia() {
		return advertencia;
	}

	public void setAdvertencia(Advertencia advertencia) {
		this.advertencia = advertencia;
	}


	@Transient
	public static String gerarCaminho(String dir, String extensao, Documento documento, Integer versao) {
		return gerarCaminho(dir, extensao, documento, versao, File.separator);
	}

	@Transient
	public static String gerarCaminho(String dir, String extensao, Documento documento, Integer versao, String separador) {

		Long documentoId = documento.getId();
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

		caminho.append(processoId).append("_").append(documentoId).append("_").append(versao).append(".").append(extensao);

		return caminho.toString();
	}

	@JoinColumn(name="GRUPO_MODELO_DOCUMENTO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}
}
