package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.ModoTipificacao;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity(name="TIPO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_PROCESSO_ID", "NOME"}))
public class TipoDocumento extends net.wasys.util.ddd.Entity {

	public static final Long COMPROVANTE_IDENTIFICACAO_PROUNI = 773L;
	public static final Long COMPROVANTE_RESIDENCIA_PROUNI = 776L;
	public static final Long COMPROVANTE_RENDIMENTO_PROUNI = 789L;
	public static final List<Long> DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_PROUNI = Arrays.asList(COMPROVANTE_RENDIMENTO_PROUNI, COMPROVANTE_RESIDENCIA_PROUNI);
	public static final List<Long> DOCUMENTOS_MEMBRO_FAMILIAR_PROUNI = Arrays.asList(COMPROVANTE_IDENTIFICACAO_PROUNI, COMPROVANTE_RESIDENCIA_PROUNI, COMPROVANTE_RENDIMENTO_PROUNI);
	public static final Long COMPROVANTE_IDENTIFICACAO_FIES = 1270L;
	public static final Long COMPROVANTE_RESIDENCIA_FIES = 1273L;
	public static final Long COMPROVANTE_RENDIMENTO_FIES = 1274L;
	public static final List<Long> DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_FIES = Arrays.asList(COMPROVANTE_RENDIMENTO_FIES, COMPROVANTE_RESIDENCIA_FIES);
	public static final List<Long> DOCUMENTOS_MEMBRO_FAMILIAR_FIES = Arrays.asList(COMPROVANTE_IDENTIFICACAO_FIES, COMPROVANTE_RESIDENCIA_FIES, COMPROVANTE_RENDIMENTO_FIES);
	public static final Long COMPROVANTE_IDENTIFICACAO_TE_FIES = 1350L;
	public static final Long COMPROVANTE_RESIDENCIA_TE_FIES = 1344L;
	public static final Long COMPROVANTE_RENDIMENTO_TE_FIES = 1345L;
	public static final List<Long> DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_TE_FIES = Arrays.asList(COMPROVANTE_RENDIMENTO_TE_FIES, COMPROVANTE_RESIDENCIA_TE_FIES);
	public static final List<Long> DOCUMENTOS_MEMBRO_FAMILIAR_TE_FIES = Arrays.asList(COMPROVANTE_IDENTIFICACAO_TE_FIES, COMPROVANTE_RESIDENCIA_TE_FIES, COMPROVANTE_RENDIMENTO_TE_FIES);
	public static final Long COMPROVANTE_IDENTIFICACAO_TE_PROUNI = 1327L;
	public static final Long COMPROVANTE_RESIDENCIA_TE_PROUNI = 1322L;
	public static final Long COMPROVANTE_RENDIMENTO_TE_PROUNI = 1321L;
	public static final List<Long> DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_TE_PRUNI = Arrays.asList(COMPROVANTE_RENDIMENTO_TE_PROUNI, COMPROVANTE_RESIDENCIA_TE_PROUNI);
	public static final List<Long> DOCUMENTOS_MEMBRO_FAMILIAR_TE_PROUNI = Arrays.asList(COMPROVANTE_IDENTIFICACAO_TE_PROUNI, COMPROVANTE_RESIDENCIA_TE_PROUNI, COMPROVANTE_RENDIMENTO_TE_PROUNI);
	public static final Long BOLETIM_DESEMPENHO_ENEM_ID = 790L;
	public static final Long FICHA_DE_INSCRICAO_ID = 1190L;
	public static final List<Long> CONTRATO_PRESTACAO_SERVICO_EDUCACIONAIS = Arrays.asList(532L);
	public static final List<Long> DOCUMENTOS_REVISAO = Arrays.asList(12L, 13L);
	public static final List<Long> DOCUMENTOS_ESPECIAIS_PROUNI_NOFICACAO = Arrays.asList(780L, 781L, 786L, 785L, 787L, 1023L, 1300L);
	public static final List<Long> DOCUMENTOS_COMP_FAMILIAR_PROUNI_NOFICACAO = Arrays.asList(789L, 773L, 776L);
	public static final List<Long> DOCUMENTO_TCB_TR_PROUNI_NOFICACAO = Arrays.asList(777L, 778L);
	public static final List<Long> DOCUMENTO_TCB_TR_PROUNI_NOFICACAO_CALENDARIO = Arrays.asList(777L, 778L, 1316L);
	public static final List<Long> DOCUMENTOS_ESPECIAIS_FIES_NOFICACAO = Arrays.asList(1286L, 1277L, 1282L, 1283L, 1285L, 1288L, 1278L, 1291L, 1292L, 1293L, 1289L);
	public static final List<Long> DOCUMENTOS_COMP_FAMILIAR_FIES_NOFICACAO = Arrays.asList(1274L, 1270L, 1273L);
	public static final List<Long> DOCUMENTOS_CONTRATO_PORTAL_IDS = Arrays.asList(901L, 911L, 906L, 697L, 714L, 707L, 874L, 880L, 894L, 985L, 994L, 993L, 980L, 981L);
	public static final Long COMPROVANTE_ENSINO_MEDIO_COD_SIA = 185L;
	public static final List<Long> DIPLOMA_ENSINO_SUPERIOR_COD_SIA = Arrays.asList(174L, 14L);
	public static final Long TERMO_DE_PENDENCIA_SISPROUNI_ID = 1358L;
	public static final Long CHECKLIST_ID = 1476L;
	public static final Long FOTOCOPIA_DA_CARTEIRA_DE_IDENTIDADE_SISPROUNI_ID = 773L;
	public static final Long FOTOCOPIA_CPF_SISPROUNI_ID = 774L;
	public static final Long TERMO_DE_RECUSA_TR_ID = 778L;
	public static final Long VESTIBULAR_PLANO_ENSINO_INSTITUIÇÃO_ORIGEM = 1027L;
	public static final Long VESTIBULAR_ENEM_PLANO_ENSINO_INSTITUIÇÃO_ORIGEM = 1030L;
	public static final Long VESTIBULAR_HISTÓRICO_ORIGINAL_INSTITUIÇÃO_ORIGEM_DECLARAÇÃO_MATRÍCULA_ORIGINAL_IES_ORIGEM = 1026L;
	public static final Long VESTIBULAR_ENEM_HISTÓRICO_ORIGINAL_INSTITUIÇÃO_ORIGEM_DECLARAÇÃO_MATRÍCULA_ORIGINAL_IES_ORIGEM = 1029L;
	public static final Long TERMO_ACEITE_INGRESSO_SIMPLIFICADO_COD_ORIGEM = 263L;
	public static final Long CARTA_APRESENTACAO_COD_ORIGEM = 264L;

	public static final List<Long> DOCUMENTOS_OCR = Arrays.asList(990L, 991L, 992L);

	public static final String NOME_COMPROVATE_IDENTIFICAO = ("DOCUMENTOS DE IDENTIFICAÇÃO");
	public static final String NOME_COMPROVANTE_RESIDENCIA = ("COMPROVANTE DE RESIDÊNCIA");
	public static final String NOME_COMPROVANTE_RENDIMENTO = ("COMPROVANTES DE RENDIMENTO");

	private Long id;
	private String nome;
	private boolean obrigatorio = true;
	private boolean reconhecimentoFacial;
	private boolean exibirNoPortal = false;
	private boolean aceiteContrato;
	private Integer ordem;
	private short taxaCompressao = 100;
	private Integer maximoPaginas;

	private ModeloOcr modeloOcr;
	private TipoProcesso tipoProcesso;
	private BaseRegistro baseRegistro;
	private String descricao;
	private boolean ativo = true;
	private boolean permiteUpload = true;
	private BigDecimal horasPrazo;
	private TipoPrazo tipoPrazo;
	private Long codOrigem;
	private boolean sempreTipificar = false;
	private boolean notificarSia = true;
	private String observacaoParaNotificacao;
	private boolean termoPastaVermelha = false;
	private Integer validadeExpiracao;
	private boolean requisitarDataValidadeExpiracao = false;
	private boolean requisitarDataEmissao = false;
	private boolean requisitarDataPorModeloDocumento = false;
	private boolean permiteRejeitar = false;

	private ModoTipificacao modoTipificacao;

	public TipoDocumento() {}

	public TipoDocumento(Long id) {
		this.id = id;
	}

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

	@Column(name="EXIBIR_NO_PORTAL", nullable=false)
	public boolean getExibirNoPortal() {
		return exibirNoPortal;
	}

	public void setExibirNoPortal(boolean exibirNoPortal) {
		this.exibirNoPortal = exibirNoPortal;
	}

	@Column(name="ACEITE_CONTRATO", nullable=false)
	public boolean getAceiteContrato() {
		return aceiteContrato;
	}

	public void setAceiteContrato(boolean aceiteContrato) {
		this.aceiteContrato = aceiteContrato;
	}

	@Column(name="ORDEM", nullable=false)
	public Integer getOrdem() {
		return this.ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Column(name="TAXA_COMPRESSAO", nullable=false)
	public short getTaxaCompressao() {
		return taxaCompressao;
	}

	public void setTaxaCompressao(short taxaCompressao) {
		this.taxaCompressao = taxaCompressao;
	}

	@Column(name="MAXIMO_PAGINAS")
	public Integer getMaximoPaginas() {
		return maximoPaginas;
	}

	public void setMaximoPaginas(Integer maximoPaginas) {
		this.maximoPaginas = maximoPaginas;
	}

	@JoinColumn(name="MODELO_OCR_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	@JoinColumn(name="BASE_REGISTRO_ID")
	@ManyToOne(fetch=FetchType.LAZY)
	public BaseRegistro getBaseRegistro() {
		return baseRegistro;
	}

	public void setBaseRegistro(BaseRegistro baseRegistro) {
		this.baseRegistro = baseRegistro;
	}

	@Column(name="DESCRICAO", length=10000)
	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="PERMITE_UPLOAD", nullable = false)
	public boolean isPermiteUpload() {
		return permiteUpload;
	}

	public void setPermiteUpload(boolean permiteUpload) {
		this.permiteUpload = permiteUpload;
	}

	@Column(name="COD_ORIGEM")
	public Long getCodOrigem() {
		return this.codOrigem;
	}

	public void setCodOrigem(Long codOrigem) {
		this.codOrigem = codOrigem;
	}

	@Column(name="HORAS_PRAZO")
	public BigDecimal getHorasPrazo() {
		return horasPrazo;
	}

	public void setHorasPrazo(BigDecimal horasPrazo) {
		this.horasPrazo = horasPrazo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PRAZO")
	public TipoPrazo getTipoPrazo() {
		return tipoPrazo;
	}

	public void setTipoPrazo(TipoPrazo tipoPrazo) {
		this.tipoPrazo = tipoPrazo;
	}

	@Column(name="SEMPRE_TIPIFICAR")
	public boolean getSempreTipificar() {
		return sempreTipificar;
	}

	public void setSempreTipificar(boolean sempreTipificar) {
		this.sempreTipificar = sempreTipificar;
	}

	@Column(name="NOTIFICAR_SIA", nullable=false)
	public boolean isNotificarSia() {
		return notificarSia;
	}

	public void setNotificarSia(boolean notificarSia) {
		this.notificarSia = notificarSia;
	}

	@Column(name="OBSERVACAO_NOTIFICACAO")
	public String getObservacaoParaNotificacao() {
		return observacaoParaNotificacao;
	}

	public void setObservacaoParaNotificacao(String observacaoParaNotificacao) {
		this.observacaoParaNotificacao = observacaoParaNotificacao;
	}

	@Column(name="TERMO_PASTA_VERMELHA", nullable=false)
	public boolean getTermoPastaVermelha() {
		return termoPastaVermelha;
	}

	public void setTermoPastaVermelha(boolean termoPastaVermelha) {
		this.termoPastaVermelha = termoPastaVermelha;
	}

	@Column(name="VALIDADE_EXPIRACAO")
	public Integer getValidadeExpiracao() {
		return validadeExpiracao;
	}

	public void setValidadeExpiracao(Integer validadeExpiracao) {
		this.validadeExpiracao = validadeExpiracao;
	}

	@Column(name="REQUISITAR_DATA_VALIDADE_EXPIRACAO", nullable=false)
	public boolean getRequisitarDataValidadeExpiracao() {
		return requisitarDataValidadeExpiracao;
	}

	public void setRequisitarDataValidadeExpiracao(boolean requisitarDataValidadeExpiracao) {
		this.requisitarDataValidadeExpiracao = requisitarDataValidadeExpiracao;
	}

	@Column(name="REQUISITAR_DATA_EMISSAO", nullable=false)
	public boolean getRequisitarDataEmissao() {
		return requisitarDataEmissao;
	}

	public void setRequisitarDataEmissao(boolean requisitarDataEmissao) {
		this.requisitarDataEmissao = requisitarDataEmissao;
	}

	@Column(name = "REQUISITAR_DATA_POR_MODELO_DOCUMENTO")
	public boolean getRequisitarDataPorModeloDocumento() {
		return requisitarDataPorModeloDocumento;
	}

	public void setRequisitarDataPorModeloDocumento(boolean requisitarDataPorModeloDocumento) {
		this.requisitarDataPorModeloDocumento = requisitarDataPorModeloDocumento;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="MODO_TIPIFICACAO")
	public ModoTipificacao getModoTipificacao() {
		return modoTipificacao;
	}

	public void setModoTipificacao(ModoTipificacao modoTipificacao) {
		this.modoTipificacao = modoTipificacao;
	}

}