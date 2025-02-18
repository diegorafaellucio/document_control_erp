package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import net.wasys.getdoc.domain.enumeration.StatusGeracaoPastaVermelha;

@ApiModel(value = "DadosPastaVermelhaResponse")
public class DadosPastaVermelhaResponse {

	private StatusGeracaoPastaVermelha statusGeracaoPastaVermelha;
	private DownloadAnexoResponse downloadAnexoResponse;

	public StatusGeracaoPastaVermelha getStatusGeracaoPastaVermelha() {
		return statusGeracaoPastaVermelha;
	}

	public void setStatusGeracaoPastaVermelha(StatusGeracaoPastaVermelha statusGeracaoPastaVermelha) {
		this.statusGeracaoPastaVermelha = statusGeracaoPastaVermelha;
	}

	public DownloadAnexoResponse getDownloadAnexoResponse() {
		return downloadAnexoResponse;
	}

	public void setDownloadAnexoResponse(DownloadAnexoResponse downloadAnexoResponse) {
		this.downloadAnexoResponse = downloadAnexoResponse;
	}
}
