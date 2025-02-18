package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.Solicitacao;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.util.DummyUtils;

public class SolicitacaoVO {

	private Solicitacao solicitacao;
	private ProcessoLog logCriacao;
	private String horasRestantes;
	private String observacaoTmp;
	private AcaoProcesso acao;
	private List<ProcessoLog> logs = new ArrayList<>();
	private List<FileVO> arquivos = new ArrayList<>();

	public Solicitacao getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(Solicitacao solicitacao) {
		this.solicitacao = solicitacao;
	}

	public ProcessoLog getLogCriacao() {
		return logCriacao;
	}

	public void setLogCriacao(ProcessoLog logCriacao) {
		this.logCriacao = logCriacao;
	}

	public String getObservacaoHtmlMin() {

		String observacao = logCriacao.getObservacao();
		if(StringUtils.isEmpty(observacao)){
			return null;
		}
		int indexOf = observacao.indexOf("\n");
		if(indexOf > 100 || indexOf < 0) {
			indexOf = Math.min(100, observacao.length());
		}
		observacao = observacao.substring(0, indexOf);

		String html = DummyUtils.stringToHTML(observacao);
		return html;
	}

	public String getHorasRestantes() {
		return horasRestantes;
	}

	public void setHorasRestantes(String horasRestantes) {
		this.horasRestantes = horasRestantes;
	}

	public String getObservacaoTmp() {
		return observacaoTmp;
	}

	public void setObservacaoTmp(String observacaoTmp) {
		this.observacaoTmp = StringUtils.trim(observacaoTmp);
	}

	public AcaoProcesso getAcao() {
		return acao;
	}

	public void setAcao(AcaoProcesso acao) {
		this.acao = acao;
	}

	public void addLog(ProcessoLog log) {
		this.logs.add(log);
	}

	public List<ProcessoLog> getLogs() {
		return logs;
	}

	public List<FileVO> getArquivos() {
		return arquivos;
	}

	public void removerAnexo(FileVO fileVO) {

		this.arquivos.remove(fileVO);

		File file = fileVO.getFile();
		DummyUtils.deleteFile(file);
	}

	public void addAnexo(String fileName, File tmpFile) {

		String fileSize = DummyUtils.toFileSize(tmpFile.length());

		FileVO fileVO = new FileVO();
		fileVO.setFile(tmpFile);
		fileVO.setName(fileName);
		fileVO.setLength(fileSize);

		this.arquivos.add(fileVO);
	}

	public List<String> getFilesNames() {

		List<String> filesNames = new ArrayList<>();
		for (FileVO fileVO : arquivos) {
			String name = fileVO.getName();
			filesNames.add(name);
		}

		return filesNames;
	}
}
