package net.wasys.getdoc.domain.entity;

import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

public abstract class GrupoAbstract extends net.wasys.util.ddd.Entity implements Cloneable {

	private Long id;
	private String nome;
	private Integer ordem;
	private Boolean abertoPadrao = false;
	private Boolean grupoDinamico = false;

	private Set<CampoAbstract> campos = new HashSet<CampoAbstract>(0);

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public Set<? extends CampoAbstract> getCampos() {
		return this.campos;
	}

	public void setCampos(Set<CampoAbstract> campos) {
		this.campos = campos;
	}

	public Boolean getAbertoPadrao() {
		return abertoPadrao;
	}

	public void setAbertoPadrao(Boolean abertoPadrao) {
		this.abertoPadrao = abertoPadrao;
	}

	public Boolean getGrupoDinamico() {
		return grupoDinamico;
	}

	public void setGrupoDinamico(Boolean grupoDinamico) {
		this.grupoDinamico = grupoDinamico;
	}

	public GrupoAbstract clone() {
		try {
			return (GrupoAbstract) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Transient
	public abstract Long getTipoCampoGrupoId();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "[" + getNome() + "]";
	}
}
