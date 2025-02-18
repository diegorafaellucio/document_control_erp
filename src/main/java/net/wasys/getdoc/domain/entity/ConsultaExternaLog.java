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

@Entity(name="CONSULTA_EXTERNA_LOG")
public class ConsultaExternaLog extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;

	private Usuario usuario;
	private Processo processo;
	private ConsultaExterna consultaExterna;

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

	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
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
	@JoinColumn(name="PROCESSO_ID")
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CONSULTA_EXTERNA_ID", nullable=false)
	public ConsultaExterna getConsultaExterna() {
		return consultaExterna;
	}

	public void setConsultaExterna(ConsultaExterna consultaExterna) {
		this.consultaExterna = consultaExterna;
	}
}
