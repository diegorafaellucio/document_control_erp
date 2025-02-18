package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity(name="BLOQUEIO_PROCESSO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"USUARIO_ID", "PROCESSO_ID"}))
public class BloqueioProcesso extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long processoId;
	private Long usuarioId;
	private Date data;

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

	@Column(name="USUARIO_ID", nullable=false)
	public Long getUsuarioId() {
		return this.usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	@Column(name="PROCESSO_ID", nullable=false)
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "[id: " + getId() + ", processoId: " + getProcessoId() + ", usuarioId: " + getUsuarioId() + "]";
	}
}
