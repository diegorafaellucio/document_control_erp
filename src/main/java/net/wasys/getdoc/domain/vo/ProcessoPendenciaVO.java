package net.wasys.getdoc.domain.vo;

import java.util.ArrayList;
import java.util.List;

import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.ProcessoPendencia;

public class ProcessoPendenciaVO {

	private ProcessoPendencia pendencia;
	private ProcessoLog logCriacao;
	private List<DocumentoVO> documentosPendentes = new ArrayList<>();

	public ProcessoPendencia getPendencia() {
		return pendencia;
	}

	public void setPendencia(ProcessoPendencia pendencia) {
		this.pendencia = pendencia;
	}

	public ProcessoLog getLogCriacao() {
		return logCriacao;
	}

	public void setLogCriacao(ProcessoLog logCriacao) {
		this.logCriacao = logCriacao;
	}

	public void addDocumentoPendente(DocumentoVO documentoVO) {
		documentosPendentes.add(documentoVO);
	}

	public List<DocumentoVO> getDocumentosPendentes() {
		return documentosPendentes;
	}
}
