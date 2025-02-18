package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.StatusGeracaoPastaVermelha;

import java.io.File;

public class ExecucaoPastaVermelhaVO {

	private StatusGeracaoPastaVermelha statusGeracaoPastaVermelha;
	private File arquivo;

	public ExecucaoPastaVermelhaVO(StatusGeracaoPastaVermelha statusGeracaoPastaVermelha, File arquivo) {
		this.statusGeracaoPastaVermelha = statusGeracaoPastaVermelha;
		this.arquivo = arquivo;
	}

	public StatusGeracaoPastaVermelha getStatusGeracaoPastaVermelha() {
		return statusGeracaoPastaVermelha;
	}

	public void setStatusGeracaoPastaVermelha(StatusGeracaoPastaVermelha statusGeracaoPastaVermelha) {
		this.statusGeracaoPastaVermelha = statusGeracaoPastaVermelha;
	}

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}
}
