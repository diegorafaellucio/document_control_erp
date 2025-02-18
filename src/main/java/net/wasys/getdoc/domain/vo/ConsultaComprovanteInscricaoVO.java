package net.wasys.getdoc.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsultaComprovanteInscricaoVO {

	@JsonProperty("COD_CAMPUS")
	private Long numCampus;
	@JsonProperty("CPF_CANDIDATO")
	private String cpfCanditato;
	@JsonProperty("NOM_FANTASIA")
	private String periodoIngresso;
	@JsonProperty("NOM_TURNO")
	private String nomTurno;
	@JsonProperty("NOM_TIPO_CURSO")
	private String nomTipoCurso;
	@JsonProperty("COD_CURSO")
	private String numCurso;
	@JsonProperty("NOM_CANDIDATO")
	private String nomCandidato;
	@JsonProperty("COD_INSTITUICAO")
	private Long codInstituicao;


	public Long getNumCampus() {
		return numCampus;
	}

	public String getCpfCanditato() {
		return cpfCanditato;
	}

	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public String getNomTurno() {
		return nomTurno;
	}

	public String getNomTipoCurso() {
		return nomTipoCurso;
	}

	public String getNumCurso() {
		return numCurso;
	}

	public String getNomCadidato() {
		return nomCandidato;
	}

	public Long getCodInstituicao() {
		return codInstituicao;
	}
}
