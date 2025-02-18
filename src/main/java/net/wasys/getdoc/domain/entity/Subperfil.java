package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.AbaPrincipal;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Entity
public class Subperfil extends net.wasys.util.ddd.Entity {

	public static final Long CONFERENTE_ID = 102L;
	public static final Long CSC_ID = 103L;
	public static final List<Long> SUBPERFIS_FIES_PROUNI_IDS = Arrays.asList(105L, 108L, 109L, 110L);
	public static final List<Long> SUBPERFIS_POLOS_PARCEIRO_IDS = Arrays.asList(110L, 108L);
	public static final Long SUBPERFIL_UNIDADES_ID = 109L;
	public static final Long CSC_ADM_ID = 106L;
	public static final Long IBMEC = 114L;
	public static final List<Long> SUBPERFIS_CSC_IDS = Arrays.asList(103L, 106L, 112L, 118L);
	public static final List<Long> SUBPERFIS_MEDICINA_IDS = Arrays.asList(117l);

	private Long id;
	private String descricao;
	private AbaPrincipal abaPrincipal;
	private Boolean permiteConclusaoEmMassa;

	private FilaConfiguracao filaConfiguracao;

	private Set<SubperfilSituacao> situacoes = new HashSet<>(0);
	private Set<SubperfilTipoDocumento> tipoDocumentos = new HashSet<>(0);

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="ABA_PRINCIPAL", nullable=false)
	public AbaPrincipal getAbaPrincipal() {
		return abaPrincipal;
	}

	public void setAbaPrincipal(AbaPrincipal abaPrincipal) {
		this.abaPrincipal = abaPrincipal;
	}

	@Column(name="PERMITE_CONCLUSAO_EM_MASSA", nullable=false)
	public Boolean getPermiteConclusaoEmMassa() {
		return permiteConclusaoEmMassa;
	}

	public void setPermiteConclusaoEmMassa(Boolean permiteConclusaoEmMassa) {
		this.permiteConclusaoEmMassa = permiteConclusaoEmMassa;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="subperfil", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<SubperfilSituacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(Set<SubperfilSituacao> situacoes) {
		this.situacoes = situacoes;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="subperfil", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<SubperfilTipoDocumento> getTipoDocumentos() {
		return tipoDocumentos;
	}

	public void setTipoDocumentos(Set<SubperfilTipoDocumento> tipoDocumentos) {
		this.tipoDocumentos = tipoDocumentos;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FILA_CONFIGURACAO_ID", nullable=false)
	public FilaConfiguracao getFilaConfiguracao() {
		return filaConfiguracao;
	}

	public void setFilaConfiguracao(FilaConfiguracao filaConfiguracao) {
		this.filaConfiguracao = filaConfiguracao;
	}

}