package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.TipoParceiro;
import net.wasys.getdoc.domain.enumeration.TipoProuni;

import java.util.Date;

public class CalendarioFiltro {

	private Date dataInicio;
	private Date dataFim;
	private Long tipoProcessoId;
	private TipoParceiro tipoParceiro;
	private TipoProuni tipoProuni;
	private String periodoIngresso;
	private boolean ativo;

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

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public TipoParceiro getTipoParceiro() {
		return tipoParceiro;
	}

	public void setTipoParceiro(TipoParceiro tipoParceiro) {
		this.tipoParceiro = tipoParceiro;
	}

	public TipoProuni getTipoProuni() {
		return tipoProuni;
	}

	public void setTipoProuni(TipoProuni tipoProuni) {
		this.tipoProuni = tipoProuni;
	}

	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public void setPeriodoIngresso(String periodoIngresso) {
		this.periodoIngresso = periodoIngresso;
	}

	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public void setDataAtual(){
		this.dataInicio = new Date();
		this.dataFim = new Date();
	}
}
