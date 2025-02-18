package net.wasys.getdoc.domain.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.wasys.util.DummyUtils;

@Entity(name="EMAIL_RECEBIDO_ANEXO")
public class EmailRecebidoAnexo extends net.wasys.util.ddd.Entity {

	private Long id;
	private String path;
	private String fileName;
	private String extensao;
	private Long tamanho;
	private String hashChecksum;
	private Boolean attachment;

	private EmailRecebido emailRecebido;

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

	@Column(name="EXTENSAO", nullable=false)
	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}

	@Column(name="FILE_NAME", nullable=false, length=100)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="PATH", nullable=false, length=200)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name="TAMANHO", nullable=false)
	public Long getTamanho() {
		return tamanho;
	}

	public void setTamanho(Long Tamanho) {
		tamanho = Tamanho;
	}

	@Column(name="HASH_CHECKSUM", nullable=false)
	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	@Column(name="ATTACHMENT", nullable=false)
	public Boolean getAttachment() {
		return attachment;
	}

	public void setAttachment(Boolean attachment) {
		this.attachment = attachment;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EMAIL_RECEBIDO_ID")
	public EmailRecebido getEmailRecebido() {
		return emailRecebido;
	}

	public void setEmailRecebido(EmailRecebido emailRecebido) {
		this.emailRecebido = emailRecebido;
	}

	public static String criaPath(EmailRecebidoAnexo anexo, String pathRaiz, String separador) {

		String nome = anexo.getFileName();
		String extensao = DummyUtils.getExtensao(nome);

		EmailRecebido emailRecebido = anexo.getEmailRecebido();
		Date dataCriacao = emailRecebido.getData();

		Calendar data = Calendar.getInstance();
		data.setTime(dataCriacao);
		int ano = data.get(Calendar.YEAR);
		int mes = data.get(Calendar.MONTH) + 1;
		String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
		int dia = data.get(Calendar.DAY_OF_MONTH);
		String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

		Long anexoId = anexo.getId();
		String anexoIdStr = anexoId.toString();
		int length = anexoIdStr.length();
		String agrupador = anexoIdStr.substring(length - 3, length);

		StringBuilder caminho = new StringBuilder(pathRaiz);
		caminho.append(ano).append(separador);
		caminho.append(mesStr).append(separador);
		caminho.append(diaStr).append(separador);
		caminho.append(agrupador).append(separador);

		caminho.append(anexoId).append("_").append(anexoId).append(".").append(extensao);

		return caminho.toString();
	}
}
