package net.wasys.util.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.wasys.util.DummyUtils;
import org.apache.commons.lang3.StringUtils;

import static net.wasys.util.DummyUtils.systraceThread;

public class EmailSender {

	protected Session mailSession;
	protected MimeMessage message = null;
	private Collection<Attachment> attachments = null;
	private StringBuffer textContent = null;
	private String contentType;

	public EmailSender(String smtpServer) {
		setup(smtpServer, null, null);
	}

	public EmailSender(String smtpServer, String username, String password) {
		setup(smtpServer, username, password);
	}

	protected void setup(String smtpServer, String username, String password) {

		Properties props = new Properties();

		props.put("mail.smtp.host", smtpServer);
		props.put("mail.mime.charset", "ISO-8859-1");

		MailAuthenticator mailAuthenticator = null;

		if (StringUtils.isNotBlank(username)) {
			props.put("mail.smtp.auth", "true");
			mailAuthenticator = new MailAuthenticator(username, password);
		}

		this.mailSession = Session.getInstance(props, mailAuthenticator);

		this.message = new MimeMessage(this.mailSession);
		this.mailSession.setDebug(false);
	}

	public void send() {
		try {
			if (isMultiPart()) {
				BodyPart messageBodyPart = new MimeBodyPart();

				if (this.textContent != null) {
					messageBodyPart.setContent(this.textContent.toString(), this.contentType);
				}

				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				Collection<Attachment> attachments = getAttachments();
				if (attachments != null) {
					for (Iterator<Attachment> it = attachments.iterator(); it.hasNext();) {

						Attachment attachment = (Attachment) it.next();

						MimeBodyPart attachmentBodyPart = new MimeBodyPart();
						attachment.attach(attachmentBodyPart);

						multipart.addBodyPart(attachmentBodyPart);
					}
				}

				this.message.setContent(multipart);
			}
			else if (this.textContent != null) {
				this.message.setContent(this.textContent.toString(), this.contentType);
			}

			this.message.saveChanges();

			Transport.send(this.message);
		}
		catch (MessagingException me) {
			throw new EmailException("Send Failed", me);
		}
		catch (Exception e) {
			throw new EmailException("Send Failed", e);
		}
	}

	public void addAttachment(Attachment attachment) {

		if (this.attachments == null) {
			this.attachments = new ArrayList<Attachment>();
		}

		this.attachments.add(attachment);
	}

	public boolean isMultiPart() {
		return ((this.attachments != null) && (this.attachments.size() > 0));
	}

	public void addBCC(String address) {
		addBCC(address, null);
	}

	public void addBCC(String address, String name) {

		try {
			this.message.addRecipient(Message.RecipientType.BCC, new InternetAddress(address, name));
		}
		catch (AddressException ae) {
			throw new EmailException("Incorrect format for mail address", ae);
		}
		catch (MessagingException me) {
			throw new EmailException("addRecipient Failed", me);
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			throw new EmailException("Default Encoding not supported", null);
		}
	}

	public void addCC(String address) {
		addCC(address, null);
	}

	public void addCC(String address, String name) {

		try {
			this.message.addRecipient(Message.RecipientType.CC, new InternetAddress(address, name));
		}
		catch (AddressException ae) {
			throw new EmailException("Incorrect format for mail address", ae);
		}
		catch (MessagingException me) {
			throw new EmailException("addRecipient Failed", me);
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			throw new EmailException("Default Encoding not supported", null);
		}
	}

	public void addTo(String address) {
		addTo(address, null);
	}

	public void addTo(String address, String name) {

		try {
			this.message.addRecipient(Message.RecipientType.TO, new InternetAddress(address, name));
		}
		catch (AddressException ae) {
			throw new EmailException("Incorrect format for mail address", ae);
		}
		catch (MessagingException me) {
			throw new EmailException("addRecipient Failed", me);
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			throw new EmailException("Default Encoding not supported", null);
		}
	}

	public void clearRecipients() {

		try {
			this.message.removeHeader("Message-ID");
			this.message.removeHeader("To");
			this.message.removeHeader("Cc");
			this.message.removeHeader("Bcc");
		}
		catch (MessagingException me) {
			throw new EmailException("clearDestinations Failed", me);
		}
	}

	public Session getMailSession() {
		return this.mailSession;
	}

	public MimeMessage getMessage() {
		return this.message;
	}

	public void setContent(String content, String contentType) {
		this.textContent = new StringBuffer(content);
		this.contentType = contentType;
	}

	public void setFrom(String address) {
		setFrom(address, null);
	}

	public void setFrom(String address, String name) {

		try {
			this.message.setFrom(new InternetAddress(address, name));
		}
		catch (AddressException ae) {
			throw new EmailException("Incorrect format for mail address", ae);
		}
		catch (MessagingException me) {
			throw new EmailException("addRecipient Failed", me);
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			throw new EmailException("Default Encoding not supported", null);
		}
	}

	public void setSubject(String newSubject) {

		try {
			this.message.setSubject(newSubject, "iso-8859-1");
		}
		catch (MessagingException me) {
			throw new EmailException("Bad Subject. Maybe contains invalida characters!", me);
		}
	}

	public Collection<Attachment> getAttachments() {
		return this.attachments;
	}

	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}

	public void setContent(String conteudo, boolean html) {

		if (html) {
			setContent(conteudo, "text/html");
		} else {
			setContent(conteudo, "text/plain");
		}
	}

	public static void main(String[] args) {

		URLAttachment imagem = new URLAttachment("http://mail.wasys.com.br/wmresource.nsf/topoEsq.jpg", "imagem");

		EmailSender sender = new EmailSender("domintra.wasys.com.br");
		sender.setFrom("admin@wasys.com.br");
		sender.addTo("maschiojv@gmail.com");
		sender.setSubject("Assunto");
		sender.setContent("<b>Ola4</b><img src='cid:" + imagem.getFileName() + "'/>", true);

		byte[] file = { 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122 };

		sender.addAttachment(new ByteArrayAttachment("arquivo_falcatrua.txt", file));
		sender.addAttachment(imagem);
		sender.send();

		DummyUtils.systraceThread("OK");
	}
}