package net.wasys.getdoc.rest.request.vo;

import java.util.List;

public class IniciarCriacaoPastaVermelhaRequestVO {

	private List<String> statusDocumentos;
	private List<String> statusProcessos;
	private String periodo;
	private Boolean ignorarFiesProuniConcluido;

	public IniciarCriacaoPastaVermelhaRequestVO() {
	}

	public IniciarCriacaoPastaVermelhaRequestVO(String periodo) {
		this.periodo = periodo;
	}

	public List<String> getStatusDocumentos() {
		return statusDocumentos;
	}

	public void setStatusDocumentos(List<String> statusDocumentos) {
		this.statusDocumentos = statusDocumentos;
	}

	public List<String> getStatusProcessos() {
		return statusProcessos;
	}

	public void setStatusProcessos(List<String> statusProcessos) {
		this.statusProcessos = statusProcessos;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public Boolean getIgnorarFiesProuniConcluido() {
		return ignorarFiesProuniConcluido;
	}

	public void setIgnorarFiesProuniConcluido(Boolean ignorarFiesProuniConcluido) {
		this.ignorarFiesProuniConcluido = ignorarFiesProuniConcluido;
	}

	@Override public String toString() {
		return "BaixarDadosPastaVermelhaRequestVO{" +
				"statusDocumentos=" + statusDocumentos +
				", statusProcessos=" + statusProcessos +
				", periodo='" + periodo + '\'' +
				", ignorarFiesProuniConcluido=" + ignorarFiesProuniConcluido +
				'}';
	}
}
