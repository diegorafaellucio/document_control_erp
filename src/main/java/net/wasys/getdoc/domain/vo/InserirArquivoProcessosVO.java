package net.wasys.getdoc.domain.vo;

import java.io.File;

public class InserirArquivoProcessosVO {

	private File arquivo;
	private String tiposDocumentosIds;
	private String processoIds;
	private boolean aprovarDocumento;

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	public String getTiposDocumentosIds() {
		return tiposDocumentosIds;
	}

	public void setTiposDocumentosIds(String tiposDocumentosIds) {
		this.tiposDocumentosIds = tiposDocumentosIds;
	}

	public String getProcessoIds() {
		return processoIds;
	}

	public void setProcessoIds(String processoIds) {
		this.processoIds = processoIds;
	}

	public boolean isAprovarDocumento() {
		return aprovarDocumento;
	}

	public void setAprovarDocumento(boolean aprovarDocumento) {
		this.aprovarDocumento = aprovarDocumento;
	}
}
