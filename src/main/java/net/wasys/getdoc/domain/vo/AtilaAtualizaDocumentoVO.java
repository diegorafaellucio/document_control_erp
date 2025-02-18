package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

import java.util.Date;

public class AtilaAtualizaDocumentoVO extends WebServiceClientVO {

	private String numeroCandidato;
	private Long documentoId;
	private String documentoNome;
	private String IrregularidadeNome;
	private String dataPendencia;
	private String horaPendencia;
	private String documentosJson;
	private String accessToken;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.ATILA_ATUALIZA_DOCUMENTO;
	}

	public String getNumeroCandidato() {
		return numeroCandidato;
	}

	public void setNumeroCandidato(String numeroCandidato) {
		this.numeroCandidato = numeroCandidato;
	}

	public Long getDocumentoId() {
		return documentoId;
	}

	public void setDocumentoId(Long documentoId) {
		this.documentoId = documentoId;
	}

	public String getDocumentoNome() {
		return documentoNome;
	}

	public void setDocumentoNome(String documentoNome) {
		this.documentoNome = documentoNome;
	}

	public String getIrregularidadeNome() {
		return IrregularidadeNome;
	}

	public void setIrregularidadeNome(String irregularidadeNome) {
		IrregularidadeNome = irregularidadeNome;
	}

	public String getDataPendencia() {
		return dataPendencia;
	}

	public void setDataPendencia(String dataPendencia) {
		this.dataPendencia = dataPendencia;
	}

	public String getHoraPendencia() {
		return horaPendencia;
	}

	public void setHoraPendencia(String horaPendencia) {
		this.horaPendencia = horaPendencia;
	}

	public String getDocumentosJson() {
		return documentosJson;
	}

	public void setDocumentosJson(String documentosJson) {
		this.documentosJson = documentosJson;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
