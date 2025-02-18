package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.StatusAtendimento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogAtendimentoFiltro {

	public enum Tipo {
		ANALITICO,
		SINTETICO
	}

	private List<Long> usuarioIdList = new ArrayList<>();
	private List<Long> statusLaboralIdList = new ArrayList<>();
	private StatusAtendimento statusAtendimento;
	private Date inicio;
	private Date fim;
	private Boolean emAndamento;
	private Tipo tipo;

	public List<Long> getStatusLaboralIdList() {
		return statusLaboralIdList;
	}

	public void setStatusLaboralIdList(List<Long> statusLaboralIdList) {
		this.statusLaboralIdList = statusLaboralIdList;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	public StatusAtendimento getStatusAtendimento() {
		return statusAtendimento;
	}

	public void setStatusAtendimento(StatusAtendimento statusAtendimento) {
		this.statusAtendimento = statusAtendimento;
	}

	public List<Long> getUsuarioIdList() {
		return usuarioIdList;
	}

	public void setUsuarioIdList(List<Long> usuarioIdList) {
		this.usuarioIdList = usuarioIdList;
	}

	public Boolean getEmAndamento() {
		return emAndamento;
	}

	public void setEmAndamento(Boolean emAndamento) {
		this.emAndamento = emAndamento;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}
}
