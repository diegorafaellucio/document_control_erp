package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;

import java.util.Date;
import java.util.List;

public class ImagemMetaFiltro {

	private Date dataInicio;
	private Date dataFim;
	private List<TipoProcesso> tipoProcessoList;
	private List<ModeloDocumento> modeloDocumentoList;
	private Boolean tipificado;
	private Origem origemProcesso;
	private Long documentoId;

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


	public List<TipoProcesso> getTipoProcessoList() {
		return tipoProcessoList;
	}

	public void setTipoProcessoList(List<TipoProcesso> tipoProcessoList) {
		this.tipoProcessoList = tipoProcessoList;
	}

	public List<ModeloDocumento> getModeloDocumentoList() {
		return modeloDocumentoList;
	}

	public void setModeloDocumentoList(List<ModeloDocumento> modeloDocumentoList) {
		this.modeloDocumentoList = modeloDocumentoList;
	}

	public Boolean getTipificado() {
		return tipificado;
	}

	public void setTipificado(Boolean tipificado) {
		this.tipificado = tipificado;
	}

	public Origem getOrigemProcesso() {
		return origemProcesso;
	}

	public void setOrigemProcesso(Origem origemProcesso) {
		this.origemProcesso = origemProcesso;
	}

	public void setDocumentoId(Long documentoId) {
		this.documentoId = documentoId;
	}

	public Long getDocumentoId() {
		return documentoId;
	}

	@Override public String toString() {
		return "ImagemMetaFiltro{" +
				"dataInicio=" + dataInicio +
				", dataFim=" + dataFim +
				", tipoProcessoList=" + tipoProcessoList +
				", modeloDocumentoList=" + modeloDocumentoList +
				", tipificado=" + tipificado +
				'}';
	}
}