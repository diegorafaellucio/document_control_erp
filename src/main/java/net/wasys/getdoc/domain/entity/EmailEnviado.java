package net.wasys.getdoc.domain.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

@Entity(name="EMAIL_ENVIADO")
public class EmailEnviado extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date dataEnvio;
	private String codigo;
	private String assunto;
	private String destinatarios;

	private Processo processo;

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

	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ENVIO", nullable=false)
	public Date getDataEnvio() {
		return dataEnvio;
	}

	@Column(name="CODIGO")
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name="ASSUNTO")
	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	@Column(name="DESTINATARIOS", nullable=false, length=500)
	public String getDestinatarios() {
		return destinatarios;
	}

	public void setDestinatarios(String destinatarios) {
		this.destinatarios = destinatarios;
	}

	@Transient
	public List<String> getDestinatariosList() {

		String destinatarios = getDestinatarios();
		if(StringUtils.isBlank(destinatarios)) {
			return null;
		}

		List<String> list = new ArrayList<>();
		String[] opcoesArray = destinatarios.split(",");
		for (String opcao : opcoesArray) {

			opcao = StringUtils.trim(opcao);
			list.add(opcao);
		}

		return list;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
}
