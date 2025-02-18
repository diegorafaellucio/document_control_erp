package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;

import java.util.Date;
import java.util.List;

public class DocumentoLogFiltro {

	private Usuario usuario;
	private Pendencia pendencia;
	private List<Long> documentoIds;
	private AcaoDocumento acaoDocumento;
	private Date dataInicio;
	private Date dataFim;
	private String observacao;
	private List<StatusDocumento> statusDifetenteDeList;
	private boolean diferenteDeOutros;
	private boolean diferenteDeTipificando;

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Pendencia getPendencia() {
		return pendencia;
	}

	public void setPendencia(Pendencia pendencia) {
		this.pendencia = pendencia;
	}

	public List<Long> getDocumentoIds() {
		return documentoIds;
	}

	public void setDocumentoIds(List<Long> documentoIds) {
		this.documentoIds = documentoIds;
	}

	public AcaoDocumento getAcaoDocumento() {
		return acaoDocumento;
	}

	public void setAcaoDocumento(AcaoDocumento acaoDocumento) {
		this.acaoDocumento = acaoDocumento;
	}

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

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public List<StatusDocumento> getStatusDifetenteDeList() {
		return statusDifetenteDeList;
	}

	public void setStatusDifetenteDeList(List<StatusDocumento> statusDifetenteDeList) {
		this.statusDifetenteDeList = statusDifetenteDeList;
	}

	public boolean isDiferenteDeOutros() {
		return diferenteDeOutros;
	}

	public void setDiferenteDeOutros(boolean diferenteDeOutros) {
		this.diferenteDeOutros = diferenteDeOutros;
	}

	public boolean isDiferenteDeTipificando() {
		return diferenteDeTipificando;
	}

	public void setDiferenteDeTipificando(boolean diferenteDeTipificando) {
		this.diferenteDeTipificando = diferenteDeTipificando;
	}
}