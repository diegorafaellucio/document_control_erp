package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.ListaChamada;
import net.wasys.getdoc.domain.enumeration.TipoCalendario;

import java.util.Date;

public class CalendarioCriterioFiltro {

	private Date dataInicio;
	private Date dataFim;
	private boolean ativo;
	private Long calendarioId;
	private ListaChamada chamada;
	private TipoCalendario tipoCalendario;

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Long getCalendarioId() {
		return calendarioId;
	}

	public void setCalendarioId(Long calendarioId) {
		this.calendarioId = calendarioId;
	}

	public ListaChamada getChamada() {
		return chamada;
	}

	public void setChamada(ListaChamada chamada) {
		this.chamada = chamada;
	}

	public TipoCalendario getTipoCalendario() {
		return tipoCalendario;
	}

	public void setTipoCalendario(TipoCalendario tipoCalendario) {
		this.tipoCalendario = tipoCalendario;
	}

	public void setDataAtual(){
		this.dataInicio = new Date();
		this.dataFim = new Date();
	}
}
