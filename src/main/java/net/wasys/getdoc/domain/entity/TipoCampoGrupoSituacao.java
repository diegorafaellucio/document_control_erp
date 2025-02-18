package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Set;

@Entity(name="TIPO_CAMPO_GRUPO_SITUACAO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_CAMPO_GRUPO_ID", "SITUACAO_ID"}))
public class TipoCampoGrupoSituacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoCampoGrupo tipoCampoGrupo;
	private Situacao situacao;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_CAMPO_GRUPO_ID", nullable=false)
	public TipoCampoGrupo getTipoCampoGrupo() {
		return tipoCampoGrupo;
	}

	public void setTipoCampoGrupo(TipoCampoGrupo tipoCampoGrupo) {
		this.tipoCampoGrupo = tipoCampoGrupo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SITUACAO_ID", nullable = false)
	public Situacao getSituacao() {
		return this.situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}
}
