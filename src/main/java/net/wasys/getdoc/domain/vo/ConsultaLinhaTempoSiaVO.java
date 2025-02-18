package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsultaLinhaTempoSiaVO {

	@JsonProperty("NSEQ_INSCRICAO")
	private String numInscricao;
	@JsonProperty("NSEQ_CANDIDATO")
	private String numCandidato;
	@JsonProperty("NOM_CANDIDATO")
	private String nomCadidato;
	@JsonProperty("NSEQ_ALUNO_CURSO")
	private Long numAlunoCurso;
	@JsonProperty("COD_MATRICULA")
	private String numMatricula;
	@JsonProperty("COD_CURSO")
	private Long numCurso;
	@JsonProperty("COD_INSTITUICAO")
	private Long numInstituicao;
	@JsonProperty("END_EMAIL")
	private String email;
	@JsonProperty("COD_FORMA_INGRESSO")
	private Long numFormaIngresso;
	@JsonProperty("NOM_MAE")
	private String nomeMae;
	@JsonProperty("NOM_PAI")
	private String nomePai;
	@JsonProperty("NUM_IDENTIDADE")
	private String numIdentidade;
	@JsonProperty("COD_MUNICIPIO_NATURAL")
	private Long numMunicipioNatural;
	@JsonProperty("SGL_UF_RG")
	private String ufRG;
	@JsonProperty("SGL_ORGAO_EMISSAO")
	private String orgaoEmissor;
	@JsonProperty("DT_EMISSAO_IDENT")
	private String dataEmissao;
	@JsonProperty("TXT_ID_PASSAPORTE")
	private String idPassaporte;
	@JsonProperty("NUM_DDD_TEL")
	private String numDDDTel;
	@JsonProperty("NUM_TELEFONE")
	private String numTelefone;

	public String getNumInscricao() {
		return numInscricao;
	}

	public String getNumCandidato() {
		return numCandidato;
	}

	public String getNomCadidato() {
		return nomCadidato;
	}

	public Long getNumAlunoCurso() {
		return numAlunoCurso;
	}

	public String getNumMatricula() {
		return numMatricula;
	}

	public Long getNumCurso() {
		return numCurso;
	}

	public Long getNumInstituicao() {
		return numInstituicao;
	}

	public String getEmail() {
		return email;
	}

	public Long getNumFormaIngresso() {
		return numFormaIngresso;
	}

	public String getNomeMae() {
		return nomeMae;
	}

	public String getNomePai() {
		return nomePai;
	}

	public String getNumIdentidade() {
		return numIdentidade;
	}

	public Long getNumMunicipioNatural() {
		return numMunicipioNatural;
	}

	public String getUfRG() {
		return ufRG;
	}

	public String getOrgaoEmissor() {
		return orgaoEmissor;
	}

	public String getDataEmissao() {
		return dataEmissao;
	}

	public String getIdPassaporte() {
		return idPassaporte;
	}

	public String getNumDDDTel() {
		return numDDDTel;
	}

	public String getNumTelefone() {
		return numTelefone;
	}
}
