package net.wasys.getdoc.domain.vo;

public class RelatorioProdutividadeProuniVO {


	public static final String DIRETORIA = "CSC";
	public static final String PROCESSO_NOME = "Serviços Financeiros";
	public static final String SUB_PROCESSO_NOME = "Financiamentos";

	private Long processoId;
	private String diretoria;
	private String nomeProcesso;
	private String subNomeProcesso;
	private String servico;
	private String sla;
	private String dataAbertura;
	private String dataVencimento;
	private String dataFechamento;
	private String solicitante;
	private String responsavel;
	private Integer quantidadeReabertura;
	private Integer quantidadeImagens;
	private Integer quantidadeAcoes;
	private String status;

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public String getDiretoria() {
		return diretoria;
	}

	public void setDiretoria(String diretoria) {
		this.diretoria = diretoria;
	}

	public String getNomeProcesso() {
		return nomeProcesso;
	}

	public void setNomeProcesso(String nomeProcesso) {
		this.nomeProcesso = nomeProcesso;
	}

	public String getSubNomeProcesso() {
		return subNomeProcesso;
	}

	public void setSubNomeProcesso(String subNomeProcesso) {
		this.subNomeProcesso = subNomeProcesso;
	}

	public String getServico() {
		return servico;
	}

	public void setServico(String servico) {
		this.servico = servico;
	}

	public String getSla() {
		return sla;
	}

	public void setSla(String sla) {
		this.sla = sla;
	}

	public String getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(String dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public String getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(String dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public String getDataFechamento() {
		return dataFechamento;
	}

	public void setDataFechamento(String dataFechamento) {
		this.dataFechamento = dataFechamento;
	}

	public String getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(String solicitante) {
		this.solicitante = solicitante;
	}

	public String getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(String responsavel) {
		this.responsavel = responsavel;
	}

	public Integer getQuantidadeReabertura() {
		return quantidadeReabertura;
	}

	public void setQuantidadeReabertura(Integer quantidadeReabertura) {
		this.quantidadeReabertura = quantidadeReabertura;
	}

	public Integer getQuantidadeImagens() {
		return quantidadeImagens;
	}

	public void setQuantidadeImagens(Integer quantidadeImagens) {
		this.quantidadeImagens = quantidadeImagens;
	}

	public Integer getQuantidadeAcoes() {
		return quantidadeAcoes;
	}

	public void setQuantidadeAcoes(Integer quantidadeAcoes) {
		this.quantidadeAcoes = quantidadeAcoes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
