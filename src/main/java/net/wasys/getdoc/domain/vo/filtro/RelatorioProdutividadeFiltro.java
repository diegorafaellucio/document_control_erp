package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;

import java.util.Date;
import java.util.List;

public class RelatorioProdutividadeFiltro extends CamposFiltro {

	public enum Tipo {
		VISUALIZACAO_POR_MOTIVO,
		VISUALIZACAO_POR_ANALISTA,
		VISUALIZACAO_POR_ANALISTA_CSC,
		VISUALIZACAO_POR_SITUACAO,
		VISUALIZACAO_POR_ANALISTA_MEDICINA
	}

	private Tipo tipo;
	private Date dataInicio;
	private Date dataFim;
	private boolean agrupar;
	private List<Situacao> situacao;
	private TipoProcesso tipoProcesso;

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

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public boolean isAgrupar() { return agrupar;	}

	public void setAgrupar(boolean agrupar) { this.agrupar = agrupar; }

	public List<Situacao> getSituacao() {
		return situacao;
	}

	public void setSituacao(List<Situacao> situacao) {
		this.situacao = situacao;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
}
