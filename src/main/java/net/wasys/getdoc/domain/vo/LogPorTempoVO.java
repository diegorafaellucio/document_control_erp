package net.wasys.getdoc.domain.vo;

public class LogPorTempoVO {

	private String descricao;
	private String horaMinuto;
	private Long tempoMedio;
	private Long tempoTotal;
	private Long tamanhoTotal;
	private Long acessos;
	private String mensagemErro;
	private String stackTrace;

	public LogPorTempoVO(String descricao, String horaMinuto, Long tempoMedio, Long tempoTotal, Long tamanhoTotal, Long acessos) {
		this.descricao = descricao;
		this.horaMinuto = horaMinuto;
		this.tempoMedio = tempoMedio;
		this.tempoTotal = tempoTotal;
		this.tamanhoTotal = tamanhoTotal;
		this.acessos = acessos;
	}

	public LogPorTempoVO(String descricao, String horaMinuto, String mensagemErro, String stackTrace) {
		this.descricao = descricao;
		this.horaMinuto = horaMinuto;
		this.mensagemErro = mensagemErro;
		this.stackTrace = stackTrace;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getHoraMinuto() {
		return horaMinuto;
	}

	public void setHoraMinuto(String horaMinuto) {
		this.horaMinuto = horaMinuto;
	}

	public Long getTempoMedio() {
		return tempoMedio;
	}

	public void setTempoMedio(Long tempoMedio) {
		this.tempoMedio = tempoMedio;
	}

	public Long getTempoTotal() {
		return tempoTotal;
	}

	public void setTempoTotal(Long tempoTotal) {
		this.tempoTotal = tempoTotal;
	}

	public Long getTamanhoTotal() {
		return tamanhoTotal;
	}

	public void setTamanhoTotal(Long tamanhoTotal) {
		this.tamanhoTotal = tamanhoTotal;
	}

	public Long getAcessos() {
		return acessos;
	}

	public void setAcessos(Long acessos) {
		this.acessos = acessos;
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