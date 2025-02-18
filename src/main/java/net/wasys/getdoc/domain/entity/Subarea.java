package net.wasys.getdoc.domain.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

@Entity
public class Subarea extends net.wasys.util.ddd.Entity {

	private Long id;
	private Long geralId;
	private String descricao;
	private String emails;
	private boolean ativo;
	private Date dataAtualizacao;

	private Area area;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="GERAL_ID")
	public Long getGeralId() {
		return geralId;
	}

	public void setGeralId(Long geralId) {
		this.geralId = geralId;
	}

	@Column(name="DESCRICAO", nullable=false, length=50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name="EMAILS", nullable=false, length=300)
	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ATUALIZACAO", nullable=false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="AREA_ID", nullable=false)
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@Transient
	public List<String> getEmailsList() {

		String emails = getEmails();
		if(StringUtils.isBlank(emails)) {
			return null;
		}

		List<String> list = new ArrayList<>();
		String[] emailsArray = emails.split(",");
		for (String email : emailsArray) {

			email = StringUtils.trim(email);
			list.add(email);
		}

		return list;
	}
}