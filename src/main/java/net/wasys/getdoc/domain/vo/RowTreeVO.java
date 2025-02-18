package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.Documento;

import java.util.Date;

public class RowTreeVO {

	private String classe;
	private Documento documento;
	private BaseRegistro baseRegistro;
	private Integer qtdDocumentos;
	private Date dataValidade;

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public BaseRegistro getBaseRegistro() {
		return baseRegistro;
	}

	public void setBaseRegistro(BaseRegistro baseRegistro) {
		this.baseRegistro = baseRegistro;
	}

	public Integer getQtdDocumentos() {
		return qtdDocumentos;
	}

	public void setQtdDocumentos(Integer qtdDocumentos) {
		this.qtdDocumentos = qtdDocumentos;
	}

	public Date getDataValidade() {
		return dataValidade;
	}

	public void setDataValidade(Date dataValidade) {
		this.dataValidade = dataValidade;
	}
}
