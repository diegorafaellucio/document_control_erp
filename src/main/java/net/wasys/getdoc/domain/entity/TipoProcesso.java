package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoPrazo;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name="TIPO_PROCESSO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames="NOME"))
public class TipoProcesso extends net.wasys.util.ddd.Entity {

	public static final Long VESTIBULAR = new Long(283);
	public static final Long TRANSFERENCIA_EXTERNA = new Long(276);
	public static final Long MSV_EXTERNA = new Long(277);
	public static final Long MSV_INTERNA = new Long(269);
	public static final Long ENEM = new Long(268);
	public static final Long POS_GRADUACAO = new Long(286);
	public static final Long SIS_PROUNI = new Long(272);
	public static final Long PS_DOUTORADO = new Long(290);
	public static final Long PS_MESTRADO = new Long(289);
	public static final Long PS_POS_DOUTORADO = new Long(292);
	public static final Long DI_DOUTORADO = new Long(293);
	public static final Long DI_MESTRADO = new Long(295);
	public static final Long SIS_FIES = new Long(301);
	public static final Long MEDICINA_VESTIBULAR = new Long(297);
	public static final Long MEDICINA_ENEM = new Long(291);
	public static final Long MEDICINA_TE = new Long(294);
	public static final Long MEDICINA_MSV_EXTERNA = new Long(313);
	public static final Long ISENCAO_DISCIPLINAS = new Long(308);
	public static final List<Long> ANALISE_ISENCAO_IDS = Arrays.asList(MSV_EXTERNA, MSV_INTERNA, POS_GRADUACAO, TRANSFERENCIA_EXTERNA, VESTIBULAR, ENEM, ISENCAO_DISCIPLINAS);
	public static final List<Long> MEDICINA_IDS = Arrays.asList(MEDICINA_TE, MEDICINA_VESTIBULAR, MEDICINA_ENEM);
	public static final List<Long> GRADUACAO_IDS = Arrays.asList(MSV_EXTERNA, MSV_INTERNA, TRANSFERENCIA_EXTERNA, VESTIBULAR, ENEM, POS_GRADUACAO);
	public static final Long TE_PROUNI = new Long(304);
	public static final Long TE_FIES = new Long(305);
	public static final Long CONVENIO_UNIVERSIDADE_EXTERIOR = new Long(312);
	public static final Long CERTIFICACAO_INTERNACIONAL = new Long(307);

	private Long id;
	private String nome;
	private BigDecimal horasPrazo;
	private TipoPrazo tipoPrazo;
	private boolean ativo = true;
	private String dica;
	private boolean preencherViaOcr;
	private Situacao situacaoInicial;
	private Situacao situacaoDestinoConclusao;
	private Integer nivelPrioridade = 1;
	private String permissoeseEnvioFarolRegra;
	private BigDecimal horasPrazoAdvertir;
	private TipoPrazo tipoPrazoAdvertir;
	private boolean isencaoDisciplinas = false;
	private boolean possuiPreIsencaoDisciplinas = false;

	private Set<TipoProcessoPermissao> permissoes = new HashSet<TipoProcessoPermissao>(0);
	private Set<TipoCampoGrupo> tipoCampoGrupo = new HashSet<TipoCampoGrupo>(0);

	public TipoProcesso() {}

	public TipoProcesso(Long id) {
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
		this.nome = nome;
	}

	@Column(name="HORAS_PRAZO", nullable=false)
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

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="PREENCHER_VIA_OCR", nullable=false)
	public boolean isPreencherViaOcr() {
		return preencherViaOcr;
	}

	public void setPreencherViaOcr(boolean preencherViaOcr) {
		this.preencherViaOcr = preencherViaOcr;
	}

	@Column(name="DICA")
	public String getDica() {
		return dica;
	}

	public void setDica(String dica) {
		this.dica = dica;
	}

	@Column(name="NIVEL_PRIORIDADE")
	public Integer getNivelPrioridade() {
		return nivelPrioridade;
	}

	public void setNivelPrioridade(Integer nivelPrioridade) {
		this.nivelPrioridade = nivelPrioridade;
	}

    @Column(name="PERMISSOES_ENVIO_FAROL_REGRA")
    public String getPermissoeseEnvioFarolRegra() {
        return permissoeseEnvioFarolRegra;
    }

    public void setPermissoeseEnvioFarolRegra(String permissoeseEnvioFarolRegra) {
        this.permissoeseEnvioFarolRegra = permissoeseEnvioFarolRegra;
    }

	@Column(name="HORAS_PRAZO_ADVERTIR")
	public BigDecimal getHorasPrazoAdvertir() {
		return horasPrazoAdvertir;
	}

	public void setHorasPrazoAdvertir(BigDecimal horasPrazoAdvertir) {
		this.horasPrazoAdvertir = horasPrazoAdvertir;
	}

	@Column(name="TIPO_PRAZO_ADVERTIR")
	public TipoPrazo getTipoPrazoAdvertir() {
		return tipoPrazoAdvertir;
	}

	public void setTipoPrazoAdvertir(TipoPrazo tipoPrazoAdvertir) {
		this.tipoPrazoAdvertir = tipoPrazoAdvertir;
	}

    @ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID")
	public Situacao getSituacaoInicial() {
		return situacaoInicial;
	}

	public void setSituacaoInicial(Situacao situacaoInicial) {
		this.situacaoInicial = situacaoInicial;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_DESTINO_CONCLUSAO_ID")
	public Situacao getSituacaoDestinoConclusao() {
		return situacaoDestinoConclusao;
	}

	public void setSituacaoDestinoConclusao(Situacao situacaoDestinoConclusao) {
		this.situacaoDestinoConclusao = situacaoDestinoConclusao;
	}

	@OrderBy("permissao")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="tipoProcesso", cascade=CascadeType.ALL)
	public Set<TipoProcessoPermissao> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(Set<TipoProcessoPermissao> permissoes) {
		this.permissoes = permissoes;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="tipoProcesso", cascade=CascadeType.ALL)
	public Set<TipoCampoGrupo> getTipoCampoGrupo() {
		return tipoCampoGrupo;
	}

	public void setTipoCampoGrupo(Set<TipoCampoGrupo> tipoCampoGrupo) {
		this.tipoCampoGrupo = tipoCampoGrupo;
	}

	@Column(name="POSSUI_ISENCAO_DISCIPLINAS", nullable=false)
	public boolean isIsencaoDisciplinas() {
		return isencaoDisciplinas;
	}

	public void setIsencaoDisciplinas(boolean analiseIsencao) {
		this.isencaoDisciplinas = analiseIsencao;
	}

	@Column(name="POSSUI_PRE_ISENCAO_DISCIPLINAS", nullable=false)
	public boolean isPossuiPreIsencaoDisciplinas() {
		return possuiPreIsencaoDisciplinas;
	}

	public void setPossuiPreIsencaoDisciplinas(boolean possuiPreAnaliseIsecao) {
		this.possuiPreIsencaoDisciplinas = possuiPreAnaliseIsecao;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + getNome() + "}";
	}

	public enum POS_GRADUACAO {}
}
