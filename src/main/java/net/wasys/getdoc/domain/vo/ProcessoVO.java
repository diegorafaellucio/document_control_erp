package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.util.DummyUtils;

import java.util.Date;
import java.util.List;

public class ProcessoVO {

	private Processo processo;
	private String horasRestantes;
	private String horasRestantesEtapa;
	private String areasPendentesStr;
	private String horasRestantesArea;
	private String horasRestantesSituacao;
	private StatusPrazo statusPrazo;
	private Boolean possuiEmailNaoLido;
	private Boolean evidenciaNaoLida;
	private Boolean reenvioNaoLido;
	private String mensagemBloqueio;
	private Boolean possuiRegrasPendentes;
	private StatusProcesso statusProcesso;
	private StatusOcr statusOcr;
	private List<AlertaSolicitacaoVO> alertasSolicitacoes;
	private String nomeAnalistaBloqueio;
	private Date dataFinalizacao;
	private Boolean passouPorPendencia;
	private String unidade;
	private String chamada;
	private String periodo;
	private String curso;

	public String getNumero() {
		return processo.getId().toString();
	}

	public String getCpfCnpj() {
		return DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CPF_CNPJ);
	}

	public ProcessoVO(Processo processo) {
		this.processo = processo;
	}

	public Processo getProcesso() {
		return processo;
	}

	public String getHorasRestantes() {
		return horasRestantes;
	}

	public void setHorasRestantes(String horasRestantes) {
		this.horasRestantes = horasRestantes;
	}

	public String getHorasRestantesEtapa() {
		return horasRestantesEtapa;
	}

	public void setHorasRestantesEtapa(String horasRestantesEtapa) {
		this.horasRestantesEtapa = horasRestantesEtapa;
	}

	public String getAreasPendentesStr() {
		return areasPendentesStr;
	}

	public void setAreasPendentesStr(String areasPendentesStr) {
		this.areasPendentesStr = areasPendentesStr;
	}

	public List<AlertaSolicitacaoVO> getAlertasSolicitacoes() {
		return alertasSolicitacoes;
	}

	public void setSolicitacoesStatus(List<AlertaSolicitacaoVO> solicitacoesStatus) {
		this.alertasSolicitacoes = solicitacoesStatus;
	}

	public String getHorasRestantesArea() {
		return horasRestantesArea;
	}

	public void setHorasRestantesArea(String horasRestantesArea) {
		this.horasRestantesArea = horasRestantesArea;
	}

	public StatusPrazo getStatusPrazo() {
		return statusPrazo;
	}

	public void setStatusPrazo(StatusPrazo statusPrazo) {
		this.statusPrazo = statusPrazo;
	}

	public Boolean getPossuiEmailNaoLido() {
		return possuiEmailNaoLido;
	}

	public void setPossuiEmailNaoLido(Boolean possuiEmailNaoLido) {
		this.possuiEmailNaoLido = possuiEmailNaoLido;
	}

	public String getMensagemBloqueio() {
		return mensagemBloqueio;
	}

	public void setMensagemBloqueio(String mensagemBloqueio) {
		this.mensagemBloqueio = mensagemBloqueio;
	}

	public Boolean getEvidenciaNaoLida() {
		return evidenciaNaoLida;
	}

	public void setEvidenciaNaoLida(Boolean evidenciaNaoLida) {
		this.evidenciaNaoLida = evidenciaNaoLida;
	}

	public Boolean getReenvioNaoLido() {
		return reenvioNaoLido;
	}

	public void setReenvioNaoLido(Boolean reenvioNaoLido) {
		this.reenvioNaoLido = reenvioNaoLido;
	}

	public String getHorasRestantesSituacao() {
		return horasRestantesSituacao;
	}

	public void setHorasRestantesSituacao(String horasRestantesSituacao) {
		this.horasRestantesSituacao = horasRestantesSituacao;
	}

	public Boolean getPossuiRegrasPendentes() {
		return possuiRegrasPendentes;
	}

	public void setPossuiRegrasPendentes(Boolean regrasExecutadas) {
		this.possuiRegrasPendentes = regrasExecutadas;
	}

	public StatusOcr getStatusOcr() { return statusOcr; }

	public void setStatusOcr(StatusOcr statusOcr) { this.statusOcr = statusOcr; }

	public StatusProcesso getStatusProcesso() {
		return statusProcesso;
	}

	public void setStatusProcesso(StatusProcesso statusProcesso) {
		this.statusProcesso = statusProcesso;
	}

	public Date getDataFinalizacao() { return dataFinalizacao; }

	public void setDataFinalizacao(Date dataFinalizacao) { this.dataFinalizacao = dataFinalizacao; }

	public Boolean getPassouPorPendencia() {
		return passouPorPendencia;
	}

	public void setPassouPorPendencia(Boolean passouPorPendencia) {
		this.passouPorPendencia = passouPorPendencia;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public String getChamada() {
		return chamada;
	}

	public void setChamada(String chamada) {
		this.chamada = chamada;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public static class AlertaSolicitacaoVO {

		private StatusSolicitacao statusSolicitacao;
		private boolean animarAlerta;

		public StatusSolicitacao getStatusSolicitacao() {
			return statusSolicitacao;
		}

		public void setStatusSolicitacao(StatusSolicitacao statusSolicitacao) {
			this.statusSolicitacao = statusSolicitacao;
		}

		public boolean getAnimarAlerta() {
			return animarAlerta;
		}

		public void setAnimarAlerta(boolean animarAlerta) {
			this.animarAlerta = animarAlerta;
		}
	}

	public String getNomeAnalistaBloqueio() { return nomeAnalistaBloqueio; }

	public void setNomeAnalistaBloqueio(String nomeAnalistaBloqueio) {
		this.nomeAnalistaBloqueio = nomeAnalistaBloqueio;
	}
}
