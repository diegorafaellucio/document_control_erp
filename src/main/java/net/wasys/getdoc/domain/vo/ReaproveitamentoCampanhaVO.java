package net.wasys.getdoc.domain.vo;

import java.util.Date;

public class ReaproveitamentoCampanhaVO {

	private String descricao;
	private Date inicioVigencia;
	private Date fimVigencia;
	private String equivalencias;
	private String tipoDocumentoObrigatorioIds;
	private String cursos;
	private String instituicoes;
	private String campus;
	private Boolean padrao;

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getInicioVigencia() {
		return inicioVigencia;
	}

	public void setInicioVigencia(Date inicioVigencia) {
		this.inicioVigencia = inicioVigencia;
	}

	public Date getFimVigencia() {
		return fimVigencia;
	}

	public void setFimVigencia(Date fimVigencia) {
		this.fimVigencia = fimVigencia;
	}

	public String getEquivalencias() {
		return equivalencias;
	}

	public void setEquivalencias(String equivalencias) {
		this.equivalencias = equivalencias;
	}

	public String getTipoDocumentoObrigatorioIds() {
		return tipoDocumentoObrigatorioIds;
	}

	public void setTipoDocumentoObrigatorioIds(String tipoDocumentoObrigatorioIds) {
		this.tipoDocumentoObrigatorioIds = tipoDocumentoObrigatorioIds;
	}

	public String getCursos() {
		return cursos;
	}

	public void setCursos(String cursos) {
		this.cursos = cursos;
	}

	public String getInstituicoes() {
		return instituicoes;
	}

	public void setInstituicoes(String instituicoes) {
		this.instituicoes = instituicoes;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public Boolean getPadrao() {
		return padrao;
	}

	public void setPadrao(Boolean padrao) {
		this.padrao = padrao;
	}

	@Override public String toString() {
		return "ReaproveitamentoCampanhaVO{" +
				"descricao='" + descricao + '\'' +
				", inicioVigencia=" + inicioVigencia +
				", fimVigencia=" + fimVigencia +
				", equivalencias='" + equivalencias + '\'' +
				", tipoDocumentoObrigatorioIds='" + tipoDocumentoObrigatorioIds + '\'' +
				", cursos='" + cursos + '\'' +
				", instituicoes='" + instituicoes + '\'' +
				", campus='" + campus + '\'' +
				", padrao=" + padrao +
				'}';
	}
}
