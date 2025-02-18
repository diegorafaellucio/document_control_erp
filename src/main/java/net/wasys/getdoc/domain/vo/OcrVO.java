package net.wasys.getdoc.domain.vo;

import java.io.File;

import net.wasys.getdoc.domain.entity.ModeloOcr;

public class OcrVO {

	private File fileImagem;
	private File fileResultado;
	private File fileModelo;
	private ModeloOcr modeloOcr;

	public OcrVO() {}

	public OcrVO(File fileImagem) {

		this.fileImagem = fileImagem;
	}

	public void setFileImagem(File fileImagem) {
		this.fileImagem = fileImagem;
	}

	public File getFileImagem() {
		return fileImagem;
	}

	public void setFileResultado(File fileResultado) {
		this.fileResultado = fileResultado;
	}

	public File getFileResultado() {
		return fileResultado;
	}

	public File getFileModelo() {
		return fileModelo;
	}

	public void setFileModelo(File fileModelo) {
		this.fileModelo = fileModelo;
	}

	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + Integer.toHexString(hashCode());
	}
}
