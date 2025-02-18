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

@Entity(name="RELATORIO_GERAL_DOCUMENTO")
public class RelatorioGeralDocumento extends net.wasys.util.ddd.Entity {

	private Long id;
	private Documento documento;
	private RelatorioGeral relatorioGeral;
	private Date data;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RELATORIO_GERAL_ID")
	public RelatorioGeral getRelatorioGeral() {
		return relatorioGeral;
	}

	public void setRelatorioGeral(RelatorioGeral relatorioGeral) {
		this.relatorioGeral = relatorioGeral;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DOCUMENTO_ID")
	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA")
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}
}