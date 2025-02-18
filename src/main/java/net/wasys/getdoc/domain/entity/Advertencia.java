package net.wasys.getdoc.domain.entity;

import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
public class Advertencia extends net.wasys.util.ddd.Entity {

	private Long id;
	private String observacao;
	private Date dataCriacao;
	private Date dataFinalizacao;

	private Irregularidade irregularidade;

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

	@Column(name="OBSERVACAO")
	public String getObservacao() {
		return this.observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CRIACAO", nullable=false)
	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FINALIZACAO")
	public Date getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(Date dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IRREGULARIDADE_ID")
	public Irregularidade getIrregularidade() {
		return this.irregularidade;
	}

	public void setIrregularidade(Irregularidade irregularidade) {
		this.irregularidade = irregularidade;
	}

	@Transient
	public String getObservacaoCurta() {
		return getObservacaoCurta2(45);
	}

	@Transient
	public String getObservacaoCurta2(int size) {

		String observacao = getObservacao();
		if(StringUtils.isBlank(observacao)) {
			return observacao;
		}

		observacao = observacao.replace("\t", " ");
		observacao = observacao.replace("\n", " ");
		observacao = observacao.replace("\r", " ");
		while(observacao.contains("  ")) {
			observacao = observacao.replace("  ", " ");
		}

		if(observacao.length() > size) {
			observacao = observacao.substring(0, (size - 5)) + "[...]";
		}

		return observacao;
	}
}
