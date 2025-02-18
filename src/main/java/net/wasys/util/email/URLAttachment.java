package net.wasys.util.email;

import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.internet.MimeBodyPart;

public class URLAttachment implements Attachment {

	private String fileName = null;
	private String contentURL = null;

	public URLAttachment() {
	}

	public URLAttachment(String contentURL, String fileName) {
		this.contentURL = contentURL;
		this.fileName = fileName;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void attach(MimeBodyPart attachmentBodyPart) throws Exception {

		attachmentBodyPart.setDisposition("inline");
		attachmentBodyPart.setDataHandler(new DataHandler(new URLDataSource(new URL(getContentURL()))));
		attachmentBodyPart.setFileName(getFileName());

		attachmentBodyPart.setContentID("<" + getFileName() + ">");
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentURL() {
		return this.contentURL;
	}

	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}
}