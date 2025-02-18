package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;

import java.util.List;

public class RelatorioPastaVermelhaFiltro {

	private List<StatusDocumento> statusDocumentos;
	private List<StatusProcesso> statusProcessos;
	private String periodoIngresso;
	private Boolean ignorarFiesProuniConcluido;

	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public void setPeriodoIngresso(String periodoIngresso) {
		this.periodoIngresso = periodoIngresso;
	}

	public Boolean getIgnorarFiesProuniConcluido() {
		return ignorarFiesProuniConcluido;
	}

	public void setIgnorarFiesProuniConcluido(Boolean ignorarFiesProuniConcluido) {
		this.ignorarFiesProuniConcluido = ignorarFiesProuniConcluido;
	}

	public List<StatusDocumento> getStatusDocumentos() {
		return statusDocumentos;
	}

	public void setStatusDocumentos(List<StatusDocumento> statusDocumentos) {
		this.statusDocumentos = statusDocumentos;
	}

	public List<StatusProcesso> getStatusProcessos() {
		return statusProcessos;
	}

	public void setStatusProcessos(List<StatusProcesso> statusProcessos) {
		this.statusProcessos = statusProcessos;
	}
}
