package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.Map;

public class EmailSmtpVO {

	private String body;
	private String subject;
	private Map<String, String> to;
	private Map<String, String> cc;
	private Map<String, String> cco;
	private Map<String, File> attachments;
	private String nomeSistema;
	private Boolean convite = false;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Map<String, String> getTo() {
		return to;
	}

	public void setTo(Map<String, String> to) {
		this.to = to;
	}

	public Map<String, String> getCc() {
		return cc;
	}

	public void setCc(Map<String, String> cc) {
		this.cc = cc;
	}

	public Map<String, String> getCco() {
		return cco;
	}

	public void setCco(Map<String, String> cco) {
		this.cco = cco;
	}

	public Map<String, File> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, File> attachments) {
		this.attachments = attachments;
	}

	public String getNomeSistema() {
		return nomeSistema;
	}

	public void setNomeSistema(String nomeSistema) {
		this.nomeSistema = nomeSistema;
	}

	public Boolean getConvite() {
		return convite;
	}

	public void setConvite(Boolean convite) {
		this.convite = convite;
	}
}