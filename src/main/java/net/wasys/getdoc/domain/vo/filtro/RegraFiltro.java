package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Situacao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RegraFiltro {

	private Long tipoProcessoId;
	private Long processoId;
	private Boolean ativa;
	private Date vigencia;
	private List<Boolean> statusList;
	private Situacao situacao;
	private String ordem;
	private Boolean apenasVigentes;

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public Date getVigencia() {
		return vigencia;
	}

	public void setVigencia(Date vigencia) {
		this.vigencia = vigencia;
	}

	public List<Boolean> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<Boolean> statusList) {
		this.statusList = statusList;
	}

	public Boolean[] getStatusArray() {
		return statusList != null ? statusList.toArray(new Boolean[statusList.size()]) : null;
	}

	public void setStatusArray(Boolean[] statusAttay) {
		if(statusAttay == null) {
			this.statusList = null;
		} else {
			this.statusList = Arrays.asList(statusAttay);
		}
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	public String getOrdem() {
		return ordem;
	}

	public void setOrdem(String ordem) {
		this.ordem = ordem;
	}

	public Boolean getApenasVigentes() {
		return apenasVigentes;
	}

	public void setApenasVigentes(Boolean apenasVigentes) {
		this.apenasVigentes = apenasVigentes;
	}
}
