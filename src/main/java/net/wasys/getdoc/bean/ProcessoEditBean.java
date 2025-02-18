package net.wasys.getdoc.bean;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.wasys.getdoc.bean.vo.ProcessoEditAutorizacao;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.getdoc.http.UploadSessionHandler;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.faces.FacesUtil;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import net.wasys.util.other.ReflectionBeanComparator;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.omnifaces.util.Faces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static net.wasys.util.DummyUtils.*;

@ManagedBean
@ViewScoped
public class ProcessoEditBean extends AbstractBean {

	@Autowired private ProcessoService processoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ImagemService imagemService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private AreaService areaService;
	@Autowired private SolicitacaoService solicitacaoService;
	@Autowired private TipoEvidenciaService tipoEvidenciaService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private IrregularidadeService irregularidadeService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private SubareaService subareaService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private EmailRecebidoService emailRecebidoService;
	@Autowired private ProcessoPendenciaService processoPendenciaService;
	@Autowired private BloqueioProcessoService bloqueioProcessoService;
	@Autowired private EmailRecebidoAnexoService emailRecebidoAnexoService;
	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private CampoOcrService campoOcrService;
	@Autowired private LogOcrService logOcrService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private ProcessoAjudaService processoAjudaService;
	@Autowired private TextoPadraoService textoPadraoService;
	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private ProcessoRegraLogService processoRegraLogService;
	@Autowired private ParametroService parametroService;
	@Autowired private RegraService regraService;
	@Autowired private CampoService campoService;
	@Autowired private TipificacaoVisionApiService tipificacaoVisionApiService;
	@Autowired private CampanhaService campanhaService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private CalculoRendaService calculoRendaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private LogAtendimentoService logAtendimentoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private TipoCampoService tipoCampoService;

	private Long id;
	private Integer chave;
	private Processo processo;
    private Irregularidade irregularidade;
    private String observacaoIrregularidade;
	private List<DocumentoVO> documentos;
	private List<DocumentoVO> documentosExcluidos;
	private Map<CampoGrupo, List<Campo>> campos;
	private Map<Long, List<DocumentoVO>> documentosProcessoId = new HashMap<>();
	private Map<Long, List<Irregularidade>> irregularidadesDocumentoIdMap = new HashMap<>();
	private Documento documento;
	private String documentosMapJson;
	private String documentosJson;
	private boolean digitalizarTwain;
	private int indiceImagem1;
	private int indiceImagem2;
	private List<LogVO> logs;
	private List<LogVO> logsAnexo;
	private boolean editando;
	private boolean editandoVisualizacao;
	private SolicitacaoVO solicitacaoVO;
	private EvidenciaVO evidenciaVO;
	private DigitalizacaoVO digitalizacaoVO;
	private EmailVO emailVO = new EmailVO();
	private List<Area> areas;
	private List<TipoEvidencia> tiposEvidencias;
	private List<SolicitacaoVO> solicitacoes;
	private List<SolicitacaoVO> solicitacoesPendentes;
	private List<EmailVO> emailsVOs;
	private EmailRecebido emailRecebido;
	private String rootPath = "/requisicoes/";
	private String horasRestantes;
	private String horasRestantesSituacao;
	private List<Irregularidade> irregularidades = new ArrayList<>();
	private List<Irregularidade> irregularidadesPorDocumento = new ArrayList<>();
	private List<Situacao> situacoes;
	private List<Subarea> subareas;
	private List<Usuario> analistas;
	private Usuario novoAnalista;
	private TipoProcesso novoTipoProcesso;
	private List<TipoProcesso> tiposProcessos;
	private Long novoDocumentoId;
	private ProcessoLog processoLog;
	private Situacao novaSituacao;
	private Integer processosAbertos;
	private Integer processosFechados;
	private List<ProcessoVO> processosMesmoCpjCnpj;
	private ProcessoPendenciaVO processoPendencia;
	private LogVO logVO;
	private ItemPendente itemPendente;
	private String novoCodigoAr;
	private RelatorioGeral relatorioGeral;
	private Long imagemId;
	private boolean mostrarAlertaRaschunho = false;
	private List<CampoOcrVO> camposOcr;
	private ComparacaoFacesVO comparacaoFacesVO;
	private ProcessoEditAutorizacao autorizacao;
	private LogOcr logOcr;
	private AnexoFiltro anexoFiltro = new AnexoFiltro();
	private boolean estaTipificando = false;
	private Integer versao;
	private List<TextoPadrao> textosPadroes;
	private List<ProcessoAjuda> pendenciasCkeckList;
	private boolean pendenciasCheckListNaoCompleto;
	private Map<ProcessoAjuda, List<ProcessoAjuda>> ajudasMarcadas = new LinkedHashMap<>();
	private List<String> fullTexts;
	private List<ProcessoRegra> regrasErro;
	private List<ProcessoRegra> regrasPendente;
	private List<ProcessoRegra> regrasVermelho;
	private List<ProcessoRegra> regrasAmarelo;
	private List<ProcessoRegra> regrasVerde;
	private List<ProcessoRegraLog> processoRegraLogs;
	private Regra regra;
	private List<ResultadoConsultaVO> resultadosConsultas;
	private List<Campo> camposSituacao; //TODO trocar isso aqui por um map
	private boolean carregouInitCampos;
	private CampoGrupo grupoExcluir;
	private String hashImagem;
	private String dataDigitalizacao;
	private Map<Long, List<Long>> documentosEquivalentes;
	private Map<Long, List<Long>> documentosEquivalidos;
	private String anexoPath;
	private String emailNotificacao = "";
	private AtomicLong sequenciaIdsCampos = new AtomicLong();
	private List<TipoProcesso> modalidades;
	private TipoProcesso modalidade;
	private List<ModeloDocumento> modeloDocumentos;
	private boolean possuiProcessoIsencao;
	private boolean revisaoAntiga;
	private String linkAluno;
	private boolean isPastaVermelha;
	private List<Long> idDocumentos;
	private RendimentoComposicaoFamiliarVO rendimentoComposicaoFamiliar;
	private List<SalarioMinimoVO> salariosMinimos;
	private SalarioMinimoVO salarioMinimoSelecionado;

	private List<ModeloDocumento> modelosDocumentos;
	private List<ModeloDocumentoVO> modelosDocumentoVO;
	private String modelosDocumentosMapJson;
	private ModeloDocumento modeloDocumento;
	private boolean isPastaAmarela;
	private Map<Long, String> irregularidadesPastaAmarelaMap = new HashMap<>();
	private List<Long> idTipoDocumentos;
	private ManutencaoDocumentoVO manutencaoDocumentoVO = new ManutencaoDocumentoVO();
	private boolean alteracaoObrigatoriedade;
	private boolean processoTemRegras;
	private boolean temDocumentos;
	private boolean temAlertarDocumentos;
	private boolean podeReceberDocumentosSisProuniNoCalendario;
	private Boolean opcoesIes;
	private boolean existMateriaIsenta;


	protected void initBean() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long usuarioLogadoId = usuarioLogado.getId();
		usuarioLogado = usuarioService.get(usuarioLogadoId);
		if(processo == null && this.id != null) {
			getProcessoFromService();
			if(processo != null) {
				StatusProcesso status = processo.getStatus();
				if(usuarioLogado.isAnalistaRole()) {
					boolean distribuirDemandaAutomaticamente = usuarioLogado.getDistribuirDemandaAutomaticamente();
					Long processoAtualId = usuarioLogado.getProcessoAtualId();
					if (Arrays.asList(StatusProcesso.CONCLUIDO, StatusProcesso.PENDENTE).contains(status) && distribuirDemandaAutomaticamente) {
						if (this.id.equals(processoAtualId)) {
							usuarioService.atualizarProcessoAtualId(usuarioLogado, null, true);
							proximaRequisicao();
							return;
						}
					}

					boolean ordemAtividadesFixa = usuarioLogado.getOrdemAtividadesFixa();
					if(!this.id.equals(processoAtualId) && !ordemAtividadesFixa) {
						logAtendimentoService.encerrarUltimoLog(usuarioLogado);
						usuarioLogado.setProcessoAtualId(this.id);
						logAtendimentoService.criarEmAnalise(usuarioLogadoId);
					}
				}
				processoLogService.marcarComoLido(processo.getId(), usuarioLogado);
				processoLogService.registrarPrimeiraEntrada(processo, usuarioLogado);
				Usuario autor = processo.getAutor();
				if (StatusProcesso.RASCUNHO.equals(status) && usuarioLogado.equals(autor)) {
					mostrarAlertaRaschunho = true;
				}
			}
		}

		HttpServletRequest request = getRequest();
		String requestURI = request.getRequestURI();

		if(carregouInitCampos) {
			carregouInitCampos = false;
		}

		if(requestURI.contains("fila")) {
			rootPath = "/requisicoes/fila/";
		}

		String edit = FacesUtil.getParam("edit");
		if(edit != null) {
			setEditando(true);
			initCampos(true);
			carregouInitCampos = true;
		}

		if(processo == null) {
			redirect(rootPath);
			return;
		}

		autorizacao = new ProcessoEditAutorizacao();
		autorizacao.setBean(this);
		autorizacao.setProcesso(processo);
		autorizacao.setProcessoService(processoService);
		autorizacao.setUsuarioLogado(usuarioLogado);
		autorizacao.setSubperfilService(subperfilService);
		autorizacao.setSituacaoService(situacaoService);
		autorizacao.setTipificacaoVisionApiService(tipificacaoVisionApiService);
		autorizacao.setCampoGrupoService(campoGrupoService);

		//Chave para identificar as operações dessa página, para o caso do usuário abrir mais de uma ao mesmo tempo. Utilizado na digitalização.
		this.chave = (int) (Math.random() * 1000000000);

		initDadosProcesso();

		areas = areaService.findAtivas();

		//FIXME: esse método está carretando campos em lazy
		tiposEvidencias = tipoEvidenciaService.findAtivas();
		modeloDocumentos = modeloDocumentoService.findAtivos();

		carregarTextoPadroes();

		String documentoIdStr = Faces.getRequestParameter("documentoId");
		if(StringUtils.isNotBlank(documentoIdStr)) {
			Long documentoId = new Long(documentoIdStr);
			this.documento = documentoService.get(documentoId);
			carregarDocumentos(documentoId);

			String reprocessarocr = Faces.getRequestParameter("reprocessarocr");
			if("true".equals(reprocessarocr)) {
				DocumentoVO documentovo2 = this.documentos.get(0);
				Documento documento2 = documentoService.get(documentovo2.getId());
				documentoService.agendarOcr(documento2, getUsuarioLogado());
				redirect(rootPath + "edit/" + processo.getId() + "?documentoId=" + documento.getId());
			}
		}

		carregarModelosDocumentos();
		modelosDocumentos = modeloDocumentoService.findAtivos();

		carregaLinkAluno();

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		if(TipoProcesso.ISENCAO_DISCIPLINAS.equals(tipoProcessoId)){
			 existMateriaIsenta = autorizacao.validarGrupoMateriasIsentas(processo);
			if(!existMateriaIsenta){
				addMessageError("adicionarGrupoMateriaIsenta.preencherCampos.error");
			}
		}
	}

	public void carregaLinkAluno() {
		Long alunoProcessoId = processo.getAlunoProcessoId();
		if(alunoProcessoId != null) {
			ConfiguracoesWsGetDocAlunoVO cVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
			if(cVO != null) {
				String linkExtranet = cVO.getLinkExtranet();
				if(StringUtils.isNotBlank(linkExtranet)) {

					String mode = DummyUtils.getMode();
					if("dev".equals(mode)) {
						linkExtranet = "http://localhost:8080/getdoc_aluno";
					}

					linkExtranet += "/requisicoes/edit/" + alunoProcessoId;
					this.linkAluno = linkExtranet;
				}
			}
		}
	}

	public void iniciaRegras() {

		Usuario usuarioLogado = getUsuarioLogado();
		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();

		Long processoId = processo.getId();
		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setProcessoId(processoId);
		filtro.setSubperfilPermitido(subperfilAtivo);
		List<ProcessoRegra> processoRegras = processoRegraService.findLasts(filtro);
		Collections.sort(processoRegras, new SuperBeanComparator<>("regra.nome"));

		regrasErro = new ArrayList<>();
		regrasPendente = new ArrayList<>();
		regrasVermelho = new ArrayList<>();
		regrasAmarelo = new ArrayList<>();
		regrasVerde = new ArrayList<>();

		for (ProcessoRegra processoRegra : processoRegras) {
			StatusProcessoRegra status = processoRegra.getStatus();
			if(StatusProcessoRegra.ERRO.equals(status)) {
				regrasErro.add(processoRegra);
			}
			else if(StatusProcessoRegra.PENDENTE.equals(status)) {
				regrasPendente.add(processoRegra);
			}
			else if(StatusProcessoRegra.REPROCESSANDO.equals(status)) {
				regrasPendente.add(processoRegra);
			}
			else {
				FarolRegra farol = processoRegra.getFarol();
				if(FarolRegra.VERMELHO.equals(farol)) {
					regrasVermelho.add(processoRegra);
				}
				else if(FarolRegra.AMARELO.equals(farol)) {
					regrasAmarelo.add(processoRegra);
				}
				else if(FarolRegra.VERDE.equals(farol)) {
					regrasVerde.add(processoRegra);
				}
			}
		}

		carregarResultadoConsultas(processoRegras);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		RegraFiltro filtro2 = new RegraFiltro();
		filtro2.setTipoProcessoId(tipoProcessoId);
		filtro2.setAtiva(true);
		filtro2.setVigencia(new Date());
		processoTemRegras = regraService.existsByFiltro(filtro2);
	}

	private void carregarResultadoConsultas(List<ProcessoRegra> processoRegras) {
		resultadosConsultas = processoRegraService.carregarConsultas(processoRegras);
	}

	public void carregarOutrosDadosProcesso(){
		relatorioGeral = relatorioGeralService.getByProcessoAndCriaCasoNaoExista(processo.getId());
	}

	public boolean temRegrasErro() {
		return regrasErro != null && !regrasErro.isEmpty();
	}

	public void reprocessarRegras() {

		try {
			Usuario usuario = getUsuarioLogado();
			processoRegraService.reprocessarTodasRegras(processo, usuario);

			iniciaRegras();
			setRequestAttribute("fecharModal", true);
			addMessage("regrasReprocessadas.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void reprocessarRegrasComErro() {

		try {
			Usuario usuarioLogado = getUsuarioLogado();

			processoRegraService.reprocessarRegrasComErro(processo, usuarioLogado);

			iniciaRegras();
			setRequestAttribute("fecharModal", true);
			addMessage("regrasReprocessadas.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void reprocessarRegra() {

		try {
			Usuario usuarioLogado = getUsuarioLogado();

			processoRegraService.reprocessarRegra(processo, regra, usuarioLogado);

			iniciaRegras();
			initDadosProcesso();
			setRequestAttribute("fecharModal", true);
			addMessage("regraReprocessada.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void iniciaAjudas() {
		ajudasMarcadas = new LinkedHashMap<>();
		Long processoId = processo.getId();
		List<ProcessoAjuda> pas = processoAjudaService.findByProcesso(processoId);
		for (ProcessoAjuda processoAjuda : pas) {
			boolean ativo = processoAjuda.isAtivo();
			if (!ativo) {
				continue;
			}
			List<ProcessoAjuda> list = new ArrayList<>();
			ajudasMarcadas.put(processoAjuda, list);
			carregaCaminhoAjudaRecursivo(processoAjuda, list);
		}
		pendenciasCkeckList = processoAjudaService.getPendencias(id);
	}

	public void carregarProcessoRegraLogs(ProcessoRegra processoRegra) {
		Long processoRegraId = processoRegra.getId();
		processoRegraLogs = processoRegraLogService.findByProcessoRegra(processoRegraId);
	}

	public void carregarRegra(ProcessoRegra processoRegra) {
		Regra r = processoRegra.getRegra();
		regra = regraService.get(r.getId());
	}

	public void carregaCaminhoAjudaRecursivo(ProcessoAjuda pa, List<ProcessoAjuda> list) {

		if (pa == null) {
			return;
		}
		list.add(pa);
		List<ProcessoAjuda> inferiores = pa.getInferiores();
		for (ProcessoAjuda inferior : inferiores) {

			boolean marcado = inferior.isMarcado();
			boolean marcado2 = pa.isMarcado();
			ProcessoAjuda proximo = pa.getProximo();
			Long proximoId = proximo != null ? proximo.getId() : null;
			Long inferiorId = inferior.getId();

			if (marcado) {
				carregaCaminhoAjudaRecursivo(inferior, list);
			}
			else if (marcado2 && proximo != null && proximoId.equals(inferiorId)) {
				carregaCaminhoAjudaRecursivo(inferior, list);
			}
		}
	}

	public void ajudaRespostaAnular(ProcessoAjuda ajuda) {
		processoAjudaService.anularResposta(ajuda);

		iniciaAjudas();
	}

	public void ajudaResposta(ProcessoAjuda ajudaOrig, Resposta resposta) {

		Usuario usuarioLogado = getUsuarioLogado();

		Long ajudaId = ajudaOrig.getId();
		ProcessoAjuda ajuda = processoAjudaService.get(ajudaId);
		ProcessoAjuda pai = ajuda;
		while(pai.getSuperior() != null) {
			pai = pai.getSuperior();
		}
		List<ProcessoAjuda> list = ajudasMarcadas.get(pai);
		List<ProcessoAjuda> inferiores = ajuda.getInferiores();

		try {
			ProcessoAjuda proximo = null;
			for (ProcessoAjuda pj : inferiores) {
				Resposta resposta2 = pj.getResposta();
				if (resposta2.equals(resposta)) {
					proximo = pj;
					break;
				}
			}

			processoAjudaService.salvarResposta(ajuda, proximo, usuarioLogado);
			ajudaOrig.setMarcado(true);
			if(proximo != null) {
				list.add(proximo);
				ajudaOrig.setProximo(proximo);
			}

			iniciaAjudas();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void limparCalculadoraRenda() {

		this.rendimentoComposicaoFamiliar = null;
		this.salarioMinimoSelecionado = null;

		atualizarRendimentoMembrosFamiliares();
	}

	public void inicializarCalculadoraRenda() {

		this.salariosMinimos = Collections.emptyList();

		List<RegistroValorVO> valoresBaseInternaSalarioMinimo = baseRegistroService.findByBaseInterna(BaseInterna.SALARIO_MINIMO_ID);
		if (valoresBaseInternaSalarioMinimo != null) {
			this.salariosMinimos = calculoRendaService.mapSalariosMinimos(valoresBaseInternaSalarioMinimo);
		}

		this.rendimentoComposicaoFamiliar = calculoRendaService.findUltimoCalculo(processo.getId());

		if (rendimentoComposicaoFamiliar != null) {

			String vigenciaSalarioMinimo = rendimentoComposicaoFamiliar.getVigenciaSalarioMinimo();
			if (CollectionUtils.isNotEmpty(salariosMinimos) && StringUtils.isNotBlank(vigenciaSalarioMinimo)) {

				for (SalarioMinimoVO salarioMinimo : salariosMinimos) {

					if (salarioMinimo.getVigencia().equals(vigenciaSalarioMinimo)) {
						salarioMinimoSelecionado = salarioMinimo;
						break;
					}
				}
			}
		}

		atualizarRendimentoMembrosFamiliares();
	}

	public void onCellEdit(CellEditEvent event) {

		atualizarRendimentoMembrosFamiliares();

		String componentId = ((DataTable) event.getSource()).getClientId(FacesContext.getCurrentInstance()) + ":"
				+ event.getRowIndex() + ":atualizar-media";

		PrimeFaces.current().executeScript("document.getElementById('" + componentId + "').click()");
	}

	public List<String> getParentescosComposicaoFamiliar() {

		List<String> parentescos = new ArrayList<>();

		List<CampoGrupo> grupos = getGrupos();
		for (CampoGrupo grupo : grupos) {

			Long tipoCampoGrupoId = grupo.getTipoCampoGrupoId();
			if (TipoCampoGrupo.MEMBRO_FAMILIAR_IDS.contains(tipoCampoGrupoId)) {

				String parentesco = "";

				List<Campo> campos = this.campos.get(grupo);
				for (Campo campo : campos) {

					String nome = campo.getNome();
					if (CampoMap.CampoEnum.PARENTESCO.getNome().equals(nome)) {

						parentesco = campo.getValor();
						parentescos.add(parentesco);
						break;
					}
				}
			}
		}

		return parentescos;
	}

	public void atualizarRendimentoMembrosFamiliares() {

		if (rendimentoComposicaoFamiliar == null) {

			rendimentoComposicaoFamiliar = new RendimentoComposicaoFamiliarVO();

			List<String> parentescos = getParentescosComposicaoFamiliar();
			parentescos.add(0, "Candidato");

			List<RendimentoMembroFamiliarVO> rendimentos = parentescos.stream().map(RendimentoMembroFamiliarVO::new).collect(Collectors.toList());
			rendimentoComposicaoFamiliar.setRendimentos(rendimentos);

			rendimentoComposicaoFamiliar.setVigenciaSalarioMinimo(this.salarioMinimoSelecionado != null ? this.salarioMinimoSelecionado.getVigencia() : "");
			rendimentoComposicaoFamiliar.setSalarioMinimoMensal(this.salarioMinimoSelecionado != null ? this.salarioMinimoSelecionado.getSalarioMensal() : "");
		}
		else {

			rendimentoComposicaoFamiliar.setVigenciaSalarioMinimo(this.salarioMinimoSelecionado != null ? this.salarioMinimoSelecionado.getVigencia() : "");
			rendimentoComposicaoFamiliar.setSalarioMinimoMensal(this.salarioMinimoSelecionado != null ? this.salarioMinimoSelecionado.getSalarioMensal() : "");
			rendimentoComposicaoFamiliar.setSalarioMaximoPermitido(this.salarioMinimoSelecionado != null ? this.salarioMinimoSelecionado.getSalarioMaximoPermitido() : "");

			calculoRendaService.atualizarCalculos(rendimentoComposicaoFamiliar);
		}
	}

	public void salvarCalculoSalarioMinimo() {

		try {

			processoLogService.criaLogCalculoSalarioMinimo(processo, getUsuarioLogado(), rendimentoComposicaoFamiliar);
			carregarLogs();

			setRequestAttribute("fecharModal", true);
			addMessage("calculoSalvo.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	@Override
	public void verificarBloqueioProcesso() {

		if(processo != null) {
			Long processoId = processo.getId();
			Usuario usuario = getUsuarioLogado();
			BloqueioErrorVO vo = bloqueioProcessoService.bloquear(processoId, usuario);
			if(vo != null) {
				String key = vo.getKey();
				Object[] args = vo.getArgs();
				String message = getMessage(key, args);
				setRequestAttribute("bloqueioMessage", message);
			}
		}
	}

	private void acessoBloqueado() {

		Usuario usuario = getUsuarioLogado();

		if (usuario.isComercialRole()) {
			return;
		}

		if(usuario.isAreaRole() || usuario.isRequisitanteRole()) {
			addMessageError("processoBloqueado.error");
			redirect(rootPath);
			return;
		}

		Long processoAtualId = usuario.getProcessoAtualId();
		if(id.equals(processoAtualId)) {
			return;
		}

		Processo processo = processoService.get(id);

		StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.CONCLUIDO.equals(status)) {

			Long processoId = processo.getId();

			if(processoAtualId != null) {
				addMessageWarn("processoBloqueado2.error", processoId);
			} else {
				if(usuario.isAnalistaRole()) {
					addMessageWarn("processoBloqueado3.error", processoId);
				}else{
					addMessageWarn("processoBloqueado4.error", processoId);
				}
			}
		}
		else {
			addMessageWarn("processoBloqueado1.error");
		}

		if(processoAtualId == null) {
			redirect(rootPath);
		} else {
			redirect(rootPath + "edit/" + processoAtualId);
		}
	}

	private void initDadosProcesso() {

		getProcessoFromService();

		processosAbertos = processoService.countAbertosByCpfCnpj(processo);
		processosFechados = processoService.countFechadosByCpfCnpj(processo);

		Usuario usuario = getUsuarioLogado();
		boolean acessoBloqueado = processoService.isAcessoBloqueado(id, usuario);
		if(acessoBloqueado) {
			acessoBloqueado();
		}
		Long processoId = processo.getId();
		if(!carregouInitCampos) {
			processoId = initCampos(false);
		}

		horasRestantes = processoService.getHorasRestantesEtapa(processo);
		horasRestantesSituacao = processoService.getHorasRestantesSituacao(processo);

		//FIXME: deve carregar os logs só quando o usuário clica na aba de acompanhamento
		carregarLogs();

		carregarSolicitacoesPendentes();

		this.novaSituacao = null;

		carregarDocumentos();
		carregarPendencia(processoId);

		//FIXME: se essa funcionalidade não está sendo usada, remover
		iniciaAjudas();

		iniciaRegras();
		carregarCampanhas();

		atualizarPermissaoEdicaoDocumentoProUni();
		carregarEmailOriginalFiesAndProuni();

		carregaIconePastaVermelha();
	}

	private void carregaIconePastaVermelha() {

		isPastaVermelha = processo.getUsaTermo() != null ? processo.getUsaTermo() : false ;
		if(isPastaVermelha) {
			Set<Long> longs = documentosEquivalentes.keySet();
			for (Long aLong : longs) {
				TipoDocumento tipoDocumento = tipoDocumentoService.get(aLong);
				boolean termoPastaVermelha = tipoDocumento.getTermoPastaVermelha();
				if(termoPastaVermelha) {
					List<Long> list = documentosEquivalentes.get(aLong);
					this.idDocumentos = list;
				}
			}
		}

		if(!isPastaVermelha) {
			StatusProcesso statusProcesso = processo.getStatus();
			isPastaVermelha = !statusProcesso.equals(StatusProcesso.CONCLUIDO) ? true : false;
		}
	}

	public void carregarEmailOriginalFiesAndProuni() {
		Long processoId = processo.getId();
		this.processo = processoService.get(processoId);
		if(processo.isSisFiesOrSisProuni()) {
			String emailOriginalCandidato = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL_NOTIFICACAO);
			emailNotificacao = emailOriginalCandidato;
		}
	}

	public void carregarPendencia(Long processoId) {
		this.processoPendencia = processoPendenciaService.getLastPendenciaByProcesso(processoId);
		if(processoPendencia != null) {

			List<DocumentoVO> documentos = getDocumentos();
			for (DocumentoVO documentoVO : documentos) {
				if(!documentoVO.isExcluido() && documentoVO.isPendente()) {
					processoPendencia.addDocumentoPendente(documentoVO);
				}
			}
		}
	}

	public Long initCampos(boolean carregarOpcoesDinamicas) {

		Long processoId = processo.getId();
		Usuario usuario = getUsuarioLogado();

		//FIXME: esse método está carregando coisas em lazy
		Map<CampoGrupo, List<Campo>> camposMap = campoService.findMapByProcesso(usuario, processoId, carregarOpcoesDinamicas);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		tipoProcesso = tipoProcessoService.get(tipoProcessoId);

		boolean isencaoDisciplinas = tipoProcesso.isIsencaoDisciplinas();

		if(isencaoDisciplinas) {
			Processo processoIsencao = processoService.getProcessoIsencaoDisciplinas(processo);
			this.possuiProcessoIsencao = processoIsencao != null;

			List<CampoGrupo> gruposRemover = new ArrayList<>();
			if (possuiProcessoIsencao) {
				Long processoIsencaoId = processoIsencao.getId();
				Map<CampoGrupo, List<Campo>> camposMap2 = campoService.findMapByProcesso(usuario, processoIsencaoId, carregarOpcoesDinamicas);
				for(CampoGrupo campoGrupo : camposMap.keySet()) {
					String nome = campoGrupo.getNome();
					for(CampoGrupo campoGrupo1 : camposMap2.keySet()){
						String nome1 = campoGrupo1.getNome();
						if(nome.equals(nome1)){
							gruposRemover.add(campoGrupo1);
						}
					}
				}

				for(CampoGrupo grupoRemover : gruposRemover) {
					camposMap2.remove(grupoRemover);
				}
				camposMap.putAll(camposMap2);
			}
		}

		Set<Campo> camposSet = mapearCamposMapParaSet(camposMap);
		atualizarViewMap(camposMap, camposSet);

		processo = processoService.get(processoId);
		Aluno aluno = processo.getAluno();
		Hibernate.initialize(aluno);

		return processoId;
	}

	private void adicionarNovoGrupoAosCampos(CampoGrupo campoGrupo) {

		Set<Campo> campos = campoGrupo.getCampos();
		this.campos.put(campoGrupo, new ArrayList<>(campos));
		Set<Campo> camposSet = mapearCamposMapParaSet(this.campos);

		atualizarViewMap(this.campos, camposSet);
	}

	private void adicionarNovoGrupoAosCamposSituacao(CampoGrupo campoGrupo) {

		Set<Campo> campos = campoGrupo.getCampos();

		this.camposSituacao.addAll(campos);
	}

	private void removerGrupoDosCampos(CampoGrupo campoGrupo) {

		this.campos.remove(campoGrupo);
		Set<Campo> camposSet = mapearCamposMapParaSet(this.campos);

		atualizarViewMap(this.campos, camposSet);
	}

	private void removerGrupoDosCamposSituacao(CampoGrupo campoGrupo) {
		this.camposSituacao.removeIf(c -> c.getGrupo().equals(campoGrupo));
	}

	private Set<Campo> mapearCamposMapParaSet(Map<CampoGrupo, List<Campo>> camposMap) {

		Set<Campo> camposSet = new LinkedHashSet<>();
		for (List<Campo> list : camposMap.values()) {
			camposSet.addAll(list);
		}

		return camposSet;
	}

	private void atualizarViewMap(Map<CampoGrupo, List<Campo>> camposMap, Set<Campo> camposSet) {

		this.campos = camposMap; // TODO sumir com essa variável "campos" pq os dados estão duplicados no viewMap

		Map<String, Object> viewMap = getViewMap();
		viewMap.put(CAMPOS_MAP, camposSet);
	}

	private void getProcessoFromService() {

		//Correção de um bug quando vem da tela do candidato
		if(id == null && processo != null) {
			this.id = processo.getId();
		}

		this.processo = processoService.get(id);
		if(processo != null) {
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			Hibernate.initialize(tipoProcesso);
			Situacao situacao = processo.getSituacao();
			Hibernate.initialize(situacao);
		}
	}

	private void carregarSolicitacoesPendentes() {

		Long processoId = processo.getId();
		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		this.solicitacoesPendentes = solicitacaoService.findVosPendentesByProcesso(processoId, roleGD);
	}

	public void carregarAnalistas() {

		UsuarioFiltro filtro = new UsuarioFiltro();
		filtro.setStatus(StatusUsuario.ATIVO);
		filtro.setRole(RoleGD.GD_ANALISTA);
		Usuario usuario = getUsuarioLogado();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null && Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID).contains(subperfilAtivo.getId())){
			filtro.setSubperfisIds(Arrays.asList(Subperfil.CSC_ID, Subperfil.CSC_ADM_ID));
		}
		this.analistas = usuarioService.findByFiltroToSelect(filtro);
	}

	public void carregarModalidades() {
		List<Long> proUniFiesIds = Arrays.asList(TipoProcesso.TE_PROUNI, TipoProcesso.TE_FIES);
		this.modalidades = tipoProcessoService.findByIds(proUniFiesIds);
	}

	public void carregarAnexos() {

		Long processoId = processo.getId();
		anexoFiltro.setProcessoId(processoId);

		List<ProcessoLogAnexo> logs1 = processoLogAnexoService.findByProcessoAnexos(anexoFiltro);

		List<LogVO> logs = new ArrayList<>();
		for (ProcessoLogAnexo pla : logs1) {
			LogVO vo = new LogVO(pla);
			logs.add(vo);
			Integer alturaImagem = pla.getAlturaImagem();
			vo.setAlturaImg(alturaImagem);
			Integer larguraImagem = pla.getLarguraImagem();
			vo.setLarguraImg(larguraImagem);
		}

		Collections.sort(logs, new ReflectionBeanComparator<>("data desc, id desc"));

		this.logsAnexo = logs;
	}

	public void carregarProcessosMesmoCpfCnpj() {

		ProcessoFiltro filtro = new ProcessoFiltro();
		Aluno aluno = processo.getAluno();
		String cpfCnpj = aluno.getCpf();
		if(StringUtils.isBlank(cpfCnpj) || Aluno.CPF_ALUNO_GENERICO.equals(cpfCnpj)) {
			return;
		}
		filtro.setCpfCnpj(cpfCnpj);

		List<StatusProcesso> statusList = Arrays.asList(StatusProcesso.CONCLUIDO, StatusProcesso.CANCELADO, StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE, StatusProcesso.PENDENTE);
		filtro.setStatusList(statusList);

		this.processosMesmoCpjCnpj = processoService.findVOsByFiltro(filtro, null, null);
	}

	public void carregarSituacoes(StatusProcesso status) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		if(status != null) {
			this.situacoes = situacaoService.findAtivas(status, tipoProcessoId);
			return;
		}

		this.situacoes = new ArrayList<>();
		Situacao situacao = processo.getSituacao();
		if (situacao == null) {
			SituacaoFiltro filtro = new SituacaoFiltro();
			filtro.setTipoProcessoId(tipoProcessoId);
			filtro.setAtiva(true);
			this.situacoes = situacaoService.findByFiltro(filtro, null, null);
			return;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			SituacaoFiltro filtro = new SituacaoFiltro();
			filtro.setTipoProcessoId(tipoProcessoId);
			filtro.setAtiva(true);
			this.situacoes = situacaoService.findByFiltro(filtro, null, null);
			return;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		Long situacaoId = situacao.getId();
		situacao = situacaoService.get(situacaoId);
		Set<ProximaSituacao> proximas = situacao.getProximas();
		for(ProximaSituacao ps : proximas) {
			Situacao proxima = ps.getProxima();
			Set<SituacaoSubperfil> subperfis = proxima.getSubperfis();
			if(!subperfis.isEmpty() && subperfilAtivo != null) {
				for(SituacaoSubperfil sp : subperfis) {
					Subperfil subperfil = sp.getSubperfil();
					if(subperfil.equals(subperfilAtivo)) {
						this.situacoes.add(proxima);
					}
				}
			}
			else {
				this.situacoes.add(proxima);
			}
		}

		Collections.sort(this.situacoes, new SuperBeanComparator<Situacao>("nome"));
	}

	public void carregarSituacoesIsencao(boolean revisaoIsencao) {
		this.camposSituacao = null;
		this.evidenciaVO = null;

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		boolean possuiPreAnaliseIsecao = tipoProcesso.isPossuiPreIsencaoDisciplinas();
		if (revisaoIsencao && (!TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId) && !TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId))) {
			this.situacoes = situacaoService.findByIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID));
		} else if(possuiPreAnaliseIsecao && (!TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId) && !TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId))) {
			this.situacoes = situacaoService.findByIds(Situacao.ISENCAO_DISCIPLINAS_PRE_ANALISE_IDS);
		} else if((!TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId) && !TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId))) {
			this.situacoes = situacaoService.findByIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_ID));
		} else if (revisaoIsencao && TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId)) {
			this.situacoes = situacaoService.findByIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_REVISAO_TE_ID));
		} else if(possuiPreAnaliseIsecao && TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId)) {
			this.situacoes = situacaoService.findByIds(Situacao.ISENCAO_DISCIPLINAS_PRE_ANALISE_TE_IDS);
		} else if(TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId)) {
			this.situacoes = situacaoService.findByIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_MEDICINA_ID));
		}else {
			this.situacoes = situacaoService.findByIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_TE_ID));
		}

		if (this.situacoes.size() == 1) {
			Situacao situacaoUnica = situacoes.get(0);
			this.novaSituacao = (situacaoUnica);
			selecionaNovaSituacao(null);
		}
	}

	public boolean podePedirIsencao(){

		if(processo == null) return false;
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		boolean isencaoDisciplinas = tipoProcesso.isIsencaoDisciplinas();

		if(!isencaoDisciplinas) {
			return false;
		}

		Long tipoProcessoId = tipoProcesso.getId();
		boolean isMedicina = TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId);

		if(isMedicina) {
			Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
			for(CampoGrupo grupo : gruposCampos) {
				String nome = grupo.getNome();
				if(CampoMap.GrupoEnum.ISENCAO_DISCIPLINA.getNome().equals(nome)){
					return  false;
				}
			}
		}

		if(possuiProcessoIsencao) {

			Processo processoIsencaoDisciplinas = processoService.getProcessoIsencaoDisciplinas(processo);
			if(isMedicina) {
				if (documentoService.validaMesmaVersãoDocumentos(processoIsencaoDisciplinas, processo)) return false;
			}
			Set<CampoGrupo> gruposCampos1 = processoIsencaoDisciplinas.getGruposCampos();
			for(CampoGrupo grupo : gruposCampos1) {
				Set<Campo> campos = grupo.getCampos();
				for(Campo campo : campos){
					String nome1 = campo.getNome();
					if(nome1.equals(CampoMap.CampoEnum.OBSERVACAO_ISENCAO_DISCIPLINA.getNome())){
						String valor = campo.getValor();
						if(valor.equals(RelatorioIsencaoDisciplinasService.PROBLEMA_DOC)){
							return true;
						}
					}
				}
				String nome = grupo.getNome();
				if(CampoMap.GrupoEnum.ISENCAO_DISCIPLINA.getNome().equals(nome) && !isMedicina){
					return  false;
				}
			}

			StatusProcesso status = processoIsencaoDisciplinas.getStatus();
			if(StatusProcesso.EM_ANALISE.equals(status)){
				return false;
			}
		}

		return true;
	}

	public boolean podePedirRevisaoIsencao(){

		if(processo == null) return false;

		TipoProcesso processoTipoProcesso = processo.getTipoProcesso();
		boolean isencaoDisciplinas = processoTipoProcesso.isIsencaoDisciplinas();

		if(!isencaoDisciplinas) {
			return false;
		}

		Long tipoProcessoId = processo.getTipoProcesso().getId();
		if(TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId)) return false;

		boolean podePedirRevisao = false;

		if(possuiProcessoIsencao) {

			Processo processoIsencao = processoService.getProcessoIsencaoDisciplinas(processo);

			Set<CampoGrupo> gruposCampos = processoIsencao.getGruposCampos();
			for(CampoGrupo grupo : gruposCampos) {
				String nome = grupo.getNome();
				if(CampoMap.GrupoEnum.ISENCAO_DISCIPLINA.getNome().equals(nome)){
					podePedirRevisao = true;
				}
				Set<Campo> campos = grupo.getCampos();
				for(Campo campo : campos){
					String nome1 = campo.getNome();
					if(nome1.equals(CampoMap.CampoEnum.RESULTADO_ISENCAO_DISCIPLINA.getNome()) && CampoMap.GrupoEnum.ISENCAO_DISCIPLINA.getNome().equals(nome)){
						String valor = campo.getValor();
						if(valor.equals(RelatorioIsencaoDisciplinasService.PROBLEMA_DOC)){
							podePedirRevisao = false;
						}
					}
				}
			}

			StatusProcesso status = processoIsencao.getStatus();
			if(!StatusProcesso.CONCLUIDO.equals(status)){
				podePedirRevisao = false;
			}
		}

		return podePedirRevisao;
	}

	public boolean temDocumentos() {
		return temDocumentos;
	}

	public void carregarDocumentos() {
		carregarDocumentos(null);
		carregarDocumentosProcessoId();
		carregarIrregularidadesDocumento();
	}

	public void carregarDocumentos(Long docId) {

		Usuario usuario = getUsuarioLogado();
		Long processoId = processo.getId();
		String contextPath = getContextPath();
		String imagePath = contextPath + ImagemFilter.PATH;

		this.documentos = documentoService.findVOsByProcesso(processoId, usuario, imagePath);
		this.temDocumentos = !documentos.isEmpty();

		Map<Long, DocumentoVO> documentosMap = new LinkedHashMap<>();
		this.documentosExcluidos = new ArrayList<>();
		this.irregularidadesPastaAmarelaMap = new HashMap<>();

		for (DocumentoVO vo : documentos) {
			Long documentoId = vo.getId();
			if(docId != null && documentoId.equals(docId)) {
				setDocumento(vo);
			}

			documentosMap.put(documentoId, vo);

			StatusDocumento status = vo.getStatus();
			String nome = vo.getNome();
			if (StatusDocumento.EXCLUIDO.equals(status) && !Documento.NOME_TIFICANDO.equals(nome)) {
				this.documentosExcluidos.add(vo);
			} else {
				if (Documento.NOME_TIFICANDO.equals(nome) && !StatusDocumento.EXCLUIDO.equals(status)) {
					estaTipificando = true;
				}
			}

			boolean existeAdvertencia = vo.getExisteAdvertencia();
			if(existeAdvertencia) {
				if(!isPastaAmarela) {
					isPastaAmarela = true;
				}
				Long tipoDocumentoId = vo.getTipoDocumento().getId();
				String irregularidadeNome = vo.getIrregularidadeNome();
				irregularidadesPastaAmarelaMap.put(tipoDocumentoId, irregularidadeNome);
			}
		}

		Set<Long> longs = irregularidadesPastaAmarelaMap.keySet();
		idTipoDocumentos = new ArrayList<>(longs);

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.addMixIn(Object.class, DynamicMixIn.class);
			SimpleFilterProvider filterProvider = new SimpleFilterProvider();
			filterProvider.addFilter("dynamicFilter", SimpleBeanPropertyFilter.serializeAllExcept("tipoDocumento"));
			mapper.setFilterProvider(filterProvider);

			mapper.getSerializationConfig().withoutAttribute("tipoDocumento");

			this.documentosMapJson = mapper.writeValueAsString(documentosMap);
			this.documentosJson = mapper.writeValueAsString(documentos);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		carregarTemAlertarDocumentos();
	}

	public void carregarCampanhas(){
		Campanha campanha = processo.getCampanha();
		if(campanha == null) {
			campanha = campanhaService.getByProcesso(processo);
		}

		Map<Long, List<Long>> equivalentesMap = new LinkedHashMap<>();
		Map<Long, List<Long>> equivalidosMap = new LinkedHashMap<>();
		if(campanha != null) {
			campanhaService.carregaObrigatoriosAndEquivalencias(campanha, null, equivalentesMap, equivalidosMap);
		}
		this.documentosEquivalentes = equivalentesMap;
		this.documentosEquivalidos = equivalidosMap;
	}

	public boolean temBarraLateral() {
		if(isEditandoVisualizacao()) {
			return false;
		}
		if(processoTemRegras()) {
			return true;
		}
		if(autorizacao.podeConcluirSituacao() && !ajudasMarcadas.isEmpty()) {
			return true;
		}
		return false;
	}

	@JsonFilter("dynamicFilter")
	public class DynamicMixIn {
	}

	public boolean estaTipificando(){
		return estaTipificando;
	}

	public boolean temAjuda() {
		//carregarAjuda();
		return !ajudasMarcadas.isEmpty();
	}

	/*public void carregarAjuda() {

		Usuario usuario = getUsuarioLogado();
		Long ultimaAjudaId = null;

		if (usuario.isAreaRole()) {
			HttpSession session = getSession();
			ultimaAjudaId = (Long) session.getAttribute(ULTIMA_AJUDA_SESSION_KEY);
		} else if (usuario.isAnalistaRole()) {
			ultimaAjudaId = processo.getUltimaAjudaId();
		}

		if (ultimaAjudaId != null) {
			ajuda = ajudaService.get(ultimaAjudaId);
		}

		if (ajuda == null) {
			Objetivo objetivo = null;
			if (usuario.isAreaRole()) {
				objetivo = Objetivo.SOLICITACAO;
			} else if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
				objetivo = Objetivo.REQUISICAO;
			}
			if (objetivo != null) {
				TipoProcesso tipoProcesso = processo.getTipoProcesso();
				Long tipoProcessoId = tipoProcesso.getId();
				ajuda = ajudaService.getByTipoProcessoAndObjetivo(tipoProcessoId, objetivo);
			}
		}
	}*/

	public void carregarSolicitacoes() {

		Long processoId = processo.getId();
		this.solicitacoes = solicitacaoService.findVosByProcesso(processoId);
	}

	public void carregarEmails() {

		Long processoId = processo.getId();
		this.emailsVOs = emailEnviadoService.findVosByProcesso(processoId);
	}

	public void carregarLogs() {

		Long processoId = processo.getId();
		List<ProcessoLog> logs1 = processoLogService.findByProcesso(processoId);
		List<DocumentoLog> logs2 = documentoLogService.findByProcesso(processoId);

		List<LogVO> logs = new ArrayList<>();
		for (ProcessoLog pl : logs1) {
			LogVO vo = new LogVO(pl);
			logs.add(vo);

			if (AcaoProcesso.ATUALIZACAO_CAMPOS.equals(pl.getAcao())) {

				net.wasys.util.rest.jackson.ObjectMapper om = new net.wasys.util.rest.jackson.ObjectMapper();
				String observacao = pl.getObservacao();
				try {
					StringBuilder sb = new StringBuilder();
					JsonNode jsonNode = om.readTree(observacao);

					for (Iterator<Map.Entry<String, JsonNode>> grupos = jsonNode.fields(); grupos.hasNext(); ) {

						Map.Entry<String, JsonNode> mapGrupos = grupos.next();
						JsonNode camposGrupoJson = mapGrupos.getValue();
						for (Iterator<Map.Entry<String, JsonNode>> campos = camposGrupoJson.fields(); campos.hasNext(); ) {

							Map.Entry<String, JsonNode> mapCampos = campos.next();
							String camposKey = mapCampos.getKey();

							sb.append(DummyUtils.capitalize(camposKey)).append(". ");
						}
					}

					String obs = sb.toString();
					if (obs.length() > 65) {
						obs = obs.substring(0, 65) + "[...]";
					}

					vo.setObservacaoCurtaAtualizacaoCampos(obs);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (AcaoProcesso.CALCULO_SALARIO_MINIMO.equals(pl.getAcao())) {

				net.wasys.util.rest.jackson.ObjectMapper om = new net.wasys.util.rest.jackson.ObjectMapper();
				String observacao = pl.getObservacao();
				try {
					StringBuilder sb = new StringBuilder();
					JsonNode jsonNode = om.readTree(observacao);

					for (Iterator<Map.Entry<String, JsonNode>> campos = jsonNode.fields(); campos.hasNext(); ) {

						Map.Entry<String, JsonNode> mapCampos = campos.next();
						String camposKey = mapCampos.getKey();

						sb.append(camposKey).append(". ");
					}

					String obs = sb.toString();
					obs = obs.substring(0, Math.min(65, obs.length())) + "[...]";
					vo.setObservacaoCurtaCalcSalarioMinimo(obs);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		for (DocumentoLog dl : logs2) {
			LogVO vo = new LogVO(dl);
			logs.add(vo);
		}

		Collections.sort(logs, new ReflectionBeanComparator<>("data, id"));

		this.logs = logs;
	}

	public void selecionaAreaSolicitacao(ValueChangeEvent event) {

		Area area = (Area) event.getNewValue();

		if(area == null) {
			subareas = null;
		}
		else {
			Long areaId = area.getId();
			subareas = subareaService.findAtivasByArea(areaId);
			if (solicitacaoVO != null) {
				Solicitacao solicitacao = solicitacaoVO.getSolicitacao();
				if(subareas.size() == 1) {
					Subarea subarea = subareas.get(0);
					solicitacao.setSubarea(subarea);
				}
			}
			if (evidenciaVO != null) {
				if(subareas.size() == 1) {
					Subarea subarea = subareas.get(0);
					evidenciaVO.setSubarea(subarea);
				}
			}
		}
	}

	public void proximaRequisicao() {

		Usuario usuario = getUsuarioLogado();

		try {
			long inicio = System.currentTimeMillis();
			Processo processo = processoService.proximoProcessoComLock(usuario);
			boolean distribuirDemandaAutomaticamente = usuario.getDistribuirDemandaAutomaticamente();
			systraceThread("Tempo total proximaRequisicao: " + (System.currentTimeMillis() - inicio) + "ms. Analista: " + usuario + ". Processo: " + processo);

			if(processo == null) {
				addMessage("semProximosProcessos.sucesso");
				if(distribuirDemandaAutomaticamente) {
					logAtendimentoService.criarDisponivel(usuario.getId());
					DummyUtils.sleep(1500);
					PrimeFaces.current().executeScript("location.reload();");
				}
				return;
			}

			if(distribuirDemandaAutomaticamente) {
				logAtendimentoService.encerrarUltimoLog(usuario);
				logAtendimentoService.criarEmAnalise(usuario.getId());
			}

			Long processoId = processo.getId();
			redirect(rootPath + "edit/" + processoId);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void iniciarTrabalho() {
		try{
			Long processoId = processo.getId();
			Usuario usuarioLogado = getUsuarioLogado();
			processoLogService.marcarComoLido(processoId, usuarioLogado);
			processoService.iniciarAnalise(processo, usuarioLogado);
			redirect(rootPath + "edit/" + processoId);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void downloadDocumentoPrev() {

		String imagemIdStr = Faces.getRequestParameter("imagemId");
		String versaoStr = Faces.getRequestParameter("versao");

		this.imagemId = new Long(imagemIdStr);
		this.versao = new Integer(versaoStr);
	}

	public StreamedContent downloadDocumento() {

		try {
			String documentoIdStr = Faces.getRequestParameter("documentoSelect1");
			Long documentoId = new Long(documentoIdStr);

			DownloadVO downloadVO = documentoService.getDownload(documentoId, versao);

			File file = downloadVO.getFile();
			String fileName = downloadVO.getFileName();

			FileInputStream fis = new FileInputStream(file);
			DefaultStreamedContent dsc =
					DefaultStreamedContent.builder()
							.contentType("application/csv")
							.name(fileName)
							.stream(() -> fis)
							.build();
			return dsc;
		}
		catch (Exception e) {
			addMessageError(e);
			return null;
		}
	}

	public void trocarAnalista() {

		Usuario usuario = getUsuarioLogado();

		try {
			processoService.trocarAnalista(processo, novoAnalista, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void trocarModalidade() {
		Usuario usuario = getUsuarioLogado();
		try {
			if(modalidade == null) {
				addMessageError("selecionarModalidade.error");
				return;
			}

			Boolean isProcessoOriginal = processoService.isProcessoOriginal(processo);
			if(isProcessoOriginal) {
				List<Processo> processoOriginados = processoService.findProcessosOriginados(processo);
				Long modalidadeId = modalidade.getId();
				Optional<Processo> processoOriginadoOptional = processoOriginados.stream().filter(p -> p.getTipoProcesso().getId().equals(modalidadeId)).findFirst();
				if(processoOriginadoOptional.isPresent()) {
					Processo processoOriginado = processoOriginadoOptional.get();
					Long processoOriginadoId = processoOriginado.getId();
					addMessageWarn("processoModalidadeExiste.warn");
					redirect("/requisicoes/fila/edit/" + processoOriginadoId);
					return;
				}
			}

			Processo novoProcesso = processoService.criaNovoProcessoModalidade(processo, modalidade, usuario);
			Long novoProcessoId = novoProcesso.getId();
			setRequestAttribute("fecharModal", true);
			addMessage("novoProcessoModalidade.sucesso");
			if(usuario.isSalaMatriculaRole()) {
				redirect("/consultas/candidato/" + novoProcessoId);
			} else {
				redirect("/requisicoes/fila/edit/" + novoProcessoId);
			}
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	private void carregarTextoPadroes() {

		TextoPadraoFiltro filtro = new TextoPadraoFiltro();
		filtro.setAtivo(true);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		filtro.setTipoProcessoId(tipoProcessoId);

		textosPadroes = textoPadraoService.findByFiltro(filtro, null, null);
	}

	public void excluirDocumento() {

		Usuario usuario = getUsuarioLogado();

		try {
			processoService.excluirDocumento(processo, documento, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void adicionarDocumento() {

		Usuario usuario = getUsuarioLogado();

		try {
			processoService.adicionarDocumento(processo, Arrays.asList(novoDocumentoId), usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void subirNivelPrioridade() {

		Usuario usuario = getUsuarioLogado();

		try {
			processoService.subirNivelPrioridade(processo, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void salvarRotacao() {

		try {
			String imagemIdStr = Faces.getRequestParameter("imagemId");
			Long imagemId = new Long(imagemIdStr);

			String rotacaoStr = Faces.getRequestParameter("rotacao");
			Integer rotacao = new Integer(rotacaoStr);

			imagemService.rotacionarImagem(imagemId, rotacao);

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void copiarImagem() {

		try {
			String indiceImagemStr = Faces.getRequestParameter("indiceImagem");
			this.indiceImagem1 = new Integer(indiceImagemStr);

			String imagemIdStr = Faces.getRequestParameter("imagemId");
			Long imagemId = new Long(imagemIdStr);

			String moverStr = Faces.getRequestParameter("mover");
			boolean mover = "true".equals(moverStr);

			Imagem imagem = imagemService.get(imagemId);
			this.documento = imagem.getDocumento();

			String destinoIdStr = Faces.getRequestParameter("destinoId");
			if(StringUtils.isBlank(destinoIdStr)) {
				addMessageError("validacao-obrigatorio.error", getMessage("documentoDestino.label"));
				return;
			}

			Long destinoId = new Long(destinoIdStr);
			Documento destino = documentoService.get(destinoId);

			Usuario usuario = getUsuarioLogado();

			documentoService.copiarImagem(imagem, destino, usuario, mover);

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void excluirImagem() {

		try {
			String indiceImagemStr = Faces.getRequestParameter("indiceImagem");
			this.indiceImagem1 = new Integer(indiceImagemStr);

			String imagemIdStr = Faces.getRequestParameter("imagemId");
			Long imagemId = new Long(imagemIdStr);

			Imagem imagem = imagemService.get(imagemId);
			this.documento = imagem.getDocumento();

			Usuario usuario = getUsuarioLogado();

			documentoService.excluirImagem(imagem, usuario, true);

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void salvarDigitalizacaoTwain() {

		try {
			Map<Long, List<FileVO>> map = getImagensMap();
			DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
			Hibernate.initialize(processo);
			digitalizacaoVO.setProcesso(processo);
			digitalizacaoVO.setOrigem(Origem.WEB);
			digitalizacaoVO.setArquivosMap(map);

			Usuario usuario = getUsuarioLogado();
			processoService.digitalizarImagens(usuario, digitalizacaoVO);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		redirect(rootPath + "edit/" + processo.getId());
	}

	public void salvarDigitalizacao() {

		try {
			Usuario usuario = getUsuarioLogado();
			processoService.digitalizarImagens(usuario, digitalizacaoVO);

			digitalizacaoVO = null;
			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void aprovarDocumento() {

		try {
			DocumentoVO documentoVO = null;
			for(DocumentoVO vo : documentos){
				if(vo.getId().equals(documento.getId())){
					documentoVO = vo;
					continue;
				}
			}

			String documentoIdStr = Faces.getRequestParameter("docId");
			Long documentoId = new Long(documentoIdStr);
			String modeloDocumentoIdStr = Faces.getRequestParameter("modeloDocumentoId");
			Long modeloDocumentoId = StringUtils.isNotBlank(modeloDocumentoIdStr) ? new Long(modeloDocumentoIdStr) : null;
			ModeloDocumento modeloDocumento = modeloDocumentoId != null ? modeloDocumentoService.get(modeloDocumentoId) : null;
			Map<Long, String> modelosData = (documentoVO!=null?documentoVO.getModelosDocumentoValidarExpiracao():null);
			Boolean modeloDataValidadeEmissao = (modelosData!=null && modelosData.get(modeloDocumentoId)!=null && !modelosData.get(modeloDocumentoId).isEmpty())?true:false;
			Usuario usuario = getUsuarioLogado();

			Documento documento = documentoService.get(documentoId);
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento != null) {

				Long tipoDocumentoId = tipoDocumento.getId();
				tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
				boolean sempreTipificar = tipoDocumento != null ? tipoDocumento.getSempreTipificar() : false;
				if(sempreTipificar && modeloDocumento == null) {
					addMessageError("validacao-obrigatorio.error", "Modelo documento");
					return;
				}

				SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy");
				String dataValidadeExpiracaoStr = Faces.getRequestParameter("dataValidadeExpiracao");
				if(StringUtils.isNotBlank(dataValidadeExpiracaoStr)) {
					Date dataValidadeExpiracaoDocumento = fmt.parse(dataValidadeExpiracaoStr);
					documento.setValidadeExpiracao(dataValidadeExpiracaoDocumento);
				}

				String dataEmissaoStr = Faces.getRequestParameter("dataEmissao");
				if(StringUtils.isNotBlank(dataEmissaoStr)){
					Date dataEmissao = fmt.parse(dataEmissaoStr);
					documento.setDataEmissao(dataEmissao);
				}

				if (!tipoDocumento.getRequisitarDataEmissao()) {
					documento.setDataEmissao(null);
				}

				if (!tipoDocumento.getRequisitarDataValidadeExpiracao()) {
					documento.setValidadeExpiracao(null);
				}

				if (tipoDocumento.getRequisitarDataPorModeloDocumento() && !modeloDataValidadeEmissao) {
					documento.setDataEmissao(null);
					documento.setValidadeExpiracao(null);
				}
			}

			documentoService.aprovar(documentoId, modeloDocumentoId, usuario);

			Integer prox = null;
			for (DocumentoVO vo : documentos) {
				Long documentoId2 = vo.getId();
				if(documentoId.equals(documentoId2)) {
					prox = documentos.indexOf(vo)+1;
					break;
				}
			}

			if(prox != null && prox < documentos.size()) {
				for (int i = prox; i < documentos.size(); i++) {
					DocumentoVO vo = documentos.get(i);
					StatusDocumento statusDoc = vo.getStatus();
					if (!statusDoc.equals(StatusDocumento.APROVADO) && !vo.isExcluido()){
						setDocumento(vo);
						break;
					}
				}
			}

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void justificarDocumento() {

		try {
			String documentoIdStr = Faces.getRequestParameter("docId");
			Long documentoId;
			if(documentoIdStr != null) {
				documentoId = new Long(documentoIdStr);
			}else{
				documentoId = documento.getId();
			}
			Usuario usuario = getUsuarioLogado();
			String observacao = Faces.getRequestParameter("observacao2");

			if(StringUtils.isBlank(observacao)) {
				addMessageError("validacao-obrigatorio.error", "Observação");
				return;
			}

			documentoService.justificar(documentoId, usuario, observacao);
			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void alterarObrigatoriedade() {

		try {
			String documentoIdStr = Faces.getRequestParameter("docId");
			Long documentoId;
			if (documentoIdStr != null) {
				documentoId = new Long(documentoIdStr);
			} else {
				documentoId = documento.getId();
			}
			boolean obrigatorio = documento.getObrigatorio();
			if (obrigatorio == alteracaoObrigatoriedade) {
				addMessage("alteracaoSalva.sucesso");
				return;
			}
			Usuario usuario = getUsuarioLogado();

			documentoService.alterarObrigatoriedade(documentoId, usuario, alteracaoObrigatoriedade);
			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		} catch (Exception e) {
			addMessageError(e);
		}
	}

	public void ajustarDocumentoCorrompido() {
		try {

			String observacao = Faces.getRequestParameter("observacao3");

			if(StringUtils.isBlank(observacao)) {
				addMessageError("validacao-obrigatorio.error", "Observação");
				return;
			}
			manutencaoDocumentoVO.setObservacao(observacao);

			String indiceImagemStr = Faces.getRequestParameter("indiceImagem");
			this.indiceImagem1 = new Integer(indiceImagemStr);

			String imagemIdStr = Faces.getRequestParameter("imagemId");
			Long imagemId = new Long(imagemIdStr);

			Imagem imagem = imagemService.get(imagemId);
			this.documento = imagem.getDocumento();

			Usuario usuario = getUsuarioLogado();
			boolean excluir = manutencaoDocumentoVO.isExcluir();
			if(excluir) {
				documentoService.excluirRegistroDeImagemFerrada(imagem, usuario, observacao);
			}
			else {
				documentoService.alterarStatus(manutencaoDocumentoVO, documento, usuario);
			}
			manutencaoDocumentoVO = new ManutencaoDocumentoVO();
			addMessage("alteracaoSalva.sucesso");
			redirect(rootPath + "edit/" + processo.getId());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		initDadosProcesso();
	}

	public void rejeitarDocumento() {

		try {

			String documentoSelect = Faces.getRequestParameter("documentoSelect1");
			Long documentoId;
			if(StringUtils.isNotBlank(documentoSelect)) {
				documentoId = new Long(documentoSelect);
			}else{
				documentoId = documento.getId();
			}

			Usuario usuario = getUsuarioLogado();

			if(StringUtils.isNotBlank(observacaoIrregularidade) && observacaoIrregularidade.length() > 500) {
				addMessageError("validacao-maxLength.error", "Observação");
				return;
			}

			if(irregularidade == null) {
				addMessageError("erroIrregularidade.error");
				return;
			}

			boolean irregularidadePastaAmarela = irregularidade.getIrregularidadePastaAmarela();
			if(irregularidadePastaAmarela) {
				documentoService.aprovarComAvertencia(documentoId, usuario, observacaoIrregularidade, irregularidade);
			} else {
				documentoService.rejeitar(documentoId, irregularidade, usuario, observacaoIrregularidade);
			}


			Integer prox = null;
			for (DocumentoVO vo : documentos) {
				Long documentoId2 = vo.getId();
				if(documentoId2.equals(documentoId2)) {
					prox = documentos.indexOf(vo) + 1;
					break;
				}
			}
			if(prox != null && prox < documentos.size()){
				DocumentoVO vo = documentos.get(prox); //pegar o próximo id da lista de documentos não aprovados
				setDocumento(vo);
			}

			addMessage("documentoRejeitado.sucesso");

			if(irregularidadesPorDocumento != null) {
				irregularidadesPorDocumento.clear();
			}

			irregularidade = null;
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public void criarSolicitacao() {
		this.emailVO = null;
		this.evidenciaVO = null;
		this.solicitacaoVO = new SolicitacaoVO();
		this.solicitacaoVO.setSolicitacao(new Solicitacao());
		this.solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_CRIACAO);
	}

	public void criarEvidencia() {
		this.emailVO = null;
		this.solicitacaoVO = null;
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.REGISTRO_EVIDENCIA);
		this.evidenciaVO.setSituacao(processo == null ? null : processo.getSituacao());
		this.camposSituacao = null;
	}

	public void criarDigitalizacao(Long documentoId) {
		initCampos(true);
		Documento documento = documentoId != null ? documentoService.get(documentoId) : null;
		this.digitalizacaoVO = new DigitalizacaoVO();
		this.digitalizacaoVO.setDocumento(documento);
		this.digitalizacaoVO.setProcesso(processo);
		this.digitalizacaoVO.setOrigem(Origem.WEB);
	}

	public void criarCancelamento() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.CANCELAMENTO);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowSituacao(true);
		StatusProcesso status = StatusProcesso.CANCELADO;
		carregarSituacoes(status);
	}

	public void criarEmAcompanhamento() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.EM_ACOMPANHAMENTO);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(true);
		this.evidenciaVO.setShowSituacao(true);
		StatusProcesso status = StatusProcesso.EM_ACOMPANHAMENTO;
		carregarSituacoes(status);
	}

	public void criarConclusaoSituacao() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(false);
		this.evidenciaVO.setShowSituacao(true);
		this.camposSituacao = null;
		carregarSituacoes(null);

		if (this.situacoes.size() == 1) {
			Situacao situacao = situacoes.get(0);
			this.evidenciaVO.setSituacao(situacao);
			selecionaNovaSituacao(null);
		}

		pendenciasCheckListNaoCompleto = false;
		for (ProcessoAjuda ajuda : ajudasMarcadas.keySet()) {
			List<ProcessoAjuda> inferiores = ajuda.getInferiores();
			boolean marcado = false;
			boolean temInferior = false;
			for (ProcessoAjuda inferior : inferiores) {
				inferior = processoAjudaService.get(inferior.getId());
				marcado |= inferior.isMarcado();
				List<ProcessoAjuda> inferiores2 = inferior.getInferiores();
				temInferior |= inferiores2.isEmpty();
			}
			if (!marcado && temInferior) {
				pendenciasCheckListNaoCompleto = true;
			}
		}
	}

	public void criarRevisao() {
		Usuario usuario = getUsuarioLogado();
		Processo processoIsencao = processoService.getProcessoIsencaoDisciplinas(processo);
		Long processoIsencaoId = processoIsencao.getId();
		if(evidenciaVO != null){
			AcaoProcesso acao = evidenciaVO.getAcao();
			if(AcaoProcesso.ALTERACAO_SITUACAO.equals(acao)){
				camposSituacao = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoIsencaoId, Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID, null, camposSituacao);
			}
		}else {
			this.evidenciaVO = new EvidenciaVO();
			this.evidenciaVO.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);
			this.evidenciaVO.setObservacaoTmp(CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome());
			this.camposSituacao = null;
			camposSituacao = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoIsencaoId, Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID, null, camposSituacao);
		}
		revisaoAntiga = false;
	}

	public void criarRevisaoAntigo() {
		Usuario usuario = getUsuarioLogado();
		Long processoId = processo.getId();
		processo = processoService.get(processoId);
		if(evidenciaVO != null){
			AcaoProcesso acao = evidenciaVO.getAcao();
			if(AcaoProcesso.ALTERACAO_SITUACAO.equals(acao)){
				camposSituacao = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, null, CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome(), camposSituacao);
			}
		}else {
			this.evidenciaVO = new EvidenciaVO();
			this.evidenciaVO.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);
			this.evidenciaVO.setObservacaoTmp(CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome());
			this.camposSituacao = null;
			camposSituacao = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, null, CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome(), camposSituacao);
		}
		revisaoAntiga = true;
	}

	public void criarPendencia() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.ENVIO_PENDENCIA);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(true);
		this.evidenciaVO.setShowSituacao(true);
		StatusProcesso status = StatusProcesso.PENDENTE;
		carregarSituacoes(status);
	}

	public void criarRespostaPendencia() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.RESPOSTA_PENDENCIA);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(false);
		this.evidenciaVO.setShowSituacao(false);
		this.situacoes = null;
	}

	public void criarReenvioAnalise() {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setAcao(AcaoProcesso.REENVIO_ANALISE);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(false);
		this.evidenciaVO.setShowSituacao(false);
		this.situacoes = null;
	}

	public void criarEmail() {
		this.solicitacaoVO = null;
		this.evidenciaVO = null;
		this.emailVO = new EmailVO();
	}

	public void responderSolicitacao(Solicitacao solicitacao) {
		this.solicitacaoVO = new SolicitacaoVO();
		this.solicitacaoVO.setSolicitacao(solicitacao);
		this.solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA);
		carregarLogs();
		carregarSolicitacoes();
		carregarSolicitacoesPendentes();
	}

	public void aceitarRespostaSolicitacao(Solicitacao solicitacao) {
		this.solicitacaoVO = new SolicitacaoVO();
		this.solicitacaoVO.setSolicitacao(solicitacao);
		this.solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_ACEITE_RESPOSTA);
		carregarLogs();
		carregarSolicitacoes();
		carregarSolicitacoesPendentes();
	}

	public void naoAceitarRespostaSolicitacao(Solicitacao solicitacao) {
		this.solicitacaoVO = new SolicitacaoVO();
		this.solicitacaoVO.setSolicitacao(solicitacao);
		this.solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_NAO_ACEITE_RESPOSTA);
		carregarLogs();
		carregarSolicitacoes();
		carregarSolicitacoesPendentes();
	}

	public void recusarSolicitacao(Solicitacao solicitacao) {
		this.solicitacaoVO = new SolicitacaoVO();
		this.solicitacaoVO.setSolicitacao(solicitacao);
		this.solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO);
		carregarLogs();
		carregarSolicitacoes();
		carregarSolicitacoesPendentes();
	}

	public void reenviarNotificacaoCandidato() {

		try {
			if(StringUtils.isBlank(emailNotificacao)) {
				addMessageError("nenhumEmailInformado.error");
				return;
			}

			Long processoId = processo.getId();
			processoService.iniciarNotificacaoCadidatoSisFiesAndSisProuni(processoId, emailNotificacao, getUsuarioLogado(), false);

			addMessage("emailEnviado.sucesso");

			setRequestAttribute("fecharModal", true);

			carregarEmailOriginalFiesAndProuni();
		}
		catch(Exception e) {
			addMessageError(e);
		}
	}

	public void enviarEmail() {

		try {
			Usuario usuario = getUsuarioLogado();

			emailEnviadoService.enviarEmail(emailVO, processo, usuario);

			carregarEmails();
			initDadosProcesso();
			addMessage("emailEnviado.sucesso");
			this.emailVO = null;
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void enviarConviteEmail() {

		try {
			Usuario usuario = getUsuarioLogado();

			emailEnviadoService.enviarConviteEmail(emailVO, processo, usuario);

			carregarEmails();
			initDadosProcesso();
			addMessage("emailEnviado.sucesso");
			this.emailVO = null;
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void salvarEvidencia() {

		try {
			Usuario usuario = getUsuarioLogado();
			Long id = usuario.getId();
			usuario = usuarioService.get(id);
			evidenciaVO.setCampos(camposSituacao);
			Situacao situacao = evidenciaVO.getSituacao();
			Long processoId = processo.getId();
			processo = processoService.get(processoId);

			processoService.salvarEvidencia(evidenciaVO, processo, usuario, true);

			addMessage("alteracaoSalva.sucesso");
			this.evidenciaVO = null;

			boolean usuarioDistribuirDemandaAutomaticamente = usuario.getDistribuirDemandaAutomaticamente();
			if (usuarioDistribuirDemandaAutomaticamente) {
				Long processoAtualId = usuario.getProcessoAtualId();
				if (processoAtualId == null) {
					logAtendimentoService.criarPausaSistema(usuario.getId());
					redirect("/requisicoes/fila/");
				} else if (processoAtualId.equals(processoId)) {
					logAtendimentoService.criarPausaSistema(usuario.getId());
					redirect("/requisicoes/fila/");
				} else {
					redirect("/requisicoes/edit/" + processoAtualId);
				}
				return;
			}

			Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
			Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;
			this.id = processo.getId();
			if(situacao != null && subperfilAtivoId != null && Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)) {
				redirect("/consultas/candidato/" + id);
			}
			else if(situacao != null) {
				redirect(rootPath);
				return;
			}
		}
		catch (Exception e ) {
			addMessageError(e);
		}

		initDadosProcesso();
		carregarSolicitacoesPendentes();
	}

	public void salvarIsencaoDisciplinas() {

		try {
			Usuario usuario = getUsuarioLogado();
			Long novaSituacaoId = novaSituacao.getId();
			Processo processoIsencaoDisciplinas = processoService.getProcessoIsencaoDisciplinas(processo);
			AcaoProcesso acao;
			if(processoIsencaoDisciplinas == null) {
				TipoProcesso tipoProcesso = tipoProcessoService.get(TipoProcesso.ISENCAO_DISCIPLINAS);
				acao = AcaoProcesso.ISENCAO_DISCIPLINAS;
				processoService.criaProcessoIsencao(usuario, processo, novaSituacao, tipoProcesso, Origem.WEB, opcoesIes);
			} else {
				if(Situacao.ISENCAO_DISCIPLINA_REVISAO_IDS.contains(novaSituacaoId)) {

					Campo campoIesDeGrupo = getCampoProcesso(processoIsencaoDisciplinas, CampoMap.GrupoEnum.DADOS_PROCESSO_ISENCAO.getNome(), CampoMap.CampoEnum.IES_DE_GRUPO.getNome());
					if(campoIesDeGrupo != null){
						if(opcoesIes) {
							String sim = Resposta.SIM.getNome();
							campoIesDeGrupo.setValor(sim);
						} else {
							String nao = Resposta.NAO.getNome();
							campoIesDeGrupo.setValor(Resposta.NAO.getNome());
						}
						camposSituacao.add(campoIesDeGrupo);
					}else{
						TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(468l);
						CampoGrupo grupo = campoGrupoService.criaGrupo(processoIsencaoDisciplinas, tipoCampoGrupo);
						TipoCampo tipoCampo = tipoCampoService.get(2869l);
						Campo campoIesDoGrupoNovo = campoService.criaCampo(grupo, tipoCampo);
						if(opcoesIes) {
							String sim = Resposta.SIM.getNome();
							campoIesDoGrupoNovo.setValor(sim);
						} else {
							String nao = Resposta.NAO.getNome();
							campoIesDoGrupoNovo.setValor(Resposta.NAO.getNome());
						}
						camposSituacao.add(campoIesDoGrupoNovo);
					}
					CampoGrupo grupo = getGrupoProcesso(processoIsencaoDisciplinas, CampoMap.GrupoEnum.COMPOSICAO_MATERIAS_ISENTAS);
					if(grupo == null){
						TipoProcesso tipoProcesso = tipoProcessoService.get(TipoProcesso.ISENCAO_DISCIPLINAS);
						Long tipoProcessoId = tipoProcesso.getId();
						String nomeGrupoComposicaoMateriaIsenta = CampoMap.GrupoEnum.COMPOSICAO_MATERIAS_ISENTAS.getNome();
						TipoCampoGrupo tipoGrupo = tipoCampoGrupoService.getByTipoProcessoAndGrupoNome(tipoProcessoId, nomeGrupoComposicaoMateriaIsenta);
						Processo processoIsencao = processoService.getProcessoIsencaoDisciplinas(processo);

						CampoGrupo grupoComposicaoMateriaIsenta = campoGrupoService.criaGrupo(processoIsencao, tipoGrupo);
						campoGrupoService.saveOrUpdate(grupoComposicaoMateriaIsenta);
					}

					processoService.atualizarProcesso(processoIsencaoDisciplinas, usuario, camposSituacao, true, false);
					processoService.copiarDocumentosIsencaoDisciplinas(usuario, processo, processoIsencaoDisciplinas);
					processoService.concluirSituacao(processoIsencaoDisciplinas, usuario, novaSituacao, processoLog, null);
					acao = AcaoProcesso.REVISAO_ISENCAO_DISCIPLINAS;
				}
				else {
					processoService.copiarDocumentosIsencaoDisciplinas(usuario, processo, processoIsencaoDisciplinas);
					processoService.concluirSituacao(processoIsencaoDisciplinas, usuario, novaSituacao, processoLog, null);
					acao = AcaoProcesso.ISENCAO_DISCIPLINAS;
				}
			}

			processoLogService.criaLog(processo, usuario, acao);

			addMessage("alteracaoSalva.sucesso");
			redirect("/requisicoes/edit/" + this.id);
		}
		catch (Exception e ) {
			addMessageError(e);
		}

		initDadosProcesso();
		carregarSolicitacoesPendentes();
	}

	@Transactional(rollbackFor = Exception.class)
	private void reaberturaProcessoIsencao(Usuario usuario, Processo processoIsencaoDisciplinas) throws Exception {
		processoService.copiarDocumentosIsencaoDisciplinas(usuario, processo, processoIsencaoDisciplinas);
		processoService.concluirSituacao(processoIsencaoDisciplinas, usuario, novaSituacao, processoLog, null);
	}

	public void salvarSolicitacao() {

		try {
			Usuario usuario = getUsuarioLogado();

			solicitacaoService.salvarSolicitacao(solicitacaoVO, processo, usuario);

			initDadosProcesso();
			carregarSolicitacoes();
			carregarSolicitacoesPendentes();
			addMessage("alteracaoSalva.sucesso");
			this.solicitacaoVO = null;
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void setDigitalizarTwain(boolean digitalizar) {

		this.digitalizarTwain = digitalizar;

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		Situacao situacao1 = situacaoService.get(situacaoId);
		processo.setSituacao(situacao1);

		if(digitalizar) {

			ServletContext servletContext = getServletContext();
			UploadSessionHandler ush = UploadSessionHandler.getIntance(servletContext);

			HttpSession session = getSession();
			ush.clear(session, chave);

		}
	}

	public Map<Long, List<FileVO>> getImagensMap() {

		Map<Long, List<Integer>> indicesDocumentos = getIndicesDocumentos();

		ServletContext servletContext = getServletContext();
		HttpServletRequest request = getRequest();
		HttpSession session = request.getSession();

		UploadSessionHandler ush = UploadSessionHandler.getIntance(servletContext);
		Map<Integer, FileVO> map = ush.getMap(session, chave);

		Map<Long, List<FileVO>> map2 = new LinkedHashMap<>();

		for (Long documentoId : indicesDocumentos.keySet()) {

			List<Integer> indices = indicesDocumentos.get(documentoId);
			Collections.sort(indices);

			List<FileVO> list2 = new ArrayList<>();
			for (Integer index : indices) {
				FileVO vo = map.get(index);
				list2.add(vo);
			}

			map2.put(documentoId, list2);
		}

		Collection<FileVO> values = map.values();
		if(!values.isEmpty()) {
			ArrayList<FileVO> list = new ArrayList<>(values);

			map2.put(null, list);
		}

		return map2;
	}

	private Map<Long, List<Integer>> getIndicesDocumentos() {

		Map<Long, List<Integer>> map = new LinkedHashMap<>();

		HttpServletRequest request = getRequest();
		Enumeration<String> parameterNames = request.getParameterNames();
		while(parameterNames.hasMoreElements()) {

			String parameterName = parameterNames.nextElement();

			if(parameterName.startsWith("img_doc_")) {

				String documentoIdStr = parameterName.replace("img_doc_", "");
				Long documentoId = new Long(documentoIdStr);
				String indices = request.getParameter(parameterName);

				List<Integer> list = new ArrayList<>();
				String[] split = indices.split(",");
				for (String str : split) {

					str = StringUtils.trim(str);
					if(StringUtils.isNotBlank(str)) {

						Integer index = new Integer(str);
						list.add(index);
					}
				}

				map.put(documentoId, list);
			}
		}

		return map;
	}

	public void enviarParaAnalise() {
		enviarParaAnalise2();
		redirect(rootPath);
	}

	void enviarParaAnalise2() {
		try {
			Usuario usuario = getUsuarioLogado();

			processo = processoService.get(processo.getId());
			processoService.enviarParaAnalise(processo, usuario);

			setDocumento(null);
			setEditando(false);
			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {
		try {
			Long processoId = processo.getId();
			Usuario usuario = getUsuarioLogado();
			processoService.excluir(processoId, usuario);
			addMessage("registroExcluido.sucesso");
			redirect(rootPath);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void salvar() {

		try {
			Map<String, Object> viewMap = getViewMap();
			Set<CampoAbstract> camposSet = (Set<CampoAbstract>) viewMap.get(CAMPOS_MAP);

			Usuario usuario = getUsuarioLogado();

			Long processoId = processo.getId();

			EditarProcessoVO editarProcessoVO = new EditarProcessoVO();
			editarProcessoVO.setProcessoId(processoId);
			editarProcessoVO.setUsuario(usuario);

			processoService.atualizarProcessoComGruposDinamicos(processo, camposSet, usuario);

            setDocumento(null);
			setEditando(false);
			setEditandoVisualizacao(false);
			addMessage("processoCadastrado.sucesso");

			HttpServletRequest request = getRequest();
			String requestURI = request.getRequestURI();

			String redirect;
			if(requestURI.contains("candidato")) {
				redirect = "/consultas/candidato/" + processoId;
			}
			else {
				redirect = rootPath + "edit/" + processoId;
			}

			redirect(redirect);
		}
		catch (Exception e) {
            addMessageError(e);
        }

        initBean();
        initDadosProcesso();
    }

    public int temAlertarSolicitacoes() {

		Usuario usuario = getUsuarioLogado();

		if(usuario.isAdminRole()
				|| usuario.isGestorRole()
				|| usuario.isAnalistaRole()) {

			RoleGD roleGD = usuario.getRoleGD();
			Long processoId = processo.getId();
			return solicitacaoService.countAguardandoAcaoByProcesso(processoId, roleGD);
		}

		return 0;
	}

	public boolean temAlertarDocumentos() {
		return temAlertarDocumentos;
	}

	//FIXME esse método TODO está sendo duplicado em ProcessoServiceRest.temAlertarDocumentos()!!!
	public void carregarTemAlertarDocumentos() {

		Usuario usuario = getUsuarioLogado();
		List<StatusDocumento> statusDocumentos = new ArrayList<>();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			statusDocumentos.add(StatusDocumento.DIGITALIZADO);
			statusDocumentos.add(StatusDocumento.INCLUIDO);
		}
		else if(usuario.isRequisitanteRole()) {
			statusDocumentos.add(StatusDocumento.PENDENTE);
			statusDocumentos.add(StatusDocumento.INCLUIDO);
		}
		else if(usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(usuario.equals(autor)) {
				statusDocumentos.add(StatusDocumento.PENDENTE);
				statusDocumentos.add(StatusDocumento.INCLUIDO);
			}
		}
		else {
			this.temAlertarDocumentos = false;
			return;
		}

		if(statusDocumentos == null) {
			this.temAlertarDocumentos = false;
			return;
		}

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		filtro.setStatusDocumentoList(statusDocumentos);
		this.temAlertarDocumentos = documentoService.existsByFiltro(filtro);
	}

	public int temAlertarEmails() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {

			Long processoId = processo.getId();
			return emailRecebidoService.countNaoLidos(processoId);
		}
		else {
			return 0;
		}
	}

	public void removerGrupoDinamico() {

		if (grupoExcluir.getId() != null) {

			try {

				processoService.removerGrupoDinamicoAndDocumento(grupoExcluir, getUsuarioLogado());

				initBean();
				initDadosProcesso();
				addMessage("grupoRemovido.sucesso");
			}
			catch(Exception e) {
				String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
				addMessageError("erroInesperadoExcluirGrupoDinamico.error", rootCauseMessage);
				e.printStackTrace();
			}
		}
		else {
			removerGrupoDosCampos(grupoExcluir);
			addMessage("grupoRemovido.sucesso");
		}

		setRequestAttribute("fecharModal", true);
	}

	public void removerGrupoDinamicoSituacao() {

		if (grupoExcluir.getId() != null) {
			processoService.removerGrupoDinamicoAndDocumento(grupoExcluir, getUsuarioLogado());
		}
		else {
			removerGrupoDosCamposSituacao(grupoExcluir);
		}

		addMessage("grupoRemovido.sucesso");
		setRequestAttribute("fecharModal", true);
	}

	public void adicionarGrupoDinamico(Long tipoCampoGrupoId) {

		int maiorIndice = campoGrupoService.getMaiorIndiceGrupoDinamico(campos, tipoCampoGrupoId, true);

		Long processoId = processo.getId();
		CampoGrupo novoGrupo = processoService.criarGrupoDinamico(processoId, tipoCampoGrupoId, maiorIndice + 1, sequenciaIdsCampos);
		adicionarNovoGrupoAosCampos(novoGrupo);

		addMessage("grupoCriado.sucesso");
	}

	public void adicionarGrupoDinamicoSituacao(Long tipoCampoGrupoId) {

		Long processoId = processo.getId();
		Map<CampoGrupo, List<Campo>> camposSituacaoMap = processoService.mapearCamposListParaMap(this.camposSituacao, processoId);

		int maiorIndiceCamposSituacao = campoGrupoService.getMaiorIndiceGrupoDinamico(camposSituacaoMap, tipoCampoGrupoId, true);
		int maiorIndiceCamposNormais = campoGrupoService.getMaiorIndiceGrupoDinamico(this.campos, tipoCampoGrupoId, false);

		int maiorIndice = Math.max(maiorIndiceCamposNormais, maiorIndiceCamposSituacao);

		CampoGrupo novoGrupo = processoService.criarGrupoDinamico(processoId, tipoCampoGrupoId, maiorIndice + 1, sequenciaIdsCampos);
		adicionarNovoGrupoAosCamposSituacao(novoGrupo);

		addMessage("grupoCriado.sucesso");
	}

	public void removerAnexo(FileVO fileVO) {

		if(this.solicitacaoVO != null) {
			this.solicitacaoVO.removerAnexo(fileVO);
		} else if(this.evidenciaVO != null) {
			this.evidenciaVO.removerAnexo(fileVO);
		} else if(this.digitalizacaoVO != null) {
			this.digitalizacaoVO.removerAnexo(fileVO);
		} else if(this.emailVO != null) {
			this.emailVO.removerAnexo(fileVO);
		}
	}

	public void uploadPrintSolicitacao() {

		List<FileVO> arquivos = this.solicitacaoVO.getArquivos();
		if(arquivos.size() >= 5) {
			addMessageError("numeroMaximoArquivos.error");
			return;
		}

		try {
			String arquivoPrintStr = Faces.getRequestParameter("arquivoPrint");
			File tmpFile = DummyUtils.getPrintFile(arquivoPrintStr);

			List<String> filesNames = this.solicitacaoVO.getFilesNames();
			String fileName = getPrintName(filesNames);
			this.solicitacaoVO.addAnexo(fileName, tmpFile);
			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void uploadPrintEvidencia() {

		List<FileVO> arquivos = this.evidenciaVO.getArquivos();
		if(arquivos.size() >= 5) {
			addMessageError("numeroMaximoArquivos.error");
			return;
		}

		try {
			String arquivoPrintStr = Faces.getRequestParameter("arquivoPrint");
			File tmpFile = DummyUtils.getPrintFile(arquivoPrintStr);

			List<String> filesNames = this.evidenciaVO.getFilesNames();
			String fileName = getPrintName(filesNames);
			this.evidenciaVO.addAnexo(fileName, tmpFile);
			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void uploadPrintEmail() {

		List<FileVO> arquivos = this.emailVO.getArquivos();
		if(arquivos.size() >= 5) {
			addMessageError("numeroMaximoArquivos.error");
			return;
		}

		try {
			String arquivoPrintStr = Faces.getRequestParameter("arquivoPrint");
			File tmpFile = DummyUtils.getPrintFile(arquivoPrintStr);

			List<String> filesNames = this.emailVO.getFilesNames();
			String fileName = getPrintName(filesNames);
			this.emailVO.addAnexo(fileName, tmpFile);
			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	private String getPrintName(List<String> filesNames) {

		int i = 1;
		String fileName;
		do {
			fileName = "print(" + i++ + ").jpg";
		}
		while(filesNames.contains(fileName));

		return fileName;
	}

	public void uploadAnexo(FileUploadEvent event) {

		try {
			List<FileVO> arquivos = null;
			if(this.solicitacaoVO != null) {
				arquivos = this.solicitacaoVO.getArquivos();
			} else if(this.digitalizacaoVO != null) {
				arquivos = this.digitalizacaoVO.getArquivos();
			} else if(this.evidenciaVO != null) {
				arquivos = this.evidenciaVO.getArquivos();
			} else if(this.emailVO != null) {
				arquivos = this.emailVO.getArquivos();
			}
			if(arquivos.size() >= 10) {
				addMessageError("numeroMaximoArquivos.error");
				return;
			}

			UploadedFile uploadedFile = event.getFile();

			String fileName = uploadedFile.getFileName();
			try{
				parametroService.validarArquivoPermitido(fileName);
			} catch (Exception e) {
				addMessageError(e);
				return;
			}

			File tmpFile = DummyUtils.getFile("anexo-proc-", uploadedFile);

			if(tmpFile != null) {

				if(this.solicitacaoVO != null) {
					this.solicitacaoVO.addAnexo(fileName, tmpFile);
				} else if(this.digitalizacaoVO != null) {
					this.digitalizacaoVO.addAnexo(fileName, tmpFile);
				} else if(this.evidenciaVO != null) {
					this.evidenciaVO.addAnexo(fileName, tmpFile);
				} else if(this.emailVO != null) {
					this.emailVO.addAnexo(fileName, tmpFile);
				}

				addMessage("arquivoCarregado.sucesso");
			}
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void downloadAnexo(ProcessoLogAnexo anexo) {

		File file = processoLogAnexoService.getFile(anexo);
		String nome = anexo.getNome();

		sendFile(file, nome);
	}

	public void downloadAnexoEmail(EmailRecebidoAnexo anexo) {

		File file = emailRecebidoAnexoService.getFile(anexo);
		String nome = anexo.getFileName();

		sendFile(file, nome);
	}

	@SuppressWarnings("unchecked")
	private Map<Long, String> getValoresCampos() {

		Map<String, Object> viewMap = getViewMap();
		Set<CampoAbstract> camposSet = (Set<CampoAbstract>) viewMap.get(CAMPOS_MAP);
		Map<Long, String> map = new LinkedHashMap<>();

		if(camposSet != null) {
			for (CampoAbstract ca : camposSet) {
				Long campoId = ca.getId();
				String valor = ca.getValor();
				map.put(campoId, valor);
			}
		}

		return map;
	}

	public void gerarHashImagem() {

		try {
			String imagemIdStr = Faces.getRequestParameter("imagemId");
			Long imagemId = new Long(imagemIdStr);
			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			Imagem imagem = imagemService.get(imagemId);
			String metaDados = imagemMeta.getMetaDados();
			Map<?, ?> map = DummyUtils.jsonStringToMap(metaDados);
			String dataDigitalizacao = (String) map.get("dataDigitalizacao");
			this.dataDigitalizacao = dataDigitalizacao;
			String hash = null;
			File file = imagemService.getFile(imagem);
			if(file.exists()){
				hash = DummyUtils.getHashChecksum(file);
			}
			hashImagem = hash;
		}
		catch (Exception e){
			addMessageError(e);
		}
	}

	public String getDocumentosMapJson() {
		return documentosMapJson;
	}

	public String getDocumentosJson() {
		return documentosJson;
	}

	public List<CampoGrupo> getGrupos() {

		if (campos == null) {
			return null;
		}

		return new ArrayList<>(campos.keySet());
	}

	public List<GrupoSuperiorVO> getGruposVO() {

		if (campos == null) {
			return null;
		}

		return processoService.getGrupoSuperiorVOS(campos.keySet());
	}

	public List<CampoLineVO> getCampos(CampoGrupo grupo) {

		List<Campo> camposList = campos.get(grupo);
		List<CampoLineVO> list = new ArrayList<>();

		for (Campo campo : camposList) {
			TipoEntradaCampo tipo = campo.getTipo();
			if(TipoEntradaCampo.TEXTO_LONGO.equals(tipo) || TipoEntradaCampo.COMBO_BOX_MULTI.equals(tipo)) {
				CampoLineVO vo = new CampoLineVO();
				vo.setColunaUnica(true);
				vo.setCampo1(campo);
				list.add(vo);
			}
			else {
				int lastIdx = list.size() - 1;
				CampoLineVO vo = lastIdx >= 0 ? list.get(lastIdx) : null;
				if(vo == null) {
					vo = new CampoLineVO();
					list.add(vo);
				}
				boolean colunaUnica = vo.isColunaUnica();
				Campo c1 = vo.getCampo1();
				Campo c2 = vo.getCampo2();
				if(!colunaUnica && c1 == null)
					vo.setCampo1(campo);
				else if(!colunaUnica && c2 == null)
					vo.setCampo2(campo);
				else {
					vo = new CampoLineVO();
					vo.setCampo1(campo);
					list.add(vo);
				}
			}
		}

		return list;
	}

	public List<CampoLineVO> getCamposSituacaoVO(CampoGrupo grupo) {

		Long processoId = processo.getId();
		Map<CampoGrupo, List<Campo>> camposSituacaoMap = processoService.mapearCamposListParaMap(this.camposSituacao, processoId);

		List<Campo> camposList = camposSituacaoMap.get(grupo);
		List<CampoLineVO> list = new ArrayList<>();

		for (Campo campo : camposList) {
			TipoEntradaCampo tipo = campo.getTipo();
			if(TipoEntradaCampo.TEXTO_LONGO.equals(tipo) || TipoEntradaCampo.COMBO_BOX_MULTI.equals(tipo)) {
				CampoLineVO vo = new CampoLineVO();
				vo.setColunaUnica(true);
				vo.setCampo1(campo);
				list.add(vo);
			}
			else {
				int lastIdx = list.size() - 1;
				CampoLineVO vo = lastIdx >= 0 ? list.get(lastIdx) : null;
				if(vo == null) {
					vo = new CampoLineVO();
					list.add(vo);
				}
				boolean colunaUnica = vo.isColunaUnica();
				Campo c1 = vo.getCampo1();
				Campo c2 = vo.getCampo2();
				if(!colunaUnica && c1 == null)
					vo.setCampo1(campo);
				else if(!colunaUnica && c2 == null)
					vo.setCampo2(campo);
				else {
					vo = new CampoLineVO();
					vo.setCampo1(campo);
					list.add(vo);
				}
			}
		}

		return list;
	}

	public List<Campo> getCampos1(CampoGrupo grupo) {
		List<Campo> list = campos.get(grupo);
		if(list == null) {
			return null;
		}

		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(0, meio);
	}

	public List<Campo> getCampos2(CampoGrupo grupo) {
		List<Campo> list = campos.get(grupo);
		if(list == null) {
			return null;
		}
		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(meio, size);
	}

	public List<DocumentoVO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoVO> documentos) {
		this.documentos = documentos;
	}

	public List<DocumentoVO> getDocumentosProcessoId(Long processoId) {
		List<DocumentoVO> list = documentosProcessoId.get(processoId);
		if(list == null) {
			return null;
		}
		return list;
	}

	public List<Long> getGrupoDocumentos() {
		if(documentosProcessoId == null) {
			return null;
		}
		return new ArrayList<>(documentosProcessoId.keySet());
	}

	public void carregarDocumentosProcessoId() {

		Map<Long, List<DocumentoVO>> listMap = new LinkedHashMap<>();
		List<DocumentoVO> voList;
		for(DocumentoVO documentoVO : documentos) {
			voList = listMap.get(documentoVO.getProcessoId());
			if (voList == null) {
				voList = new ArrayList<>();
			}
			if (!documentoVO.isExcluido()) {
				voList.add(documentoVO);
				listMap.put(documentoVO.getProcessoId(), voList);
			}
		}
		this.documentosProcessoId = listMap;
	}

	private void carregarIrregularidadesDocumento() {
		this.irregularidadesDocumentoIdMap = documentoService.getMapIrregularidadesPorDocumento(documentos);
	}

	public boolean possuiRegrasPendentes() {
		if(processo == null) return false;

		Long processoId = processo.getId();
		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setRegrasExecutadas(false);
		filtro.setProcessoId(processoId);
		int count = processoService.countByFiltro(filtro);
		if(count == 0 ) {
			this.processo = processoService.get(processoId);
		}
		return count > 0;
	}

	public int verificaGrupoDocumentos(Long processoId){
		processoId = processoId != null ? processoId : processo.getId();
		List<DocumentoVO> voList = documentosProcessoId.get(processoId);
		int count = 0;
		for (DocumentoVO documentoVO : voList) {
			StatusDocumento status = documentoVO.getStatus();
			if (!status.equals(StatusDocumento.APROVADO)) {
				count++;
			}
		}
		return count;
	}

	public void carregaCampos() {
		initBean();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void prepararReconhecimentoFacial(DocumentoVO vo) {

		Long documentoId = vo.getId();
		Documento documento = documentoService.get(documentoId);
		this.documento = documento;

		String contextPath = getContextPath();
		String imagePath = contextPath + ImagemFilter.PATH;

		comparacaoFacesVO = documentoService.compararFaces(documento);
		comparacaoFacesVO.setImagePath(imagePath);
	}

	public void setDocumento(DocumentoVO vo) {

		if(vo == null) {
			this.documento = null;
		}
		else {
			Long documentoId = vo.getId();
			Documento documento = documentoService.get(documentoId);
			this.documento = documento;
		}
	}

	public void setDocumentoOcr(DocumentoVO vo) {

		Long documentoId = vo.getId();
		Documento documento = documentoService.get(documentoId);
		setDocumentoOcr(documento);
	}

	private void setDocumentoOcr(Documento documento) {

		this.documento = documento;
		Long documentoId = documento.getId();

		List<CampoOcr> list = campoOcrService.findByDocumento(documentoId);
		this.camposOcr = campoOcrService.getCampoOcrVoList(documento, list, getContextPath());

		logOcr = logOcrService.getLastLog(documentoId);
	}

	public void setDocumentoText() {
		String imagemIdStr = Faces.getRequestParameter("imagemId");
		String versaoStr = Faces.getRequestParameter("versao");

		this.imagemId = imagemIdStr != null ? new Long(imagemIdStr) : null;
		this.versao = new Integer(versaoStr);
		this.fullTexts = documentoService.getFullTexts(documento, imagemId, versao);
	}

	public List<String> getFullTexts() {
		return fullTexts;
	}

	public Documento getDocumento() {
		return documento;
	}

	public int getIndiceImagem1() {
		return indiceImagem1;
	}

	public int getIndiceImagem2() {
		return indiceImagem2;
	}

	public boolean getDigitalizarTwain() {
		return digitalizarTwain;
	}

	public List<LogVO> getLogs() {
		return logs;
	}

	public Integer getChave() {
		return chave;
	}

	public void setChave(Integer chave) {
		this.chave = chave;
	}

	public boolean isEditandoVisualizacao() {
		return editandoVisualizacao;
	}

	public void setEditandoVisualizacao(boolean editandoVisualizacao) {
		if(editandoVisualizacao) {
			setEditando(true);
		}
		this.editandoVisualizacao = editandoVisualizacao;
	}

	public boolean isEditando() {
		return editando;
	}

	public void setEditando(boolean editando) {
		this.editando = editando;
	}

	public List<Area> getAreas() {
		return areas;
	}

	public SolicitacaoVO getSolicitacaoVO() {
		return solicitacaoVO;
	}

	public EvidenciaVO getEvidenciaVO() {
		return evidenciaVO;
	}

	public List<SolicitacaoVO> getSolicitacoes() {
		return solicitacoes;
	}

	public List<SolicitacaoVO> getSolicitacoesPendentes() {
		return solicitacoesPendentes;
	}

	public List<TipoEvidencia> getTiposEvidencias() {
		return tiposEvidencias;
	}

	public EmailVO getEmailVO() {
		return emailVO;
	}

	public void setEmailVO(EmailVO emailVO) {
		this.emailVO = emailVO;
	}

	public List<EmailVO> getEmailsVOs() {
		return emailsVOs;
	}

	public List<String> getEmailsPossiveis(String query) {

		Set<String> set = new TreeSet<>();

		for (CampoGrupo campoGrupo : this.campos.keySet()) {
			List<Campo> campos = this.campos.get(campoGrupo);
			for (Campo campo : campos) {
				TipoEntradaCampo tipo = campo.getTipo();
				if(TipoEntradaCampo.EMAIL.equals(tipo)) {
					String nome = campo.getNome();
					String valor = campo.getValor();
					if(StringUtils.isNotBlank(valor)) {
						nome = DummyUtils.capitalize(nome);
						set.add(nome + " [" + valor + "]");
					}
				}
			}
		}

		Usuario autor = processo.getAutor();
		if (autor != null) {
			set.add("Autor [" + autor.getEmail() + "]");
		}

		ArrayList<String> list = new ArrayList<>();

		list.add(query);
		list.addAll(set);

		return list;
	}

	public EmailRecebido getEmailRecebido() {
		return emailRecebido;
	}

	public void setEmailRecebido(EmailRecebido emailRecebido) {

		if(emailRecebido != null) {
			Usuario usuario = getUsuarioLogado();
			Date dataLeitura = emailRecebido.getDataLeitura();
			if(usuario.isAnalistaRole() && dataLeitura == null) {
				marcarEmailRecebidoComoLido(emailRecebido);
			}
		}

		this.emailRecebido = emailRecebido;
	}

	public void marcarEmailRecebidoComoLido(EmailRecebido emailRecebido) {

		Usuario usuario = getUsuarioLogado();
		try {
			processoService.marcarEmailRecebidoComoLido(processo, emailRecebido, usuario);
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEmails();
	}

	public void marcarEmailRecebidoComoNaoLido(EmailRecebido emailRecebido) {

		Usuario usuario = getUsuarioLogado();
		try {
			processoService.marcarEmailRecebidoComoNaoLido(processo, emailRecebido, usuario);
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEmails();
	}

	public void selecionaNovaSituacao(AjaxBehaviorEvent event) {

		Situacao situacao;
		if(evidenciaVO == null) {
			situacao = novaSituacao;
		} else {
			situacao = evidenciaVO.getSituacao();
		}

		if(situacao != null) {
			if(situacao.isNotificarAutor()) {
				List<String> emails = new ArrayList<>();
				String email = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL);
				emails.add(email);
				Usuario autor = processo.getAutor();
				if (autor != null) {
					String email2 = autor.getEmail();
					emails.add(email2);
				}
				evidenciaVO.setShowEmail(true);
				evidenciaVO.setDestinatariosList(emails);
			}

			StatusProcesso status = situacao.getStatus();
			boolean showPrazoDias = StatusProcesso.EM_ACOMPANHAMENTO.equals(status);
			if(showPrazoDias) {
				this.evidenciaVO.setShowPrazoDias(showPrazoDias);
				BigDecimal horasPrazo = situacao.getHorasPrazo();
				if(horasPrazo != null) {
					String[] expedienteArray = parametroService.getExpediente();
					Expediente expediente = new Expediente(expedienteArray);
					BigDecimal horas = expediente.getHoras();
					BigDecimal prazoDias = horasPrazo.divide(horas, RoundingMode.CEILING);
					evidenciaVO.setPrazoDias(prazoDias.intValue());
				}
			}

			Long situacaoId = situacao.getId();
			String situacaoNome = situacao.getNome();
			Processo processoCampos;
			if(Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID.equals(situacaoId) || Situacao.ISENCAO_DISCIPLINAS_REVISAO_TE_ID.equals(situacaoId)){
				processoCampos = processoService.getProcessoIsencaoDisciplinas(processo);
			} else {
				Long processoId = processo.getId();
				processoCampos = processoService.get(processoId);
			}
			Long processoId = processoCampos.getId();
			Usuario usuario = getUsuarioLogado();

			camposSituacao = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, situacaoId, null, camposSituacao);

			Set<CampoGrupo> gruposCampos = processoCampos.getGruposCampos();
			for(CampoGrupo campoGrupo : gruposCampos){
				String nome = campoGrupo.getNome();
				if(CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome().equals(nome) && Situacao.ANALISE_ISENCAO_CONCLUIDA.equals(situacaoNome)){
					List<Campo> campos = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, null, CampoMap.GrupoEnum.REVISAO_ISENCAO.getNome(), camposSituacao);
					camposSituacao.addAll(campos);
				}
			}
		}
	}

	public void adicionarTexto(TextoPadrao tp) {

		Usuario usuarioLogado = getUsuarioLogado();
		String texto = textoPadraoService.renderizar(tp, processo, pendenciasCkeckList, usuarioLogado);
		if (this.evidenciaVO != null) {
			this.evidenciaVO.setObservacaoTmp(texto);
		}
		else if (this.solicitacaoVO != null) {
			this.solicitacaoVO.setObservacaoTmp(texto);
		}
		else if (this.emailVO != null) {
			this.emailVO.setObservacaoTmp(texto);
		}
	}

	public void limparFiltroCarregarAnexos() {
		anexoFiltro = new AnexoFiltro();
		carregarAnexos();
	}

	public String getHorasRestantes() {
		return horasRestantes;
	}

	public void setHorasRestantes(String horasRestantes) {
		this.horasRestantes = horasRestantes;
	}

	public List<Subarea> getSubareas() {
		return subareas;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public Usuario getNovoAnalista() {
		return novoAnalista;
	}

	public void setNovoAnalista(Usuario novoAnalista) {
		this.novoAnalista = novoAnalista;
	}

	public TipoProcesso getNovoTipoProcesso() {
		return novoTipoProcesso;
	}

	public void setNovoTipoProcesso(TipoProcesso novoTipoProcesso) {
		this.novoTipoProcesso = novoTipoProcesso;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public Long getNovoDocumento() {
		return novoDocumentoId;
	}

	public void setNovoDocumento(Long novoDocumentoId) {
		this.novoDocumentoId = novoDocumentoId;
	}

	public List<DocumentoVO> getDocumentosExcluidos() {
		return documentosExcluidos;
	}

	public ProcessoLog getProcessoLog() {
		return processoLog;
	}

	public void setProcessoLog(ProcessoLog processoLog) {
		this.processoLog = processoLog;
	}

	public Integer getProcessosAbertos() {
		return processosAbertos;
	}

	public Integer getProcessosFechados() {
		return processosFechados;
	}

	public List<ProcessoVO> getProcessosMesmoCpjCnpj() {
		return processosMesmoCpjCnpj;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public Situacao getNovaSituacao() {
		return novaSituacao;
	}

	public void setNovaSituacao(Situacao novaSituacao) {
		this.novaSituacao = novaSituacao;
	}

	public DigitalizacaoVO getDigitalizacaoVO() {
		return digitalizacaoVO;
	}

	public void setDigitalizacaoVO(DigitalizacaoVO digitalizacaoVO) {
		this.digitalizacaoVO = digitalizacaoVO;
	}

	public ProcessoPendenciaVO getProcessoPendencia() {
		return processoPendencia;
	}

	public LogVO getLogVO() {
		return logVO;
	}

	public void setLogVO(LogVO logVO) {
		this.logVO = logVO;
	}

	public ItemPendente getItemPendente() {
		return itemPendente;
	}

	public void setItemPendente(ItemPendente itemPendente) {
		this.itemPendente = itemPendente;
	}

	public String getNovoCodigoAr() {
		return novoCodigoAr;
	}

	public void setNovoCodigoAr(String novoCodigoAr) {
		this.novoCodigoAr = novoCodigoAr;
	}

	public RelatorioGeral getRelatorioGeral() {
		return relatorioGeral;
	}

	public void setRelatorioGeral(RelatorioGeral relatorioGeral) {
		this.relatorioGeral = relatorioGeral;
	}

	public boolean isMostrarAlertaRaschunho() {
		return mostrarAlertaRaschunho;
	}

	public void setMostrarAlertaRaschunho(boolean mostrarAlertaRaschunho) {
		this.mostrarAlertaRaschunho = mostrarAlertaRaschunho;
	}

	public List<LogVO> getLogsAnexo() {
		return logsAnexo;
	}

	public Map<ProcessoAjuda, List<ProcessoAjuda>> getAjudasMarcadas() {
		return ajudasMarcadas;
	}

	public void setAjudasMarcadas(Map<ProcessoAjuda, List<ProcessoAjuda>> ajudasMarcadas) {
		this.ajudasMarcadas = ajudasMarcadas;
	}

	public String getHorasRestantesSituacao() {
		return horasRestantesSituacao;
	}

	public void setHorasRestantesSituacao(String horasRestantesTarefa) {
		this.horasRestantesSituacao = horasRestantesTarefa;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public String getAnexoPath() {
		return anexoPath;
	}

	public void setAnexoPath(String anexoPath) {
		this.anexoPath = anexoPath;
	}

	public List<CampoOcrVO> getCamposOcr() {
		return camposOcr;
	}

	public ComparacaoFacesVO getComparacaoFacesVO() {
		return comparacaoFacesVO;
	}

	public ProcessoEditAutorizacao getAutorizacao() {
		return autorizacao;
	}

	public LogOcr getLogOcr() {
		return logOcr;
	}

	public void reprocessarOcr() {

		try {
			String documentoIdStr = Faces.getRequestParameter("docId");
			Usuario usuario = getUsuarioLogado();
			Long documentoId = new Long(documentoIdStr);
			Documento doc = documentoService.get(documentoId);

			documentoService.agendarOcr(doc, usuario);

			setDocumentoOcr(doc);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initDadosProcesso();
	}

	public AnexoFiltro getAnexoFiltro() {
		return anexoFiltro;
	}

	public List<TextoPadrao> getTextosPadroes() {

		List<TextoPadrao> filterTextosPadroes = new ArrayList<TextoPadrao>();
		if (textosPadroes == null) {
			return null;
		}

		for (TextoPadrao textoPadrao : textosPadroes) {
			String permissoesDeUso = textoPadrao.getPermissoesDeUso();
			if(this.solicitacaoVO != null && permissoesDeUso.contains(TextoPadrao.SOLICITACAO_AREA)) {
				filterTextosPadroes.add(textoPadrao);
			}
			else if(this.evidenciaVO != null) {
				Situacao situacao = evidenciaVO.getSituacao();
				boolean isEncaminhado = situacao != null ? situacao.isEncaminhado() : false;
				if(permissoesDeUso.contains(TextoPadrao.REGISTRO_EVIDENCIA) ||
						(isEncaminhado && permissoesDeUso.contains(TextoPadrao.SOLICITACAO_AREA))) {
					filterTextosPadroes.add(textoPadrao);
				}
			}
			else if(this.emailVO != null && permissoesDeUso.contains(TextoPadrao.ENVIO_EMAIL)) {
				filterTextosPadroes.add(textoPadrao);
			}
		}

		return filterTextosPadroes;
	}

	public boolean processoTemRegras() {
		return processoTemRegras;
	}

	public void crop() {

		Double x = Double.parseDouble(Faces.getRequestParameter("coordenadasX"));
		Double y = Double.parseDouble(Faces.getRequestParameter("coordenadasY"));
		Double w = Double.parseDouble(Faces.getRequestParameter("coordenadasW"));
		Double h = Double.parseDouble(Faces.getRequestParameter("coordenadasH"));
		int img = Integer.parseInt(Faces.getRequestParameter("imagem"));

		int x2 = x.intValue();
		int y2 = y.intValue();
		int w2 = w.intValue();
		int h2 = h.intValue();

		try {
			Usuario usuarioLogado = getUsuarioLogado();
			Documento documento = getDocumento();
			Long documentoId = documento.getId();
			Long imagemId =  new Long(img);

			documentoService.cropImagem(usuarioLogado, documentoId, imagemId, Origem.WEB, new int[]{x2, y2, w2, h2});

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		initBean();
		return;
	}

	public void girarPdf() {

		int imgId = Integer.parseInt(Faces.getRequestParameter("imagemId"));
		String direcao = Faces.getRequestParameter("direcao");
		int rotacao = direcao.equals("direita") ? 90 : -90;

		try {

			Usuario usuarioLogado = getUsuarioLogado();
			Documento documento = getDocumento();
			Long imagemId =  new Long(imgId);

			documentoService.girarPdf(usuarioLogado, documento, imagemId, rotacao);
			initBean();

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);

		}
	}

	public List<ProcessoAjuda> getPendenciasCkeckList() {
		return pendenciasCkeckList;
	}

	public List<Irregularidade> getIrregularidadesAutoComplete(String query) {

		irregularidadesPorDocumento = new ArrayList<>();

		String documentoIdStr = Faces.getRequestParameter("documentoSelect1");
		if(StringUtils.isNotBlank(documentoIdStr)) {
			Long documentoId = Long.valueOf(documentoIdStr);
			irregularidadesPorDocumento = irregularidadesDocumentoIdMap.get(documentoId);
		}

		List<Irregularidade> irregularidades = (irregularidadesPorDocumento != null && !irregularidadesPorDocumento.isEmpty()) ? irregularidadesPorDocumento : this.irregularidades;
		String queryLowerCase = query.toLowerCase();
		List<Irregularidade> irregularidadesFiltradas =  irregularidades.stream().filter(t -> t.getNome().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());

		return irregularidadesFiltradas;
	}

	public boolean getPendenciasCheckListNaoCompleto() {
		return pendenciasCheckListNaoCompleto;
	}

	public List<ProcessoRegra> getRegrasErro() {
		return regrasErro;
	}

	public List<ProcessoRegra> getRegrasVermelho() {
		return regrasVermelho;
	}

	public List<ProcessoRegra> getRegrasPendente() {
		return regrasPendente;
	}

	public List<ProcessoRegra> getRegrasAmarelo() {
		return regrasAmarelo;
	}

	public List<ProcessoRegra> getRegrasVerde() {
		return regrasVerde;
	}

	public List<ProcessoRegraLog> getProcessoRegraLogs() {
		return processoRegraLogs;
	}

	public List<ResultadoConsultaVO> getResultadosConsultas() {
		return resultadosConsultas;
	}

	public List<Campo> getCamposSituacao() {
		return camposSituacao;
	}

	public List<GrupoSuperiorVO> getGruposSituacaoVO() {

		Long processoId = processo.getId();
		Map<CampoGrupo, List<Campo>> camposSituacaoMap = processoService.mapearCamposListParaMap(this.camposSituacao, processoId);

		List<GrupoSuperiorVO> grupoSuperiorVOS = processoService.getGrupoSuperiorVOS(camposSituacaoMap.keySet());
		return grupoSuperiorVOS;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setGrupoExcluir(CampoGrupo grupoExcluir) {

		if(grupoExcluir == null) {
			this.grupoExcluir = null;
		}
		else {
			this.grupoExcluir = grupoExcluir;

			boolean temImagensRelacionadas = grupoExcluir.getId() != null && imagemService.campoGrupoTemImagensRelacionadas(grupoExcluir);
			setRequestAttribute("temImagensRelacionadas", temImagensRelacionadas);
		}
	}

	public Boolean verificarVencido(){
		Date hoje = new Date();
		Date validadeExpiracao = documento.getValidadeExpiracao();
		Date dataExpiracao = (validadeExpiracao != null? validadeExpiracao: hoje);
		return hoje.after(dataExpiracao);
	}

	public Boolean verificarVencido(DocumentoVO doc){
		Date hoje = new Date();
		Date dataExpiracao = (doc.getValidadeExpiracao() != null? doc.getValidadeExpiracao() : hoje);
		return hoje.after(dataExpiracao);
	}

	public List<ModeloDocumento> getModeloDocumentosAutoComplete(String query) {
		String queryLowerCase = query.toLowerCase();
		List<ModeloDocumento> modeloDocumentosFiltrados =  modeloDocumentos.stream().filter(md -> md.getDescricao().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());

		return modeloDocumentosFiltrados;
	}

	public Map<Long, List<Long>> getDocumentosEquivalentes() {
		return documentosEquivalentes;
	}

	public String getNomeDocumento(Long tipoDocumentoId) {
		TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
		return tipoDocumento != null ? tipoDocumento.getNome() : null;
	}

	public void setAutorizacao(ProcessoEditAutorizacao autorizacao) {
		this.autorizacao = autorizacao;
	}

	public String getHashImagem() {
		return hashImagem;
	}

	public String getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public String getCorEquivalencia(Long tipoDocId){

		if(tipoDocId == null) {
			return null;
		}

		Long processoId = processo.getId();
		String amarelo = "#f5d716";
		String azulClaro = "#d1ecf1";
		String laranja = "#f0ad4e";
		String verde = "#98ff98";
		String cinza = "#d3d3d3";
		String cor = null;
		List<StatusDocumento> docNaoEntregue = Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.EXCLUIDO, StatusDocumento.PENDENTE);
		List<StatusDocumento> docEntregue = Arrays.asList(StatusDocumento.DIGITALIZADO, StatusDocumento.APROVADO);
		//FIXME: esse método está sendo chamado duas vezes para cada ducumento
		Documento documentoAtual = documentoService.getFirstByTipoDocumentoId(tipoDocId, processoId);
		StatusDocumento documentoAtualStatus = documentoAtual.getStatus();
		TipoDocumento tipoDocumento = documentoAtual.getTipoDocumento();
		boolean termoPastaVermelha = tipoDocumento != null ? tipoDocumento.getTermoPastaVermelha() : false;
		List<Long> equivalidosId = documentosEquivalidos.get(tipoDocId);
		List<Long> equivalentesId = documentosEquivalentes.get(tipoDocId);
		Boolean usaTermo = processo.getUsaTermo();
		usaTermo = usaTermo != null ? usaTermo : false;

		if(equivalidosId != null) {

			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setProcesso(processo);
			filtro.setTipoDocumentoIdList(equivalidosId);
			List<Documento> equivalidos = documentoService.findByFiltro(filtro, null, null);
			for(Documento equivalido : equivalidos){
				StatusDocumento equivalidoStatus = equivalido.getStatus();
				if(docEntregue.contains(equivalidoStatus)){
					cor = laranja;
				}
				if(docNaoEntregue.contains(equivalidoStatus)){
					cor = laranja;
				}
				if(docNaoEntregue.contains(equivalidoStatus) && docNaoEntregue.contains(documentoAtualStatus) || StatusDocumento.PENDENTE.equals(documentoAtualStatus)){
					cor = cinza;
				}
				boolean todosDigitalizados = documentoService.isTodosDigitalizados(Arrays.asList(processoId), equivalidosId);
				if(todosDigitalizados && docEntregue.contains(documentoAtualStatus)){
					cor = verde;
				}
				if(termoPastaVermelha && usaTermo){
					cor = amarelo;
				}
			}

			return cor;
		}

		if(equivalentesId != null){
			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setProcesso(processo);
			filtro.setTipoDocumentoIdList(equivalentesId);
			List<Documento> equivalentes = documentoService.findByFiltro(filtro, null, null);
			for(Documento equivalente : equivalentes){
				StatusDocumento equivalenteStatus = equivalente.getStatus();
				if(docEntregue.contains(equivalenteStatus)){
					cor = cinza;
				}
				if(docNaoEntregue.contains(equivalenteStatus) && docEntregue.contains(documentoAtualStatus)){
					cor = azulClaro;
				}
				boolean todosDigitalizados = documentoService.isTodosDigitalizados(Arrays.asList(processoId), equivalentesId);
				if(todosDigitalizados && docEntregue.contains(documentoAtualStatus)){
					cor = verde;
				}
				if(termoPastaVermelha && usaTermo){
					cor = amarelo;
				}
			}
			return cor;
		}

		return null;
	}

	public boolean isDocumentoPastaAmarela(Long tipoDocId){

		if(tipoDocId == null) {
			return false;
		}

		Long processoId = processo.getId();

		//FIXME: esse método está sendo chamado duas vezes para cada ducumento
		Documento documentoAtual = documentoService.getFirstByTipoDocumentoId(tipoDocId, processoId);
		Advertencia advertencia = documentoAtual.getAdvertencia();
		if(advertencia == null) {
			return false;
		}

		return true;
	}

	public void atualizarPermissaoEdicaoDocumentoProUni() {
		podeReceberDocumentosSisProuniNoCalendario = documentoService.atualizarPermissaoEdicaoDocumentoProUni(processo, this.documentos);
	}

	public Map<Long, List<Long>> getDocumentosEquivalidos() {
		return documentosEquivalidos;
	}

	public void setEvidenciaVO(EvidenciaVO evidenciaVO) {
		this.evidenciaVO = evidenciaVO;
	}

	public String getEmailNotificacao() {
		return emailNotificacao;
	}

	public void setEmailNotificacao(String emailNotificacao) {
		this.emailNotificacao = emailNotificacao;
	}

	public Irregularidade getIrregularidade() {
		return irregularidade;
	}

	public void setIrregularidade(Irregularidade irregularidade) {
		this.irregularidade = irregularidade;
	}

	public String getObservacaoIrregularidade() {
		return observacaoIrregularidade;
	}

	public void setObservacaoIrregularidade(String observacaoIrregularidade) {
		this.observacaoIrregularidade = observacaoIrregularidade;
	}

	public List<Irregularidade> getIrregularidadesPorDocumento() {
		return irregularidadesPorDocumento;
	}

	public List<TipoProcesso> getModalidades() {
		return modalidades;
	}

	public TipoProcesso getModalidade() {
		return modalidade;
	}

	public void setModalidade(TipoProcesso modalidade) {
		this.modalidade = modalidade;
	}

	public List<ModeloDocumento> getModeloDocumentos() {
		return modeloDocumentos;
	}

	public void setModeloDocumentos(List<ModeloDocumento> modeloDocumentos) {
		this.modeloDocumentos = modeloDocumentos;
	}

	public boolean isPossuiProcessoIsencao() {
		return possuiProcessoIsencao;
	}

	public boolean isRevisaoAntiga() {
		return revisaoAntiga;
	}

	public String getLinkAluno() {
		return linkAluno;
	}

	public boolean getPastaVermelha() {
		return isPastaVermelha;
	}

	public boolean isPastaAmarela() {
		return isPastaAmarela;
	}

	public List<Long> getIdDocumentos() {
		return idDocumentos;
	}

	public List<ModeloDocumento> getModelosDocumentos() {
		return modelosDocumentos;
	}

	public void setModelosDocumentos(List<ModeloDocumento> modelosDocumentos) {
		this.modelosDocumentos = modelosDocumentos;
	}

	public ProcessoService getProcessoService() {
		return processoService;
	}

	public void setProcessoService(ProcessoService processoService) {
		this.processoService = processoService;
	}

	public List<ModeloDocumentoVO> getModelosDocumentoVO() {
		return modelosDocumentoVO;
	}

	public void setModelosDocumentoVO(List<ModeloDocumentoVO> modelosDocumentoVO) {
		this.modelosDocumentoVO = modelosDocumentoVO;
	}

	public String getModelosDocumentosMapJson() {
		return modelosDocumentosMapJson;
	}

	public void setModelosDocumentosMapJson(String modelosDocumentosMapJson) {
		this.modelosDocumentosMapJson = modelosDocumentosMapJson;
	}

	public void carregarModelosDocumentos() {

		this.modelosDocumentoVO = modeloDocumentoService.findVOsByProcesso();

		Map<Long, ModeloDocumentoVO> modelosDocumentoMap = new LinkedHashMap<>();

		for (ModeloDocumentoVO vo : this.modelosDocumentoVO) {
			Long modeloDocumentoId = vo.getId();
			modelosDocumentoMap.put(modeloDocumentoId, vo);
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			this.modelosDocumentosMapJson = mapper.writeValueAsString(modelosDocumentoMap);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public RendimentoComposicaoFamiliarVO getRendimentoComposicaoFamiliar() {
		return rendimentoComposicaoFamiliar;
	}

	public void setRendimentoComposicaoFamiliar(RendimentoComposicaoFamiliarVO rendimentoComposicaoFamiliar) {
		this.rendimentoComposicaoFamiliar = rendimentoComposicaoFamiliar;
	}

	public List<SalarioMinimoVO> getSalariosMinimos() {
		return salariosMinimos;
	}

	public void setSalariosMinimos(List<SalarioMinimoVO> salariosMinimos) {
		this.salariosMinimos = salariosMinimos;
	}

	public SalarioMinimoVO getSalarioMinimoSelecionado() {
		return salarioMinimoSelecionado;
	}

	public void setSalarioMinimoSelecionado(SalarioMinimoVO salarioMinimoSelecionado) {
		this.salarioMinimoSelecionado = salarioMinimoSelecionado;
	}

	public List<Long> getIdTipoDocumentos() {
		return idTipoDocumentos;
	}

	public void setIdTipoDocumentos(List<Long> idTipoDocumentos) {
		this.idTipoDocumentos = idTipoDocumentos;
	}

	public Map<Long, String> getIrregularidadesPastaAmarelaMap() {
		return irregularidadesPastaAmarelaMap;
	}

	public void setIrregularidadesPastaAmarelaMap(Map<Long, String> irregularidadesPastaAmarelaMap) {
		this.irregularidadesPastaAmarelaMap = irregularidadesPastaAmarelaMap;
	}

	public ManutencaoDocumentoVO getManutencaoDocumentoVO() {
		return manutencaoDocumentoVO;
	}

	public void setManutencaoDocumentoVO(ManutencaoDocumentoVO manutencaoDocumentoVO) {
		this.manutencaoDocumentoVO = manutencaoDocumentoVO;
	}
	public boolean isAlteracaoObrigatoriedade() {
		return alteracaoObrigatoriedade;
	}

	public void setAlteracaoObrigatoriedade(boolean alteracaoObrigatoriedade) {
		this.alteracaoObrigatoriedade = alteracaoObrigatoriedade;
	}

	public boolean isPodeReceberDocumentosSisProuniNoCalendario() {
		return podeReceberDocumentosSisProuniNoCalendario;
	}

	public Boolean getOpcoesIes() {
		return opcoesIes;
	}

	public void setOpcoesIes(Boolean opcoesIes) {
		this.opcoesIes = opcoesIes;
	}

	public boolean isExistMateriaIsenta() {
		return existMateriaIsenta;
	}

	public void setExistMateriaIsenta(boolean existMateriaIsenta) {
		this.existMateriaIsenta = existMateriaIsenta;
	}
}
