package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name="PROCESSO_NOTIFICAR_ATILA")
public class ProcessoNotificarAtila extends net.wasys.util.ddd.Entity {

	private Long id;
	private Processo processo;
	private Boolean notificarAtila;

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

	@Column(name="NOTIFICAR_ATILA")
	public Boolean getNotificarAtila() {
		return notificarAtila;
	}

	public void setNotificarAtila(Boolean notificarAtila) {
		this.notificarAtila = notificarAtila;
	}
}
