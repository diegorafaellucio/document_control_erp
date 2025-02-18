package net.wasys.util.email;

import javax.mail.internet.MimeBodyPart;

public abstract interface Attachment {

	public abstract String getFileName();

	public abstract void attach(MimeBodyPart paramBodyPart) throws Exception;
}