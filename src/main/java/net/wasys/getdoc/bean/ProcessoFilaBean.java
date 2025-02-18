package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ProcessoVoDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.OrdemDinamicaVO;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro.TipoOrdenacao;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import java.util.*;
import java.util.stream.Collectors;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@SessionScoped
public class ProcessoFilaBean extends AbstractBean {

	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private FilaConfiguracaoService filaConfiguracaoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private LogAtendimentoService logAtendimentoService;

	private ProcessoFiltro filtro;
	private ProcessoVoDataModel dataModel;

	private List<Usuario> analistas;
	private List<TipoProcesso> tiposProcessos;
	private List<Situacao> situacoes;
	private List<Situacao> situacoesConclusao;
	private List<Situacao> situacoesIsencao;
	private List<RegistroValorVO> cursos;
	private List<RegistroValorVO> areas;
	private List<RegistroValorVO> campusList;
	private List<String> periodosIngresso;
	private String curso;
	private String area;
	private String campus;

	private List<ProcessoVO> processosSelecionados;
	private Usuario novoAnalista;
	private Situacao novaSituacao;
	private Situacao novaSituacaoIsencao;
	//private Map<StatusProcesso, Long> mapCountStatus;
	private OrdemDinamicaVO ordemDinamica = new OrdemDinamicaVO();

	private Map<String, List<Long>> colunasPersonalizadas;

	private TipoProcesso tipoProcessoCampos;
	private Boolean permissaoFiltroTP;
	private FilaConfiguracao filaConfiguracao;
	private Boolean isCSCAdmin = false;

	protected void initBean() {

		//FIXME mudar isso pro UtilBean, HomeBean ou outra tela mais leve
		String subperfilIdStr = Faces.getRequestParameter("subperfil");
		if(StringUtils.isNotBlank(subperfilIdStr)) {
			usuarioService.trocarSubperfil(getUsuarioLogado(), new Long(subperfilIdStr));
		}

		UsuarioFiltro f = new UsuarioFiltro();
		f.setRole(RoleGD.GD_ANALISTA);
		f.setStatus(StatusUsuario.ATIVO);
		Usuario usuario = getUsuarioLogado();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null && Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID).contains(subperfilAtivo.getId())){
			f.setSubperfisIds(Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID));
		}

		analistas = usuarioService.findByFiltroToSelect(f);

		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);

		carregarProximasSituacoesIsencao(usuario, subperfilAtivo);

		tipoProcessoCampos = null;
		permissaoFiltroTP = true;

		situacoes = situacaoService.findAtivasToSelect(null);
		Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
		situacoesConclusao = situacaoService.findAtivasToSelect(StatusProcesso.CONCLUIDO);

		if(mostrarFiltroCELAC()) {
			carregarCELAC();
		}

		if (getFlashAttribute("relatorioAcompanhamentoVO") != null) {
			filtro = (ProcessoFiltro) getFlashAttribute("processoFiltro");
		}
		else if (filtro == null) {
			filtro = new ProcessoFiltro();
			filtro(usuario);
		}
		if(usuario.isComercialRole()) {
			filtro.setAutor(usuario);
		}
		filtro.setTipoOrdenacao(TipoOrdenacao.PENDENCIAS_ANALISTA);

		colunasPersonalizadas = processoService.montarColunaPersonalizada(usuario);

		dataModel = new ProcessoVoDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(processoService);

		buscar();

		filaConfiguracao = filaConfiguracaoService.carregarConfiguracoesFila(usuario);
	}

	private void carregarProximasSituacoesIsencao(Usuario usuario, Subperfil subperfilAtivo) {
		boolean adminRole = usuario.isAdminRole();
		situacoesIsencao = new ArrayList<>();
		if(adminRole) {
			isCSCAdmin = true;
			SituacaoFiltro situacaoFiltro = new SituacaoFiltro();
			situacaoFiltro.setNomeContem("N2");
			situacaoFiltro.setAtiva(true);
			situacoesIsencao = situacaoService.findByFiltro(situacaoFiltro, null, null);
		}else if (subperfilAtivo != null) {
			situacoesIsencao = new ArrayList<>();
			Long subperfilAtivoId = subperfilAtivo.getId();
			if (Subperfil.CSC_ADM_ID.equals(subperfilAtivoId)) {
				isCSCAdmin = true;
				Set<SubperfilSituacao> subperfilAtivoSituacoes = subperfilAtivo.getSituacoes();
				for (SubperfilSituacao subperfilAtivoSituacao : subperfilAtivoSituacoes) {
					Situacao situacao = subperfilAtivoSituacao.getSituacao();
					Long situacaoId = situacao.getId();
					situacao = situacaoService.get(situacaoId);
					String situacaoNome = situacao.getNome();
					if (StringUtils.contains(situacaoNome, "N2")) {
						situacoesIsencao.add(situacao);
					}
				}
			}
		}
		Collections.sort(situacoesIsencao);
	}

	private void filtro(Usuario usuario) {

		if(usuario.isAdminRole() ) {

			List<StatusProcesso> statusList = Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE, StatusProcesso.EM_ACOMPANHAMENTO, StatusProcesso.ENCAMINHADO);
			filtro.setStatusList(statusList);

			filtro.setUsuarioRascunhos(usuario);
		}
		else if(usuario.isAnalistaRole() || usuario.isGestorRole()) {

			Subperfil subperfil = usuario.getSubperfilAtivo();
			if(subperfil == null && usuario.isAnalistaRole()) {
				//seta um filtro que nunca vá trazer nada
				filtro.setCamposFiltro(Arrays.asList(new CampoDinamicoVO(CampoMap.CampoEnum.REGIONAL, "-1")));
				addMessageError("analistaSubperfilNotFound.error");
				return;
			}

			List<Long> regionalLong = usuarioService.findRegionais(usuario);
			List<Long> campusLong = usuarioService.findCampus(usuario);
			filtro.setRegionais(regionalLong);
			filtro.setCampus(campusLong);

			if(usuario.isAnalistaRole()) {
				filtro.confBySubperfil(usuario, subperfil);
			}else{
				List<StatusProcesso> statusList = Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE, StatusProcesso.EM_ACOMPANHAMENTO, StatusProcesso.ENCAMINHADO);
				filtro.setStatusList(statusList);
			}

			//filtro.setUsuarioRascunhos(usuario);
		}
		else if(usuario.isAreaRole()) {

			Area area = usuario.getArea();
			if(area == null) {
				redirect("/");
			}

			filtro.setAreaPendencia(area);

			filtro.setUsuarioRascunhos(usuario);
		}
		else if(usuario.isRequisitanteRole()) {

			List<StatusProcesso> statusList = StatusProcesso.getPendenciaRequisitante();
			statusList = new ArrayList<>(statusList);

			filtro.setStatusList(statusList);

			filtro.setAutor(usuario);
		}
	}

	public void buscar() {
		Usuario usuario = getUsuarioLogado();
		//mapCountStatus = processoService.getStatusAnalise(usuario, filtro);
	}

	public void limpar() {
		filtro = null;
		initBean();
	}

	public void proximaRequisicao() {

		Usuario usuario = getUsuarioLogado();
		boolean distribuirDemandaAutomaticamente = usuario.getDistribuirDemandaAutomaticamente();

		try {

			if(distribuirDemandaAutomaticamente) {
				boolean impedir = logAtendimentoService.verificarTempoPausa(usuario);
				if(impedir){
					addMessageWarn("tempoMinimoPausa.warn");
					return;
				}
			}

			long inicio = System.currentTimeMillis();
			Processo processo = processoService.proximoProcessoComLock(usuario);
			systraceThread("Tempo total proximaRequisicao: " + (System.currentTimeMillis() - inicio) + "ms. Analista: " + usuario + ". Processo: " + processo);

			if(processo == null) {
				addMessage("semProximosProcessos.sucesso");
				if(distribuirDemandaAutomaticamente) {
					logAtendimentoService.criarDisponivel(usuario.getId());
					PrimeFaces.current().executeScript("location.reload();");
				}
				return;
			}

			if(distribuirDemandaAutomaticamente) {
				logAtendimentoService.encerrarUltimoLog(usuario);
				logAtendimentoService.criarEmAnalise(usuario.getId());
			}

			Long processoId = processo.getId();
			redirect("/requisicoes/fila/edit/" + processoId);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void trocarAnalistas() {

		try {
			List<Long> processosIds = new ArrayList<>();

			if (novoAnalista == null) {
				addMessageWarn("novoAnalista.required.error");
				return;
			}

			for (ProcessoVO vo : processosSelecionados) {

				Processo processo = vo.getProcesso();
				Long processoId = processo.getId();
				processosIds.add(processoId);
			}

			Usuario usuario = getUsuarioLogado();
			processoService.trocarAnalistas(processosIds, novoAnalista, usuario);

			String nome = novoAnalista.getNome();
			addMessage("alteracaoAnalistas.sucesso", processosIds.size(), nome);
			novoAnalista = null;
			processosSelecionados = null;
			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void concluirEmMassa() {

		try {
			List<Processo> processos = new ArrayList<>();
			for (ProcessoVO vo : processosSelecionados) {

				Processo processo = vo.getProcesso();
				processos.add(processo);
			}

			Usuario usuario = getUsuarioLogado();
			// TODO CAMPO DE OBSERVACAO
			processoService.concluirEmMassa(processos, usuario, novaSituacao, "");

			setRequestAttribute("fecharModal", true);
			addMessage("conclusaoProcessos.sucesso", processosSelecionados.size());

			novaSituacao = null;
			processosSelecionados = null;

			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void alterarSituacoesEmMassaIsencao() {

		try {
			Usuario usuario = getUsuarioLogado();
			List<Situacao> proximaSituacao = filtro.getProximaSituacao();
			if(proximaSituacao != null && !proximaSituacao.isEmpty()) {
				List<Processo> processos = new ArrayList<>();
				for (ProcessoVO vo : processosSelecionados) {

					Processo processo = vo.getProcesso();
					processos.add(processo);
				}

				processoService.alterarSituacaoEmMassaIsencao(processos, proximaSituacao, usuario, "");

				setRequestAttribute("fecharModal", true);
				addMessage("conclusaoProcessos.sucesso", processosSelecionados.size());
			} else {
				addMessageWarn("proximasSituacoes.required.error");
			}
			redirect("/requisicoes/fila/");


			processosSelecionados = null;
			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public boolean mostrarFiltro() {
		Usuario usuarioLogado = getUsuarioLogado();
		return usuarioLogado.isAdminRole() || usuarioLogado.isGestorRole() || usuarioLogado.isAnalistaRole();
	}

	public boolean mostrarFiltroAnalista() {
		Usuario usuarioLogado = getUsuarioLogado();
		boolean mostrar = !usuarioLogado.isAnalistaRole();
		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
		if(subperfilAtivo != null && Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID).contains(subperfilAtivo.getId())){
			mostrar = true;
		}
		return mostrar;
	}

	public boolean podeTrocarAnalistas() {

		Usuario usuario = getUsuarioLogado();
		boolean adminRole = usuario.isAdminRole();
		boolean gestorRole = usuario.isGestorRole();

		Boolean analistaCSC = false;

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null){
			Long subperfilAtivoId = subperfilAtivo.getId();
			if(Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID).contains(subperfilAtivoId)){
				analistaCSC = true;
			}
		}

		return adminRole || gestorRole || analistaCSC;
	}

	public boolean podeVerificarProxima() {

		Usuario usuarioLogado = getUsuarioLogado();
		if(!usuarioLogado.isAnalistaRole()) {
			return false;
		}

		Boolean valor = false;
		if (filaConfiguracao != null) {
			valor = filaConfiguracao.isVerificarProximaRequisicao();
		}

		return valor;
	}

	public List<Campo> findCamposColunaPersonalizada(List<Long> tipoCampoIds, Long processoId) {
		return processoService.findCamposColunaPersonalizada(tipoCampoIds, processoId);
	}

	public String getValorBaseInternaLabel(Long baseInternaId, String chaveUnicidade) {
		return processoService.getValorBaseInternaLabel(baseInternaId, chaveUnicidade);
	}

	public boolean mostrarFiltroCELAC(){
		Usuario usuarioLogado = getUsuarioLogado();
		Boolean adminRole = usuarioLogado.isAdminRole();
		Boolean gestorRole = usuarioLogado.isGestorRole();
		Boolean analistaCSC = false;

		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
		if(subperfilAtivo != null){
			Long subperfilAtivoId = subperfilAtivo.getId();
			if(Subperfil.SUBPERFIS_CSC_IDS.contains(subperfilAtivoId)){
				analistaCSC = true;
			}
		}

		return adminRole || gestorRole || analistaCSC;
	}

	public void carregarCELAC(){
		cursos = new ArrayList<>();
		List<String> cursoRemove = new ArrayList<>();
		List<RegistroValorVO> allCursos = baseRegistroService.findByBaseInterna(BaseInterna.CURSO_ID, true);
		for(RegistroValorVO curso : allCursos){
			String label = curso.getLabel().trim();
			if(!cursoRemove.contains(label)){
				cursoRemove.add(label);
				cursos.add(curso);
			}
		}

		areas = new ArrayList<>();
		List<String> areaRemove = new ArrayList<>();
		List<RegistroValorVO> allAreas = baseRegistroService.findByBaseInterna(BaseInterna.AREA_ID, true);
		for(RegistroValorVO area : allAreas){
			String label = area.getLabel().trim();
			if(!areaRemove.contains(label)){
				areaRemove.add(label);
				areas.add(area);
			}
		}

		campusList = new ArrayList<>();
		List<String> campusRemove = new ArrayList<>();
		List<RegistroValorVO> allCampus = baseRegistroService.findByBaseInterna(BaseInterna.CAMPUS_ID, true);
		for(RegistroValorVO area : allCampus){
			String label = area.getLabel().trim();
			if(!campusRemove.contains(label)){
				campusRemove.add(label);
				campusList.add(area);
			}
		}

		periodosIngresso = new ArrayList<>();
		List<RegistroValorVO> registroValorVOS = baseRegistroService.findByBaseInterna(BaseInterna.PERIODOS_INGRESSO_ID, true);
		for(RegistroValorVO periodoIngresso : registroValorVOS){
			String label = periodoIngresso.getLabel().trim();
			periodosIngresso.add(label);
		}
	}

	public List<RegistroValorVO> getAreas() {
		return areas;
	}

	public List<RegistroValorVO> getCursos() {
		return cursos;
	}

	public List<RegistroValorVO> getCampusList() {
		return campusList;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		if(StringUtils.isNotBlank(curso)) {
			List<String> chavesUnicidades = baseRegistroService.findChaveUnicidadeByPesquisa(BaseInterna.CURSO_ID, curso);
			filtro.setCursos(chavesUnicidades);
		}else{
			filtro.setCursos(null);
		}
		this.curso = curso;
	}


	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		if(StringUtils.isNotBlank(area)) {
			filtro.setArea(area);
		}else{
			filtro.setArea(null);
		}
		this.area = area;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		if(StringUtils.isNotBlank(campus)) {
			List<String> chavesUnicidades = baseRegistroService.findChaveUnicidadeByPesquisa(BaseInterna.CAMPUS_ID, campus);
			List<Long> chavesUnicidadesLong = new ArrayList<>();
			for(String chavesUnicidade : chavesUnicidades){
				String s = DummyUtils.limparCharsChaveUnicidade(chavesUnicidade);
				chavesUnicidadesLong.add(new Long(s));
			}
			filtro.setCampus(chavesUnicidadesLong);
		}else{
			filtro.setCampus(null);
		}
		this.campus = campus;
	}

	public void confirmarFiltros() {
		try {
			filtro.configuraTipoProcessoCampos(tipoProcessoCampos, baseRegistroService);

			setRequestAttribute("fecharModal", true);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void bloqueiaTiposProcessos(ValueChangeEvent event) {

		tipoProcessoCampos = (TipoProcesso) event.getNewValue();

		if (tipoProcessoCampos != null) {
			permissaoFiltroTP = false;
			filtro.setTiposProcesso(null);
		}
		else {
			permissaoFiltroTP = true;
			filtro.setCamposFiltro(null);
		}
	}

	public ProcessoVoDataModel getDataModel() {
		return dataModel;
	}

	public ProcessoFiltro getFiltro() {
		return filtro;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<ProcessoVO> getProcessosSelecionados() {
		return processosSelecionados;
	}

	public void setProcessosSelecionados(List<ProcessoVO> processosSelecionados) {
		this.processosSelecionados = processosSelecionados;
	}

	public Usuario getNovoAnalista() {
		return novoAnalista;
	}

	public void setNovoAnalista(Usuario novoAnalista) {
		this.novoAnalista = novoAnalista;
	}

	//public Map<StatusProcesso, Long> getMapCountStatus() {
	//	return mapCountStatus;
	//}

	public Situacao getNovaSituacao() {
		return novaSituacao;
	}

	public void setNovaSituacao(Situacao novaSituacao) {
		this.novaSituacao = novaSituacao;
	}

	public Situacao getNovaSituacaoIsencao() {
		return novaSituacaoIsencao;
	}

	public void setNovaSituacaoIsencao(Situacao novaSituacaoIsencao) {
		this.novaSituacaoIsencao = novaSituacaoIsencao;
	}

	public List<Situacao> getSituacoesIsencao() {
		return situacoesIsencao;
	}

	public void setSituacoesIsencao(List<Situacao> situacoesIsencao) {
		this.situacoesIsencao = situacoesIsencao;
	}

	public List<Situacao> getSituacoesConclusao() {
		return situacoesConclusao;
	}

	public void setSituacoesConclusao(List<Situacao> situacoesConclusao) {
		this.situacoesConclusao = situacoesConclusao;
	}

	public TipoProcesso getTipoProcessoCampos() {
		return tipoProcessoCampos;
	}

	public void setTipoProcessoCampos(TipoProcesso tipoProcessoCampos) {
		this.tipoProcessoCampos = tipoProcessoCampos;
	}

	public Boolean getPermissaoFiltroTP() {
		return permissaoFiltroTP;
	}

	public void setPermissaoFiltroTP(Boolean permissaoFiltroTP) {
		this.permissaoFiltroTP = permissaoFiltroTP;
	}

	public Map<String, List<Long>> getColunasPersonalizadas() {
		return colunasPersonalizadas;
	}

	public void setColunasPersonalizadas(Map<String, List<Long>> colunasPersonalizadas) {
		this.colunasPersonalizadas = colunasPersonalizadas;
	}

	public FilaConfiguracao getFilaConfiguracao() {
		return filaConfiguracao;
	}

	public void atualizaDynamicSort(boolean dynamicSort){
		filtro.setDynamicSort(dynamicSort);
	}

	public void montaOrdemDinamica() {
		String nomeColuna = ordemDinamica.getKey();
		SortOrder ordem = ordemDinamica.getOrder();

		Long tipoCampoId = colunasPersonalizadas.get(nomeColuna).get(0);
		TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);

		filtro.setOrdem(ordem);
		filtro.setDynamicSort(true);
		filtro.setCampoDinamico(tipoCampo);

		buscar();
	}

	public OrdemDinamicaVO getOrdemDinamica() { return ordemDinamica; }

	public void setOrdemDinamica(OrdemDinamicaVO ordemDinamica) { this.ordemDinamica = ordemDinamica; }

	public List<String> getPeriodosIngresso() {
		return periodosIngresso;
	}

	public void setPeriodosIngresso(List<String> periodosIngresso) {
		this.periodosIngresso = periodosIngresso;
	}

	public Boolean getCSCAdmin() {
		return isCSCAdmin;
	}
}
