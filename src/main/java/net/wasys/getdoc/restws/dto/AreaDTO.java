package net.wasys.getdoc.restws.dto;

import java.io.Serializable;
import java.util.Date;

public class AreaDTO implements Serializable {

	private Long id;
	private String descricao;
	private Boolean ativo;
	private Date dataAtualizacao;
	private Boolean acessoLiberado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public Boolean getAcessoLiberado() {
		return acessoLiberado;
	}

	public void setAcessoLiberado(Boolean acessoLiberado) {
		this.acessoLiberado = acessoLiberado;
	}
}
