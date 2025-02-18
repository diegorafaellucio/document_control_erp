package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;

@Entity(name="TIPO_CAMPO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_CAMPO_GRUPO_ID", "NOME"}))
public class TipoCampo extends CampoAbstract {

	public static final String NOM_MANTENEDORA = "NOM_MANTENEDORA";
	public static final String COD_CAMPUS = "COD_CAMPUS";
	public static final String NOM_CAMPUS = "NOM_CAMPUS";
	public static final String NOM_INSTITUICAO = "NOM_INSTITUICAO";
	public static final String COD_INSTITUICAO = "COD_INSTITUICAO";
	public static final String POLO_PARCEIRO = "poloParceiro";
	public static final String NOM_REGIONAL = "NOM_REGIONAL";
	public static final String COD_REGIONAL = "COD_REGIONAL";
	public static final String NOM_TURNO = "NOM_TURNO";
	public static final String COD_TURNO = "COD_TURNO";
	public static final String NOM_TIPO_CURSO = "NOM_TIPO_CURSO";
	public static final String COD_TIPO_CURSO = "COD_TIPO_CURSO";
	public static final String COD_TIPO_BOLSA = "COD_TIPO_BOLSA";
	public static final String NOM_TIPO_BOLSA = "NOM_TIPO_BOLSA";
	public static final String TXT_MODALIDADE_ENSINO = "TXT_MODALIDADE_ENSINO";
	public static final String COD_FORMA_INGRESSO = "COD_FORMA_INGRESSO";
	public static final String NOM_FORMA_INGRESSO = "NOM_FORMA_INGRESSO";
	public static final String COD_CURSO = "COD_CURSO";
	public static final String NOM_CURSO = "NOM_CURSO";
	public static final String COD_FINANCIAMENTO = "COD_FINANCIAMENTO";
	public static final String NOM_FINANCIAMENTO = "NOM_FINANCIAMENTO";
	public static final String ID_CURRICULO_CURSO = "ID_CURRICULO_CURSO";
	public static final String DADOS_CAMPOS = "DADOS_CAMPOS";
	public static final String OBRIGATORIO = "OBRIGATÓRIO";
	public static final String EDITAVEL = "EDITAVEL";
	public static final String NOM_AREA = "NOM_AREA";
	public static final String MODALIDADE = "MODALIDADE";

	private Boolean defineUnicidade = false;
	private String origem;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return super.getId();
	}

	@Override
	@Column(name="NOME", length=50, nullable=false)
	public String getNome() {
		return super.getNome();
	}

	@Override
	@Column(name="TIPO", length=1, nullable=false)
	@Enumerated(EnumType.STRING)
	public TipoEntradaCampo getTipo() {
		return super.getTipo();
	}

	@Override
	@Column(name="TAMANHO_MAXIMO")
	public Integer getTamanhoMaximo() {
		return super.getTamanhoMaximo();
	}

	@Override
	@Column(name="TAMANHO_MINIMO")
	public Integer getTamanhoMinimo() {
		return super.getTamanhoMinimo();
	}

	@Override
	@Column(name="OBRIGATORIO", nullable=false)
	public boolean getObrigatorio() {
		return super.getObrigatorio();
	}

	@Override
	@Column(name="ORDEM", nullable=false)
	public Integer getOrdem() {
		return super.getOrdem();
	}

	@Override
	@Column(name="OPCOES", length=500)
	public String getOpcoes() {
		return super.getOpcoes();
	}

	@Override
	@Column(name="DICA", length=100)
	public String getDica() {
		return super.getDica();
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_CAMPO_GRUPO_ID", nullable=false)
	public TipoCampoGrupo getGrupo() {
		return (TipoCampoGrupo) super.getGrupo();
	}

	@Override
	@Column(name="EDITAVEL")
	public Boolean getEditavel() {
		return super.getEditavel();
	}

	@Override
	@Column(name="DEFINE_UNICIDADE")
	public Boolean getDefineUnicidade() {
		return super.getDefineUnicidade();
	}

	@Override
	@Column(name="PAIS", length=501)
	public String getPais() {
		return super.getPais();
	}

	@Override
	@Column(name="CRITERIO_EXIBICAO", length=502)
	public String getCriterioExibicao() {
		return super.getCriterioExibicao();
	}

	@Override
	@Column(name="CRITERIO_FILTRO", length=503)
	public String getCriterioFiltro() {
		return super.getCriterioFiltro();
	}

	@Column(name="ORIGEM", length=100)
	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	@Override
	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID")
	public BaseInterna getBaseInterna() {
		return super.getBaseInterna();
	}

	@Override
	@Transient
	public Long getTipoCampoId() {
		return getId();
	}
}