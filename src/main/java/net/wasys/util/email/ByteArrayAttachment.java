package net.wasys.util.email;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

public class ByteArrayAttachment implements Attachment {

	private String fileName = null;
	private byte[] file;

	public ByteArrayAttachment() {
	}

	public ByteArrayAttachment(String fileName, byte[] file) {
		this.fileName = fileName;
		this.file = file;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void attach(MimeBodyPart attachmentBodyPart) throws Exception {

		attachmentBodyPart.setDisposition("attachment");
		attachmentBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(this.file, "application/octet-stream")));
		attachmentBodyPart.setFileName(getFileName());
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getFile() {
		return this.file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}
}