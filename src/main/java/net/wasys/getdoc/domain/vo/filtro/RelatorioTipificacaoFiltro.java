package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoTipificacao;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;

public class RelatorioTipificacaoFiltro {

	private ConsiderarData considerarData;

	public enum ConsiderarData {
		CRIACAO,
		ATUALIZACAO
	}

	private Date dataInicio;
	private Date dataFim;
	private List<TipoProcesso> tiposProcessoList;
	private List<TipoDocumento> tiposDocumento;
	private List<StatusProcesso> statusList;
	private TipoTipificacao tipoTipificacao;
	private List<ModeloDocumento> modeloDocumentoList;
	private List<TipoProcesso> tipoProcessoList;

	public ConsiderarData getConsiderarData() {
		return considerarData;
	}

	public void setConsiderarData(ConsiderarData considerarData) {
		this.considerarData = considerarData;
	}

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

	public List<TipoProcesso> getTiposProcessoList() {
		return tiposProcessoList;
	}

	public void setTiposProcessoList(List<TipoProcesso> tiposProcessoList) {
		this.tiposProcessoList = tiposProcessoList;
	}

	public List<TipoDocumento> getTiposDocumento() {
		return tiposDocumento;
	}

	public void setTiposDocumento(List<TipoDocumento> tiposDocumento) {
		this.tiposDocumento = tiposDocumento;
	}

	public List<StatusProcesso> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<StatusProcesso> statusList) {
		this.statusList = statusList;
	}

	public TipoTipificacao getTipoTipificacao() {
		return tipoTipificacao;
	}

	public void setTipoTipificacao(TipoTipificacao tipoTipificacao) {
		this.tipoTipificacao = tipoTipificacao;
	}

	public List<ModeloDocumento> getModeloDocumentoList() {
		return modeloDocumentoList;
	}

	public void setModeloDocumentoList(List<ModeloDocumento> modeloDocumentoList) {
		this.modeloDocumentoList = modeloDocumentoList;
	}

	public List<TipoProcesso> getTipoProcessoList() {
		return tipoProcessoList;
	}

	public void setTipoProcessoList(List<TipoProcesso> tipoProcessoList) {
		this.tipoProcessoList = tipoProcessoList;
	}
}