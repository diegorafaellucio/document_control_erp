package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.TipoProcesso;

public class TipoProcessoVO {

	private TipoProcesso tipoProcesso;
	private long countCampanhas;
	private long countCampos;
	private long countDocumentos;
	private long countSituacoes;

	public TipoProcessoVO() {
	}

	public TipoProcessoVO(TipoProcesso tipoProcesso, long countCampanhas, long countCampos, long countDocumentos, long countSituacoes) {
		this.tipoProcesso = tipoProcesso;
		this.countCampanhas = countCampanhas;
		this.countCampos = countCampos;
		this.countDocumentos = countDocumentos;
		this.countSituacoes = countSituacoes;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public long getCountCampanhas() {
		return countCampanhas;
	}

	public void setCountCampanhas(long countCampanhas) {
		this.countCampanhas = countCampanhas;
	}

	public long getCountCampos() {
		return countCampos;
	}

	public void setCountCampos(long countCampos) {
		this.countCampos = countCampos;
	}

	public long getCountDocumentos() {
		return countDocumentos;
	}

	public void setCountDocumentos(long countDocumentos) {
		this.countDocumentos = countDocumentos;
	}

	public long getCountSituacoes() {
		return countSituacoes;
	}

	public void setCountSituacoes(long countSituacoes) {
		this.countSituacoes = countSituacoes;
	}
}
