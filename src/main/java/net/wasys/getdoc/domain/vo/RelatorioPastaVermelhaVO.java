package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;

public class RelatorioPastaVermelhaVO {

	private Long processoId;
	private String nomeSituacao;
	private Long tipoProcessoId;
	private String nomeTipoProcesso;
	private String dataCriacao;
	private String dataEnvioAnalise;
	private Boolean usaTermo;
	private String formaDeIngresso;
	private String periodoDeIngresso;
	private String regional;
	private String codInstituicao;
	private String instituicao;
	private String codCampus;
	private String campus;
	private String poloParceiro;
	private String codCurso;
	private String curso;
	private String cpf;
	private String matricula;
	private String numeroCandidato;
	private String NumeroInscricao;
	private String nomeAluno;
	private String telefone;
	private String celular;
	private String email;
	private Long codDocumento;
	private String nomeDocumento;
	private StatusDocumento statusDocumento;
	private String dataDigitalizacao;
	private String irregularidade;
	private String indicacaoFies;
	private String indicacaoProuni;
	private String situacaoAluno;

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public String getNomeSituacao() {
		return nomeSituacao;
	}

	public void setNomeSituacao(String nomeSituacao) {
		this.nomeSituacao = nomeSituacao;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public String getNomeTipoProcesso() {
		return nomeTipoProcesso;
	}

	public void setNomeTipoProcesso(String nomeTipoProcesso) {
		this.nomeTipoProcesso = nomeTipoProcesso;
	}

	public String getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(String dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public String getDataEnvioAnalise() {
		return dataEnvioAnalise;
	}

	public void setDataEnvioAnalise(String dataEnvioAnalise) {
		this.dataEnvioAnalise = dataEnvioAnalise;
	}

	public String getFormaDeIngresso() {
		return formaDeIngresso;
	}

	public void setFormaDeIngresso(String formaDeIngresso) {
		this.formaDeIngresso = formaDeIngresso;
	}

	public String getPeriodoDeIngresso() {
		return periodoDeIngresso;
	}

	public void setPeriodoDeIngresso(String periodoDeIngresso) {
		this.periodoDeIngresso = periodoDeIngresso;
	}

	public String getRegional() {
		return regional;
	}

	public void setRegional(String regional) {
		this.regional = regional;
	}

	public String getCodInstituicao() {
		return codInstituicao;
	}

	public void setCodInstituicao(String codInstituicao) {
		this.codInstituicao = codInstituicao;
	}

	public String getInstituicao() {
		return instituicao;
	}

	public void setInstituicao(String instituicao) {
		this.instituicao = instituicao;
	}

	public String getCodCampus() {
		return codCampus;
	}

	public void setCodCampus(String codCampus) {
		this.codCampus = codCampus;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public String getCodCurso() {
		return codCurso;
	}

	public void setCodCurso(String codCurso) {
		this.codCurso = codCurso;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public String getNumeroCandidato() {
		return numeroCandidato;
	}

	public void setNumeroCandidato(String numeroCandidato) {
		this.numeroCandidato = numeroCandidato;
	}

	public String getNumeroInscricao() {
		return NumeroInscricao;
	}

	public void setNumeroInscricao(String numeroInscricao) {
		NumeroInscricao = numeroInscricao;
	}

	public String getNomeAluno() {
		return nomeAluno;
	}

	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getCelular() {
		return celular;
	}

	public void setCelular(String celular) {
		this.celular = celular;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getCodDocumento() {
		return codDocumento;
	}

	public void setCodDocumento(Long codDocumento) {
		this.codDocumento = codDocumento;
	}

	public String getNomeDocumento() {
		return nomeDocumento;
	}

	public void setNomeDocumento(String nomeDocumento) {
		this.nomeDocumento = nomeDocumento;
	}

	public StatusDocumento getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(StatusDocumento statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	public String getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public void setDataDigitalizacao(String dataDigitalizacao) {
		this.dataDigitalizacao = dataDigitalizacao;
	}

	public String getPoloParceiro() {
		return poloParceiro;
	}

	public void setPoloParceiro(String poloParceiro) {
		this.poloParceiro = poloParceiro;
	}

	public Boolean getUsaTermo() {
		return usaTermo;
	}

	public void setUsaTermo(Boolean usaTermo) {
		this.usaTermo = usaTermo;
	}

	public String getIrregularidade() {
		return irregularidade;
	}

	public void setIrregularidade(String irregularidade) {
		this.irregularidade = irregularidade;
	}

	public String getIndicacaoFies() {
		return tipoProcessoId.equals(TipoProcesso.SIS_FIES) || tipoProcessoId.equals(TipoProcesso.TE_FIES) ? "Sim" : "Não";
	}

	public void setIndicacaoFies(String indicacaoFies) {
		this.indicacaoFies = indicacaoFies;
	}

	public String getIndicacaoProuni() {
		return tipoProcessoId.equals(TipoProcesso.SIS_PROUNI) || tipoProcessoId.equals(TipoProcesso.TE_PROUNI) ? "Sim" : "Não";
	}

	public void setIndicacaoProuni(String indicacaoProuni) {
		this.indicacaoProuni = indicacaoProuni;
	}

	public String getSituacaoAluno() {
		return situacaoAluno;
	}

	public void setSituacaoAluno(String situacaoAluno) {
		this.situacaoAluno = situacaoAluno;
	}
}
