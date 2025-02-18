package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name="PROXIMA_SITUACAO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"ATUAL_ID", "PROXIMA_ID"}))
public class ProximaSituacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Situacao atual;
	private Situacao proxima;

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
	@JoinColumn(name="ATUAL_ID", nullable=false)
	public Situacao getAtual() {
		return atual;
	}

	public void setAtual(Situacao atual) {
		this.atual = atual;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROXIMA_ID", nullable=false)
	public Situacao getProxima() {
		return proxima;
	}

	public void setProxima(Situacao proxima) {
		this.proxima = proxima;
	}


}
