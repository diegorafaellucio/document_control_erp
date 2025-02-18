package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.wasys.getdoc.domain.entity.EmailEnviado;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.util.DummyUtils;

public class EmailVO {

	private String assunto;
	private String observacaoTmp;
	private List<FileVO> arquivos = new ArrayList<>();
	private List<String> destinatariosList;
	private EmailEnviado emailEnviado;
	private List<EmailRecebido> emailsRecebidos = new ArrayList<>();
	private ProcessoLog logCriacao;
	private String body;
	private Date dataInicioConvite;
	private Date dataFimConvite;

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public String getObservacaoTmp() {
		return observacaoTmp;
	}

	public void setObservacaoTmp(String observacaoTmp) {
		this.observacaoTmp = StringUtils.trim(observacaoTmp);
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

	public List<String> getDestinatariosList() {
		return destinatariosList;
	}

	public void setDestinatariosList(List<String> destinatariosList) {
		this.destinatariosList = destinatariosList;
	}

	public EmailEnviado getEmailEnviado() {
		return emailEnviado;
	}

	public void setEmailEnviado(EmailEnviado emailEnviado) {
		this.emailEnviado = emailEnviado;
	}

	public void addEmailRecebido(EmailRecebido emailRecebido) {
		if(emailRecebido != null) {
			emailsRecebidos.add(emailRecebido);
		}
	}

	public List<EmailRecebido> getEmailsRecebidos() {
		return emailsRecebidos;
	}

	public ProcessoLog getLogCriacao() {
		return logCriacao;
	}

	public void setLogCriacao(ProcessoLog logCriacao) {
		this.logCriacao = logCriacao;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getDataInicioConvite() {
		return dataInicioConvite;
	}

	public void setDataInicioConvite(Date dataInicioConvite) {
		this.dataInicioConvite = dataInicioConvite;
	}

	public Date getDataFimConvite() {
		return dataFimConvite;
	}

	public void setDataFimConvite(Date dataFimConvite) {
		this.dataFimConvite = dataFimConvite;
	}
}
