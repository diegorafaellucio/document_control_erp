package net.wasys.getdoc.rest.response.vo;

import java.util.List;

public class AlunoDocumentosDigitalizados {

	private String cpfAluno;
	private String modeloDocumento;
	private String nomeDocumento;
	private boolean composicaoFamiliar;
	private List<DadosUploadDocumentoAlunoRequestVO> arquivos;

	public String getCpfAluno() {
		return cpfAluno;
	}

	public void setCpfAluno(String cpfAluno) {
		this.cpfAluno = cpfAluno;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public boolean isComposicaoFamiliar() {
		return composicaoFamiliar;
	}

	public String getNomeDocumento() {
		return nomeDocumento;
	}

	public void setNomeDocumento(String nomeDocumento) {
		this.nomeDocumento = nomeDocumento;
	}

	public void setComposicaoFamiliar(boolean composicaoFamiliar) {
		this.composicaoFamiliar = composicaoFamiliar;
	}

	public List<DadosUploadDocumentoAlunoRequestVO> getArquivos() {
		return arquivos;
	}

	public void setArquivos(List<DadosUploadDocumentoAlunoRequestVO> arquivos) {
		this.arquivos = arquivos;
	}

	public static class DadosUploadDocumentoAlunoRequestVO {

		private String nomeArquivo;
		private String path;

		public String getNomeArquivo() {
			return nomeArquivo;
		}

		public void setNomeArquivo(String nomeArquivo) {
			this.nomeArquivo = nomeArquivo;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}
}