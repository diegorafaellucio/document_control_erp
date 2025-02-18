package net.wasys.getdoc.domain.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import net.wasys.util.DummyUtils;
import net.wasys.util.other.StringZipUtils;

@Entity(name="EMAIL_RECEBIDO")
public class EmailRecebido extends net.wasys.util.ddd.Entity {

	private Long id;
	private String subject;
	private Date sentDate;
	private Date receivedDate;
	private String emailFrom;
	private String replyTo;
	private byte[] conteudo;
	private byte[] conteudoHtml;
	private String messageUid;
	private String conversationId;
	private Date data;
	private Date dataLeitura;

	private EmailEnviado emailEnviado;
	private Processo processo;

	private Set<EmailRecebidoAnexo> anexos = new HashSet<>();

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_LEITURA")
	public Date getDataLeitura() {
		return dataLeitura;
	}

	public void setDataLeitura(Date dataLeitura) {
		this.dataLeitura = dataLeitura;
	}

	@Column(name="CONTEUDO")
	public byte[] getConteudo() {
		return conteudo;
	}

	public void setConteudo(byte[] conteudo) {
		this.conteudo = conteudo;
	}

	@Column(name="CONTEUDO_HTML")
	public byte[] getConteudoHtml() {
		return conteudoHtml;
	}

	public void setConteudoHtml(byte[] conteudoHtml) {
		this.conteudoHtml = conteudoHtml;
	}

	@Column(name="SUBJECT")
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name="SENT_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	@Column(name="RECEIVED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	@Column(name="EMAIL_FROM")
	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	@Column(name="REPLY_TO")
	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	@Column(name="MESSAGE_UID")
	public String getMessageUid() {
		return messageUid;
	}

	public void setMessageUid(String messageUid) {
		this.messageUid = messageUid;
	}

	@Column(name="CONVERSATION_ID")
	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EMAIL_ENVIADO_ID")
	public EmailEnviado getEmailEnviado() {
		return emailEnviado;
	}

	public void setEmailEnviado(EmailEnviado emailEnviado) {
		this.emailEnviado = emailEnviado;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_ID", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@OrderBy("id")
	@OneToMany(fetch=FetchType.EAGER, mappedBy="emailRecebido")
	public Set<EmailRecebidoAnexo> getAnexos() {
		return anexos;
	}

	public void setAnexos(Set<EmailRecebidoAnexo> anexos) {
		this.anexos = anexos;
	}

	@Transient
	public String getConteudoCurto() {

		byte[] conteudo = getConteudo();
		if(conteudo == null) {
			conteudo = getConteudoHtml();
		}

		String conteudoStr = StringZipUtils.uncompress(conteudo);

		if(StringUtils.isBlank(conteudoStr)) {
			return conteudoStr;
		}

		conteudoStr = conteudoStr.replace("\t", " ");
		conteudoStr = conteudoStr.replace("\n", " ");
		conteudoStr = conteudoStr.replace("\r", " ");
		while(conteudoStr.contains("  ")) {
			conteudoStr = conteudoStr.replace("  ", " ");
		}

		if(conteudoStr.length() > 140) {
			conteudoStr = conteudoStr.substring(0, 135) + "[...]";
		}

		return conteudoStr;
	}

	@Transient
	public String getConteudoLong() {

		byte[] conteudoHtml = getConteudoHtml();

		if(conteudoHtml != null) {

			String conteudoStr = StringZipUtils.uncompress(conteudoHtml);
			return conteudoStr;
		}

		byte[] conteudo = getConteudo();
		if(conteudo == null) {

			String conteudoStr = StringZipUtils.uncompress(conteudo);
			conteudoStr = DummyUtils.stringToHTML(conteudoStr);
		}

		return null;
	}
}
