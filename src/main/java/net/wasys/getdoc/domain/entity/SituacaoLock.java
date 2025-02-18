package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name = "SITUACAO_LOCK")
@Table(uniqueConstraints=@UniqueConstraint(columnNames="SITUACAO_ID"))
public class SituacaoLock extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long situacaoId;

	public SituacaoLock() {}

	public SituacaoLock(Long id) {
		this.id = id;
	}

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

	@Column(name = "situacao_id")
	public Long getSituacaoId() {
		return situacaoId;
	}

	public void setSituacaoId(Long situacaoId) {
		this.situacaoId = situacaoId;
	}

}
