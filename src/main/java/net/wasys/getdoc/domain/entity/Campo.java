package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;

import javax.persistence.*;

@Entity(name="CAMPO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"CAMPO_GRUPO_ID", "NOME"}))
public class Campo extends CampoAbstract {

	private Long tipoCampoId;

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
	@Column(name="VALOR", length=500, nullable=false)
	public String getValor() {
		return super.getValor();
	}

	@Override
	@Column(name="OPCOES", length=500)
	public String getOpcoes() {
		return super.getOpcoes();
	}

	@Override
	@Column(name="DICA", length=200)
	public String getDica() {
		return super.getDica();
	}

	@Override
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAMPO_GRUPO_ID", nullable=false)
	public CampoGrupo getGrupo() {
		return (CampoGrupo) super.getGrupo();
	}

	public void setGrupo(CampoGrupo grupo) {
		super.setGrupo(grupo);
	}

	@Column(name="TIPO_CAMPO_ID")
	public Long getTipoCampoId() {
		return tipoCampoId;
	}

	public void setTipoCampoId(Long tipoCampoId) {
		this.tipoCampoId = tipoCampoId;
	}

	@Override
	@Column(name="EDITAVEL")
	public Boolean getEditavel() {
		return super.getEditavel();
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

	@Override
	@Column(name="OPCAO_ID", length=5000)
	public String getOpcaoId() {
		return super.getOpcaoId();
	}

	@Override
	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID")
	public BaseInterna getBaseInterna() {
		return super.getBaseInterna();
	}
}
