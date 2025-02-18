package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusLogOcr;

import javax.persistence.*;
import java.io.File;
import java.util.Date;

@Entity(name="LOG_ANALISE_IA")
public class LogAnaliseIA extends net.wasys.util.ddd.Entity {

	private Long id;
	private Processo processo;
	private Documento documento;
	private TipoProcesso tipoProcesso;
	private Date data;
	private String statusDocumento;
	private String motivoDocumento;
	private String statusProcesso;
	private String motivoProcesso;
	private boolean tipificou;
	private boolean ocr;
	private String metadados;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DOCUMENTO_ID", nullable=false)
	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento processo) {
		this.documento = processo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name="STATUS_DOCUMENTO")
	public String getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(String statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	@Column(name="MOTIVO_DOCUMENTO")
	public String getMotivoDocumento() {
		return motivoDocumento;
	}

	public void setMotivoDocumento(String motivoDocumento) {
		this.motivoDocumento = motivoDocumento;
	}

	@Column(name="STATUS_PROCESSO")
	public String getStatusProcesso() {
		return statusProcesso;
	}

	public void setStatusProcesso(String statusProcesso) {
		this.statusProcesso = statusProcesso;
	}

	@Column(name="MOTIVO_PROCESSO")
	public String getMotivoProcesso() {
		return motivoProcesso;
	}

	public void setMotivoProcesso(String motivoProcesso) {
		this.motivoProcesso = motivoProcesso;
	}

	@Column(name="TIPIFICOU")
	public boolean isTipificou() {
		return tipificou;
	}

	public void setTipificou(boolean tipificou) {
		this.tipificou = tipificou;
	}

	@Column(name="OCR")
	public boolean isOcr() {
		return ocr;
	}

	public void setOcr(boolean ocr) {
		this.ocr = ocr;
	}

	@Column(name="METADADOS")
	public String getMetadados() {
		return metadados;
	}

	public void setMetadados(String metadados) {
		this.metadados = metadados;
	}
}
