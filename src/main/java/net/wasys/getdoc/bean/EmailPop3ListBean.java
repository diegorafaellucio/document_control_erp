package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.mail.MessagingException;
import javax.mail.Store;

import org.springframework.beans.factory.annotation.Autowired;

import com.sun.mail.pop3.POP3Folder;

import net.wasys.getdoc.bean.datamodel.EmailPop3DataModel;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.service.EmailPop3Service;
import net.wasys.getdoc.domain.service.EmailRecebidoAnexoService;
import net.wasys.util.faces.AbstractBean;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@SessionScoped
public class EmailPop3ListBean extends AbstractBean {

	@Autowired private EmailPop3Service emailPop3Service;
	@Autowired private EmailRecebidoAnexoService emailRecebidoAnexoService;

	private EmailPop3DataModel dataModel;
	private Store conexaoPop3;
	private POP3Folder folderPop3;
	private EmailRecebido emailRecebido;

	protected void initBean() {

		dataModel = new EmailPop3DataModel();
		dataModel.setService(emailPop3Service);
		dataModel.setFolderPop3(folderPop3);
	}

	public void conectarPop3() {

		try {
			conexaoPop3 = emailPop3Service.conectarPop3();
			folderPop3 = emailPop3Service.abrirInbox(conexaoPop3);
			dataModel.setFolderPop3(folderPop3);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public EmailPop3DataModel getDataModel() {
		return dataModel;
	}

	@PreDestroy
	public void preDestroy() {
		if(folderPop3 != null) {
			try {
				folderPop3.close(false);
			} catch (MessagingException e1) {
				e1.printStackTrace();
			}
		}
		if(conexaoPop3 != null) {
			try {
				conexaoPop3.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public POP3Folder getFolderPop3() {
		return folderPop3;
	}

	public void setEmailRecebido(EmailRecebido emailRecebido) {

		try {
			emailPop3Service.carregarAnexos(emailRecebido, folderPop3);
		}
		catch (Exception e) {
			e.printStackTrace();
			addMessageError(e);
		}

		this.emailRecebido = emailRecebido;
	}

	public EmailRecebido getEmailRecebido() {
		return emailRecebido;
	}

	public void downloadAnexoEmail(EmailRecebidoAnexo anexo) {

		File file = emailRecebidoAnexoService.getFile(anexo);
		String nome = anexo.getFileName();

		sendFile(file, nome);
	}
}
