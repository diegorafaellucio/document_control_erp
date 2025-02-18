package net.wasys.getdoc.restws.dto;

import java.io.Serializable;
import java.util.Date;

public class SubareaDTO implements Serializable {

	private Long id;
	private String descricao;
	private Boolean ativo;
	private Date dataAtualizacao;
	private String email;
	private Long areaId;
	private Long usuarioUltimaAtualizacaoId;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public Long getUsuarioUltimaAtualizacaoId() {
		return usuarioUltimaAtualizacaoId;
	}

	public void setUsuarioUltimaAtualizacaoId(Long usuarioUltimaAtualizacaoId) {
		this.usuarioUltimaAtualizacaoId = usuarioUltimaAtualizacaoId;
	}
}
