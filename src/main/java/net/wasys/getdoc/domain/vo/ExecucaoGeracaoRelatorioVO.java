package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoExtensaoRelatorio;

import java.util.Date;

public class ExecucaoGeracaoRelatorioVO {

	private Long id;
	private Long configuracaoGeracaoRelatorioId;
	private String nome;
	private String horario;
	private String caminhoArquivo;
	private String observacao;
	private Boolean sucesso;
	private boolean executando = false;
	private Date inicio;
	private Date fim;
	private TipoExtensaoRelatorio extensao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getConfiguracaoGeracaoRelatorioId() {
		return configuracaoGeracaoRelatorioId;
	}

	public void setConfiguracaoGeracaoRelatorioId(Long configuracaoGeracaoRelatorioId) {
		this.configuracaoGeracaoRelatorioId = configuracaoGeracaoRelatorioId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getCaminhoArquivo() {
		return caminhoArquivo;
	}

	public void setCaminhoArquivo(String caminhoArquivo) {
		this.caminhoArquivo = caminhoArquivo;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Boolean getSucesso() {
		return sucesso;
	}

	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	public boolean isExecutando() {
		return executando;
	}

	public void setExecutando(boolean executando) {
		this.executando = executando;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	public TipoExtensaoRelatorio getExtensao() {
		return extensao;
	}

	public void setExtensao(TipoExtensaoRelatorio extensao) {
		this.extensao = extensao;
	}
}
