package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;

import java.util.Date;
import java.util.List;

public class LogAnaliseIAFiltro {

	public enum ConsiderarData {
		CRIACAO,
		TIPIFICACAO
	}

	private Date dataInicio;
	private Date dataFim;
	private ConsiderarData considerarData;
	private boolean tipificado;
	private boolean ocr;
	private List<TipoDocumento> tipoDocumentoList;
	private List<Long> tipoDocumentoIdList;
	private List<StatusDocumento> statusDocumentoList;

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

	public ConsiderarData getConsiderarData() {
		return considerarData;
	}

	public void setConsiderarData(ConsiderarData considerarData) {
		this.considerarData = considerarData;
	}

	public boolean isTipificado() {
		return tipificado;
	}

	public void setTipificado(boolean tipificado) {
		this.tipificado = tipificado;
	}

	public boolean isOcr() {
		return ocr;
	}

	public void setOcr(boolean ocr) {
		this.ocr = ocr;
	}

	public List<TipoDocumento> getTipoDocumentoList() {
		return tipoDocumentoList;
	}

	public void setTipoDocumentoList(List<TipoDocumento> tipoDocumentoList) {
		this.tipoDocumentoList = tipoDocumentoList;
	}

	public List<Long> getTipoDocumentoIdList() {
		return tipoDocumentoIdList;
	}

	public void setTipoDocumentoIdList(List<Long> tipoDocumentoIdList) {
		this.tipoDocumentoIdList = tipoDocumentoIdList;
	}

	public List<StatusDocumento> getStatusDocumentoList() {
		return statusDocumentoList;
	}

	public void setStatusDocumentoList(List<StatusDocumento> statusDocumentoList) {
		this.statusDocumentoList = statusDocumentoList;
	}
}
