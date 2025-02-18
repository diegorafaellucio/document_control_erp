package net.wasys.getdoc.domain.entity;

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

import org.apache.commons.lang.StringUtils;

@Entity
public class Pendencia extends net.wasys.util.ddd.Entity {

	private Long id;
	private String observacao;
	private String justificativa;
	private Date dataCriacao;
	private Date dataFinalizacao;

	private Documento documento;
	private Irregularidade irregularidade;
	private boolean notificadoAtila;

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

	@Column(name="JUSTIFICATIVA")
	public String getJustificativa() {
		return justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
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
	@JoinColumn(name="DOCUMENTO_ID", nullable=false)
	public Documento getDocumento() {
		return this.documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IRREGULARIDADE_ID")
	public Irregularidade getIrregularidade() {
		return this.irregularidade;
	}

	public void setIrregularidade(Irregularidade irregularidade) {
		this.irregularidade = irregularidade;
	}

	@Column(name="NOTIFICADO_ATILA", nullable=false)
	public boolean isNotificadoAtila() {
		return notificadoAtila;
	}

	public void setNotificadoAtila(boolean notificadoAtila) {
		this.notificadoAtila = notificadoAtila;
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
