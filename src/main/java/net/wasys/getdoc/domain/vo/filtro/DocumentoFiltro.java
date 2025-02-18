package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.enumeration.StatusOcr;

import java.util.Date;
import java.util.List;

public class DocumentoFiltro {

	public enum Fetch {
		TIPO_DOCUMENTO("tipoDocumento"),
		MODELO_DOCUMENTO("modeloDocumento");

		private String column;
		Fetch(String column) {
			this.column = column;
		}
		public String getColumn() {
			return column;
		}
	}

	private String nome;
	private Boolean obrigatorio;
	private List<StatusDocumento> statusDocumentoList;
	private List<StatusDocumento> statusDifetenteDeList;
	private Date dataDigitalizacaoInicio;
	private Date dataDigitalizacaoFim;
	private StatusOcr statusOcr;
	private StatusFacial statusFacial;
	private boolean aguardandoReconhecimentoFacial;
	private boolean tipificar;
	private Origem origem;
	private boolean notificadoSia;
	private List<Long> tipoDocumentoIdList;
	private Processo processo;
	private boolean diferenteDeOutros;
	private CampoGrupo grupoRelacionado;
	private boolean diferenteDeTipificando;
	private List<Long> processoList;
	private List<Long> codsOrigem;
	private List<Fetch> fetch;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public List<StatusDocumento> getStatusDifetenteDeList() {
		return statusDifetenteDeList;
	}

	public void setStatusDifetenteDeList(List<StatusDocumento> statusDifetenteDeList) {
		this.statusDifetenteDeList = statusDifetenteDeList;
	}

	public List<StatusDocumento> getStatusDocumentoList() {
		return statusDocumentoList;
	}

	public void setStatusDocumentoList(List<StatusDocumento> statusDocumentoList) {
		this.statusDocumentoList = statusDocumentoList;
	}

	public Date getDataDigitalizacaoInicio() {
		return dataDigitalizacaoInicio;
	}

	public void setDataDigitalizacaoInicio(Date dataDigitalizacaoInicio) {
		this.dataDigitalizacaoInicio = dataDigitalizacaoInicio;
	}

	public Date getDataDigitalizacaoFim() {
		return dataDigitalizacaoFim;
	}

	public void setDataDigitalizacaoFim(Date dataDigitalizacaoFim) {
		this.dataDigitalizacaoFim = dataDigitalizacaoFim;
	}

	public StatusOcr getStatusOcr() {
		return statusOcr;
	}

	public void setStatusOcr(StatusOcr statusOcr) {
		this.statusOcr = statusOcr;
	}

	public StatusFacial getStatusFacial() {
		return statusFacial;
	}

	public void setStatusFacial(StatusFacial statusFacial) {
		this.statusFacial = statusFacial;
	}

	public boolean isAguardandoReconhecimentoFacial() {
		return aguardandoReconhecimentoFacial;
	}

	public void setAguardandoReconhecimentoFacial(boolean aguardandoReconhecimentoFacial) {
		this.aguardandoReconhecimentoFacial = aguardandoReconhecimentoFacial;
	}

	public boolean isTipificar() {
		return tipificar;
	}

	public void setTipificar(boolean tipificar) {
		this.tipificar = tipificar;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public boolean isNotificadoSia() {
		return notificadoSia;
	}

	public void setNotificadoSia(boolean notificadoSia) {
		this.notificadoSia = notificadoSia;
	}

	public List<Long> getTipoDocumentoIdList() {
		return tipoDocumentoIdList;
	}

	public void setTipoDocumentoIdList(List<Long> tipoDocumentoIdList) {
		this.tipoDocumentoIdList = tipoDocumentoIdList;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public boolean isDiferenteDeOutros() {
		return diferenteDeOutros;
	}

	public void setDiferenteDeOutros(boolean diferenteDeOutros) {
		this.diferenteDeOutros = diferenteDeOutros;
	}

	public CampoGrupo getGrupoRelacionado() {
		return grupoRelacionado;
	}

	public void setGrupoRelacionado(CampoGrupo grupoRelacionado) {
		this.grupoRelacionado = grupoRelacionado;
	}

	public boolean isDiferenteDeTipificando() {
		return diferenteDeTipificando;
	}

	public void setDiferenteDeTipificando(boolean diferenteDeTipificando) {
		this.diferenteDeTipificando = diferenteDeTipificando;
	}

	public List<Long> getProcessoList() {
		return processoList;
	}

	public void setProcessoList(List<Long> processoList) {
		this.processoList = processoList;
	}

	public List<Long> getCodsOrigem() {
		return codsOrigem;
	}

	public void setCodsOrigem(List<Long> codsOrigem) {
		this.codsOrigem = codsOrigem;
	}

	public List<Fetch> getFetch() {
		return fetch;
	}

	public void setFetch(List<Fetch> fetch) {
		this.fetch = fetch;
	}
}
