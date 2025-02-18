package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.rest.request.vo.RequestFilaConfiguracao;

import javax.persistence.*;

@Entity
@Table(name = "FILA_CONFIGURACAO", uniqueConstraints=@UniqueConstraint(columnNames="DESCRICAO"))
public class FilaConfiguracao extends net.wasys.util.ddd.Entity {

	public static final Long FILA_CONSULTA_CANDIDATO = new Long(103);

	private Long id;
	private String descricao;
	private String status;
	private String colunas;
	private boolean exibirNaoAssociados;
	private boolean exibirAssociadosOutros;
	private boolean permissaoEditarOutros;
	private boolean verificarProximaRequisicao;
	private boolean padrao;
	private boolean exibirColunaAnalista;

	public FilaConfiguracao() {}

	public FilaConfiguracao(Long id) {
		this.id = id;
	}

	public FilaConfiguracao(RequestFilaConfiguracao requestFilaConfiguracao) {
	    this.id = requestFilaConfiguracao.getId();
		this.descricao = requestFilaConfiguracao.getDescricao();
		this.status = requestFilaConfiguracao.getStatus();
		this.exibirNaoAssociados = requestFilaConfiguracao.isExibirNaoAssociados();
		this.exibirAssociadosOutros = requestFilaConfiguracao.isExibirAssociadosOutros();
		this.permissaoEditarOutros = requestFilaConfiguracao.isPermissaoEditarOutros();
		this.verificarProximaRequisicao = requestFilaConfiguracao.isVerificarProximaRequisicao();
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

	@Column(name="DESCRICAO", nullable=false, length=120)
	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="EXIBIR_NAO_ASSOCIADOS")
	public boolean isExibirNaoAssociados() {
		return exibirNaoAssociados;
	}

	public void setExibirNaoAssociados(boolean exibirNaoAssociados) {
		this.exibirNaoAssociados = exibirNaoAssociados;
	}

	@Column(name="EXIBIR_ASSOCIADOS_OUTROS")
	public boolean isExibirAssociadosOutros() {
		return exibirAssociadosOutros;
	}

	public void setExibirAssociadosOutros(boolean exibirAssociadosOutros) {
		this.exibirAssociadosOutros = exibirAssociadosOutros;
	}

	@Column(name="PERMISSAO_EDITAR_OUTROS")
	public boolean isPermissaoEditarOutros() {
		return permissaoEditarOutros;
	}

	public void setPermissaoEditarOutros(boolean permissaoEditarOutros) {
		this.permissaoEditarOutros = permissaoEditarOutros;
	}

	@Column(name="VERIFICAR_PROXIMA_REQUISICAO")
	public boolean isVerificarProximaRequisicao() {
		return verificarProximaRequisicao;
	}

	public void setVerificarProximaRequisicao(boolean verificarProxima) {
		this.verificarProximaRequisicao = verificarProxima;
	}

	@Column(name="STATUS")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name="COLUNAS_PERSONALIZADAS")
	public String getColunas() {
		return colunas;
	}

	public void setColunas(String colunas) {
		this.colunas = colunas;
	}

	@Column(name="PADRAO")
	public boolean isPadrao() {
		return padrao;
	}

	public void setPadrao(boolean padrao) {
		this.padrao = padrao;
	}

	@Column(name="EXIBIR_COLUNA_ANALISTA")
	public boolean getExibirColunaAnalista() {
		return exibirColunaAnalista;
	}

	public void setExibirColunaAnalista(boolean exibirColunaAnalista) {
		this.exibirColunaAnalista = exibirColunaAnalista;
	}
}