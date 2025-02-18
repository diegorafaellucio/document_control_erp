package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.ProcessoService;

public class RelatorioAcompanhamentoVO {

	private ProcessoService processoService;
	private StatusProcesso status;
	private Usuario analista;
	private TipoProcesso tipoProcesso;
	private Area area;

	private String nomeLinha;
	private int total;
	private int atrasados;
	private int alertas;
	private int ok;
	private int atrasadosEmAnalise;
	private int alertasEmAnalise;
	private int okEmAnalise;
	private int atrasadosEmAcompanhamento;
	private int alertasEmAcompanhamento;
	private int okEmAcompanhamento;
	private Situacao situacao;
	private int totalEmAnalise;
	private int totalEmAcompanhamento;
	private String tempoMedio;
	private Integer hoje;

	public void add(Processo processo) {

		total++;

		status = processo.getStatus();
		StatusPrazo statusPrazo = null;

		if (situacao != null) {
			statusPrazo = processoService.getStatusPrazoSituacao(processo);
		} else {
			StatusProcesso processoStatus = processo.getStatus();
			if(StatusProcesso.CONCLUIDO.equals(processoStatus) || StatusProcesso.EM_ANALISE.equals(processoStatus)) {
				statusPrazo = processoService.getStatusPrazoAnalise(processo);
			} else {
				statusPrazo = processoService.getStatusPrazo(processo);
			}
		}
		if(StatusPrazo.ADVERTIR.equals(statusPrazo)) {
			alertas++;
			StatusProcesso status = processo.getStatus();
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				alertasEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				alertasEmAcompanhamento++;
			}
		} else if(StatusPrazo.ALERTAR.equals(statusPrazo)) {
			atrasados++;
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				atrasadosEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				atrasadosEmAcompanhamento++;
			}
		} else {
			ok++;
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				okEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				okEmAcompanhamento++;
			}
		}
		if(StatusProcesso.EM_ANALISE.equals(status)) {
			totalEmAnalise++;
		} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
			totalEmAcompanhamento++;
		}
	}

	public void add(RelatorioGeralSituacao processo) {

		total++;

		StatusPrazo statusPrazo = processo.getStatusPrazoSituacao();

		if(StatusPrazo.ADVERTIR.equals(statusPrazo)) {
			alertas++;
			StatusProcesso status = processo.getSituacao().getStatus();
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				alertasEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				alertasEmAcompanhamento++;
			}
		} else if(StatusPrazo.ALERTAR.equals(statusPrazo)) {
			atrasados++;
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				atrasadosEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				atrasadosEmAcompanhamento++;
			}
		} else {
			ok++;
			if(StatusProcesso.EM_ANALISE.equals(status)) {
				okEmAnalise++;
			} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
				okEmAcompanhamento++;
			}
		}
		if(StatusProcesso.EM_ANALISE.equals(status)) {
			totalEmAnalise++;
		} else if (StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
			totalEmAcompanhamento++;
		}
	}

	public StatusProcesso getStatus() {
		return status;
	}

	public void setStatus(StatusProcesso status) {
		this.status = status;
	}

	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public ProcessoService getProcessoService() {
		return processoService;
	}

	public void setProcessoService(ProcessoService processoService) {
		this.processoService = processoService;
	}

	public String getNomeLinha() {
		return nomeLinha;
	}

	public void setNomeLinha(String nomeLinha) {
		this.nomeLinha = nomeLinha;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getAtrasados() {
		return atrasados;
	}

	public void setAtrasados(int atrasados) {
		this.atrasados = atrasados;
	}

	public int getAlertas() {
		return alertas;
	}

	public void setAlertas(int alertas) {
		this.alertas = alertas;
	}

	public int getOk() {
		return ok;
	}

	public void setOk(int ok) {
		this.ok = ok;
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	public int getAlertasEmAcompanhamento() {
		return alertasEmAcompanhamento;
	}

	public void setAlertasEmAcompanhamento(int alertasEmAcompanhamento) {
		this.alertasEmAcompanhamento = alertasEmAcompanhamento;
	}

	public int getAlertasEmAnalise() {
		return alertasEmAnalise;
	}

	public void setAlertasEmAnalise(int alertasEmAnalise) {
		this.alertasEmAnalise = alertasEmAnalise;
	}

	public int getAtrasadosEmAcompanhamento() {
		return atrasadosEmAcompanhamento;
	}

	public void setAtrasadosEmAcompanhamento(int atrasadosEmAcompanhamento) {
		this.atrasadosEmAcompanhamento = atrasadosEmAcompanhamento;
	}

	public int getAtrasadosEmAnalise() {
		return atrasadosEmAnalise;
	}

	public void setAtrasadosEmAnalise(int atrasadosEmAnalise) {
		this.atrasadosEmAnalise = atrasadosEmAnalise;
	}

	public int getOkEmAcompanhamento() {
		return okEmAcompanhamento;
	}

	public void setOkEmAcompanhamento(int okEmAcompanhamento) {
		this.okEmAcompanhamento = okEmAcompanhamento;
	}

	public int getOkEmAnalise() {
		return okEmAnalise;
	}

	public void setOkEmAnalise(int okEmAnalise) {
		this.okEmAnalise = okEmAnalise;
	}

	public int getTotalEmAcompanhamento() {
		return totalEmAcompanhamento;
	}

	public void setTotalEmAcompanhamento(int totalEmAcompanhamento) {
		this.totalEmAcompanhamento = totalEmAcompanhamento;
	}

	public int getTotalEmAnalise() {
		return totalEmAnalise;
	}

	public void setTotalEmAnalise(int totalEmAnalise) {
		this.totalEmAnalise = totalEmAnalise;
	}

	public String getTempoMedio() {
		return tempoMedio;
	}

	public void setTempoMedio(String tempoMedio) {
		this.tempoMedio = tempoMedio;
	}

	public Integer getHoje() {
		return hoje;
	}

	public void setHoje(Integer hoje) {
		this.hoje = hoje;
	}
}
