package net.wasys.getdoc.domain.vo.filtro;

import java.util.*;
import java.util.stream.Collectors;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.FaseEtapa;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.util.DummyUtils;
import org.hibernate.Hibernate;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;

public class ProcessoFiltro extends CamposFiltro implements Cloneable {

	private List<Fetch> fetch;

	public enum Fetch {
		CAMPANHA("campanha"),
		SITUACAO("situacao"),
		TIPO_PROCESSO("tipoProcesso");

		private String column;
		Fetch(String column) {
			this.column = column;
		}
		public String getColumn() {
			return column;
		}
	}

	public enum TipoOrdenacao {
		PENDENCIAS_ANALISTA,
		PENDENCIAS_AREA,
		NORMAL,
		PENDENCIAS_ACOMPANHAMENTO,
		RECENTEMENTE_APROVADO
	}

	public enum ConsiderarData {
		CRIACAO,
		ENVIO_ANALISE,
		FINALIZACAO,
	}

	private Date dataInicio;
	private Date dataFim;
	private String campoOrdem;
	private SortOrder ordem;
	private Usuario analista;
	private List<StatusProcesso> statusList;
	private Usuario analistaProx;
	private TipoOrdenacao tipoOrdenacao;
	private Area areaPendencia;
	private Area areaPendenciaAnalista;
	private Usuario usuarioRascunhos;
	private Usuario autor;
	private ConsiderarData considerarData;
	private String cpfCnpj;
	private Date dataUltimaAlteracaoAnalistaFim;
	private List<Situacao> situacao;
	private List<Situacao> proximaSituacao;
	private Long processoId;
	private String nomeCliente;
	private String texto;
	private Boolean possuiEmailNaoLido;
	private List<StatusPrazo> statusPrazo;
	private Boolean trazerNaoAssociados;
	private Boolean regrasExecutadas;
	private Date proximoPrazoFim;
	private boolean semAnalista;
	private String numCandidatoInscricao;
	private Aluno aluno;
	private List<Long> regionais;
	private List<Long> campus;
	private List<String> nomesSituacoes;
	private Boolean poloParceiro;
	private Boolean utilizaTermo;
	private List<Etapa> etapas;
	private int pagina;
	private int paginacao;
	private List<String> nomesEtapas;
	private List<String> cursos;
	private String area;
	private boolean dynamicSort;
	private TipoCampo campoDinamico;
	private String localDeOferta;
	private boolean dataFimDoDia = true;
	private FaseEtapa faseEtapa;
	private List<String> periodosIngresso;
	private String matricula;
	private List<Long> desconsiderarTipoProcessoIds;
	private List<StatusProcesso> desconsiderarStatusProcesso;
	private Subperfil subperfil;
	private List<Long> tiposProcessoNot;
	private List<Long> processoIdList;
	private Long logImportacaoId;
	private List<Long> desconsiderarSituacoesIds;
	private boolean processoOriginalTransformado;

	public Boolean getTrazerNaoAssociados() {
		return trazerNaoAssociados;
	}

	public void setTrazerNaoAssociados(Boolean trazerNaoAssociados) {
		this.trazerNaoAssociados = trazerNaoAssociados;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	public List<StatusProcesso> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<StatusProcesso> statusList) {
		this.statusList = statusList;
	}

	public StatusProcesso[] getStatusArray() {
		return statusList != null ? statusList.toArray(new StatusProcesso[statusList.size()]) : null;
	}

	public void setStatusArray(StatusProcesso[] statusAttay) {
		if(statusAttay == null) {
			this.statusList = null;
		} else {
			this.statusList = Arrays.asList(statusAttay);
		}
	}

	public Usuario getAnalistaProx() {
		return analistaProx;
	}

	public void setAnalistaProx(Usuario analistaProx) {
		this.analistaProx = analistaProx;
	}

	public TipoOrdenacao getTipoOrdenacao() {
		return tipoOrdenacao;
	}

	public void setTipoOrdenacao(TipoOrdenacao tipoOrdenacao) {
		this.tipoOrdenacao = tipoOrdenacao;
	}

	public Area getAreaPendencia() {
		return areaPendencia;
	}

	public void setAreaPendencia(Area areaPendencia) {
		this.areaPendencia = areaPendencia;
	}

	public Area getAreaPendenciaAnalista() {
		return areaPendenciaAnalista;
	}

	public void setAreaPendenciaAnalista(Area areaPendenciaAnalista) {
		this.areaPendenciaAnalista = areaPendenciaAnalista;
	}
	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public int getPaginacao() {
		return paginacao;
	}

	public void setPaginacao(int paginacao) {
		this.paginacao = paginacao;
	}

	public Usuario getUsuarioRascunhos() {
		return usuarioRascunhos;
	}

	public void setUsuarioRascunhos(Usuario usuarioRascunhos) {
		this.usuarioRascunhos = usuarioRascunhos;
	}

	public Usuario getAutor() {
		return autor;
	}

	public void setAutor(Usuario autor) {
		this.autor = autor;
	}

	public ConsiderarData getConsiderarData() {
		return considerarData;
	}

	public void setConsiderarData(ConsiderarData considerarData) {
		this.considerarData = considerarData;
	}

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public Date getDataUltimaAlteracaoAnalistaFim() {
		return dataUltimaAlteracaoAnalistaFim;
	}

	public void setDataUltimaAcaoAnalistaFim(Date dataUltimaAlteracaoAnalistaFim) {
		this.dataUltimaAlteracaoAnalistaFim = dataUltimaAlteracaoAnalistaFim;
	}

	public List<Situacao> getSituacao() {
		return situacao;
	}

	public void setSituacao(List<Situacao> situacao) {
		this.situacao = situacao;
	}

	public List<Situacao> getProximaSituacao() {
		return proximaSituacao;
	}

	public void setProximaSituacao(List<Situacao> proximaSituacao) {
		this.proximaSituacao = proximaSituacao;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public ProcessoFiltro clone() {
		try {
			return (ProcessoFiltro) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Boolean getPossuiEmailNaoLido() {
		return possuiEmailNaoLido;
	}

	public void setPossuiEmailNaoLido(Boolean possuiEmailNaoLido) {
		this.possuiEmailNaoLido = possuiEmailNaoLido;
	}

	public List<StatusPrazo> getStatusPrazo() {
		return statusPrazo;
	}

	public void setStatusPrazo(List<StatusPrazo> statusPrazo) {
		this.statusPrazo = statusPrazo;
	}

	public Boolean getRegrasExecutadas() {
		return regrasExecutadas;
	}

	public void setRegrasExecutadas(Boolean regrasExecutadas) {
		this.regrasExecutadas = regrasExecutadas;
	}

	public Date getProximoPrazoFim() {
		return proximoPrazoFim;
	}

	public void setProximoPrazoFim(Date proximoPrazoFim) {
		this.proximoPrazoFim = proximoPrazoFim;
	}

	public boolean isSemAnalista() {
		return semAnalista;
	}

	public void setSemAnalista(boolean semAnalista) {
		this.semAnalista = semAnalista;
	}

	public boolean isProcessoOriginalTransformado() {
		return processoOriginalTransformado;
	}

	public void setProcessoOriginalTransformado(boolean processoOriginalTransformado) {
		this.processoOriginalTransformado = processoOriginalTransformado;
	}

	public void confBySubperfil(Usuario usuario, Subperfil subperfil) {

		Boolean trazerNaoAssociados = false;
		Boolean exibirAssociadosOutros = false;
		String statusFila = null;
		if (subperfil != null) {
			FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
			if (filaConfiguracao != null) {
				trazerNaoAssociados = filaConfiguracao.isExibirNaoAssociados();
				exibirAssociadosOutros = filaConfiguracao.isExibirAssociadosOutros();
				statusFila = filaConfiguracao.getStatus();
			}
		}

		setTrazerNaoAssociados(trazerNaoAssociados);
		setAnalista(exibirAssociadosOutros ? null : usuario);

		List<StatusProcesso> statusList = DummyUtils.stringToList(statusFila, StatusProcesso.class);
		setStatusList(statusList);

		if(exibirAssociadosOutros || trazerNaoAssociados) {
			List<Situacao> situacaoList = new ArrayList<>();
			Set<SubperfilSituacao> situacoes = subperfil.getSituacoes();
			for (SubperfilSituacao ss : situacoes) {
				Situacao situacao = ss.getSituacao();
				Hibernate.initialize(situacao);
				situacaoList.add(situacao);
			}
			setSituacao(situacaoList);
		}
	}

	public String getNumCandidatoInscricao() {
		return numCandidatoInscricao;
	}

	public void setNumCandidatoInscricao(String numCandidatoInscricao) {
		this.numCandidatoInscricao = numCandidatoInscricao;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public List<String> getNomesSituacoes() {
		return nomesSituacoes;
	}

	public void setNomesSituacoes(List<String> nomesSituacoes) {
		this.nomesSituacoes = nomesSituacoes;
	}

	public List<Long> getRegionais() {
		return regionais;
	}

	public void setRegionais(List<Long> regionais) {
		this.regionais = regionais;
	}

	public List<Long> getCampus() {
		return campus;
	}

	public void setCampus(List<Long> campus) {
		this.campus = campus;
	}

	public Boolean getPoloParceiro() {
		return poloParceiro;
	}

	public void setPoloParceiro(Boolean poloParceiro) {
		this.poloParceiro = poloParceiro;
	}

	public Boolean getUtilizaTermo() {
		return utilizaTermo;
	}

	public void setUtilizaTermo(Boolean utilizaTermo) {
		this.utilizaTermo = utilizaTermo;
	}

	public List<Etapa> getEtapas() {
		return etapas;
	}

	public void setEtapas(List<Etapa> etapas) {
		this.etapas = etapas;
	}

	public List<String> getNomesEtapas() {
		return nomesEtapas;
	}

	public void setNomesEtapas(List<String> nomesEtapas) {
		this.nomesEtapas = nomesEtapas;
	}

	public List<String> getCursos() {
		return cursos;
	}

	public void setCursos(List<String> cursos) {
		this.cursos = cursos;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isDynamicSort() {
		return dynamicSort;
	}

	public void setDynamicSort(boolean dynamicSort) {
		this.dynamicSort = dynamicSort;
	}

	public TipoCampo getCampoDinamico() {
		return campoDinamico;
	}

	public void setCampoDinamico(TipoCampo campoDinamico) {
		this.campoDinamico = campoDinamico;
	}

	public void setOrdem(SortOrder ordem) {
		this.ordem = ordem;
	}

	public String getLocalDeOferta() {
		return localDeOferta;
	}

	public void setLocalDeOferta(String localDeOferta) {
		this.localDeOferta = localDeOferta;
	}

	public boolean isDataFimDoDia() {
		return dataFimDoDia;
	}

	public void setDataFimDoDia(boolean dataFimDoDia) {
		this.dataFimDoDia = dataFimDoDia;
	}

	public FaseEtapa getFaseEtapa() {
		return faseEtapa;
	}

	public void setFaseEtapa(FaseEtapa faseEtapa) {
		this.faseEtapa = faseEtapa;
	}

	public List<String> getPeriodosIngresso() {
		return periodosIngresso;
	}

	public void setPeriodosIngresso(List<String> periodosIngresso) {
		this.periodosIngresso = periodosIngresso;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public List<Long> getDesconsiderarTipoProcessoIds() {
		return desconsiderarTipoProcessoIds;
	}

	public void setDesconsiderarTipoProcessoIds(List<Long> desconsiderarTipoProcessoIds) {
		this.desconsiderarTipoProcessoIds = desconsiderarTipoProcessoIds;
	}

	public List<StatusProcesso> getDesconsiderarStatusProcesso() {
		return desconsiderarStatusProcesso;
	}

	public void setDesconsiderarStatusProcesso(List<StatusProcesso> desconsiderarStatusProcesso) {
		this.desconsiderarStatusProcesso = desconsiderarStatusProcesso;
	}

	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}

	public List<Long> getTiposProcessoNot() {
		return tiposProcessoNot;
	}

	public void setTiposProcessoNot(List<Long> tiposProcessoNot) {
		this.tiposProcessoNot = tiposProcessoNot;
	}

	public List<Fetch> getFetch() {
		return fetch;
	}

	public void setFetch(List<Fetch> fetch) {
		this.fetch = fetch;
	}

	public List<Long> getProcessoIdList() {
		return processoIdList;
	}

	public void setProcessoIdList(List<Long> processoIdList) {
		this.processoIdList = processoIdList;
	}

	public Long getLogImportacaoId() {
		return logImportacaoId;
	}

	public void setLogImportacaoId(Long logImportacaoId) {
		this.logImportacaoId = logImportacaoId;
	}

	public List<Long> getDesconsiderarSituacoesIds() {
		return desconsiderarSituacoesIds;
	}

	public void setDesconsiderarSituacoesIds(List<Long> desconsiderarSituacoesIds) {
		this.desconsiderarSituacoesIds = desconsiderarSituacoesIds;
	}

	@Override
	public String toString() {
		return "ProcessoFiltro{" +
				"fetch=" + fetch +
				", dataInicio=" + dataInicio +
				", dataFim=" + dataFim +
				", campoOrdem='" + campoOrdem + '\'' +
				", ordem=" + ordem +
				", analista=" + analista +
				", statusList=" + statusList +
				", tiposProcesso=" + getTiposProcesso() +
				", analistaProx=" + analistaProx +
				", tipoOrdenacao=" + tipoOrdenacao +
				", areaPendencia=" + areaPendencia +
				", areaPendenciaAnalista=" + areaPendenciaAnalista +
				", usuarioRascunhos=" + usuarioRascunhos +
				", autor=" + autor +
				", considerarData=" + considerarData +
				", cpfCnpj='" + cpfCnpj + '\'' +
				", dataUltimaAlteracaoAnalistaFim=" + dataUltimaAlteracaoAnalistaFim +
				", situacao=" + situacao +
				", processoIdStr='" + processoId + '\'' +
				", nomeCliente='" + nomeCliente + '\'' +
				", texto='" + texto + '\'' +
				", camposFiltro=" + getCamposFiltro() +
				", possuiEmailNaoLido=" + possuiEmailNaoLido +
				", statusPrazo=" + statusPrazo +
				", trazerNaoAssociados=" + trazerNaoAssociados +
				", regrasExecutadas=" + regrasExecutadas +
				", proximoPrazoFim=" + proximoPrazoFim +
				", semAnalista=" + semAnalista +
				", numCandidatoInscricao='" + numCandidatoInscricao + '\'' +
				", aluno=" + aluno +
				", regionais=" + regionais +
				", campus=" + campus +
				", nomesSituacoes=" + nomesSituacoes +
				", poloParceiro=" + poloParceiro +
				", utilizaTermo=" + utilizaTermo +
				", etapas=" + etapas +
				", pagina=" + pagina +
				", paginacao=" + paginacao +
				", nomesEtapas=" + nomesEtapas +
				", cursos=" + cursos +
				", area='" + area + '\'' +
				", dynamicSort=" + dynamicSort +
				", campoDinamico=" + campoDinamico +
				", localDeOferta='" + localDeOferta + '\'' +
				", dataFimDoDia=" + dataFimDoDia +
				", faseEtapa=" + faseEtapa +
				", periodosIngresso=" + periodosIngresso +
				", matricula='" + matricula + '\'' +
				", desconsiderarTipoProcessoIds=" + desconsiderarTipoProcessoIds +
				", subperfil=" + subperfil +
				", tiposProcessoNot=" + tiposProcessoNot +
				", processoIdList=" + processoIdList +
				'}';
	}
}
