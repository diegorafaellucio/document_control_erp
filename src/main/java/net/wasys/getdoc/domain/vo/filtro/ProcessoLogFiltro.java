package net.wasys.getdoc.domain.vo.filtro;

import java.util.Arrays;
import java.util.List;

import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;

public class ProcessoLogFiltro extends ProcessoFiltro {

	private List<AcaoProcesso> acaoList;
	private List<Long> tipoEvidencia;
	private Long processoId;
	private List<Long> situacaoAnteriorId;
	private boolean filtrarRoles = true;
	private Boolean apenasComSituacao;
	private Boolean apenasComEtapa;

	public List<AcaoProcesso> getAcaoList() {
		return acaoList;
	}

	public void setAcaoList(List<AcaoProcesso> acaoList) {
		this.acaoList = acaoList;
	}

	public AcaoProcesso[] getAcaoArray() {
		return acaoList != null ? acaoList.toArray(new AcaoProcesso[acaoList.size()]) : null;
	}

	public void setAcaoArray(AcaoProcesso[] acaoArray) {
		if(acaoArray == null) {
			this.acaoList = null;
		} else {
			this.acaoList = Arrays.asList(acaoArray);
		}
	}

	public List<Long> getTipoEvidencia() {
		return tipoEvidencia;
	}

	public void setTipoEvidencia(List<Long> tipoEvidencia) {
		this.tipoEvidencia = tipoEvidencia;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public List<Long> getSituacaoAnteriorId() {
		return situacaoAnteriorId;
	}

	public void setSituacaoAnteriorId(List<Long> situacaoAnteriorId) {
		this.situacaoAnteriorId = situacaoAnteriorId;
	}

	public void setFiltrarRoles(boolean filtrarRoles) {
		this.filtrarRoles = filtrarRoles;
	}

	public boolean getFiltrarRoles() {
		return filtrarRoles;
	}

	public Boolean isApenasComSituacao() {
		return apenasComSituacao;
	}

	public void setApenasComSituacao(Boolean apenasComSituacao) {
		this.apenasComSituacao = apenasComSituacao;
	}

	public Boolean getApenasComEtapa() {
		return apenasComEtapa;
	}

	public void setApenasComEtapa(Boolean apenasComEtapa) {
		this.apenasComEtapa = apenasComEtapa;
	}

	@Override public String toString() {
		return "ProcessoLogFiltro{" +
				"acaoList=" + acaoList +
				", tipoEvidencia=" + tipoEvidencia +
				", processoId=" + processoId +
				", situacaoAnteriorId=" + situacaoAnteriorId +
				", filtrarRoles=" + filtrarRoles +
				", apenasComSituacao=" + apenasComSituacao +
				", apenasComEtapa=" + apenasComEtapa +
				'}';
	}
}
