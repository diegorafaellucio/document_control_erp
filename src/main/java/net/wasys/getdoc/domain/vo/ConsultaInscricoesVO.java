package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ConsultaInscricoesVO {

	public enum Perspectivas {
		INSCRITO,
		CANDIDATO,
		ALUNO
	}

	@JsonProperty("PERSPECTIVA")
	private String perspectiva;
	@JsonProperty("NUM_SEQ_INSCRICAO")
	private String numInscricao;
	@JsonProperty("NUM_SEQ_CANDIDATO")
	private String numCandidato;
	@JsonProperty("PERIODO_INGRESSO")
	private String periodoIngresso;
	@JsonProperty("COD_INSTITUICAO")
	private Long codInstituicao;
	@JsonProperty("NOM_INSTITUICAO")
	private String nomInstituicao;
	@JsonProperty("COD_CAMPUS")
	private Long codCampus;
	@JsonProperty("NOM_CAMPUS")
	private String nomCampus;
	@JsonProperty("COD_FORMA_INGRESSO")
	private Long codFormaIngresso;
	@JsonProperty("FORMA_INGRESSO")
	private String formaIngresso;
	@JsonProperty("NOME")
	private String nomCandidato;
	@JsonProperty("COD_CURSO")
	private Long codCurso;
	@JsonProperty("NOM_CURSO")
	private String nomCurso;
	@JsonProperty("COD_REGIONAL")
	private Long codRegional;
	@JsonProperty("NOM_REGIONAL")
	private String nomRegional;
	@JsonProperty("COD_MATRICULA")
	private String codMatricula;
	@JsonProperty("DDD_TELEFONE")
	private String dddTelefone;
	@JsonProperty("NUM_TELEFONE")
	private String numTelefone;
	@JsonProperty("DDD_CELULAR")
	private String dddCelular;
	@JsonProperty("NUM_CELULAR")
	private String numCelular;
	@JsonProperty("NUM_IDENTIDADE")
	private String numIdentidade;
	@JsonProperty("SGL_ORGAO_EMISSAO")
	private String sglOrgaoEmissao;
	@JsonProperty("SGL_UF_RG")
	private String sglUfRg;
	@JsonProperty("DT_EMISSAO_IDENT")
	private Date dtEmissaoIdent;
	@JsonProperty("CPF_CANDIDATO")
	private String cpf;
	@JsonProperty("END_EMAIL")
	private String email;
	@JsonProperty("DT_INSCRICAO")
	private Date dataInscricao;
	@JsonProperty("COD_TURNO")
	private Long codTurno;
	@JsonProperty("NOM_TURNO")
	private String nomTurno;
	@JsonProperty("COD_TIPO_CURSO")
	private Long codTipoCurso;
	@JsonProperty("NOM_TIPO_CURSO")
	private String nomTipoCurso;
	@JsonProperty("ESTABELECIMENTO_ORIGEM")
	private String codIesOrigem;
	@JsonProperty("NOM_ESTABELECIMENTO_ORIGEM")
	private String nomIesOrigem;
	@JsonProperty("COD_CURRICULO")
	private String codCurriculo;
	@JsonProperty("TXT_ID_PASSAPORTE")
	private String txtIdPassaporte;
	@JsonProperty("NOM_MAE")
	private String nomMae;
	@JsonProperty("NOM_PAI")
	private String nomPai;
	@JsonProperty("COD_AREA_CONHECIMENTO")
	private String codAreaConhecimento;
	@JsonProperty("NOM_AREA")
	private String nomArea;
	@JsonProperty("COD_SITUACAO_ALUNO")
	private Long codSituacaoAluno;
	@JsonProperty("NOM_SITUACAO_ALUNO")
	private String nomSituacaoAluno;
	private Date dataVinculoSia;

	public String getPerspectiva() {
		return perspectiva;
	}

	public void setPerspectiva(String perspectiva) {
		this.perspectiva = perspectiva;
	}

	public String getNumInscricao() {
		return numInscricao;
	}

	public String getNumCandidato() {
		return numCandidato;
	}

	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public Long getCodInstituicao() {
		return codInstituicao;
	}

	public String getNomInstituicao() {
		return nomInstituicao;
	}

	public Long getCodCampus() {
		return codCampus;
	}

	public String getNomCampus() {
		return nomCampus;
	}

	public Long getCodFormaIngresso() {
		return codFormaIngresso;
	}

	public String getFormaIngresso() {
		return formaIngresso;
	}

	public String getNomCandidato() {
		return nomCandidato;
	}

	public Long getCodCurso() {
		return codCurso;
	}

	public String getNomCurso() {
		return nomCurso;
	}

	public Long getCodRegional() {
		return codRegional;
	}

	public String getNomRegional() {
		return nomRegional;
	}

	public String getCodMatricula() {
		return codMatricula;
	}

	public String getDddTelefone() {
		return dddTelefone;
	}

	public String getNumTelefone() {
		return numTelefone;
	}

	public String getDddCelular() {
		return dddCelular;
	}

	public String getNumCelular() {
		return numCelular;
	}

	public String getNumIdentidade() {
		return numIdentidade;
	}

	public String getSglOrgaoEmissao() {
		return sglOrgaoEmissao;
	}

	public String getSglUfRg() {
		return sglUfRg;
	}

	public Date getDtEmissaoIdent() {
		return dtEmissaoIdent;
	}

	public String getCpf() {
		return cpf;
	}

	public String getEmail() {
		return email;
	}

	public Date getDataInscricao() {
		return dataInscricao;
	}

	public Long getCodTurno() {
		return codTurno;
	}

	public String getNomTurno() {
		return nomTurno;
	}

	public Long getCodTipoCurso() {
		return codTipoCurso;
	}

	public String getNomTipoCurso() {
		return nomTipoCurso;
	}

	public String getCodIesOrigem() {
		return codIesOrigem;
	}

	public String getNomIesOrigem() {
		return nomIesOrigem;
	}

	public String getCodCurriculo() {
		return codCurriculo;
	}

	public String getTxtIdPassaporte() {
		return txtIdPassaporte;
	}

	public String getNomMae() {
		return nomMae;
	}

	public String getNomPai() {
		return nomPai;
	}

	public String getCodAreaConhecimento() {
		return codAreaConhecimento;
	}

	public String getNomArea() {
		return nomArea;
	}

	public Long getCodSituacaoAluno() {
		return codSituacaoAluno;
	}

	public String getNomSituacaoAluno() {
		return nomSituacaoAluno;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}

	public void setPeriodoIngresso(String periodoIngresso) {
		this.periodoIngresso = periodoIngresso;
	}

	public void setCodInstituicao(Long codInstituicao) {
		this.codInstituicao = codInstituicao;
	}

	public void setNomInstituicao(String nomInstituicao) {
		this.nomInstituicao = nomInstituicao;
	}

	public void setCodCampus(Long codCampus) {
		this.codCampus = codCampus;
	}

	public void setNomCampus(String nomCampus) {
		this.nomCampus = nomCampus;
	}

	public void setCodFormaIngresso(Long codFormaIngresso) {
		this.codFormaIngresso = codFormaIngresso;
	}

	public void setFormaIngresso(String formaIngresso) {
		this.formaIngresso = formaIngresso;
	}

	public void setNomCandidato(String nomCandidato) {
		this.nomCandidato = nomCandidato;
	}

	public void setCodCurso(Long codCurso) {
		this.codCurso = codCurso;
	}

	public void setNomCurso(String nomCurso) {
		this.nomCurso = nomCurso;
	}

	public void setCodRegional(Long codRegional) {
		this.codRegional = codRegional;
	}

	public void setNomRegional(String nomRegional) {
		this.nomRegional = nomRegional;
	}

	public void setCodMatricula(String codMatricula) {
		this.codMatricula = codMatricula;
	}

	public void setDddTelefone(String dddTelefone) {
		this.dddTelefone = dddTelefone;
	}

	public void setNumTelefone(String numTelefone) {
		this.numTelefone = numTelefone;
	}

	public void setDddCelular(String dddCelular) {
		this.dddCelular = dddCelular;
	}

	public void setNumCelular(String numCelular) {
		this.numCelular = numCelular;
	}

	public void setNumIdentidade(String numIdentidade) {
		this.numIdentidade = numIdentidade;
	}

	public void setSglOrgaoEmissao(String sglOrgaoEmissao) {
		this.sglOrgaoEmissao = sglOrgaoEmissao;
	}

	public void setSglUfRg(String sglUfRg) {
		this.sglUfRg = sglUfRg;
	}

	public void setDtEmissaoIdent(Date dtEmissaoIdent) {
		this.dtEmissaoIdent = dtEmissaoIdent;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setDataInscricao(Date dataInscricao) {
		this.dataInscricao = dataInscricao;
	}

	public void setCodTurno(Long codTurno) {
		this.codTurno = codTurno;
	}

	public void setNomTurno(String nomTurno) {
		this.nomTurno = nomTurno;
	}

	public void setCodTipoCurso(Long codTipoCurso) {
		this.codTipoCurso = codTipoCurso;
	}

	public void setNomTipoCurso(String nomTipoCurso) {
		this.nomTipoCurso = nomTipoCurso;
	}

	public void setCodIesOrigem(String codIesOrigem) {
		this.codIesOrigem = codIesOrigem;
	}

	public void setNomIesOrigem(String nomIesOrigem) {
		this.nomIesOrigem = nomIesOrigem;
	}

	public void setCodCurriculo(String codCurriculo) {
		this.codCurriculo = codCurriculo;
	}

	public void setTxtIdPassaporte(String txtIdPassaporte) {
		this.txtIdPassaporte = txtIdPassaporte;
	}

	public void setNomMae(String nomMae) {
		this.nomMae = nomMae;
	}

	public void setNomPai(String nomPai) {
		this.nomPai = nomPai;
	}

	public void setCodAreaConhecimento(String codAreaConhecimento) {
		this.codAreaConhecimento = codAreaConhecimento;
	}

	public void setNomArea(String nomArea) {
		this.nomArea = nomArea;
	}

	public void setCodSituacaoAluno(Long codSituacaoAluno) {
		this.codSituacaoAluno = codSituacaoAluno;
	}

	public void setNomSituacaoAluno(String nomSituacaoAluno) {
		this.nomSituacaoAluno = nomSituacaoAluno;
	}

	public Date getDataVinculoSia() {
		return dataVinculoSia;
	}

	public void setDataVinculoSia(Date dataVinculoSia) {
		this.dataVinculoSia = dataVinculoSia;
	}
}
