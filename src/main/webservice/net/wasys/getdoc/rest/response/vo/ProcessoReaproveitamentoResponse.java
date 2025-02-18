package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPasta;

@ApiModel(value = "ProcessoReaproveitamentoResponse")
public class ProcessoReaproveitamentoResponse {

	@ApiModelProperty(notes = "ID do processo do aluno no GetDoc Captação")
	private Long processoCaptacaoId;

	@ApiModelProperty(notes = "Status do Processo no GetDoc Captação")
	private StatusProcesso statusProcesso;

	@ApiModelProperty(notes = "Processo Prouni Ou Fies no GetDoc Captação?")
	private Boolean prouniOuFies;

	@ApiModelProperty(notes = "Situação de Pasta Vermelha no processo GetDoc Captação")
	private TipoPasta tipoPasta;

	public Long getProcessoCaptacaoId() {
		return processoCaptacaoId;
	}

	public void setProcessoCaptacaoId(Long processoCaptacaoId) {
		this.processoCaptacaoId = processoCaptacaoId;
	}

	public StatusProcesso getStatusProcesso() {
		return statusProcesso;
	}

	public void setStatusProcesso(StatusProcesso statusProcesso) {
		this.statusProcesso = statusProcesso;
	}

	public Boolean getProuniOuFies() {
		return prouniOuFies;
	}

	public void setProuniOuFies(Boolean prouniOuFies) {
		this.prouniOuFies = prouniOuFies;
	}

	public TipoPasta getTipoPasta() {
		return tipoPasta;
	}

	public void setTipoPasta(TipoPasta tipoPasta) {
		this.tipoPasta = tipoPasta;
	}
}
