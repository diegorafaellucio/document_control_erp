package net.wasys.getdoc.domain.vo;

public class StatusWSVO {

	private String tipo;
	private String horaMinuto;
	private String mensagemErro;
	private String stackTrace;

	public StatusWSVO(String tipo, String horaMinuto, String mensagemErro, String stackTrace) {
		this.tipo = tipo;
		this.horaMinuto = horaMinuto;
		this.mensagemErro = mensagemErro;
		this.stackTrace = stackTrace;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getHoraMinuto() {
		return horaMinuto;
	}

	public void setHoraMinuto(String horaMinuto) {
		this.horaMinuto = horaMinuto;
	}

	public String getMensagemErro() { return mensagemErro; }

	public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
