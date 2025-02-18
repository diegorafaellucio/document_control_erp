package net.wasys.getdoc.domain.vo;

public class ConfiguracoesWsSantanderVO {

	private String endPointFinanciamentosOnline;
	private String username;
	private String key;
	private String cnpj;
	private String codigoGrupoCanal;
	private String numeroIntermediario;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getNumeroIntermediario() {
		return numeroIntermediario;
	}

	public void setNumeroIntermediario(String numeroIntermediario) {
		this.numeroIntermediario = numeroIntermediario;
	}

	public String getCodigoGrupoCanal() {
		return codigoGrupoCanal;
	}

	public void setCodigoGrupoCanal(String codigoGrupoCanal) {
		this.codigoGrupoCanal = codigoGrupoCanal;
	}

	public String getEndPointFinanciamentosOnline() {
		return endPointFinanciamentosOnline;
	}

	public void setEndPointFinanciamentosOnline(String endPointFinanciamentosOnline) {
		this.endPointFinanciamentosOnline = endPointFinanciamentosOnline;
	}
}
