package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.wasys.getdoc.domain.enumeration.PermissaoTP;

@Entity(name="TIPO_PROCESSO_PERMISSAO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_PROCESSO_ID", "PERMISSAO"}))
public class TipoProcessoPermissao extends net.wasys.util.ddd.Entity {

	private Long id;
	private PermissaoTP permissao;

	private TipoProcesso tipoProcesso;

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
	@Column(name="PERMISSAO", nullable=false)
	public PermissaoTP getPermissao() {
		return permissao;
	}

	public void setPermissao(PermissaoTP permissao) {
		this.permissao = permissao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return this.tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
}
