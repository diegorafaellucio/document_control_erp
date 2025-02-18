package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Area extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long geralId;
	private String descricao;
	private Integer horasPrazo;
	private boolean ativo;
	private Date dataAtualizacao;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ATUALIZACAO", nullable=false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="HORAS_PRAZO")
	public Integer getHorasPrazo() {
		return horasPrazo;
	}

	public void setHorasPrazo(Integer horasPrazo) {
		this.horasPrazo = horasPrazo;
	}

	@Column(name="GERAL_ID")
	public Long getGeralId() {
		return geralId;
	}

	public void setGeralId(Long geralId) {
		this.geralId = geralId;
	}
}