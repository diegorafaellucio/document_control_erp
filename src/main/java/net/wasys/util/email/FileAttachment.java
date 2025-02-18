package net.wasys.util.email;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;

public class FileAttachment implements Attachment {

	private File file;
	private String fileName;

	public FileAttachment() {
	}

	public FileAttachment(String file) {
		this.file = new File(file);
	}

	public FileAttachment(File file) {
		this.file = file;
	}

	public String getFileName() {

		if (this.fileName == null) {
			return getFile().getName();
		}

		return this.fileName;
	}

	public void attach(MimeBodyPart attachmentBodyPart) throws Exception {

		attachmentBodyPart.setDisposition("attachment");
		attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(this.file)));
		attachmentBodyPart.setFileName(getFileName());
		attachmentBodyPart.setContentID("<" + getFileName() + ">");
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDisposition() {
		return "attachment";
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}