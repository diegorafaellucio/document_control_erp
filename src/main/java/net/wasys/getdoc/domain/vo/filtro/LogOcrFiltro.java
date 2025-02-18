package net.wasys.getdoc.domain.vo.filtro;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.StatusLogOcr;

public class LogOcrFiltro implements Cloneable {

	public enum ConsiderarData {
		CRIACAO_PROCESSO,
		CRIACAO_LOG
	}

	private Date dataInicio;
	private Date dataFim;
	private String tipoRelatorio;
	private ConsiderarData considerarData;
	private Integer numeroFalhas;
	private List<String> documentos;
	private Boolean preparada;
	private List<StatusLogOcr> statusList;

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

	public List<StatusLogOcr> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<StatusLogOcr> statusList) {
		this.statusList = statusList;
	}

	public StatusLogOcr[] getStatusArray() {
		return statusList != null ? statusList.toArray(new StatusLogOcr[statusList.size()]) : null;
	}

	public void setStatusArray(StatusLogOcr[] statusAttay) {
		if(statusAttay == null) {
			this.statusList = null;
		} else {
			this.statusList = Arrays.asList(statusAttay);
		}
	}

	public void setStatus(StatusLogOcr... statusAttay) {
		setStatusArray(statusAttay);
	}

	public String getTipoRelatorio() {
		return tipoRelatorio;
	}

	public void setTipoRelatorio(String tipoRelatorio) {
		this.tipoRelatorio = tipoRelatorio;
	}

	public ConsiderarData getConsiderarData() {
		return considerarData;
	}

	public void setConsiderarData(ConsiderarData considerarData) {
		this.considerarData = considerarData;
	}

	public List<String> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<String> documentos) {
		this.documentos = documentos;
	}

	public Integer getNumeroFalhas() {
		return numeroFalhas;
	}

	public void setNumeroFalhas(Integer numeroFalhas) {
		this.numeroFalhas = numeroFalhas;
	}

	public Boolean getPreparada() {
		return preparada;
	}

	public void setPreparada(Boolean preparada) {
		this.preparada = preparada;
	}
}
