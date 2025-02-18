package net.wasys.getdoc.domain.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name="CAMPO_GRUPO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"PROCESSO_ID", "NOME"}))
public class CampoGrupo extends GrupoAbstract {

	private Processo processo;
	private Long tipoCampoGrupoId;
	private TipoCampoGrupo tipoSubgrupo;

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
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return this.processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@OrderBy("ordem")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="grupo")
	@SuppressWarnings("unchecked")
	public Set<Campo> getCampos() {
		return (Set<Campo>) super.getCampos();
	}

	@Override
	@Column(name="ABERTO_PADRAO")
	public Boolean getAbertoPadrao() {
		return super.getAbertoPadrao();
	}

	@Override
	@Column(name="GRUPO_DINAMICO")
	public Boolean getGrupoDinamico() {
		return super.getGrupoDinamico();
	}

	@Column(name="TIPO_CAMPO_GRUPO_ID")
	public Long getTipoCampoGrupoId() {
		return tipoCampoGrupoId;
	}

	public void setTipoCampoGrupoId(Long tipoCampoGrupoId) {
		this.tipoCampoGrupoId = tipoCampoGrupoId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_SUBGRUPO_ID")
	public TipoCampoGrupo getTipoSubgrupo() {
		return tipoSubgrupo;
	}

	public void setTipoSubgrupo(TipoCampoGrupo tipoSubGrupo) {
		this.tipoSubgrupo = tipoSubGrupo;
	}
}
