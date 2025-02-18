package net.wasys.getdoc.restws.dto;

import java.io.Serializable;
import java.util.Date;

public class UsuarioDTO implements Serializable {

	private Long id;
	private String nome;
	private String login;
	private String senha;
	private String email;
	private String telefone;
	private String status;
	private Date dataExpiracaoSenha;
	private String senhasAnteriores;
	private Date dataCadastro;
	private Date dataUltimoAcesso;
	private String motivoBloqueio;
	private Date dataBloqueio;
	private Date dataExpiracaoBloqueio;
	private String motivoDesativacao;
	private Date dataAtualizacao;
	private Long areaId;
	private Date dataExpiracao;

	private RoleDTO[] roles;
	private UsuarioRegionalDTO[] regionais;
	private UsuarioCampusDTO[] campus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDataExpiracaoSenha() {
		return dataExpiracaoSenha;
	}

	public void setDataExpiracaoSenha(Date dataExpiracaoSenha) {
		this.dataExpiracaoSenha = dataExpiracaoSenha;
	}

	public String getSenhasAnteriores() {
		return senhasAnteriores;
	}

	public void setSenhasAnteriores(String senhasAnteriores) {
		this.senhasAnteriores = senhasAnteriores;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Date getDataUltimoAcesso() {
		return dataUltimoAcesso;
	}

	public void setDataUltimoAcesso(Date dataUltimoAcesso) {
		this.dataUltimoAcesso = dataUltimoAcesso;
	}

	public String getMotivoBloqueio() {
		return motivoBloqueio;
	}

	public void setMotivoBloqueio(String motivoBloqueio) {
		this.motivoBloqueio = motivoBloqueio;
	}

	public Date getDataBloqueio() {
		return dataBloqueio;
	}

	public void setDataBloqueio(Date dataBloqueio) {
		this.dataBloqueio = dataBloqueio;
	}

	public Date getDataExpiracaoBloqueio() {
		return dataExpiracaoBloqueio;
	}

	public void setDataExpiracaoBloqueio(Date dataExpiracaoBloqueio) {
		this.dataExpiracaoBloqueio = dataExpiracaoBloqueio;
	}

	public String getMotivoDesativacao() {
		return motivoDesativacao;
	}

	public void setMotivoDesativacao(String motivoDesativacao) {
		this.motivoDesativacao = motivoDesativacao;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public RoleDTO[] getRoles() {
		return roles;
	}

	public void setRoles(RoleDTO[] roles) {
		this.roles = roles;
	}

	public UsuarioRegionalDTO[] getRegionais() {
		return regionais;
	}

	public void setRegionais(UsuarioRegionalDTO[] regionais) {
		this.regionais = regionais;
	}

	public UsuarioCampusDTO[] getCampus() {
		return campus;
	}

	public void setCampus(UsuarioCampusDTO[] campus) {
		this.campus = campus;
	}

	public Date getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(Date dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}
}
