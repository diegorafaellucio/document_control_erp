package net.wasys.getdoc.domain.vo;

public class RelatorioProdutividadeVO {

	private Long registroId;
	private String registroDescricao;
	private String ordenacao;
	private long atividades;
	private long requisicoes;
	private long cadastroManual;
	private long cadastroAutomatio;
	private long emAcompanhamento;
	private long finalizadosAnalise;
	private long finalizadosAcompanhamento;
	private long finalizadosPreAnalise;

	public Long getRegistroId() {
		return registroId;
	}

	public void setRegistroId(Long registroId) {
		this.registroId = registroId;
	}

	public String getRegistroDescricao() {
		return registroDescricao;
	}

	public void setRegistroDescricao(String registroDescricao) {
		this.registroDescricao = registroDescricao;
	}

	public long getAtividades() {
		return atividades;
	}

	public void setAtividades(long atividades) {
		this.atividades = atividades;
	}

	public long getRequisicoes() {
		return requisicoes;
	}

	public void setRequisicoes(long requisicoes) {
		this.requisicoes = requisicoes;
	}

	public long getCadastroManual() {
		return cadastroManual;
	}

	public void setCadastroManual(long cadastroManual) {
		this.cadastroManual = cadastroManual;
	}

	public long getCadastroAutomatio() {
		return cadastroAutomatio;
	}

	public void setCadastroAutomatio(long cadastroAutomatio) {
		this.cadastroAutomatio = cadastroAutomatio;
	}

	public long getCadastroTotal() {
		return getCadastroAutomatio() + getCadastroManual();
	}

	public long getEmAcompanhamento() {
		return emAcompanhamento;
	}

	public void setEmAcompanhamento(long emAcompanhamento) {
		this.emAcompanhamento = emAcompanhamento;
	}

	public long getFinalizadosAnalise() {
		return finalizadosAnalise;
	}

	public void setFinalizadosAnalise(long finalizadosAnalise) {
		this.finalizadosAnalise = finalizadosAnalise;
	}

	public long getFinalizadosAcompanhamento() {
		return finalizadosAcompanhamento;
	}

	public void setFinalizadosAcompanhamento(long finalizadosAcompanhamento) {
		this.finalizadosAcompanhamento = finalizadosAcompanhamento;
	}

	public long getFinalizadosTotal() {
		return getFinalizadosAcompanhamento() + getFinalizadosAnalise() + getFinalizadosPreAnalise();
	}

	public String getOrdenacao() {
		return ordenacao;
	}

	public void setOrdenacao(String ordenacao) {
		this.ordenacao = ordenacao;
	}

	public long getFinalizadosPreAnalise() {
		return finalizadosPreAnalise;
	}

	public void setFinalizadosPreAnalise(long finalizadosPreAnalise) {
		this.finalizadosPreAnalise = finalizadosPreAnalise;
	}

}
