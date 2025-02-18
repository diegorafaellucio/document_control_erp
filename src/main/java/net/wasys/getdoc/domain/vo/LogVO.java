package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.util.DummyUtils;
import org.hibernate.Hibernate;

import java.util.Date;
import java.util.Set;

public class LogVO {

	private DocumentoLog documentoLog;
	private ProcessoLog processoLog;
	private ProcessoLogAnexo processoLogAnexo;
	private int alturaImg;
	private int larguraImg;
	private String observacaoCurtaAtualizacaoCampos;
	private String observacaoCurtaCalcSalarioMinimo;

	public LogVO(ProcessoLogAnexo processoLogAnexo) {
		this.processoLogAnexo = processoLogAnexo;
		this.processoLog = processoLogAnexo.getProcessoLog();
		Hibernate.initialize(this.processoLog.getAnexos());
	}

	public LogVO(ProcessoLog processoLog) {
		this.processoLog = processoLog;
	}

	public LogVO(DocumentoLog documentoLog) {
		this.documentoLog = documentoLog;
	}

	public Long getId() {

		if(processoLog != null) {
			return processoLog.getId();
		}
		if(documentoLog != null) {
			return documentoLog.getId();
		}

		return null;
	}

	public Date getData() {

		if(processoLog != null) {
			return processoLog.getData();
		}
		if(documentoLog != null) {
			return documentoLog.getData();
		}

		return null;
	}

	public Usuario getUsuario() {

		if(processoLog != null) {
			return processoLog.getUsuario();
		}
		if(documentoLog != null) {
			return documentoLog.getUsuario();
		}

		return null;
	}

	public String getAcaoKey() {

		if(processoLog != null) {

			AcaoProcesso acao = processoLog.getAcao();
			return "AcaoProcesso." + acao + ".label";
		}

		if(documentoLog != null) {

			AcaoDocumento acao = documentoLog.getAcao();
			return "AcaoDocumento." + acao + ".label";
		}

		return null;
	}

	public Documento getDocumento() {

		if(documentoLog != null) {
			return documentoLog.getDocumento();
		}

		return null;
	}

	public TipoEvidencia getTipoEvidencia() {

		if(processoLog != null) {
			return processoLog.getTipoEvidencia();
		}

		return null;
	}

	public Solicitacao getSolicitacao() {

		if(processoLog != null) {
			return processoLog.getSolicitacao();
		}

		return null;
	}

	public String getObservacaoFull() {
		if(processoLog != null) {
			return processoLog.getObservacao();
		}
		if(documentoLog != null) {

			Pendencia pendencia = documentoLog.getPendencia();
			if(pendencia != null) {
				return pendencia.getObservacao();
			}
			return documentoLog.getObservacao();
		}

		return null;
	}

	public String getObservacao() {

		if(processoLog != null) {
			return processoLog.getObservacaoCurta();
		}
		if(documentoLog != null) {

			Pendencia pendencia = documentoLog.getPendencia();
			if(pendencia != null) {
				return pendencia.getObservacaoCurta();
			}
			return documentoLog.getObservacaoCurta();
		}

		return null;
	}

	public boolean getTemAnexo() {

		if(processoLog != null) {

			Set<ProcessoLogAnexo> anexos = processoLog.getAnexos();
			return !anexos.isEmpty();
		}

		return false;
	}

	public ProcessoLog getProcessoLog() {
		return processoLog;
	}

	public ProcessoLogAnexo getProcessoLogAnexo() {
		return processoLogAnexo;
	}

	public void setProcessoLogAnexo(ProcessoLogAnexo processoLogAnexo) {
		this.processoLogAnexo = processoLogAnexo;
	}

	public int getAlturaImg() {
		return alturaImg;
	}

	public void setAlturaImg(int alturaImg) {
		this.alturaImg = alturaImg;
	}

	public int getLarguraImg() {
		return larguraImg;
	}

	public void setLarguraImg(int larguraImg) {
		this.larguraImg = larguraImg;
	}

	public Boolean getTamanhoPequeno(){
		return larguraImg <200 || alturaImg<200;
	}

	public String getObservacaoCurtaAtualizacaoCampos() {
		return observacaoCurtaAtualizacaoCampos;
	}

	public void setObservacaoCurtaAtualizacaoCampos(String observacaoCurtaAtualizacaoCampos) {
		this.observacaoCurtaAtualizacaoCampos = observacaoCurtaAtualizacaoCampos;
	}

	public String getObservacaoCurtaCalcSalarioMinimo() {
		return observacaoCurtaCalcSalarioMinimo;
	}

	public void setObservacaoCurtaCalcSalarioMinimo(String observacaoCurtaCalcSalarioMinimo) {
		this.observacaoCurtaCalcSalarioMinimo = observacaoCurtaCalcSalarioMinimo;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{data:" + DummyUtils.formatDateTime3(getData()) + "}";
	}
}
