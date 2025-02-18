package net.wasys.getdoc.domain.vo;

public class ConfiguracaoGeracaoRelatorioVO {

	private String horario;
	private String emailsEnvioSucesso;
	private String emailsEnvioErro;
	private boolean ativo;

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getEmailsEnvioSucesso() {
		return emailsEnvioSucesso;
	}

	public void setEmailsEnvioSucesso(String emailsEnvioSucesso) {
		this.emailsEnvioSucesso = emailsEnvioSucesso;
	}

	public String getEmailsEnvioErro() {
		return emailsEnvioErro;
	}

	public void setEmailsEnvioErro(String emailsEnvioErro) {
		this.emailsEnvioErro = emailsEnvioErro;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}