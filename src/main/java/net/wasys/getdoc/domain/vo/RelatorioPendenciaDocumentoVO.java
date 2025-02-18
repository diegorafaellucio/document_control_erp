package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.Origem;

import java.util.Date;
import java.util.List;

public class RelatorioPendenciaDocumentoVO implements Comparable<RelatorioPendenciaDocumentoVO> {

	private long processoId;
	private String nomeSituacao;
	private String origem;
	private String nomeAluno;
	private String cpf;
	private String situacaoAluno;
	private String periodoDoc;
	private String nomeDocumento;
	private long totalVezesEmAnalise;
	private String statusDocumento;
	private String observacaoPendencia;
	private long totalImagens;
	private Date dataEnvioAnalise;
	private Date dataFimAnalise;
	private List<String> documentos;
	private String irregularidade;
	private boolean obrigatorio;
	private Integer versaoAtual;
	private Boolean usaTermo;
	private Boolean pastaAmarela;
	private String tipoProcesso;
	private String modeloDocumento;
	private String situacaoAnterior;
	private Long numeroDePaginas;
	private Date dataCriacaoProcesso;
	private String loginUsuario;
	private String nomeUsuario;
	private Long documentoId;
	private String analistaLogin;
	private String analistaNome;
	private String numeroMembro;
	private String camposDinamicos;
	private Boolean pastaVermelha;
	private String nomeRegional;
	private Origem origemDocumento;

	private Long getId() {
		return Long.valueOf(this.processoId);
	}

	@Override
	public int compareTo(RelatorioPendenciaDocumentoVO o) {
		return this.getId().compareTo(o.getId());
	}

	public long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(long processoId) {
		this.processoId = processoId;
	}

	public String getNomeSituacao() {
		return nomeSituacao;
	}

	public void setNomeSituacao(String nomeSituacao) {
		this.nomeSituacao = nomeSituacao;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	public String getNomeAluno() {
		return nomeAluno;
	}

	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getSituacaoAluno() {
		return situacaoAluno;
	}

	public void setSituacaoAluno(String situacaoAluno) {
		this.situacaoAluno = situacaoAluno;
	}

	public String getPeriodoDoc() {
		return periodoDoc;
	}

	public void setPeriodoDoc(String periodoDoc) {
		this.periodoDoc = periodoDoc;
	}

	public String getNomeDocumento() {
		return nomeDocumento;
	}

	public void setNomeDocumento(String nomeDocumento) {
		this.nomeDocumento = nomeDocumento;
	}

	public long getTotalVezesEmAnalise() {
		return totalVezesEmAnalise;
	}

	public void setTotalVezesEmAnalise(long totalVezesEmAnalise) {
		this.totalVezesEmAnalise = totalVezesEmAnalise;
	}

	public String getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(String statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	public String getObservacaoPendencia() {
		return observacaoPendencia;
	}

	public void setObservacaoPendencia(String observacaoPendencia) {
		this.observacaoPendencia = observacaoPendencia;
	}

	public long getTotalImagens() {
		return totalImagens;
	}

	public void setTotalImagens(long totalImagens) {
		this.totalImagens = totalImagens;
	}

	public Date getDataEnvioAnalise() {
		return dataEnvioAnalise;
	}

	public void setDataEnvioAnalise(Date dataEnvioAnalise) {
		this.dataEnvioAnalise = dataEnvioAnalise;
	}

	public Date getDataFimAnalise() {
		return dataFimAnalise;
	}

	public void setDataFimAnalise(Date dataFimAnalise) {
		this.dataFimAnalise = dataFimAnalise;
	}

	public List<String> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<String> documentos) {
		this.documentos = documentos;
	}

	public String getIrregularidade() {
		return irregularidade;
	}

	public void setIrregularidade(String irregularidade) {
		this.irregularidade = irregularidade;
	}

	public boolean isObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public Integer getVersaoAtual() {
		return versaoAtual;
	}

	public void setVersaoAtual(Integer versaoAtual) {
		this.versaoAtual = versaoAtual;
	}

	public Boolean getUsaTermo() {
		return usaTermo;
	}

	public void setUsaTermo(Boolean usaTermo) {
		this.usaTermo = usaTermo;
	}

	public Boolean getPastaAmarela() {
		return pastaAmarela;
	}

	public void setPastaAmarela(Boolean pastaAmarela) {
		this.pastaAmarela = pastaAmarela;
	}

	public String getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(String tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public String getSituacaoAnterior() {
		return situacaoAnterior;
	}

	public void setSituacaoAnterior(String situacaoAnterior) {
		this.situacaoAnterior = situacaoAnterior;
	}

	public Long getNumeroDePaginas() {
		return numeroDePaginas;
	}

	public void setNumeroDePaginas(Long numeroDePaginas) {
		this.numeroDePaginas = numeroDePaginas;
	}

	public Date getDataCriacaoProcesso() {
		return dataCriacaoProcesso;
	}

	public void setDataCriacaoProcesso(Date dataCriacaoProcesso) {
		this.dataCriacaoProcesso = dataCriacaoProcesso;
	}

	public String getLoginUsuario() {
		return loginUsuario;
	}

	public void setLoginUsuario(String loginUsuario) {
		this.loginUsuario = loginUsuario;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public Long getDocumentoId() {
		return documentoId;
	}

	public void setDocumentoId(Long documentoId) {
		this.documentoId = documentoId;
	}

	public String getAnalistaLogin() {
		return analistaLogin;
	}

	public void setAnalistaLogin(String analistaLogin) {
		this.analistaLogin = analistaLogin;
	}

	public String getAnalistaNome() {
		return analistaNome;
	}

	public void setAnalistaNome(String analistaNome) {
		this.analistaNome = analistaNome;
	}

	public String getNumeroMembro() {
		return numeroMembro;
	}

	public void setNumeroMembro(String numeroMembro) {
		this.numeroMembro = numeroMembro;
	}

	public String getCamposDinamicos() {
		return camposDinamicos;
	}

	public void setCamposDinamicos(String camposDinamicos) {
		this.camposDinamicos = camposDinamicos;
	}

	public Boolean getPastaVermelha() {
		return pastaVermelha;
	}

	public void setPastaVermelha(Boolean pastaVermelha) {
		this.pastaVermelha = pastaVermelha;
	}

	public String getNomeRegional() {
		return nomeRegional;
	}

	public void setNomeRegional(String nomeRegional) {
		this.nomeRegional = nomeRegional;
	}

	public Origem getOrigemDocumento() {
		return origemDocumento;
	}

	public void setOrigemDocumento(Origem origemDocumento) {
		this.origemDocumento = origemDocumento;
	}

}
