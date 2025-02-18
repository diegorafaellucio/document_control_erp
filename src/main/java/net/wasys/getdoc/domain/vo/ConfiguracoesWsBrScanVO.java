package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsBrScanVO {

	private String endpointCadastrar;
	private String endpointConsultar;
	private String usuario;
	private String senha;
	private String contrato;
	private Integer validade;

	public String getEndpointCadastrar() {
		return endpointCadastrar;
	}

	public void setEndpointCadastrar(String endpointCadastrar) {
		this.endpointCadastrar = endpointCadastrar;
	}

	public String getEndpointConsultar() {
		return endpointConsultar;
	}

	public void setEndpointConsultar(String endpointConsultar) {
		this.endpointConsultar = endpointConsultar;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getContrato() {
		return contrato;
	}

	public void setContrato(String contrato) {
		this.contrato = contrato;
	}

	public Integer getValidade() {
		return validade;
	}

	public void setValidade(Integer validade) {
		this.validade = validade;
	}
}
