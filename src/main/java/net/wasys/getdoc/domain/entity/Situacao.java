package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="NOME"))
public class Situacao extends net.wasys.util.ddd.Entity {

	public static final String ANALISE_ISENCAO_REVISAO = "3.2 - Análise Isenção de Disciplina - Revisão";
	public static final String ANALISE_ISENCAO_CONCLUIDA = "3.3 - Análise Isenção de Disciplina Concluída";
	public static final String EM_ANALISE = "1.1 - Em Análise";
	public static final String ALUNO = "- ALUNO";

	public static final List<Long> RASCUNHO_IDS = Arrays.asList(223L, 199L, 260L, 220L, 251L, 196L);
	public static final List<Long> DOCUMENTO_PENDENTE_IDS = Arrays.asList(243L, 245L, 262L, 241L, 253L, 247L);
	public static final List<Long> DOCUMENTACAO_APROVADA_IDS = Arrays.asList(242L, 244L, 263L, 240L, 254L, 238L);
	public static final List<Long> GERAL_RASCUNHO_IDS = Arrays.asList(313L, 324L, 320L, 223L, 260L, 208L, 288L, 306L, 220L, 251L, 196L, 303L, 199L, 403L, 335L, 295L);
	public static final List<Long> GERAL_EM_ANALISE_IDS = Arrays.asList(307L, 340L, 341L, 348L, 349L, 350L, 345L, 346L, 351L, 353L, 352L, 414L, 413L, 424L, 420L, 356L, 421L, 427L, 342L, 296L, 289L, 197L, 347L, 430L, 429L, 224L, 252L, 302L, 200L, 261L, 339L, 404L, 314L, 325L, 321L, 336L, 355L, 405L, 343L, 344L, 221L, 209L);
	public static final List<Long> GERAL_APROVADO_IDS = Arrays.asList(305L, 263L, 312L, 294L, 425L, 244L, 358L, 240L, 238L, 242L, 301L, 254L, 338L, 416L, 319L, 330L, 323L);
	public static final List<Long> ANALISE_ISENCAO_IDS = Arrays.asList(264L, 279L, 270L, 267L, 280L, 266L, 271L, 281L, 265L, 268L, 283L, 269L, 275L, 284L, 274L, 276L, 285L, 277L, 437L, 432L, 433L, 434L, 435L, 436L, 496L, 511L, 512L, 513L, 514L, 515L, 507L, 506L, 508L, 509L, 510L, 552L, 553L, 556L, 554L, 557L, 555L, 558L, 559L, 560L, 561L);
	public static final List<Long> ANALISE_ISENCAO_IBMEC_IDS = Arrays.asList(571L, 572L, 582L, 583L);
	public static final List<Long> ISENCAO_DISCIPLINAS_IDS = Arrays.asList(511L, 512L, 507L, 506L, 508L, 509L, 510L, 552L, 553L, 556L, 554L, 557L, 555L, 558L);
	public static final Long ISENCAO_DISCIPLINAS_ID = (506L);
	public static final Long ISENCAO_DISCIPLINAS_TE_ID = (508L);
	public static final Long ISENCAO_DISCIPLINAS_MEDICINA_ID = (568l);
	public static final Long ISENCAO_DISCIPLINAS_MEDICINA_CONLUIDA_ID = (569l);
	public static final Long ISENCAO_DISCIPLINAS_PRE_ANALISE_ID = (507L);
	public static final Long ISENCAO_DISCIPLINAS_REVISAO_ID = (511L);
	public static final Long ISENCAO_DISCIPLINAS_REVISAO_TE_ID = (512L);
	public static final List<Long> ISENCAO_DISCIPLINAS_MEDICINA_IDS = Arrays.asList(ISENCAO_DISCIPLINAS_MEDICINA_ID, ISENCAO_DISCIPLINAS_MEDICINA_CONLUIDA_ID);
	public static final List<Long> ISENCAO_DISCIPLINAS_PRE_ANALISE_IDS = Arrays.asList(ISENCAO_DISCIPLINAS_ID, ISENCAO_DISCIPLINAS_PRE_ANALISE_ID);
	public static final List<Long> ISENCAO_DISCIPLINAS_PRE_ANALISE_TE_IDS = Arrays.asList(ISENCAO_DISCIPLINAS_PRE_ANALISE_ID, ISENCAO_DISCIPLINAS_TE_ID);
	public static final List<Long> ISENCAO_DISCIPLINA_REVISAO_IDS = Arrays.asList(ISENCAO_DISCIPLINAS_REVISAO_ID, ISENCAO_DISCIPLINAS_REVISAO_TE_ID);
	public static final List<Long> ERRO_NOTIFICACAO_CANDIDATO_IDS = Arrays.asList(405L, 347L);
	public static final List<Long> CONSULTAR_SIA_FIES_PROUNI_IDS = Arrays.asList(273L, 410L);
	public static final List<Long> SITUACAO_CONCLUIDO_FIES_PROUNI_ID = Arrays.asList(358L, 423L, 416L);
	public static final List<Long> SITUACAO_CONCLUIDO_FIES_PROUNI_ENVIAR_PARA_ANALISE_ID = Arrays.asList(425L, 584L, 490L, 411L, 498L, 483L, 465L, 472L, 403L, 406L, 481L, 482L, 208L, 259L, 422L, 423L, 447L, 450L, 464L, 602L, 603L);
	public static final List<Long> SITUACAO_PENDENTE_PROUNI_ID = Arrays.asList(259L, 422L, 423L, 428L);
	public static final Long COLHER_ASSINATURA_TCB_TR = (272L);
	public static final Long SOLICITAR_EMISSAO_TCB_TR = (360L);
	public static final List<Long> SITUACAO_NAO_ENVIAR_PARA_EMITIR_TR_PROUNI = Arrays.asList(209L, 272L, 282L, 347L, 354L, 355L, 356L, 357L, 358L, 419L, 420L, 421L, 423L, 424L, 425L, 426L, 427L, 429L, 430L, 438L, 439L, 440L, 442L, 443L, 445L);
	public static final Long SITUACAO_ENVIO_PARA_EMITIR_TR_SISPROUNI = (441L);
	public static final Long CONCLUIDO_PENDENTE_VINCULO_SISFIES_ID = (603L);
	public static final Long CONCLUIDO_PENDENTE_VINCULO_SISPROUNI_ID = (602L);
	public static final List<Long> SISFIES_SISPROUNI_TRANSFORMADO_ID = Arrays.asList(495L, 476L, 477L, 489L);
	public static final Long SISPROUNI_PROCESSAMENTO_SISTEMICO_ID = (624L);
	public static final Long SISPROUNI_DIGITALIZADO_INCOMPLETO_ID = (626L);
	public static final Long SISPROUNI_DIGITALIZADO_SEM_VINCULO_ID = (625L);
	public static final Long SISPROUNI_NOVO_ID = (208L);
	public static final Long SISFIES_TRIAGEM_SISTEMICA_ID = (559L);
	public static final Long DOCUMENTACAO_PENDENTE_POLO_PARCEIRO_TR_ID = (621L);
	public static final Long ISENCAO_DISCIPLINAS_ALUNO = (640L);

	public static final String AGUARDANDO_TIPIFICACAO = "1.1.1 – Em Análise (Tipificação)";
	public static final String AGUARDANDO_OCR = "1.1.2 - Aguardando OCR";
	public static final String AGUARDANDO_ANALISE_IA = "1.1.3 - Aguardando Análise IA";
	public static final String DOCUMENTACAO_APROVADA = "4.0 - Documentação Aprovada";
	public static final String DOCUMENTACAO_PENDENTE = "2.0 - Documentação Pendente";

	private Long id;
	private String nome;
	private BigDecimal horasPrazo;
	private TipoPrazo tipoPrazo;
	private StatusProcesso status;
	private boolean ativa = true;
	private boolean distribuicaoAutomatica = true;
	private boolean notificarAutor;
	private boolean encaminhado;
	private BigDecimal horasPrazoAdvertir;
	private TipoPrazo tipoPrazoAdvertir;
	private boolean permiteEditarDocumentos;
	private boolean permiteEditarCampos;
	private boolean permiteTipificacao;
	private boolean contarTempoSlaEtapa;
	private boolean notificarDocumentosSia = false;
	private String acaoFinanciamentos;

	private Situacao situacaoRetorno;
	private TipoProcesso tipoProcesso;
	private Etapa etapa;

	private Set<ProximaSituacao> proximas = new HashSet<ProximaSituacao>(0);
	private Set<SituacaoSubperfil> subperfis = new HashSet<SituacaoSubperfil>(0);

	private boolean bloqueioDeDigitalizacaoPorCalendario;

	public Situacao() {}
	
	public Situacao(Long id) {
		this.id = id;
	}

	public Situacao(Long id, String nome, String tipoProcessoNome) {
		this.id = id;
		this.nome = nome;
		this.tipoProcesso = new TipoProcesso();
		this.tipoProcesso.setNome(tipoProcessoNome);
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

	@Column(name="NOME", nullable=false, length=120)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="ATIVA", nullable=false)
	public boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
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

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS")	
	public StatusProcesso getStatus() {
		return status;
	}

	public void setStatus(StatusProcesso status) {
		this.status = status;
	}

	@Column(name="DISTRIBUICAO_AUTOMATICA", nullable=false)
	public boolean isDistribuicaoAutomatica() {
		return distribuicaoAutomatica;
	}

	public void setDistribuicaoAutomatica(boolean distribuicaoAutomatica) {
		this.distribuicaoAutomatica = distribuicaoAutomatica;
	}

	@Column(name="NOTIFICAR_AUTOR")
	public boolean isNotificarAutor() {
		return notificarAutor;
	}

	public void setNotificarAutor(boolean notificarAutor) {
		this.notificarAutor = notificarAutor;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_RETORNO_ID")
	public Situacao getSituacaoRetorno() {
		return situacaoRetorno;
	}

	public void setSituacaoRetorno(Situacao situacaoRetorno) {
		this.situacaoRetorno = situacaoRetorno;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID")
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="atual", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<ProximaSituacao> getProximas() {
		return proximas;
	}

	public void setProximas(Set<ProximaSituacao> proximas) {
		this.proximas = proximas;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="situacao", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<SituacaoSubperfil> getSubperfis() {
		return subperfis;
	}

	public void setSubperfis(Set<SituacaoSubperfil> subperfis) {
		this.subperfis = subperfis;
	}

	@Column(name="ENCAMINHADO", nullable = false)
	public boolean isEncaminhado() {
		return encaminhado;
	}

	public void setEncaminhado(boolean encaminhado) {
		this.encaminhado = encaminhado;
	}

	@Column(name="HORAS_PRAZO_ADVERTIR", nullable = false)
	public BigDecimal getHorasPrazoAdvertir() {
		return horasPrazoAdvertir;
	}

	public void setHorasPrazoAdvertir(BigDecimal horasPrazoAdvertir) {
		this.horasPrazoAdvertir = horasPrazoAdvertir;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PRAZO_ADVERTIR", nullable = false)
	public TipoPrazo getTipoPrazoAdvertir() {
		return tipoPrazoAdvertir;
	}

	public void setTipoPrazoAdvertir(TipoPrazo tipoPrazoAdvertir) {
		this.tipoPrazoAdvertir = tipoPrazoAdvertir;
	}

	@Column(name="PERMITE_EDITAR_DOCUMENTOS", nullable = false)
	public boolean isPermiteEditarDocumentos() {
		return permiteEditarDocumentos;
	}

	public void setPermiteEditarDocumentos(boolean permiteEditarDocumentos) {
		this.permiteEditarDocumentos = permiteEditarDocumentos;
	}

	@Column(name="PERMITE_EDITAR_CAMPOS", nullable = false)
	public boolean isPermiteEditarCampos() {
		return permiteEditarCampos;
	}

	public void setPermiteEditarCampos(boolean permiteEditarCampos) {
		this.permiteEditarCampos = permiteEditarCampos;
	}

	@Column(name="PERMITE_TIPIFICACAO", nullable = false)
	public boolean isPermiteTipificacao() {
		return permiteTipificacao;
	}

	public void setPermiteTipificacao(boolean permiteTipificacao) {
		this.permiteTipificacao = permiteTipificacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ETAPA_ID")
	public Etapa getEtapa() {
		return etapa;
	}

	public void setEtapa(Etapa etapa) {
		this.etapa = etapa;
	}

	@Column(name="CONTAR_TEMPO_SLA_ETAPA")
	public boolean getContarTempoSlaEtapa() {
		return contarTempoSlaEtapa;
	}

	public void setContarTempoSlaEtapa(boolean contarTempoSlaEtapa) {
		this.contarTempoSlaEtapa = contarTempoSlaEtapa;
	}

	@Column(name="NOTIFICAR_DOCUMENTOS_SIA")
	public boolean isNotificarDocumentosSia() {
		return notificarDocumentosSia;
	}

	public void setNotificarDocumentosSia(boolean notificarDocumentosSia) {
		this.notificarDocumentosSia = notificarDocumentosSia;
	}

	@Transient
	public boolean isBloqueioDeDigitalizacaoPorCalendario() {
		return bloqueioDeDigitalizacaoPorCalendario;
	}

	public void setBloqueioDeDigitalizacaoPorCalendario(boolean bloqueioDeDigitalizacaoPorCalendario) {
		this.bloqueioDeDigitalizacaoPorCalendario = bloqueioDeDigitalizacaoPorCalendario;
	}

	@Column(name="ACAO_FINANCIAMENTOS", nullable=true, length=100)
	public String getAcaoFinanciamentos() {
		return acaoFinanciamentos;
	}

	public void setAcaoFinanciamentos(String acaoFinanciamentos) {
		this.acaoFinanciamentos = acaoFinanciamentos;
	}

}
