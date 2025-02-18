package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="BASE_REGISTRO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"BASE_INTERNA_ID", "CHAVE_UNICIDADE"}))
public class BaseRegistro extends net.wasys.util.ddd.Entity {

	public static final String COD_REGIONAL = "COD_REGIONAL";
	public static final String COD_CAMPUS= "COD_CAMPUS";
	public static final String COD_CURSO= "COD_CURSO";
	public static final String POLO_PARCEIRO = "poloParceiro";

	public static final String SALARIO_MINIMO_VIGENCIA = "VIGÊNCIA";
	public static final String SALARIO_MINIMO_MULTIPLICADOR = "MULTIPLICADOR";
	public static final String SALARIO_MINIMO_VALOR_MENSAL = "VALOR MENSAL";

	private Long id;
	private String chaveUnicidade;
	private boolean ativo = true;
	private Date dataAtualizacao;

	private BaseInterna baseInterna;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID", nullable=false)
	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@Column(name="CHAVE_UNICIDADE", length=200, nullable=false)
	public String getChaveUnicidade() {
		return chaveUnicidade;
	}

	public void setChaveUnicidade(String chaveUnicidade) {
		this.chaveUnicidade = chaveUnicidade;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="DATA_ATUALIZACAO", nullable=false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
}