package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.sun.mail.pop3.POP3Message;
import net.wasys.util.LogLevel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.util.MailSSLSocketFactory;

import net.wasys.getdoc.domain.entity.EmailEnviado;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.Bolso;
import net.wasys.util.other.StringZipUtils;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EmailPop3Service {

	private static String INBOX_NAME = "INBOX";

	@Autowired private ProcessoService processoService;
	@Autowired private ResourceService resourceService;
	@Autowired private EmailRecebidoService emailRecebidoService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ParametroService parametroService;

	@Transactional(rollbackFor = Exception.class)
	public void importarEmails() {

		Store conexaoPop3 = conectarPop3();
		POP3Folder inbox = abrirInbox(conexaoPop3);
		boolean tudoOk = false;

		try {
			importarEmails(inbox);

			tudoOk = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			if(inbox != null) {
				try {
					inbox.close(tudoOk);
				} catch (MessagingException e1) {
					systraceThread("Erro inesperado" + DummyUtils.getExceptionMessage(e1));
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
	}

	private void importarEmails(POP3Folder inbox) throws MessagingException, IOException {

		Message[] messages = inbox.getMessages();

		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];

			String uid = inbox.getUID(message);
			boolean exists = emailRecebidoService.existsByMessageUid(uid);
			if(exists) {
				continue;
			}

			EmailRecebido er = lerMensagem(message, false);

			EmailEnviado ee = emailEnviadoService.identificarEmail(er);

			if(ee != null) {

				er = lerMensagem(message, true);

				er.setEmailEnviado(ee);
				Processo processo = ee.getProcesso();
				er.setProcesso(processo);

				emailRecebidoService.save(er);

				message.setFlag(Flags.Flag.DELETED, true);
			}
			else {

				boolean apagar = false;

				String subject = message.getSubject();
				if("Quota warning".equals(subject)) {
					emailSmtpService.enviarAlertaPop3(er);
					apagar = true;
				}

				Date sentDate = message.getSentDate();
				Date dataCorte = DateUtils.addDays(new Date(), -20);

				apagar |= sentDate.before(dataCorte);
				apagar |= "Undelivered Mail Returned to Sender".equals(subject);

				if(apagar) {

					message.setFlag(Flags.Flag.DELETED, true);
				}
				else {
					try {
						er = lerMensagem(message, true);
						processoService.criaNovoProcessoEmail(er);
						message.setFlag(Flags.Flag.DELETED, true);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public List<EmailRecebido> listMessages(POP3Folder folderPop3, int first, int last) {

		Message[] messages = null;
		try {
			messages = folderPop3.getMessages(first, last);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<EmailRecebido> list = new ArrayList<>();

		for (int i = 0; i < messages.length; i++) {

			try {
				Message message = messages[i];
				EmailRecebido er = lerMensagem(message, false);
				list.add(er);
			}
			catch (EOFException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return list;
	}

	public int countMessages(POP3Folder folderPop3) {

		try {
			int messageCount = folderPop3.getMessageCount();
			return messageCount;
		}
		catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private EmailRecebido lerMensagem(Message message, boolean carregarAnexos) throws MessagingException, IOException {

		String subject = message.getSubject();
		subject = subject != null ? subject : "";
		if(subject.length() > 300) {
			subject = subject.substring(0, 295) + "[...]";
		}

		Date sentDate = message.getSentDate();

		Date receivedDate = message.getReceivedDate();

		Address[] from = message.getFrom();
		String fromStr = from != null ? Arrays.toString(from) : null;
		if(fromStr.length() > 500) {
			fromStr = fromStr.substring(0, 495) + "[...]";
		}

		Address[] replyTo = message.getReplyTo();
		String replyToStr = replyTo != null ? Arrays.toString(replyTo) : null;
		if(replyToStr.length() > 500) {
			replyToStr = replyToStr.substring(0, 495) + "[...]";
		}

		EmailRecebido er = new EmailRecebido();
		er.setSubject(subject);
		er.setSentDate(sentDate);
		er.setReceivedDate(receivedDate);
		er.setEmailFrom(fromStr);
		er.setReplyTo(replyToStr);

		POP3Folder folder = (POP3Folder) message.getFolder();
		String uid = folder.getUID(message);
		er.setMessageUid(uid);

		StringBuilder conteudo = new StringBuilder();
		StringBuilder conteudoHtml = new StringBuilder();
		Set<EmailRecebidoAnexo> anexos = er.getAnexos();

		Object msgObj = "[???]";

		try {
			msgObj = message.getContent();
		}
		catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			readContent(msgObj, conteudo, conteudoHtml, anexos, carregarAnexos);
		}
		catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		for (EmailRecebidoAnexo anexo : anexos) {
			anexo.setEmailRecebido(er);
		}

		String conteudoStr = conteudo.toString();
		conteudoStr = StringUtils.trim(conteudoStr);
		if(StringUtils.isNotBlank(conteudoStr)) {
			byte[] compress = StringZipUtils.compress(conteudoStr);
			er.setConteudo(compress);
		}

		String conteudoHtmlStr = conteudoHtml.toString();
		conteudoHtmlStr = StringUtils.trim(conteudoHtmlStr);
		if(StringUtils.isNotBlank(conteudoHtmlStr)) {
			byte[] compress = StringZipUtils.compress(conteudoHtmlStr);
			er.setConteudoHtml(compress);
		}

		return er;
	}

	public void carregarAnexos(EmailRecebido er, POP3Folder folderPop3) throws Exception {

		String messageUid = er.getMessageUid();
		Message[] messages = folderPop3.getMessages();

		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];
			Object msgObj = message.getContent();
			POP3Folder folder = (POP3Folder) message.getFolder();
			String uid = folder.getUID(message);

			if(uid.equals(messageUid)) {

				Set<EmailRecebidoAnexo> anexos = er.getAnexos();
				StringBuilder conteudoHtml = new StringBuilder();
				StringBuilder conteudo = new StringBuilder();
				readContent(msgObj, conteudo, conteudoHtml, anexos, true);
			}
		}
	}

	private void readContent(Object msgObj, StringBuilder conteudo, StringBuilder conteudoHtml, Collection<EmailRecebidoAnexo> anexos, boolean carregarAnexos) throws MessagingException, IOException {

		if (msgObj instanceof MimeBodyPart) {
			MimeBodyPart part = (MimeBodyPart) msgObj;
			Object content = part.getContent();
			readMultipart(content, conteudo, conteudoHtml, anexos, carregarAnexos);
		}
		else if (msgObj instanceof Multipart) {

			readMultipart(msgObj, conteudo, conteudoHtml, anexos, carregarAnexos);
		}
		else if (msgObj instanceof POP3Message) {
			POP3Message part = (POP3Message) msgObj;
			Object content = part.getContent();
			readMultipart(content, conteudo, conteudoHtml, anexos, carregarAnexos);
		}
		else {

			Class<?> class1 = msgObj.getClass();
			String simpleName = class1.getSimpleName();
			conteudoHtml.append("---------- Mensagem não identificada ----------");
			conteudoHtml.append("<br><i>[" + simpleName + "]</i><br><br> " + msgObj);
		}
	}

	private void readMultipart(Object msgObj, StringBuilder conteudo, StringBuilder conteudoHtml, Collection<EmailRecebidoAnexo> anexos, boolean carregarAnexos) throws MessagingException, IOException {

		Multipart multipart = (Multipart) msgObj;

		int count = multipart.getCount();
		for (int j = 0; j < count; j++) {

			Part part = multipart.getBodyPart(j);
			String contentType = part.getContentType();
			String disposition = part.getDisposition();

			if (Part.ATTACHMENT.equals(disposition)) {

				EmailRecebidoAnexo anexo = carregarAnexo(carregarAnexos, part, contentType);
				if(anexo != null) {
					anexo.setAttachment(true);
					anexos.add(anexo);
				}
			}
			else if (contentType.startsWith("image/png") || contentType.startsWith("image/jpeg") || contentType.startsWith("image/jpg")) {

				EmailRecebidoAnexo anexo = carregarAnexo(carregarAnexos, part, contentType);
				if(anexo != null) {
					anexo.setAttachment(false);
					anexos.add(anexo);
				}
			}
			else if (contentType.startsWith("multipart/alternative")) {

				readContent(part, conteudo, conteudoHtml, anexos, carregarAnexos);
			}
			else if (contentType.startsWith("multipart/related")) {

				readContent(part, conteudo, conteudoHtml, anexos, carregarAnexos);
			}
			else if (contentType.startsWith("text/plain")) {

				Object content = part.getContent();
				conteudo.append(String.valueOf(content)).append("\n");
			}
			else if (contentType.startsWith("text/html")) {

				Object content = part.getContent();
				conteudoHtml.append(String.valueOf(content)).append("\n");
			}
			else {

				String fileName = getFileName(part);
				conteudoHtml.append("\n\n<i>[" + contentType + "]</i> " + fileName + "\n<br>");
			}
		}
	}

	private EmailRecebidoAnexo carregarAnexo(boolean carregarAnexos, Part part, String contentType) throws MessagingException, IOException, FileNotFoundException {

		String fileName = getFileName(part);
		if(StringUtils.isBlank(fileName)) {
			return null;
		}

		String extensao = DummyUtils.getExtensao(fileName);

		String blackList = parametroService.getValor(ParametroService.P.EXTENSOES_BLACK_LIST);

		if(blackList.contains(extensao)){
			EmailRecebidoAnexo anexoBloqueado = new EmailRecebidoAnexo();
			anexoBloqueado.setExtensao(extensao);
			anexoBloqueado.setFileName("[BLOQUEADO] " + fileName);

			return anexoBloqueado;
		}

		if (extensao != null && (extensao.equals(fileName) || extensao.length() > 5)) {
			extensao = "";
		}

		if(contentType.startsWith("message/rfc822") && StringUtils.isBlank(extensao)) {
			extensao = "eml";
			fileName += "." + extensao;
		}

		EmailRecebidoAnexo anexo = new EmailRecebidoAnexo();
		anexo.setExtensao(extensao);
		anexo.setFileName(fileName);

		if(carregarAnexos) {

			File file = getFile(part, extensao, contentType);
			String absolutePath = file.getAbsolutePath();
			anexo.setPath(absolutePath);
		}

		return anexo;
	}

	private String getFileName(Part part) throws MessagingException {

		String fileName = part.getFileName();

		if(StringUtils.isBlank(fileName)) {
			return null;
		}

		try {
			fileName = MimeUtility.decodeText(fileName);
		}
		catch (UnsupportedEncodingException e) {}

		fileName = fileName.replace("\\", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(":", "_");
		fileName = fileName.replace("*", "_");
		fileName = fileName.replace("?", "_");
		fileName = fileName.replace("\"", "_");
		fileName = fileName.replace("<", "_");
		fileName = fileName.replace(">", "_");
		fileName = fileName.replace("|", "_");

		return fileName;
	}

	private File getFile(Part part, String extensao, String contentType) throws MessagingException, IOException, FileNotFoundException {

		File file = File.createTempFile("pop3_", "." + extensao);
		DummyUtils.deleteOnExitFile(file);
		FileOutputStream fos = new FileOutputStream(file);

		Object obj = part.getContent();
		if (obj instanceof InputStream) {
			InputStream is = (InputStream) obj;
			IOUtils.copy(is, fos);
		}
		else if(obj instanceof MimeMessage) {
			MimeMessage mm = (MimeMessage) obj;
			if(contentType.startsWith("message/rfc822")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				mm.writeTo(baos);
				baos.close();
				Base64.Decoder decoder = Base64.getMimeDecoder();
				byte[] decode = decoder.decode(baos.toByteArray());
				IOUtils.write(decode, fos);
			}
			else {
				mm.writeTo(fos);
			}
		}

		fos.flush();
		fos.close();

		return file;
	}

	public POP3Folder abrirInbox(Store conexao) {

		try {
			POP3Folder folder = (POP3Folder) conexao.getFolder(INBOX_NAME);
			folder.open(Folder.READ_WRITE);
			return folder;
		}
		catch (Exception e) {

			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			rootCauseMessage = rootCauseMessage != null ? rootCauseMessage : e.getMessage();

			systraceThread("Falha ao tentar abrir a pasta " + INBOX_NAME, LogLevel.ERROR);
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	public Store conectarPop3() {

		String host = resourceService.getValue("pop3.host");
		String port = resourceService.getValue("pop3.port");
		String username = resourceService.getValue("pop3.username");
		String password = resourceService.getValue("pop3.password");
		String isSslStr = resourceService.getValue("pop3.isSsl");
		boolean isSsl = "true".equals(isSslStr);

		Properties props = new Properties();
		if(StringUtils.isNotBlank(port)) {
			props.setProperty("mail.pop3.port", port);
		}
		if(isSsl) {
			props.setProperty("mail.pop3.ssl.enable", "true");
			props.setProperty("mail.protocol.ssl.trust", "pop3.live.com");
			try {
				MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
				socketFactory.setTrustAllHosts(true);
				props.put("mail.pop3s.ssl.socketFactory", socketFactory);
			}
			catch (GeneralSecurityException e1) {
				systraceThread("erro ao criar socketFactory: " + DummyUtils.getExceptionMessage(e1), LogLevel.ERROR);
				e1.printStackTrace();
			}
		}

		Session session = Session.getDefaultInstance(props, null);

		Store store = null;
		Exception exception = null;
		int tentativas = 0;
		do {
			try {
				store = tentarConectarPop3(host, username, password, isSsl, session);
				exception = null;
			}
			catch (Exception e) {

				tentativas++;
				exception = e;
				systraceThread("Erro na " + tentativas + "ª tentativa de acessar o servidor POP3: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);

				DummyUtils.sleep(30 * 1000);
			}
		}
		while(tentativas < 3 && store == null);

		if(exception != null) {

			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(exception);
			rootCauseMessage = rootCauseMessage != null ? rootCauseMessage : exception.getMessage();

			systraceThread("Falha ao tentar conectar com o servidor POP3. Host: " + host + " Username: " + username + " Password: " + password, LogLevel.ERROR);
			exception.printStackTrace();

			throw new RuntimeException(exception);
		}

		return store;
	}

	private Store tentarConectarPop3(final String host, final String username, final String password, boolean isSsl, final Session session) throws Exception {

		final Bolso<Store> storeBolso = new Bolso<Store>(null);
		final Bolso<Exception> exceptionBolso = new Bolso<Exception>(null);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {

				String protocol = isSsl ? "pop3s" : "pop3";
				try {
					Store store = session.getStore(protocol);
					store.connect(host, username, password);

					storeBolso.setObjeto(store);
				}
				catch (Exception e) {
					String exceptionMessage = DummyUtils.getExceptionMessage(e);
					systraceThread("ERRO ao conectar no POP3!!! " + exceptionMessage, LogLevel.ERROR);
					systraceThread("host: " + host + " username: " + username + " password: " + password + " protocol: " + protocol, LogLevel.ERROR);
					exceptionBolso.setObjeto(e);
				}
			}
		};
		DummyUtils.join(runnable, 60 * 1000);
		Store store = storeBolso.getObjeto();
		Exception exception = exceptionBolso.getObjeto();

		if(store == null && exception == null) {
			throw new RuntimeException("Timeout na conexão");
		}

		if(exception != null) {
			throw exception;
		}

		return store;
	}
}
