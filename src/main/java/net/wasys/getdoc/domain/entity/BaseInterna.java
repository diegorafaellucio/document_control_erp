package net.wasys.getdoc.domain.entity;

import net.wasys.util.rest.jackson.ObjectMapper;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Entity(name="BASE_INTERNA")
public class BaseInterna extends net.wasys.util.ddd.Entity {

	public static final Long TAXONOMIA = new Long(14);
	public static final Long MANTENEDORA_ID = new Long(13);
	public static final Long INSTITUICAO_ID = new Long(9);
	public static final Long REGIONAL_ID = new Long(10);
	public static final Long MODALIDADE_ENSINO_ID = new Long(3);
	public static final Long CURRICULO_CURSO_ID = new Long(4);
	public static final Long TURNO_ID = new Long(5);
	public static final Long TIPO_CURSO_ID = new Long(8);
	public static final Long FORMA_INGRESSO_ID = new Long(2);
	public static final Long CURSO_ID = new Long(6);
	public static final Long CAMPUS_ID = new Long(7);
	public static final Long BANCOS_FINANCIAMENTOS_ID = new Long(11);
	public static final Long TIPO_BOLSA_PROUNI_ID = new Long(12);
	public static final Long AREA_ID = new Long(16);
	public static final Long PERIODOS_INGRESSO_ID = new Long(21);
	public static final Long SALARIO_MINIMO_ID = 24L;
	public static final Long PERIODO_DE_INGRESSO = 26L;
	public static final Long TIPO_DE_BOLSA = 28L;
	public static final Long IES_DE_ORIGEM_ISENCAO = 30L;
	public static final Long CURSO_DE_ORIGEM_ISENCAO = 29L;
	public static final Long INSTITUICAO_ORIGEM_ID = new Long(18);

	private Long id;
	private String nome;
	private String descricao;
	private String colunasUnicidade;
	private String colunaLabel;
	private Date dataAlteracao;
	private boolean ativa = true;

	private Set<BaseRelacionamento> relacionamentos = new HashSet<>(0);

	public BaseInterna() {}

	public BaseInterna(Long id) {
		this.id = id;
	}

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NOME", nullable=false, length=50)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="DESCRICAO", nullable=false)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="COLUNAS_UNICIDADE", nullable=false)
	public String getColunasUnicidade() {
		return colunasUnicidade;
	}

	public void setColunasUnicidade(String colunasUnicidade) {
		this.colunasUnicidade = colunasUnicidade;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO", nullable=false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAtualizacao) {
		this.dataAlteracao = dataAtualizacao;
	}

	@Column(name="ATIVA", nullable=false)
	public boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	@Column(name="COLUNA_LABEL", length=500)
	public String getColunaLabel() {
		return colunaLabel;
	}

	public void setColunaLabel(String colunaLabel) {
		this.colunaLabel = colunaLabel;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="baseInterna", cascade=CascadeType.ALL)
	public Set<BaseRelacionamento> getRelacionamentos() {
		return relacionamentos;
	}

	public void setRelacionamentos(Set<BaseRelacionamento> relacionamentos) {
		this.relacionamentos = relacionamentos;
	}

	@Transient
    public List<String> getColunasUnicidadeList() {
		ObjectMapper om = new ObjectMapper();
		return om.readValue(colunasUnicidade, List.class);
    }

	@Override
	public String toString() {
		return super.toString() + "{nome=" + nome + "}";
	}
}