package net.wasys.getdoc.domain.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Entity(name="TIPO_CAMPO_GRUPO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_PROCESSO_ID", "NOME"}))
public class TipoCampoGrupo extends GrupoAbstract {

	public static final Long FIES_MEMBRO_FAMILIAR_ID = 425L;
	public static final Long PROUNI_MEMBRO_FAMILIAR_ID = 401L;
	public static final Long TE_FIES_MEMBRO_FAMILIAR_ID = 443L;
	public static final Long TE_PROUNI_MEMBRO_FAMILIAR_ID = 440L;

	public static final List<Long> MEMBRO_FAMILIAR_IDS = Arrays.asList(TE_PROUNI_MEMBRO_FAMILIAR_ID, TE_FIES_MEMBRO_FAMILIAR_ID, PROUNI_MEMBRO_FAMILIAR_ID, FIES_MEMBRO_FAMILIAR_ID);

	private TipoProcesso tipoProcesso;
	private Boolean criacaoProcesso = true;
	private Boolean grupoDinamico = false;
	private Set<TipoCampoGrupoSituacao> situacoes = new HashSet<>(0);
	private TipoCampoGrupo subgrupo;

	public TipoCampoGrupo() {}

	public TipoCampoGrupo(Long id) {
		setId(id);
	}

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return super.getId();
	}

	@Column(name="NOME", length=50, nullable=false)
	public String getNome() {
		return super.getNome();
	}

	@Column(name="ORDEM", nullable=false)
	public Integer getOrdem() {
		return super.getOrdem();
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@OrderBy("ordem")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="grupo")
	@SuppressWarnings("unchecked")
	public Set<TipoCampo> getCampos() {
		return (Set<TipoCampo>) super.getCampos();
	}

	@Column(name="CRIACAO_PROCESSO", nullable=false)
	public Boolean getCriacaoProcesso() {
		return criacaoProcesso;
	}

	public void setCriacaoProcesso(Boolean criacaoProcesso) {
		this.criacaoProcesso = criacaoProcesso;
	}

	@Column(name="ABERTO_PADRAO", nullable=false)
	public Boolean getAbertoPadrao() {
		return super.getAbertoPadrao();
	}

	@Column(name="GRUPO_DINAMICO", nullable=false)
	public Boolean getGrupoDinamico() {
		return grupoDinamico;
	}

	public void setGrupoDinamico(Boolean grupoDinamico) {
		this.grupoDinamico = grupoDinamico;
	}

	@OrderBy("id")
	@OneToMany(fetch=FetchType.EAGER, mappedBy="tipoCampoGrupo", cascade= CascadeType.ALL, orphanRemoval=true)
	public Set<TipoCampoGrupoSituacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(Set<TipoCampoGrupoSituacao> situacoes) {
		this.situacoes = situacoes;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBGRUPO_ID")
	public TipoCampoGrupo getSubgrupo() {
		return subgrupo;
	}

	public void setSubgrupo(TipoCampoGrupo subGrupo) {
		this.subgrupo = subGrupo;
	}

	@Override
	@Transient
	public Long getTipoCampoGrupoId() {
		return getId();
	}
}
