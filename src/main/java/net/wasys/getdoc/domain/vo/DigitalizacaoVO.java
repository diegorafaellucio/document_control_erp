package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.util.DummyUtils;

public class DigitalizacaoVO {

	private Processo processo;
	private Documento documento;
	private TipoDocumento tipoDocumento;
	private List<FileVO> arquivos = new ArrayList<>();
	private Map<Long, List<FileVO>> arquivosMap;
	private Origem origem;
	private boolean reenvioAnalise = true;
	private boolean podeUsarDocumentoOutros;

	public List<FileVO> getArquivos() {
		return arquivos;
	}

	public void setArquivos(List<FileVO> arquivos) {
		this.arquivos = arquivos;
	}

	public void removerAnexo(FileVO fileVO) {

		this.arquivos.remove(fileVO);

		File file = fileVO.getFile();
		DummyUtils.deleteFile(file);
	}

	public void addAnexo(String fileName, File tmpFile) {

		FileVO fileVO = criarAnexo(fileName, tmpFile);

		this.arquivos.add(fileVO);
	}

	public FileVO criarAnexo(String fileName, File tmpFile) {

		String fileSize = DummyUtils.toFileSize(tmpFile.length());

		FileVO fileVO = new FileVO();
		fileVO.setFile(tmpFile);
		fileVO.setName(fileName);
		fileVO.setLength(fileSize);

		return fileVO;
	}

	public List<String> getFilesNames() {

		List<String> filesNames = new ArrayList<>();
		for (FileVO fileVO : arquivos) {
			String name = fileVO.getName();
			filesNames.add(name);
		}

		return filesNames;
	}

	public String getNome() {
		if(documento != null) {
			return documento.getNome();
		}
		else if(tipoDocumento != null) {
			return tipoDocumento.getNome();
		}
		return null;
	}

	public Processo getProcesso() {
		return processo;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public Map<Long, List<FileVO>> getArquivosMap() {
		return arquivosMap;
	}

	public void setArquivosMap(Map<Long, List<FileVO>> arquivosMap) {
		this.arquivosMap = arquivosMap;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public boolean isReenvioAnalise() {
		return reenvioAnalise;
	}

	public void setReenvioAnalise(boolean reenvioAnalise) {
		this.reenvioAnalise = reenvioAnalise;
	}

	public boolean isPodeUsarDocumentoOutros() {
		return podeUsarDocumentoOutros;
	}

	public void setPodeUsarDocumentoOutros(boolean podeUsarDocumentoOutros) {
		this.podeUsarDocumentoOutros = podeUsarDocumentoOutros;
	}

	@Override public String toString() {
		return "DigitalizacaoVO{" +
				"processo=" + processo +
				", documento=" + documento +
				", tipoDocumento=" + tipoDocumento +
				", arquivos=" + arquivos +
				", arquivosMap=" + arquivosMap +
				", origem=" + origem +
				", reenvioAnalise=" + reenvioAnalise +
				", podeUsarDocumentoOutros=" + podeUsarDocumentoOutros +
				'}';
	}
}
