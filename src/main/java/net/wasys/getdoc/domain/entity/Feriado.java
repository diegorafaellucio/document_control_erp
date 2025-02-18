package net.wasys.getdoc.domain.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"DATA"}))
public class Feriado extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long geralId;
	private String descricao;
	private Date data;
	private Date dataAtualizacao;
	private Boolean paralizacao;

	private Set<FeriadoSituacao> situacoes = new HashSet<>(0);
	private Set<FeriadoParalizacao> paralizacoes = new HashSet<>(0);

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", length=100)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Temporal(TemporalType.DATE)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ATUALIZACAO", nullable=false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name="GERAL_ID")
	public Long getGeralId() {
		return geralId;
	}

	public void setGeralId(Long geralId) {
		this.geralId = geralId;
	}

	@Column(name="PARALIZACAO")
	public Boolean getParalizacao() {
		return paralizacao;
	}

	public void setParalizacao(Boolean paralizacao) {
		this.paralizacao = paralizacao;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="feriado", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<FeriadoSituacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(Set<FeriadoSituacao> situacoes) {
		this.situacoes = situacoes;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="feriado", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<FeriadoParalizacao> getParalizacoes() {
		return paralizacoes;
	}

	public void setParalizacoes(Set<FeriadoParalizacao> paralizacoes) {
		this.paralizacoes = paralizacoes;
	}
}
