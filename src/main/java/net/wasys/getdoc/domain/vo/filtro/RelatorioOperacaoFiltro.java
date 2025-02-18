package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;
import java.util.List;

public class RelatorioOperacaoFiltro {

	private Date dataInicio;
	private Date dataFim;
	private List<Long> tipoProcessoIds;
	private List<Long> subperfilIds;

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

	public List<Long> getTipoProcessoIds() {
		return tipoProcessoIds;
	}

	public void setTipoProcessoIds(List<Long> tipoProcessoIds) {
		this.tipoProcessoIds = tipoProcessoIds;
	}

	public List<Long> getSubperfilIds() {
		return subperfilIds;
	}

	public void setSubperfilIds(List<Long> subperfilIds) {
		this.subperfilIds = subperfilIds;
	}
}
