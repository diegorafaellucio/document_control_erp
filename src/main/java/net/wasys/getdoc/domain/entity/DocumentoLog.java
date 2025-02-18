package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.*;

import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import org.apache.commons.lang.StringUtils;

@Entity(name="DOCUMENTO_LOG")
public class DocumentoLog extends net.wasys.util.ddd.Entity {

	private Long id;
	private AcaoDocumento acao;
	private Date data;

	private Usuario usuario;
	private Documento documento;
	private Pendencia pendencia;
	private String observacao;

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

	@Enumerated(EnumType.STRING)
	@Column(name="ACAO", nullable=false)
	public AcaoDocumento getAcao() {
		return this.acao;
	}

	public void setAcao(AcaoDocumento acao) {
		this.acao = acao;
	}

	@Deprecated
	public void setData(Date data) {
		this.data = data;
	}

	@Version
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	@Column(name="OBSERVACAO")
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
	@JoinColumn(name="PENDENCIA_ID")
	public Pendencia getPendencia() {
		return pendencia;
	}

	public void setPendencia(Pendencia pendencia) {
		this.pendencia = pendencia;
	}

	@Transient
	public String getObservacaoCurta() {
		return getObservacaoCurta2(70);
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
