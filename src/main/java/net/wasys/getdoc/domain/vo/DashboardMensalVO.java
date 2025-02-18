package net.wasys.getdoc.domain.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardMensalVO {

	private ListaProcessoVO tipoProcessoPorDia = new ListaProcessoVO();
	private ListaProcessoVO tipoProcessoPorDiaTratado = new ListaProcessoVO();
	private ListaProcessoVO tipoProcessoPorMes = new ListaProcessoVO();
	private TempoOperacao tempoOperacao = new TempoOperacao();
	private TempoOperacao tempoOperacaoMes = new TempoOperacao();
	private List<Long> produtividadePorDia = new ArrayList<>();
	private List<Long> produtividadePorMes = new ArrayList<>();


	public ListaProcessoVO getTipoProcessoPorDia() {
		return tipoProcessoPorDia;
	}

	public void setTipoProcessoPorDia(ListaProcessoVO tipoProcessoPorDia) {
		this.tipoProcessoPorDia = tipoProcessoPorDia;
	}

	public ListaProcessoVO getTipoProcessoPorDiaTratado() {
		return tipoProcessoPorDiaTratado;
	}

	public void setTipoProcessoPorDiaTratado(ListaProcessoVO tipoProcessoPorDiaTratado) {
		this.tipoProcessoPorDiaTratado = tipoProcessoPorDiaTratado;
	}

	public ListaProcessoVO getTipoProcessoPorMes() {
		return tipoProcessoPorMes;
	}

	public void setTipoProcessoPorMes(ListaProcessoVO tipoProcessoPorMes) {
		this.tipoProcessoPorMes = tipoProcessoPorMes;
	}

	public TempoOperacao getTempoOperacaoMes() {
		return tempoOperacaoMes;
	}

	public void setTempoOperacaoMes(TempoOperacao tempoOperacaoMes) {
		this.tempoOperacaoMes = tempoOperacaoMes;
	}

	public TempoOperacao getTempoOperacao() {
		return tempoOperacao;
	}

	public void setTempoOperacao(TempoOperacao tempoOperacao) {
		this.tempoOperacao = tempoOperacao;
	}

	public List<Long> getProdutividadePorDia() {
		return produtividadePorDia;
	}

	public void setProdutividadePorDia(List<Long> produtividadePorDia) {
		this.produtividadePorDia = produtividadePorDia;
	}

	public List<Long> getProdutividadePorMes() {
		return produtividadePorMes;
	}

	public void setProdutividadePorMes(List<Long> produtividadePorMes) {
		this.produtividadePorMes = produtividadePorMes;
	}

	public static class ListaProcessoVO {
		private Map<String, List<Long>> qtdPorDia;
		private List<Long> totalPorDia;
		private long totalPeriodo;

		public Map<String, List<Long>> getQtdPorDia() {
			return qtdPorDia;
		}
		public void setQtdPorDia(Map<String, List<Long>> qtdPorDia) {
			this.qtdPorDia = qtdPorDia;
		}
		public List<Long> getTotalPorDia() {
			return totalPorDia;
		}
		public void setTotalPorDia(List<Long> totalPorDia) {
			this.totalPorDia = totalPorDia;
		}
		public long getTotalPeriodo() {
			return totalPeriodo;
		}
		public void setTotalPeriodo(long totalPeriodo) {
			this.totalPeriodo = totalPeriodo;
		}
	}


	public static class TempoOperacao {
		private List<Double> conferido;

		public List<Double> getConferido() {
			return conferido;
		}
		public void setConferido(List<Double> conferido) {
			this.conferido = conferido;
		}
	}

	public static class DashProcessoVO{
		private Long id;
		private Long processoId;
		private String motivoRequisicao;
		private String nome;
		private String nomeSocial;
		private String cpf;
		private String passaporte;
		private String identidade;
		private String orgaoEmissor;
		private String dataEmissao;
		private String curso;
		private String campus;
		private String dataCriacao;
		private String dataEnvioAnalise;
		private String dataFinalizacaoAnalise;
		private String dataFinalizacao;
		private String prazoLimiteAnalise;
		private String tempoEmAnalise;
		private String slaPrevisto;
		private String slaAtendido;
		private String situacao;
		private String status;
		private String analista;
		private String analistaLogin;
		private String modalidadeEnsino;
		private String regional;
		private String formaIngresso;
		private String instituicao;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getProcessoId() {
			return processoId;
		}

		public void setProcessoId(Long processoId) {
			this.processoId = processoId;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getCpf() {
			return cpf;
		}

		public void setCpf(String cpf) {
			this.cpf = cpf;
		}

		public String getPassaporte() {
			return passaporte;
		}

		public void setPassaporte(String passaporte) {
			this.passaporte = passaporte;
		}

		public String getIdentidade() {
			return identidade;
		}

		public void setIdentidade(String identidade) {
			this.identidade = identidade;
		}

		public String getOrgaoEmissor() {
			return orgaoEmissor;
		}

		public void setOrgaoEmissor(String orgaoEmissor) {
			this.orgaoEmissor = orgaoEmissor;
		}

		public String getDataEmissao() {
			return dataEmissao;
		}

		public void setDataEmissao(String dataEmissao) {
			this.dataEmissao = dataEmissao;
		}

		public String getCurso() {
			return curso;
		}

		public void setCurso(String curso) {
			this.curso = curso;
		}

		public String getCampus() {
			return campus;
		}

		public void setCampus(String campus) {
			this.campus = campus;
		}

		public String getDataCriacao() {
			return dataCriacao;
		}

		public void setDataCriacao(String dataCriacao) {
			this.dataCriacao = dataCriacao;
		}

		public String getDataEnvioAnalise() {
			return dataEnvioAnalise;
		}

		public void setDataEnvioAnalise(String dataEnvioAnalise) {
			this.dataEnvioAnalise = dataEnvioAnalise;
		}

		public String getDataFinalizacaoAnalise() {
			return dataFinalizacaoAnalise;
		}

		public void setDataFinalizacaoAnalise(String dataFinalizacaoAnalise) {
			this.dataFinalizacaoAnalise = dataFinalizacaoAnalise;
		}

		public String getDataFinalizacao() {
			return dataFinalizacao;
		}

		public void setDataFinalizacao(String dataFinalizacao) {
			this.dataFinalizacao = dataFinalizacao;
		}

		public String getPrazoLimiteAnalise() {
			return prazoLimiteAnalise;
		}

		public void setPrazoLimiteAnalise(String prazoLimiteAnalise) {
			this.prazoLimiteAnalise = prazoLimiteAnalise;
		}

		public String getSlaPrevisto() {
			return slaPrevisto;
		}

		public void setSlaPrevisto(String slaPrevisto) {
			this.slaPrevisto = slaPrevisto;
		}

		public String getSlaAtendido() {
			return slaAtendido;
		}

		public void setSlaAtendido(String slaAtendido) {
			this.slaAtendido = slaAtendido;
		}

		public String getSituacao() {
			return situacao;
		}

		public void setSituacao(String situacao) {
			this.situacao = situacao;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getAnalista() {
			return analista;
		}

		public void setAnalista(String analista) {
			this.analista = analista;
		}

		public String getAnalistaLogin() {
			return analistaLogin;
		}

		public void setAnalistaLogin(String analistaLogin) {
			this.analistaLogin = analistaLogin;
		}

		public String getMotivoRequisicao() {
			return motivoRequisicao;
		}

		public void setMotivoRequisicao(String motivoRequisicao) {
			this.motivoRequisicao = motivoRequisicao;
		}

		public String getNomeSocial() {
			return nomeSocial;
		}

		public void setNomeSocial(String nomeSocial) {
			this.nomeSocial = nomeSocial;
		}

		public String getTempoEmAnalise() {
			return tempoEmAnalise;
		}

		public void setTempoEmAnalise(String tempoEmAnalise) {
			this.tempoEmAnalise = tempoEmAnalise;
		}

		public String getModalidadeEnsino() {
			return modalidadeEnsino;
		}

		public void setModalidadeEnsino(String modalidadeEnsino) {
			this.modalidadeEnsino = modalidadeEnsino;
		}

		public String getRegional() {
			return regional;
		}

		public void setRegional(String regional) {
			this.regional = regional;
		}

		public String getFormaIngresso() {
			return formaIngresso;
		}

		public void setFormaIngresso(String formaIngresso) {
			this.formaIngresso = formaIngresso;
		}

		public String getInstituicao() {
			return instituicao;
		}

		public void setInstituicao(String instituicao) {
			this.instituicao = instituicao;
		}
	}
}
