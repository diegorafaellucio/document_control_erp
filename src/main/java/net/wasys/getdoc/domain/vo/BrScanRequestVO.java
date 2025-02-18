package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class BrScanRequestVO extends WebServiceClientVO {

	public static final String DOCUMENTO = "DOCUMENTO";
	public static final String CPF = "CPF";
	public static final String SELFIE = "SELFIE";

	private String documentoIdentificacao;
	private String selfie;
	private String cpf;

	public String getDocumentoIdentificacao() {
		return documentoIdentificacao;
	}

	public void setDocumentoIdentificacao(String documentoIdentificacao) {
		this.documentoIdentificacao = documentoIdentificacao;
	}

	public String getSelfie() {
		return selfie;
	}

	public void setSelfie(String selfie) {
		this.selfie = selfie;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.BRSCAN;
	}
}
