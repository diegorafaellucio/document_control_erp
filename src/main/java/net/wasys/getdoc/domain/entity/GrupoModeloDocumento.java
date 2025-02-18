package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="GRUPO_MODELO_DOCUMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"DESCRICAO"}))
public class GrupoModeloDocumento extends net.wasys.util.ddd.Entity {
	public static final Long CNH_ID = 3L;
	public static final Long RG_ID = 1L;
	public static final Long ENEM_ID = 4L;

	private Long id;
	private String descricao;
	private Date dataAlteracao;
	private CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CATEGORIA_GRUPO_MODELO_DOCUMENTO_ID")
	public CategoriaGrupoModeloDocumento getCategoriaGrupoModeloDocumento() {
		return categoriaGrupoModeloDocumento;
	}

	public void setCategoriaGrupoModeloDocumento(CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento) {
		this.categoriaGrupoModeloDocumento = categoriaGrupoModeloDocumento;
	}


}
