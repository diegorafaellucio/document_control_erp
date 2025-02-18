package net.wasys.getdoc.bean.vo;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.ConsultaInscricoesVO;

public class ProcessoSiaVO {

	private Long id;
	private TipoProcesso tipoProcesso;
	private Processo processo;
	private ConsultaInscricoesVO vo;
	private StatusProcesso status;

	public ProcessoSiaVO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public ConsultaInscricoesVO getVo() {
		return vo;
	}

	public void setVo(ConsultaInscricoesVO vo) {
		this.vo = vo;
	}

	public StatusProcesso getStatus() {
		return status;
	}

	public void setStatus(StatusProcesso status) {
		this.status = status;
	}
}
