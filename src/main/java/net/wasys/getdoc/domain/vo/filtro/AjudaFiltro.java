package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.TipoProcesso;

public class AjudaFiltro implements Cloneable {

	private TipoProcesso tipoProcesso;
	private Long tipoProcessoId;
	private boolean somenteNoInicial;

	public AjudaFiltro clone() {
		try {
			return (AjudaFiltro) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}
	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}
	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}
	public boolean isSomenteNoInicial() {
		return somenteNoInicial;
	}
	public void setSomenteNoInicial(boolean somenteNoInicial) {
		this.somenteNoInicial = somenteNoInicial;
	}

}
