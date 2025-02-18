package net.wasys.getdoc.domain.entity;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity(name="ENDERECO_CEP")
public class EnderecoCep extends net.wasys.util.ddd.Entity {

	private Long id;
	private String cep;
	private String tipoLogradouro;
	private String logradouro;
	private String complemento;
	private String local;
	private String bairro;
	private String cidade;
	private String uf;
	private Date dataAtualizacao;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="CEP")
	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	@Column(name="TIPO_LOGRADOURO")
	public String getTipoLogradouro() {
		return tipoLogradouro;
	}

	public void setTipoLogradouro(String tipoLogradouro) {
		this.tipoLogradouro = tipoLogradouro;
	}

	@Column(name="LOGRADOURO")
	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	@Column(name="COMPLEMENTO")
	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	@Column(name="LOCAL")
	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	@Column(name="BAIRRO")
	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	@Column(name="CIDADE")
	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	@Column(name="UF")
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name="DATA_ATUALIZACAO")
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Transient
	public String getLogradouroOk() {

		String tipoLogradouro = getTipoLogradouro();
		String logradouro = getLogradouro();

		if(StringUtils.isNotBlank(tipoLogradouro) && StringUtils.isNotBlank(logradouro)){
			if(logradouro.startsWith(tipoLogradouro)) {
				return logradouro;
			}
			return tipoLogradouro + " " + logradouro;
		}
		return "";
	}
}