package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.ProcessoRepository;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.aluno.GetDocAlunoService;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.ProcessoVO.AlertaSolicitacaoVO;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.getdoc.faces.validator.CampoValidator;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.*;
import net.wasys.util.other.Bolso;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import net.wasys.util.other.KeyLock;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static net.wasys.util.DummyUtils.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class ProcessoService {

	public static ThreadLocal<Boolean> LOCK_ENABLED = new ThreadLocal<>();

	@Autowired private ProcessoRepository processoRepository;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private CampoService campoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	@Autowired private ParametroService parametroService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private SolicitacaoService solicitacaoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private EmailRecebidoService emailRecebidoService;
	@Autowired private UsuarioTipoProcessoService usuarioTipoProcessoService;
	@Autowired private ProcessoPendenciaService processoPendenciaService;
	@Autowired private BloqueioProcessoService bloqueioProcessoService;
	@Autowired private MessageService messageService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private AjudaService ajudaService;
	@Autowired private ProcessoAjudaService processoAjudaService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private FilaConfiguracaoService filaConfiguracaoService;
	@Autowired private CampanhaService campanhaService;
	@Autowired private FeriadoService feriadoService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;
	@Autowired private SiaService siaService;
	@Autowired private GetDocAlunoService getDocAlunoService;
	@Autowired private ImagemService imagemService;
	@Autowired private EtapaService etapaService;
	@Autowired private ProximoProcessoService proximoProcessoService;
	@Autowired private LogAtendimentoService logAtendimentoService;
	@Autowired private UsuarioSubperfilService usuarioSubperfilService;
	@Autowired private ConfiguracaoOCRService configuracaoOCRService;
	@Autowired private ProcessoNotificarAtilaService processoNotificarAtilaService;

	private final ProximoProcessoBulkhead bulkhead = new ProximoProcessoBulkhead();

	public boolean isPeriodoIngressoTEFinanciamentoValido(Processo processoFinanciamentoAtual, ConsultaInscricoesVO candidatoCulando) {

		String periodoIngressoVinculando = candidatoCulando.getPeriodoIngresso();
		periodoIngressoVinculando = consultaCandidatoService.formatarPeriodo(periodoIngressoVinculando);
		String periodoDeIngressoProcesso = periodoDeIngressoProcesso = DummyUtils.getCampoProcessoValor(processoFinanciamentoAtual, CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO);

		if(periodoDeIngressoProcesso == null || periodoDeIngressoProcesso.isEmpty()) {
			throw new MessageKeyException("candidatoTE.periodoIngressoProcessoNuloOuVazio.error");
		}

		if(periodoIngressoVinculando == null || periodoIngressoVinculando.isEmpty()) {
			throw new MessageKeyException("candidatoTE.periodoIngressoVinculandoTENuloOuVazio.error");
		}

		if(periodoDeIngressoProcesso.equals(periodoIngressoVinculando)) {
			return true;
		}

		String periodoDeIngressoTeSiaFormatada = DummyUtils.formatarChaveUnicidadeBaseInterna(periodoIngressoVinculando);

		String semestreIngressoVinculando = baseRegistroService.getLabel(BaseInterna.PERIODO_DE_INGRESSO, periodoDeIngressoTeSiaFormatada);
		if(StringUtils.isBlank(semestreIngressoVinculando)) {
			throw new MessageKeyException("candidatoTE.vinculoNegado.periodoNaoCadastrado.error", periodoIngressoVinculando);
		}

		return isPeriodoIngressoFinanciamentoMaiorQueSiaTE(periodoDeIngressoProcesso, semestreIngressoVinculando);
	}

	@Test
	public void testeIsPeriodoIngressoFinanciamentoMaiorQueSiaTE() {
		DummyUtils.setLogLevel(LogLevel.DEBUG);
		DummyUtils.systraceThread("Testando validação de perído. INICIO.");
		assert isPeriodoIngressoFinanciamentoMaiorQueSiaTE("2019.2", "2020.2") == false;
		assert isPeriodoIngressoFinanciamentoMaiorQueSiaTE("2020.1", "2020.2") == false;
		assert isPeriodoIngressoFinanciamentoMaiorQueSiaTE("2020.2", "2020.2") == true;
		assert isPeriodoIngressoFinanciamentoMaiorQueSiaTE("2021.1", "2020.2") == true;
		DummyUtils.systraceThread("Testando validação de perído. FIM.");
	}

	private boolean isPeriodoIngressoFinanciamentoMaiorQueSiaTE(String periodoDeIngressoProcesso, String semestreIngressoVinculando) {

		String[] splitProcesso = periodoDeIngressoProcesso.split("\\.");
		String anoProcessoStr = splitProcesso[0];
		int anoProcesso = Integer.parseInt(anoProcessoStr);
		String semestreProcessoStr = splitProcesso[1];
		int semestreProcesso = Integer.parseInt(semestreProcessoStr);

		String[] splitSia = semestreIngressoVinculando.split("\\.");
		String anoIngressoVinculandoStr = splitSia[0];
		int anoVinculando = Integer.parseInt(anoIngressoVinculandoStr);
		String semestreIngressoVinculandoStr = splitSia[1];
		int semestreVinculando = Integer.parseInt(semestreIngressoVinculandoStr);

		if (anoProcesso > anoVinculando) {
			return true;
		}
		else if (anoProcesso == anoVinculando && semestreProcesso >= semestreVinculando) {
			return true;
		}
		else {
			return false;
		}
	}

	public static class CampoDePara {
		String de;
		String para;

		public CampoDePara(String de, String para) {
			this.de = de;
			this.para = para;
		}

		public String getDe() {
			return de;
		}

		public void setDe(String de) {
			this.de = de;
		}

		public String getPara() {
			return para;
		}

		public void setPara(String para) {
			this.para = para;
		}
	}

	public class ProcessoCriadoException extends RuntimeException {
		private Processo processo;

		public ProcessoCriadoException(Processo processo, Exception e) {
			super(e);
			this.processo = processo;
		}

		public Processo getProcesso() {
			return processo;
		}
	}

	public Processo get(Long id) {
		return processoRepository.get(id);
	}

	public Processo getLastByFiltro(ProcessoFiltro filtro) {
		return processoRepository.getLastByFiltro(filtro);
	}

	@Transactional(rollbackFor = Exception.class)
	public void atualizarUltimaAjuda(Long processoId, Long ultimaAjudaId) {
		processoRepository.atualizarUltimaAjuda(processoId, ultimaAjudaId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void excluir(Long processoId, Usuario usuario) throws ProcessoRestException {

		try {
			Long usuarioId = usuario.getId();
			String usuarioNome = usuario.getNome();
			systraceThread("Usuário: " + usuarioNome + " #" + usuarioId + ". Processo: " + processoId, LogLevel.FATAL);

			processoRepository.deleteById(processoId);
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void criaNovoProcessoEmail(EmailRecebido er) throws Exception {

		TipoProcesso tipoProcesso = tipoProcessoService.getByNome("EMAIL");
		if(tipoProcesso != null) {
			Long tipoProcessoId = tipoProcesso.getId();

			CriacaoProcessoVO vo = new CriacaoProcessoVO();
			vo.setTipoProcesso(tipoProcesso);
			vo.setOrigem(Origem.EMAIL);

			Set<CampoAbstract> valoresCampos = new HashSet<CampoAbstract>();
			Map<TipoCampoGrupo, List<TipoCampo>> mapByTipoProcesso = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);
			Collection<List<TipoCampo>> values = mapByTipoProcesso.values();
			for(List<TipoCampo> list : values) {
				for(TipoCampo tipoCampo : list) {
					valoresCampos.add(tipoCampo);
				}
			}

			vo.setValoresCampos(valoresCampos);
			vo.setAcao(AcaoProcesso.CRIACAO_CLIENTE);

			Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);
			Set<TipoCampoGrupo> keySet = map.keySet();
			for(TipoCampoGrupo grupo : keySet) {
				List<TipoCampo> list = map.get(grupo);
				for(TipoCampo campo : list) {
					String nome = campo.getNome();
					nome = DummyUtils.substituirCaracteresEspeciais(nome);
					if("DESCRICAO".equals(nome) || "OBSERVACAO".equals(nome)) {
						campo.setValor(nome);
					}
				}
			}

			Processo processo = new Processo();
			vo.setProcesso(processo);

			criaProcesso(vo);

			Set<EmailRecebidoAnexo> anexos = er.getAnexos();
			if(anexos != null && !anexos.isEmpty()) {
				ProcessoLog pl = new ProcessoLog();
				pl.setAcao(AcaoProcesso.ANEXO_PROCESSO);
				pl.setProcesso(processo);
				StatusProcesso status = processo.getStatus();
				pl.setStatusProcesso(status);
				processoLogService.saveOrUpdate(pl);

				for(EmailRecebidoAnexo era : anexos) {
					FileVO fileVo = new FileVO();

					File file = new File(era.getPath());
					fileVo.setName(era.getFileName());
					fileVo.setFile(file);

					processoLogAnexoService.criar(pl, fileVo);
				}
			}
		}
	}

	private void criarAjuda(Processo processo) {

		List<Ajuda> ajudas = ajudaService.getByTipoProcesso(processo.getTipoProcesso().getId());
		for(Ajuda ajuda : ajudas) {
			ProcessoAjuda processoAjuda = new ProcessoAjuda(ajuda, processo);
			TipoDocumento tipoDocumento = ajuda.getTipoDocumento();
			if(tipoDocumento == null) {
				processoAjuda.setAtivo(true);
			}
			else {
				processoAjuda.setAtivo(tipoDocumento.getObrigatorio());
			}
			processoAjudaService.salvar(processoAjuda);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public Processo criaProcesso(CriacaoProcessoVO vo) throws Exception {

		long inicio = System.currentTimeMillis();

		Collection<CampoAbstract> valoresCampos = vo.getValoresCampos();
		TipoProcesso tipoProcesso = vo.getTipoProcesso();
		Usuario usuario = vo.getUsuario();
		Map<Long, String> valoresChaveUnicidade = getValoresChaveUnicidade(tipoProcesso, valoresCampos);
		String chaveLock;
		if(valoresChaveUnicidade != null && !valoresChaveUnicidade.isEmpty()) {
			chaveLock = valoresChaveUnicidade.toString();
		}
		else if(usuario != null) {
			chaveLock = String.valueOf(usuario.getId());
		}
		else {
			chaveLock = String.valueOf(Math.random());
		}

		KeyLock lock = null;
		Boolean lockEnabled2 = LOCK_ENABLED.get();
		if(lockEnabled2 == null || lockEnabled2) {
			lock = new KeyLock(DummyUtils.getCurrentMethodName() + "-" + chaveLock);
			lock.lock2();
		}

		try {
			if(lockEnabled2 == null || lockEnabled2) {
				/*validação de duplo click*/
				//tempo para a transação anterior dar commit
				DummyUtils.sleep(200);
				List<Long> processosIdsList = getByChaveUnicidade(tipoProcesso, null, valoresCampos);
				if(processosIdsList != null && !processosIdsList.isEmpty()) {
					Long processoId = processosIdsList.get(0);
					return get(processoId);
				}
				/*validação de duplo click*/
			}

			AcaoProcesso acao = vo.getAcao();
			AcaoProcesso acao2 = acao != null ? acao : AcaoProcesso.CRIACAO;

			Bolso<ProcessoLog> bolso = new Bolso<>();
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				ProcessoService processoService = applicationContext.getBean(ProcessoService.class);
				ProcessoLog log = processoService.criaProcesso2(vo, acao2);
				bolso.setObjeto(log);

				Processo processo = log.getProcesso();
				processoRepository.deatach(processo);
			});
			tw.runNewThread();
			tw.throwException();
			ProcessoLog log = bolso.getObjeto();
			Processo processo = log.getProcesso();

			Long processoId = processo.getId();
			try {
				TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
				tw2.setRunnable(() -> {

					//colocando no ThreadLocal da nova thread
					LOCK_ENABLED.set(lockEnabled2);

					if(AcaoProcesso.CRIACAO_CLIENTE.equals(acao)) {
						enviarParaAnalise(processo, usuario, StatusProcesso.AGUARDANDO_ANALISE, null, null, true, true);
					}
					else {
						Long tipoProcessoId = tipoProcesso.getId();
						criarPrimeiraSituacao(usuario, log, processo, tipoProcessoId);

						Situacao situacao = processo.getSituacao();
						if(situacao != null) {
							Long situacaoId = situacao.getId();
							List<Campo> campos = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, situacaoId, null, null);
							if(campos != null && !campos.isEmpty()) {
								atualizarProcesso(processo, usuario, campos);
							}
						}

						List<TipoDocumento> byReaproveitavel = tipoDocumentoService.findByReaproveitavel(tipoProcessoId);
						if(!byReaproveitavel.isEmpty() && !DummyUtils.isDev()) {
							reaproveitarDocumentos(processo, usuario, Origem.WEB);
						}
					}
				});
				tw2.runNewThread();
				tw2.throwException();

				return processo;
			}
			catch(Exception e) {
				e.printStackTrace();

				TransactionWrapper tw3 = new TransactionWrapper(applicationContext);
				tw3.setRunnable(() -> processoRepository.deleteById(processoId));
				tw3.runNewThread();

				throw new ProcessoCriadoException(processo, e);
			}
		}
		finally {
			if(lockEnabled2 == null || lockEnabled2) {
				lock.unlock();
			}

			systraceThread("tempo: " + (System.currentTimeMillis() - inicio) + " " + tipoProcesso);
		}
	}

	public void criarPrimeiraSituacao(Usuario usuario, ProcessoLog log, Processo processo, Long tipoProcessoId) throws Exception {

		Situacao situacao0 = situacaoService.getFirstByTipoProcesso(tipoProcessoId);
		concluirSituacao(processo, usuario, situacao0, log, null);
	}

	/** @deprecated não usar, usar apenas criaProcesso() */
	@Deprecated
	@Transactional(rollbackFor = Exception.class)
	public ProcessoLog criaProcesso2(CriacaoProcessoVO vo, AcaoProcesso acao) {

		try {
			TipoProcesso tipoProcesso = vo.getTipoProcesso();
			Usuario usuario = vo.getUsuario();
			Collection<CampoAbstract> valoresCampos = vo.getValoresCampos();
			Origem origem = vo.getOrigem();
			origem = origem != null ? origem : Origem.WEB;
			Aluno aluno = vo.getAluno();

			Processo processo = vo.getProcesso();
			processo = processo != null ? processo : new Processo();
			processo.setTipoProcesso(tipoProcesso);
			processo.setDataCriacao(new Date());
			processo.setStatus(StatusProcesso.RASCUNHO);
			processo.setNivelPrioridade(1);
			processo.setOrigem(origem);
			processo.setAluno(aluno);
			if(!AcaoProcesso.IMPORTACAO.equals(acao)) {
				processo.setAutor(usuario);
			}

			if(usuario != null && usuario.isAnalistaRole() && AcaoProcesso.CRIACAO.equals(acao)) {
				processo.setAnalista(usuario);
			}

			Long processoId = processo.getId();
			validaUnicidade(tipoProcesso, processoId, valoresCampos);

			definirNumCandidatoInstricao(processo, valoresCampos);

			MultiValueMap<TipoCampoGrupo, CampoGrupo> grupos = criaGrupos(processo, valoresCampos);
			criaCampos(processo, grupos);
			preencheCampos(grupos, valoresCampos);
			boolean validarObrigatorio = !AcaoProcesso.IMPORTACAO.equals(acao);

			validaCampos(processo, validarObrigatorio);

			Campanha campanha = campanhaService.getByProcesso(processo);
			processo.setCampanha(campanha);

			processoRepository.saveOrUpdate(processo);

			ProcessoLog log0 = processoLogService.criaLog(processo, usuario, acao);
			log0.setObservacao("Origem: " + origem);

			criaDocumentos(tipoProcesso, processo, usuario);
			criarAjuda(processo);

			Map<TipoDocumento, DigitalizacaoVO> digitalizacao = vo.getDigitalizacao();
			if(digitalizacao != null) {
				digitalizarImagens(processo, digitalizacao, usuario, origem);
			}

			return log0;
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void definirNumCandidatoInstricao(Processo processo, Collection<CampoAbstract> valoresCampos){

		CampoAbstract campoCandidato = getCampoProcesso(valoresCampos, CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome(), CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
		String numCandidato = campoCandidato != null ? campoCandidato.getValor() : null;
		processo.setNumCandidato(numCandidato);

		CampoAbstract campoInscricao = getCampoProcesso(valoresCampos, CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome(), CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
		String numInscricao = campoInscricao != null ? campoInscricao.getValor() : null;
		processo.setNumInscricao(numInscricao);

		processoRepository.saveOrUpdate(processo);
	}

	private void reaproveitarDocumentos(Processo processo, Usuario usuario, Origem origem) {

		ImagemTransaction imagemTransaction = new ImagemTransaction();

		try {
			Aluno aluno = processo.getAluno();
			Long processoId = processo.getId();
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();

			//TODO busca os documentos reaproveitáveis no getdoc_aluno

			AlunoConsultaVO vo = new AlunoConsultaVO(TipoConsultaExterna.REAPROVEITA_DOCUMENTO_GETDOC_ALUNO);
			String cpf = aluno.getCpf();
			cpf = DummyUtils.getCpfCnpjDesformatado(cpf);
			vo.setCpf(cpf);
			List<Long> codOrigens = new ArrayList<>();
			List<TipoDocumento> tipoDocumentosReaproveitaveis = tipoDocumentoService.findByReaproveitavel(tipoProcessoId);
			for (TipoDocumento reaproveitavel: tipoDocumentosReaproveitaveis) {
				Long codOrigem = reaproveitavel.getCodOrigem();
				if(codOrigem != null) {
					codOrigens.add(codOrigem);
				}
			}
			vo.setCodOrigens(codOrigens);

			Map<String, List<String>> consultar = getDocAlunoService.consultarDocumentosReaproveitaveis(vo);
			systraceThread(String.valueOf(consultar));

			if(consultar != null) {
				Map<Long, List<FileVO>> imagens = new HashMap<>();
				Set<String> cods = consultar.keySet();
				for (String cod : cods) {
					List<String> paths = consultar.get(cod);
					List<FileVO> filesVO = new ArrayList<>();
					for (String path : paths) {
						File file = new File(path);
						FileVO fileVO = new FileVO(file);
						filesVO.add(fileVO);
					}
					TipoDocumento tipoDocumento = tipoDocumentoService.getByCodOrigem(tipoProcessoId, Long.valueOf(cod));
					Long tipoDocumentoId = tipoDocumento.getId();
					Documento documento = documentoService.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);
					Long documentoId = documento.getId();
					imagens.put(documentoId, filesVO);
				}

				digitalizarImagens(usuario, processo, imagens, Origem.GETDOC_ALUNO, imagemTransaction);
				imagemTransaction.isDigitalizadasExistem();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String method = DummyUtils.getCurrentMethodName();
			emailSmtpService.enviarEmailException(method, e);
		}
	}

    private void validaUnicidade(TipoProcesso tipoProcesso, Long processoId, Collection<CampoAbstract> campos) {
		List<Long> processosIds = getByChaveUnicidade(tipoProcesso, processoId, campos);
		if(processosIds != null && !processosIds.isEmpty()) {
			StringBuilder ids = new StringBuilder();
			for(Long processosId : processosIds) {
				ids.append(processosId).append(", ");
			}
			String ids2 = ids.substring(0, ids.length() - 2);
			throw new MessageKeyException("jaExisteProcesso.motivo.numero.error", ids2);
		}
	}

	private Map<Long, String> getValoresChaveUnicidade(TipoProcesso tipoProcesso, Collection<CampoAbstract> valoresCampos) {

		Long tipoProcessoId = tipoProcesso.getId();
		Map<Long, TipoCampo> camposUnicidade = tipoCampoService.findDefinemUnicidade(tipoProcessoId);
		if(!camposUnicidade.isEmpty()) {
			Map<Long, String> valoresUnicidade = new LinkedHashMap<>();
			for(CampoAbstract c : valoresCampos) {
				Long tipoCampoId = c.getTipoCampoId();
				if(camposUnicidade.containsKey(tipoCampoId)) {
					String valor = c.getValor();
					valoresUnicidade.put(tipoCampoId, valor);
				}
			}
			return valoresUnicidade;
		}

		return null;
	}

	private List<Long> getByChaveUnicidade(TipoProcesso tipoProcesso, Long processoId, Collection<CampoAbstract> valoresCampos) {
		Long tipoProcessoId = tipoProcesso.getId();
		Map<Long, TipoCampo> definemUnicidade = tipoCampoService.findDefinemUnicidade(tipoProcessoId);
		if(!definemUnicidade.isEmpty()) {
			Map<Long, String> valoresUnicidade = getValoresChaveUnicidade(tipoProcesso, valoresCampos);
			List<Long> processosIds = processoRepository.findIdsByUnicidade(definemUnicidade.keySet(), valoresUnicidade, tipoProcessoId, processoId);
			return processosIds;
		}
		return null;
	}

	private List<CampoAbstract> validaCampos(Processo processo, boolean validarObrigatorio) throws MessageKeyException {
		Long processoId = processo.getId();
		List<Campo> campos = campoService.findByProcesso(processoId);
		List<CampoAbstract> todosCampos = validaCampos(campos, validarObrigatorio);
		return todosCampos;
	}

	private List<CampoAbstract> validaCampos(List<Campo> campos, boolean validarObrigatorio) throws MessageKeyException {

		List<CampoAbstract> todosCampos = new ArrayList<>();
		for(Campo campo : campos) {
			CampoValidator.validate(campo, validarObrigatorio);
			todosCampos.add(campo);
		}
		return todosCampos;
	}

	private void preencheCampos(MultiValueMap<TipoCampoGrupo, CampoGrupo> grupos, Collection<CampoAbstract> valoresCampos) {

		Map<String, Campo> map = new LinkedHashMap<>();
		for(TipoCampoGrupo tipoGrupo : grupos.keySet()) {
			List<CampoGrupo> campoGrupos = grupos.get(tipoGrupo);
			for(CampoGrupo campoGrupo : campoGrupos) {
				Set<Campo> campos = campoGrupo.getCampos();
				for(Campo campo : campos) {
					CampoGrupo grupo = campo.getGrupo();
					String grupoNome = grupo.getNome();
					String campoNome = campo.getNome();
					map.put(grupoNome + " - " + campoNome, campo);
				}
			}
		}

		for(CampoAbstract ca : valoresCampos) {
			if(ca == null) continue;

			if (ca instanceof TipoCampo) {
				TipoCampo tipoCampo = (TipoCampo) ca;
				TipoCampoGrupo grupo = tipoCampo.getGrupo();
				String grupoNome = grupo.getNome();
				String campoNome = tipoCampo.getNome();
				Campo campo = map.get(grupoNome + " - " + campoNome);
				if (campo == null)
					continue;
				String valor = ca.getValor();
				if (campo == null)
					continue;
				campo.setValor(valor);
				campoService.saveOrUpdate(campo);
			}
			else if (ca instanceof Campo) {

				CampoGrupo grupo = (CampoGrupo) ca.getGrupo();
				String grupoNome = grupo.getNome();
				String campoNome = ca.getNome();

				Campo campo = map.get(grupoNome + " - " + campoNome);
				if (campo == null) {
					systraceThread("Campo não encontrado para chave=" + grupoNome + " - " + campoNome);
					continue;
				}

				String valor = ca.getValor();
				campo.setValor(valor);

				campoService.saveOrUpdate(campo);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void digitalizarImagens(Processo processo, Map<TipoDocumento, DigitalizacaoVO> digitalizacao, Usuario usuario, Origem origem) throws Exception {

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
		Map<TipoDocumento, Documento> map = new HashMap<>();
		for(Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			map.put(tipoDocumento, documento);
		}

		Set<TipoDocumento> keySet = digitalizacao.keySet();
		for(TipoDocumento tipoDocumento : keySet) {
			DigitalizacaoVO digitalizacaoVO = digitalizacao.get(tipoDocumento);
			digitalizacaoVO.setProcesso(processo);
			Documento documento = map.get(tipoDocumento);
			digitalizacaoVO.setDocumento(documento);
			digitalizacaoVO.setOrigem(origem);
			digitalizarImagens(usuario, digitalizacaoVO);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(Processo processo, Usuario usuario, List<? extends CampoAbstract> campos) throws Exception {
		return atualizarProcesso(processo, usuario, campos, true);
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(Processo processo, Usuario usuario, List<? extends CampoAbstract> campos, boolean validarCampos) throws Exception {
		return atualizarProcesso(processo, usuario, campos, validarCampos, true);
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(Processo processo, Usuario usuario, List<? extends CampoAbstract> campos, boolean validarCampos, boolean reprocessarRegras) throws Exception {

		Map<String, CampoGrupo> mapGrupos = new HashMap<>();
		Map<Long, Campo> mapCampos = new HashMap<>();
		Set<CampoGrupo> grupos = processo.getGruposCampos();
		for(CampoGrupo grupo : grupos) {
			String grupoNome = grupo.getNome();
			mapGrupos.put(grupoNome, grupo);
			Set<Campo> campos1 = grupo.getCampos();
			for(Campo campo : campos1) {
				Long tipoCampoId = campo.getTipoCampoId();
				mapCampos.put(tipoCampoId, campo);
			}
		}

		Map<Long, String> valores = new LinkedHashMap<>();
		for(CampoAbstract campo : campos) {
			String valor = campo.getValor();
			if(campo instanceof TipoCampo) {
				TipoCampo tipoCampo = (TipoCampo) campo;
				Long tipoCampoId = tipoCampo.getId();
				Campo campo2 = mapCampos.get(tipoCampoId);

				if(campo2 == null) {
					TipoCampoGrupo tipoGrupo = tipoCampo.getGrupo();
					String grupoNome = tipoGrupo.getNome();
					CampoGrupo grupo2 = mapGrupos.get(grupoNome);

					if(grupo2 == null) {
						grupo2 = campoGrupoService.criaGrupo(processo, tipoGrupo);
						campoGrupoService.saveOrUpdate(grupo2);
						mapGrupos.put(grupoNome, grupo2);
						grupos.add(grupo2);
					}

					campo2 = campoService.criaCampo(grupo2, tipoCampo);
					campoService.saveOrUpdate(campo2);
					grupo2.getCampos().add(campo2);
				}

				campo = campo2;
			}
			GrupoAbstract grupo = campo.getGrupo();
			Long grupoId = grupo.getId();
			if(grupoId == null) {
				campoGrupoService.saveOrUpdate((CampoGrupo) grupo);
				String grupoNome = grupo.getNome();
				mapGrupos.put(grupoNome, (CampoGrupo) grupo);
			}
			Long campoId = campo.getId();
			if(campoId == null) {
				campoService.saveOrUpdate((Campo) campo);
			}
			valores.put(campoId, valor);
		}

		atualizarProcesso(processo, usuario, valores, validarCampos, reprocessarRegras);
		return processo;
	}

	@Transactional(rollbackFor = Exception.class)
	public void atualizarProcessoComGruposDinamicos(Processo processo, Set<CampoAbstract> campos, Usuario usuario) throws Exception {

		boolean processoEhFiesOuProUni = processo.isSisFiesOrSisProuni();

		List<GrupoAbstract> grupos = campos.stream().map(CampoAbstract::getGrupo).sorted(Comparator.comparing(GrupoAbstract::getOrdem)).collect(Collectors.toList());

		for (GrupoAbstract grupoAbstract : grupos) {

			if (grupoAbstract.getId() == null) {

				CampoGrupo grupo = (CampoGrupo) grupoAbstract;
				grupo.setProcesso(processo);

				campoGrupoService.saveOrUpdate(grupo);

				Boolean grupoDinamico = grupo.getGrupoDinamico();
				if ((grupoDinamico != null && grupoDinamico) && processoEhFiesOuProUni) {

					ProcessoLog logCriacaoGrupo = criaProcessoLogDeGrupoDinamico(grupo, usuario, AcaoProcesso.CRIOU_NOVO_GRUPO);
					processoLogService.saveOrUpdate(logCriacaoGrupo);

					List<Documento> documentosNovos = criarDocumentosDinamicos(grupo, processo);
					ProcessoLog logCriacaoDocumentos = criarProcessoLogDeDocumentosDinamicos(processo, usuario, documentosNovos, AcaoProcesso.CRIOU_DOCUMENTO);
					processoLogService.saveOrUpdate(logCriacaoDocumentos);
				}
			}
		}

		for (CampoAbstract campo : campos) {

			if (campo.getId() == null || campo.getId() < 0L) {

				campo.setId(null);
				campoService.saveOrUpdate((Campo) campo);
			}
		}

		Map<Long, String> valores = new HashMap<>();
		for (CampoAbstract campo : campos) {
			valores.put(campo.getId(), campo.getValor());
		}

		atualizarProcesso(processo, usuario, valores, false, true);
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(EditarProcessoVO editarProcessoVO) throws Exception {

		Long processoId = editarProcessoVO.getProcessoId();
		Processo processo = processoRepository.get(processoId);

		List<CampoGrupo> grupos = editarProcessoVO.getGrupos();
		for(CampoGrupo grupo : grupos) {
			Long id1 = grupo.getId();
			if(id1 == null) {
				campoGrupoService.saveOrUpdate(grupo);
				Set<Campo> campos = grupo.getCampos();
				processoRepository.deatach(grupo);
			}
		}

		Usuario usuario = editarProcessoVO.getUsuario();
		Map<Long, String> valores = editarProcessoVO.getValores();
		boolean validarCampos = editarProcessoVO.getValidarCampos();
		atualizarProcesso(processo, usuario, valores, validarCampos);

		return processo;
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(Processo processo, Usuario usuario, Map<Long, String> valores, boolean validarCampos) throws Exception {
		return atualizarProcesso(processo, usuario, valores, validarCampos, true);
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo atualizarProcesso(Processo processo, Usuario usuario, Map<Long, String> valores, boolean validarCampos, boolean reprocessarRegras) throws Exception {

		try {
			Map<CampoGrupo, List<Campo>> camposMap = new LinkedHashMap<>();
			Map<Long, CampoAbstract> camposMap2 = new LinkedHashMap<>();
			List<CampoAbstract> camposList = new ArrayList<>();

			List<CampoGrupo> grupos = campoGrupoService.findByProcesso(processo.getId());

			for(CampoGrupo grupo : grupos) {

				List<Campo> campos = campoService.findByGrupoId(grupo.getId());
				camposMap.put(grupo, new ArrayList<>(campos));
				camposList.addAll(campos);
				for(Campo campo : campos) {

					Long campoId = campo.getId();
					if(valores.containsKey(campoId)) {

						String valor = valores.get(campoId);
						campo.setValor(valor);

						if(validarCampos) {
							CampoValidator.validate(campo, true);
						}

						campoService.saveOrUpdate(campo);
						camposMap2.put(campoId, campo);

						if(campo.getNome().equals(CampoMap.CampoEnum.NUM_CANDIDATO.getNome())){
							String numCandidato = campo.getValor();
							processo.setNumCandidato(numCandidato);
						}
						else if(campo.getNome().equals(CampoMap.CampoEnum.NUM_INSCRICAO.getNome())){
							String numInscricao = campo.getValor();
							processo.setNumInscricao(numInscricao);
						} else if(campo.getNome().equals(CampoMap.CampoEnum.SITUACAO_ALUNO.getNome())){
							String situacaoAluno = campo.getValor();
							boolean situacaoAlunoBoolean = situacaoAluno != null && CampoService.CAMPOS_SITUACAO_ALUNO_ELIMINADO_CANCELADO.contains(situacaoAluno);
							processo.setEliminadoOrCancelado(situacaoAlunoBoolean);
						}
					}
				}
			}

			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			Long processoId = processo.getId();
			validaUnicidade(tipoProcesso, processoId, camposList);

			if(valores.size() > 0) {
				criaLogCampos(processo, usuario, valores, camposMap2);
			}

			verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ATUALIZACAO_CAMPOS);

			if(reprocessarRegras) {
				processoRegraService.reprocessaRegrasAoAlterarCampos(processo, usuario, camposMap);
			}

			if(processo.isSisFiesOrSisProuni()) {

				List<Campo> todosCampos = new ArrayList<>();
				camposMap.values().forEach(todosCampos::addAll);
				corrigirObrigatoriedadeDocsMenorDeIdade(processo, todosCampos, usuario);
			}

			return processo;
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			HibernateRepository.verifyConstrantViolation(e);
		}

		return null;
	}

	private void corrigirObrigatoriedadeDocsMenorDeIdade(Processo processo, List<Campo> todosCamposProcesso, Usuario usuario) {

		if (!processo.isSisFiesOrSisProuni()) {
			return;
		}

		Campo campoDataCadastro = DummyUtils.getCampoProcesso(todosCamposProcesso, CampoMap.CampoEnum.DATA_CADASTRO);
		Date dataCadastro = getDataCadastroOuCriacao(processo, campoDataCadastro);

		Long processoId = processo.getId();
		List<CampoGrupo> gruposMembroFamiliar = campoGrupoService.findByProcessoIdAndNome(processoId, CampoMap.GrupoEnum.MEMBRO_FAMILIAR.getNome());

		for (CampoGrupo grupoMembroFamiliar : gruposMembroFamiliar) {
			atualizarObrigatoriedadeDocumentosMembroFamiliar(processo, dataCadastro, grupoMembroFamiliar);
		}
	}

	public Date getDataCadastroOuCriacao(Processo processo, Campo campoDataCadastro) {

		Date dataCadastro = null;

		String dataCadastroStr = campoDataCadastro != null ? campoDataCadastro.getValor() : DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.DATA_CADASTRO);

		if (isNotBlank(dataCadastroStr)) {
			try {
				dataCadastro = DummyUtils.parseDate(dataCadastroStr);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (dataCadastro == null) {
			dataCadastro = processo.getDataCriacao();
		}

		return dataCadastro;
	}

	private void atualizarObrigatoriedadeDocumentosMembroFamiliar(Processo processo, Date dataInicioContrato, CampoGrupo grupoMembroFamiliar) {

		Boolean isMenorDeIdade = membroFamiliarEhMenorDeIdade(processo, dataInicioContrato, grupoMembroFamiliar.getId());

		if (isMenorDeIdade != null) {

			Boolean isMenorDeIdadeAssalariado = false;
			if(isMenorDeIdade) {
				isMenorDeIdadeAssalariado = membroFamiliarEhAssalariado(processo, grupoMembroFamiliar.getId());
			}

			List<Long> tipoDocumentIdList = new ArrayList<>();
			tipoDocumentIdList.addAll(TipoDocumento.DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_PROUNI);
			tipoDocumentIdList.addAll(TipoDocumento.DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_FIES);
			tipoDocumentIdList.addAll(TipoDocumento.DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_TE_FIES);
			tipoDocumentIdList.addAll(TipoDocumento.DOCUMENTOS_RESIDENCIA_RENDIMENTO_MEMBRO_FAMILIAR_TE_PRUNI);

			List<Long> tipoDocumentosRendimentosIdList = Arrays.asList(TipoDocumento.COMPROVANTE_RENDIMENTO_PROUNI, TipoDocumento.COMPROVANTE_RENDIMENTO_TE_PROUNI, TipoDocumento.COMPROVANTE_RENDIMENTO_FIES, TipoDocumento.COMPROVANTE_RENDIMENTO_TE_FIES);

			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setGrupoRelacionado(grupoMembroFamiliar);
			filtro.setProcesso(processo);
			filtro.setTipoDocumentoIdList(tipoDocumentIdList);

			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

			for (Documento documento : documentos) {

				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				Long tipoDocumentoId = tipoDocumento.getId();
				if(isMenorDeIdadeAssalariado && tipoDocumentosRendimentosIdList.contains(tipoDocumentoId)){
					documento.setObrigatorio(true);
				} else {
					documento.setObrigatorio(!isMenorDeIdade);
				}

				StatusDocumento status = documento.getStatus();
				boolean obrigatorio = documento.getObrigatorio();
				if(obrigatorio && status.equals(StatusDocumento.EXCLUIDO)){
					documento.setStatus(StatusDocumento.INCLUIDO);
				}

				documentoService.saveOrUpdate(documento);
			}
		}
	}

	public Boolean membroFamiliarEhAssalariado(Processo processo, Long grupoMembroFamiliarId ) {

		BigDecimal renda = campoService.getRendaGrupoMembroFamiliar(grupoMembroFamiliarId);

		if (renda == null) {
			systraceThread("Renda está vazia para o processo=" + processo);
			return false;
		}

		return renda.compareTo(BigDecimal.ZERO) > 0;
	}

	public Boolean membroFamiliarEhMenorDeIdade(Processo processo, Date dataInicioContrato, Long grupoMembroFamiliarId ) {

		if (dataInicioContrato == null) {
			systraceThread("Data inicio contrato está vazio para o processo=" + processo);
			return null;
		}

		Date dataNascimento = campoService.getDataNascimentoGrupoMembroFamiliar(grupoMembroFamiliarId);

		if (dataNascimento == null) {
			systraceThread("Data nascimento está vazia para o processo=" + processo);
			return null;
		}

		int idade = getIdade(dataNascimento, dataInicioContrato);
		return idade < 18;
	}

	private List<CampoAbstract> criaLogCampos(Processo processo, Usuario usuario, Map<Long, String> valores, Map<Long, CampoAbstract> camposMap) throws Exception {

		//pega de outra sessão pra conseguir recuperar o que está no banco, antes do commit atual
		Bolso<Map<String, Map<String, String>>> bolso = new Bolso<>();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			Long processoId = processo.getId();
			Map<String, Map<String, String>> valoresAtuais = campoService.findValoresMapByProcesso(processoId);
			bolso.setObjeto(valoresAtuais);
		});
		tw.runNewThread();
		tw.throwException();

		Map<String, Map<String, String>> valoresAtuais = bolso.getObjeto();

		Map<String, Map<String, CampoDePara>> valoresAlterados = new LinkedHashMap<>();
		List<CampoAbstract> camposAlterados = new ArrayList<>();
		valoresAtuais.forEach((grupo, campos) -> {
			campos.forEach((campo, valorVelho) -> {
				int index = campo.indexOf("-");
				String campoNome = campo.substring(index + 1);
				String campoIdStr = campo.substring(0, index);
				Long campoId = new Long(campoIdStr);
				String valorNovo = valores.get(campoId);
				if(valorNovo != null && !valorNovo.equals(valorVelho)) {

					CampoAbstract campoObj = camposMap.get(campoId);
					TipoEntradaCampo tipo = campoObj.getTipo();
					if(TipoEntradaCampo.COMBO_BOX_ID.equals(tipo)) {
						BaseInterna baseInterna = campoObj.getBaseInterna();
						Long baseInternaId = baseInterna.getId();
						valorVelho = baseRegistroService.getLabel(baseInternaId, valorVelho);
						valorNovo = baseRegistroService.getLabel(baseInternaId, valorNovo);
					}

					Map<String, CampoDePara> campos2 = valoresAlterados.get(grupo);
					campos2 = campos2 != null ? campos2 : new LinkedHashMap<>();
					valoresAlterados.put(grupo, campos2);
					campos2.put(campoNome, new CampoDePara(valorVelho, valorNovo));
					camposAlterados.add(campoObj);
				}
			});
		});

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ATUALIZACAO_CAMPOS);
		String jsonStr = DummyUtils.objectToJson(valoresAlterados);
		String jsonFormatado = DummyUtils.stringToJson(jsonStr);
		log.setObservacao(jsonFormatado);
		processoLogService.saveOrUpdate(log);

		return camposAlterados;
	}

	private void criaDocumentos(TipoProcesso tipoProcesso, Processo processo, Usuario usuario) {

		Long tipoProcessoId = tipoProcesso.getId();
		List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
		Map<Long, Boolean> obrigatoriedade = tipoDocumentoService.findObrigatoriedadeByProcessoAndTipoProcessoId(processo, tipoProcessoId);

		//TODO COMENTADO PARA CADASTRAR TODOS OS DOCUMENTOS DOS PROCESSOS
		/*Set<Long> documentosOk = new HashSet<>();
		Long processoId = processo.getId();
		List<Documento> documentos = documentoService.findByProcesso(processoId);
		for (Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			Long tipoDocumentoId = tipoDocumento.getId();
			documentosOk.add(tipoDocumentoId);
		}*/

		for(TipoDocumento tipoDocumento : tiposDocumentos) {

			String nome = tipoDocumento.getNome();
			Long tipoDocumentoId = tipoDocumento.getId();
			boolean obrigatorio = obrigatoriedade.get(tipoDocumentoId);
			boolean reconhecimentoFacial = tipoDocumento.getReconhecimentoFacial();
			Integer ordem = tipoDocumento.getOrdem();
			short taxaCompressao = tipoDocumento.getTaxaCompressao();
			ModeloOcr modeloOcr = tipoDocumento.getModeloOcr();

			Documento documento = new Documento();
			documento.setNome(nome);
			documento.setReconhecimentoFacial(reconhecimentoFacial);
			documento.setOrdem(ordem);
			documento.setTaxaCompressao(taxaCompressao);
			documento.setTipoDocumento(tipoDocumento);
			documento.setModeloOcr(modeloOcr);

			documento.setObrigatorio(obrigatorio);

			StatusDocumento status = obrigatorio ? StatusDocumento.INCLUIDO : StatusDocumento.EXCLUIDO;
			AcaoDocumento acao = obrigatorio ? AcaoDocumento.CRIACAO_INCLUIDO : null;

			documento.setStatus(status);
			documento.setProcesso(processo);

			documentoService.saveOrUpdate(documento);

			if(acao != null) {
				documentoLogService.criaLog(documento, usuario, acao);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public Documento criaDocumento(Processo processo, TipoDocumento tipoDocumento, Usuario usuario) {

		String nome = tipoDocumento.getNome();
		Long tipoDocumentoId = tipoDocumento.getId();
		boolean obrigatorio = tipoDocumento.getObrigatorio();
		boolean reconhecimentoFacial = tipoDocumento.getReconhecimentoFacial();
		Integer ordem = tipoDocumento.getOrdem();
		short taxaCompressao = tipoDocumento.getTaxaCompressao();
		ModeloOcr modeloOcr = tipoDocumento.getModeloOcr();

		Documento documento = new Documento();
		documento.setNome(nome);
		documento.setReconhecimentoFacial(reconhecimentoFacial);
		documento.setOrdem(ordem);
		documento.setTaxaCompressao(taxaCompressao);
		documento.setTipoDocumento(tipoDocumento);
		documento.setModeloOcr(modeloOcr);

//		if(Arrays.asList(TipoDocumento.DOCUMENTO_DE_MANUTENCAO_DA_BOLSA, TipoDocumento.DOCUMENTO_DE_ADITAMENTO).contains(tipoDocumentoId)) {
//			defineObrigatoriedadeDocumentosFiesProuni(processo, documento);
//		}
//		else {
		documento.setObrigatorio(obrigatorio);
//		}

		StatusDocumento status = obrigatorio ? StatusDocumento.INCLUIDO : StatusDocumento.EXCLUIDO;
		AcaoDocumento acao = obrigatorio ? AcaoDocumento.CRIACAO_INCLUIDO : null;

		documento.setStatus(status);
		documento.setProcesso(processo);

		documentoService.saveOrUpdate(documento);

		if(acao != null) {
			documentoLogService.criaLog(documento, usuario, acao);
		}

		return documento;
	}

	private MultiValueMap<TipoCampoGrupo, CampoGrupo> criaGrupos(Processo processo, Collection<CampoAbstract> valoresCampos) {

		Map<Long, Map<String, GrupoAbstract>> gruposCadastroMap = new LinkedHashMap<>();
		for(CampoAbstract campo : valoresCampos) {
			GrupoAbstract grupo = campo.getGrupo();
			Long tipoCampoGrupoId = grupo.getTipoCampoGrupoId();

			Map<String, GrupoAbstract> map2 = gruposCadastroMap.get(tipoCampoGrupoId);
			map2 = map2 != null ? map2 : new LinkedHashMap<>();
			gruposCadastroMap.put(tipoCampoGrupoId, map2);

			map2.put(grupo.getNome(), grupo);
		}

		MultiValueMap<TipoCampoGrupo, CampoGrupo> map = new LinkedMultiValueMap<>();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		List<TipoCampoGrupo> grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
		for(TipoCampoGrupo tipoGrupo : grupos) {

			Boolean criacaoProcesso = tipoGrupo.getCriacaoProcesso();
			if(criacaoProcesso == null || criacaoProcesso) {

				CampoGrupo grupo = campoGrupoService.criaGrupo(processo, tipoGrupo);
				campoGrupoService.saveOrUpdate(grupo);
				map.add(tipoGrupo, grupo);
				processo.getGruposCampos().add(grupo);

				Map<String, GrupoAbstract> map2 = gruposCadastroMap.get(tipoGrupo.getTipoCampoGrupoId());
				if(map2 != null) {
					Collection<GrupoAbstract> values = map2.values();
					for(GrupoAbstract grupoAbstract : values) {

						if(!grupoAbstract.getNome().equals(grupo.getNome())) {
							CampoGrupo grupo2 = campoGrupoService.criaGrupo(processo, tipoGrupo);
							grupo2.setNome(grupoAbstract.getNome());
							campoGrupoService.saveOrUpdate(grupo2);
							map.add(tipoGrupo, grupo2);
							processo.getGruposCampos().add(grupo2);
						}
					}
				}
			}
		}

		return map;
	}

	private void criaCampos(Processo processo, MultiValueMap<TipoCampoGrupo, CampoGrupo> gruposMap) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);

		for(TipoCampoGrupo tipoGrupo : map.keySet()) {

			List<CampoGrupo> grupos = gruposMap.get(tipoGrupo);
			if(grupos == null || grupos.isEmpty())
				continue;

			for(CampoGrupo grupo : grupos) {
				Set<Campo> campos = grupo.getCampos();
				List<TipoCampo> tiposCampos = map.get(tipoGrupo);
				for(TipoCampo tipoCampo : tiposCampos) {
					Campo campo = campoService.criaCampo(grupo, tipoCampo);
					campoService.saveOrUpdate(campo);
					campos.add(campo);
				}
			}
		}
	}

	public List<ProcessoVO> findVOsByPendencia(ProcessoFiltro filtro, Integer inicio, Integer max) {

		List<ProcessoVO> vos = findVOsByFiltro(filtro, inicio, max);

		filtro.setStatusList(Arrays.asList(StatusProcesso.CONCLUIDO, StatusProcesso.CANCELADO));
		filtro.setDataInicio(DateUtils.addDays(new Date(), -2));
		filtro.setDataInicio(new Date());
		List<ProcessoVO> vos2 = findVOsByFiltro(filtro, inicio, max);
		vos.addAll(vos2);

		return vos;
	}

	public List<ProcessoVO> findVOsByFiltro(ProcessoFiltro filtro, Integer inicio, Integer max) {

		List<Processo> processos = processoRepository.findByFiltro(filtro, inicio, max);

		List<Long> processosIds = new ArrayList<>();
		Area areaPendencia = filtro.getAreaPendencia();
		Usuario analista = filtro.getAnalista();

		List<ProcessoVO> list = buildVOs(processos, processosIds, areaPendencia);

		Map<Long, String> areasPendentesMap = new HashMap<>();
		Map<Long, Set<AlertaSolicitacaoVO>> statusPendentesMap = new HashMap<>();
		Map<Long, Boolean> emailNaoLidoMap = new HashMap<>();
		Set<Long> evidenciasNaoLidas = new HashSet<>();
		Set<Long> reenviosNaoLidos = new HashSet<>();

		areasPendentesMap = solicitacaoService.getAreasPendentesStr(processosIds, areaPendencia);
		evidenciasNaoLidas = processoLogService.getEvidenciasNaoLidas(processosIds);
		reenviosNaoLidos = processoLogService.getReenviosNaoLidas(processosIds);

		Map<Long, Set<StatusSolicitacao>> statusPendentes = solicitacaoService.getStatusPendentes(processosIds, null);
		Map<Long, Set<StatusSolicitacao>> statusPendentes2 = new HashMap<>();

		Area area = analista != null ? analista.getArea() : null;
		if(area != null) {
			Long areaId = area.getId();
			statusPendentes2 = solicitacaoService.getStatusPendentes(processosIds, areaId);
		}

		for(Long processoId : statusPendentes.keySet()) {

			Set<AlertaSolicitacaoVO> setStatuPendente = statusPendentesMap.get(processoId);
			setStatuPendente = setStatuPendente != null ? setStatuPendente : new HashSet<AlertaSolicitacaoVO>();
			statusPendentesMap.put(processoId, setStatuPendente);

			Set<StatusSolicitacao> set = statusPendentes.get(processoId);
			Set<StatusSolicitacao> set2 = statusPendentes2.get(processoId);
			for(StatusSolicitacao statusSolicitacao : set) {

				AlertaSolicitacaoVO vo = new AlertaSolicitacaoVO();
				vo.setStatusSolicitacao(statusSolicitacao);
				boolean animarAlerta = set2 != null && set2.contains(statusSolicitacao);
				vo.setAnimarAlerta(animarAlerta);
				setStatuPendente.add(vo);
			}
		}

		emailNaoLidoMap = emailRecebidoService.getNaoLidos(processosIds);

		List<VerificacaoBloqueioVO> verificacoesBloqueios = bloqueioProcessoService.findVerificacoesBloqueios(processosIds, false);
		Map<Long, VerificacaoBloqueioVO> verificacoesBloqueiosMap = new HashMap<>();
		for(VerificacaoBloqueioVO verificacaoBloqueioVO : verificacoesBloqueios) {
			Long processoId = verificacaoBloqueioVO.getProcessoId();
			verificacoesBloqueiosMap.put(processoId, verificacaoBloqueioVO);
		}

		for(ProcessoVO vo : list) {

			Processo processo = vo.getProcesso();
			Long processoId = processo.getId();

			String areasPendentes = areasPendentesMap.get(processoId);
			vo.setAreasPendentesStr(areasPendentes);

			Set<AlertaSolicitacaoVO> set = statusPendentesMap.get(processoId);
			if(set != null) {
				vo.setSolicitacoesStatus(new ArrayList<>(set));
			}

			Boolean possuiEmailNaoLido = emailNaoLidoMap.get(processoId);
			vo.setPossuiEmailNaoLido(possuiEmailNaoLido);

			boolean evidenciaNaoLida = evidenciasNaoLidas.contains(processoId);
			vo.setEvidenciaNaoLida(evidenciaNaoLida);

			boolean reenvioNaoLido = reenviosNaoLidos.contains(processoId);
			vo.setReenvioNaoLido(reenvioNaoLido);

			VerificacaoBloqueioVO verificacaoBloqueioVO = verificacoesBloqueiosMap.get(processoId);
			if(verificacaoBloqueioVO != null) {

				String usuarioNome = verificacaoBloqueioVO.getUsuarioNome();
				String mensagem = messageService.getValue("processoBloqueado.error", usuarioNome);
				vo.setMensagemBloqueio(mensagem);
			}

			StatusProcesso processoStatus = processo.getStatus();
			vo.setStatusProcesso(processoStatus);

			Boolean pendenciaByProcesso = existPendenciaByProcesso(processo);
			vo.setPassouPorPendencia(pendenciaByProcesso);

			Date dataFinalizacao = processo.getDataFinalizacao();
			vo.setDataFinalizacao(dataFinalizacao);
		}

		return list;
	}

	public HorasUteisCalculator buildHUC() {
		return buildHUC(null);
	}

	public HorasUteisCalculator buildHUC(Long situacaoId) {

		List<Date> feriados = feriadoService.findAllDatas();
		String[] expedienteArray = parametroService.getExpediente();
		Expediente expediente = new Expediente(expedienteArray);
		HorasUteisCalculator huc = new HorasUteisCalculator(expediente, null);
		huc.setFeriados(feriados);
		if (situacaoId != null) {
			List<Date> bySituacao = feriadoService.findBySituacao(situacaoId);
			huc.addParalizacao(bySituacao);
		}

		return huc;
	}

	public Map<Long, HorasUteisCalculator> buildHUCBySituacoes(List<Situacao> situacaos){
		Map<Long, HorasUteisCalculator> allSituacoes = new LinkedHashMap<>();
		for(Situacao situacao : situacaos){
			Long id = situacao.getId();
			HorasUteisCalculator huc = buildHUC(id);
			allSituacoes.put(id, huc);
		}
		return allSituacoes;
	}

	private List<ProcessoVO> buildVOs(List<Processo> processos, List<Long> processosIds, Area areaPendencia) {

		Date agora = new Date();

		List<ProcessoVO> list = new ArrayList<>(processos.size());
		for(Processo processo : processos) {

			Long processoId = processo.getId();
			Situacao situacao = processo.getSituacao();
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = buildHUC(situacaoId);

			if(processosIds != null) {
				processosIds.add(processoId);
			}

			ProcessoVO vo = new ProcessoVO(processo);

			String horasRestantes = getHorasRestantes(huc, processo);
			vo.setHorasRestantes(horasRestantes);

			String horasRestantesEtapa = getHorasRestantesEtapa(processo);
			vo.setHorasRestantesEtapa(horasRestantesEtapa);

			String horasRestantesSituacao = getHorasRestantesSituacao(huc, processo);
			vo.setHorasRestantesSituacao(horasRestantesSituacao);

			StatusPrazo statusPrazo = getStatusPrazo(processo, areaPendencia, huc);
			vo.setStatusPrazo(statusPrazo);

			if(areaPendencia != null) {

				Long areaId = areaPendencia.getId();
				Date prazoArea = solicitacaoService.getProximoPrazo(processoId, areaId);

				if(prazoArea != null) {
					BigDecimal horas = huc.getHoras(agora, prazoArea);
					BigDecimal horasExpediente = huc.getHorasExpediente();
					String horasRestantes2 = DummyUtils.getHoras(horasExpediente, horas, true, false);
					vo.setHorasRestantesArea(horasRestantes2);
				}
			}

			list.add(vo);
		}

		return list;
	}

	public StatusPrazo getStatusPrazoSituacao(Processo processo) {
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		Date prazoLimiteSituacao = processo.getPrazoLimiteSituacao();
		return getStatusPrazo(processo, huc, prazoLimiteSituacao, false);
	}

	public StatusPrazo getStatusPrazoAnalise(Processo processo) {
		Date prazoLimiteAnalise = processo.getPrazoLimiteAnalise();
		if(prazoLimiteAnalise == null) {
			return null;
		}
		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		dataFinalizacaoAnalise = dataFinalizacaoAnalise != null ? dataFinalizacaoAnalise : new Date();

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);

		BigDecimal horasPrazoAnalise = huc.getHoras(dataFinalizacaoAnalise, prazoLimiteAnalise);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		BigDecimal horasPrazoAdvertir = tipoProcesso.getHorasPrazoAdvertir();
		return StatusPrazo.getByHorasRestantes(horasPrazoAnalise, horasPrazoAdvertir);
	}

	private StatusPrazo getStatusPrazo(Processo processo, Area area, HorasUteisCalculator huc) {

		if(area != null) {
			Long areaId = area.getId();
			Long processoId = processo.getId();
			Date prazoArea = solicitacaoService.getProximoPrazo(processoId, areaId);
			return getStatusPrazo(processo, huc, prazoArea, false);
		}

		return getStatusPrazo(processo);
	}

	public StatusPrazo getStatusPrazo(Processo processo) {

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);

		StatusPrazo statusPrazo1 = null;

		Long processoId = processo.getId();
		ProcessoLog lastLogByProcesso = processoLogService.findLastLogByProcesso(processoId, situacaoId);
		if(lastLogByProcesso != null) {
			Date prazoLimiteEtapa = lastLogByProcesso.getPrazoLimiteEtapa();
			if (prazoLimiteEtapa != null) {
				statusPrazo1 = getStatusPrazo(processo, huc, prazoLimiteEtapa, true);
				if (StatusPrazo.ALERTAR.equals(statusPrazo1)) {
					return StatusPrazo.ALERTAR;
				}
			}
		}

		Date prazoLimiteSituacao = processo.getPrazoLimiteSituacao();
		StatusPrazo statusPrazo2 = getStatusPrazo(processo, huc, prazoLimiteSituacao, false);
		if(StatusPrazo.ALERTAR.equals(statusPrazo2)) {
			return StatusPrazo.ALERTAR;
		}

		if(StatusPrazo.ADVERTIR.equals(statusPrazo1) || StatusPrazo.ADVERTIR.equals(statusPrazo2)) {
			return StatusPrazo.ADVERTIR;
		}

		return StatusPrazo.NORMAL;
	}

	private StatusPrazo getStatusPrazo(Processo processo, HorasUteisCalculator huc, Date proximoPrazo, Boolean isEtapa) {

		StatusPrazo statusPrazo;
		if(proximoPrazo != null) {

			Situacao situacao = processo.getSituacao();
			Date agora = new Date();
			BigDecimal horas = huc.getHoras(agora, proximoPrazo);

			if(isEtapa) {
				Etapa etapa = situacao.getEtapa();
				BigDecimal horasPrazoAdvertirEtapa = etapa.getHorasPrazoAdvertir();
				StatusPrazo statusPrazoEtapa = StatusPrazo.getByHorasRestantes(horas, horasPrazoAdvertirEtapa);
				if(StatusPrazo.ALERTAR.equals(statusPrazoEtapa)) {
					return StatusPrazo.ALERTAR;
				}
				if(StatusPrazo.ADVERTIR.equals(statusPrazoEtapa)) {
					return StatusPrazo.ADVERTIR;
				}
			} else {
				BigDecimal horasPrazoAdvertirSituacao = situacao.getHorasPrazoAdvertir();

				StatusPrazo statusPrazoSituacao = StatusPrazo.getByHorasRestantes(horas, horasPrazoAdvertirSituacao);

				if(StatusPrazo.ALERTAR.equals(statusPrazoSituacao)) {
					return StatusPrazo.ALERTAR;
				}

				if(StatusPrazo.ADVERTIR.equals(statusPrazoSituacao)) {
					return StatusPrazo.ADVERTIR;
				}
			}
		}

		statusPrazo = StatusPrazo.NORMAL;

		return statusPrazo;
	}

	public String getHorasRestantes(Processo processo) {
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		return getHorasRestantes(huc, processo);
	}

	public String getHorasRestantes(HorasUteisCalculator huc, Processo processo) {

		Date agora = new Date();
		Date prazo = processo.getProximoPrazo();

		if(prazo == null) {
			return "";
		}

		BigDecimal horas = huc.getHoras(agora, prazo);
		BigDecimal horasExpediente = huc.getHorasExpediente();
		String horasRestantes = DummyUtils.getHoras(horasExpediente, horas, true, false);
		return horasRestantes;
	}

	public String getHorasRestantesEtapa(Processo processo) {
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		Long processoId = processo.getId();
		ProcessoLog lastLogByProcesso = processoLogService.findLastLogByProcesso(processoId, situacaoId);
		if(lastLogByProcesso != null) {
			Date prazoLimiteEtapa = lastLogByProcesso.getPrazoLimiteEtapa();
			if (prazoLimiteEtapa != null) {
				HorasUteisCalculator huc = buildHUC(situacaoId);
				String horasRestantesEtapa = getHorasRestantesEtapa(huc, prazoLimiteEtapa);

				return horasRestantesEtapa;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getHorasRestantesEtapa(HorasUteisCalculator huc, Date prazoLimiteEtapa) {

		Date agora = new Date();
		BigDecimal horas = huc.getHoras(agora, prazoLimiteEtapa);

		BigDecimal horasExpediente = huc.getHorasExpediente();
		String horasRestantes = DummyUtils.getHoras(horasExpediente, horas, true, false);
		return horasRestantes;
	}

	public String getHorasRestantesSituacao(Processo processo) {
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		return getHorasRestantesSituacao(huc, processo);
	}

	public String getHorasRestantesSituacao(HorasUteisCalculator huc, Processo processo) {

		Date agora = new Date();
		Date prazo = processo.getPrazoLimiteSituacao();

		if(prazo == null) {
			return "";
		}

		BigDecimal horas = huc.getHoras(agora, prazo);
		BigDecimal horasExpediente = huc.getHorasExpediente();
		String horasRestantes = DummyUtils.getHoras(horasExpediente, horas, true, false);

		return horasRestantes;
	}

	public List<Processo> findByFiltro(ProcessoFiltro filtro, Integer inicio, Integer max) {
		return processoRepository.findByFiltro(filtro, inicio, max);
	}

	public int countByFiltro(ProcessoFiltro filtro) {
		return processoRepository.countByFiltro(filtro);
	}

	public List<Long> findIdsByFiltro(ProcessoFiltro filtro, Integer inicio, Integer max) {
		return processoRepository.findIdsByFiltro(filtro, inicio, max);
	}

	@Transactional(rollbackFor = Exception.class)
	public CriaProcessoResultVO criaProcesso(final Usuario usuario, final TipoProcesso tipoProcesso, final List<FileVO> arquivos, Origem origem) throws Exception {

		final Processo processo = new Processo();

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			processo.setTipoProcesso(tipoProcesso);
			processo.setDataCriacao(new Date());
			processo.setStatus(StatusProcesso.RASCUNHO);
			processo.setNivelPrioridade(1);
			processo.setAutor(usuario);
			processoRepository.saveOrUpdate(processo);

			criaDocumentos(tipoProcesso, processo, usuario);

			DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
			digitalizacaoVO.setProcesso(processo);
			digitalizacaoVO.setArquivos(arquivos);
			digitalizacaoVO.setOrigem(origem);

			digitalizarImagens(usuario, digitalizacaoVO);

			CriacaoProcessoVO vo = new CriacaoProcessoVO();
			vo.setTipoProcesso(tipoProcesso);
			vo.setUsuario(usuario);
			vo.setProcesso(processo);

			Long tipoProcessoId = tipoProcesso.getId();
			Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);
			Set<CampoAbstract> valoresCampos = new LinkedHashSet<>();
			for(TipoCampoGrupo grupo : map.keySet()) {
				List<TipoCampo> list = map.get(grupo);
				valoresCampos.addAll(list);
			}
			vo.setValoresCampos(valoresCampos);

			criaProcesso(vo);
		});
		tw.runNewThread();
		tw.throwException();

		CriaProcessoResultVO vo = new CriaProcessoResultVO();
		vo.setProcesso(processo);

		try {
			TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
			tw2.setRunnable(() -> {
				enviarParaAnalise(processo, usuario);
			});
			tw2.runNewThread();
			tw2.throwException();
		}
		catch(Exception e) {
			vo.setException(e);
		}

		return vo;
	}

	@Transactional(rollbackFor = Exception.class)
	public void criaProcessoIsencao(Usuario usuario, Processo processoPai, Situacao novaSituacao, TipoProcesso tipoProcesso, Origem origem, Boolean opcoesIes) throws Exception {

		final Processo processo = new Processo();

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			Long processoId = processoPai.getId();
			Aluno aluno = processoPai.getAluno();
			processo.setAluno(aluno);
			processo.setTipoProcesso(tipoProcesso);
			processo.setProcessoOriginal(processoPai);
			processo.setOrigem(origem);
			processo.setTipoProcesso(tipoProcesso);
			processo.setDataCriacao(new Date());
			processo.setDataEnvioAnalise(new Date());
			processo.setStatus(StatusProcesso.RASCUNHO);
			processo.setNivelPrioridade(1);
			processo.setAutor(usuario);

			processoRepository.saveOrUpdate(processo);

			Campanha campanha = campanhaService.getByProcesso(processo);
			processo.setCampanha(campanha);

			criaDocumentos(tipoProcesso, processo, usuario);

			copiarDocumentosIsencaoDisciplinas(usuario, processoPai, processo);

			List<CampoAbstract> valoresCampos = new ArrayList<CampoAbstract>();
			List<CampoGrupo> camposGrupos = campoGrupoService.findByProcesso(processoId);

			for (CampoGrupo campoGrupo : camposGrupos) {
				Set<Campo> campos = campoGrupo.getCampos();
				Long tipoCampoGrupoId = campoGrupo.getTipoCampoGrupoId();

				for (Campo campo : campos) {
					Long tipoCampoId = campo.getTipoCampoId();
					TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);
					if(tipoCampo == null) continue;
					String opcaoId = campo.getOpcaoId();
					String valor = campo.getValor();
					tipoCampo.setValor(valor);
					tipoCampo.setOpcaoId(opcaoId);
					valoresCampos.add(tipoCampo);
				}
			}

			Long tipoProcessoId = tipoProcesso.getId();
			String nomeGrupoComposicaoMateriaIsenta = CampoMap.GrupoEnum.COMPOSICAO_MATERIAS_ISENTAS.getNome();
			TipoCampoGrupo tipoGrupo = tipoCampoGrupoService.getByTipoProcessoAndGrupoNome(tipoProcessoId, nomeGrupoComposicaoMateriaIsenta);

			CampoGrupo grupoComposicaoMateriaIsenta = campoGrupoService.criaGrupo(processo, tipoGrupo);
			campoGrupoService.saveOrUpdate(grupoComposicaoMateriaIsenta);

			String sim = Resposta.SIM.getNome();
			String nao = Resposta.NAO.getNome();
			String nomeGrupo = CampoMap.GrupoEnum.DADOS_PROCESSO_ISENCAO.getNome();

			TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.getByTipoProcessoAndGrupoNome(TipoProcesso.ISENCAO_DISCIPLINAS, nomeGrupo);
			Set<TipoCampo> tipoCampoList = tipoCampoGrupo.getCampos();

			if(opcoesIes != null){
				for (TipoCampo tipoCampoIesDeGrupo : tipoCampoList) {
					String nomeCampo = tipoCampoIesDeGrupo.getNome();
					String nomeIes = CampoMap.CampoEnum.IES_DE_GRUPO.getNome();
					if (nomeIes.equals(nomeCampo)) {
						if (opcoesIes) {
							tipoCampoIesDeGrupo.setValor(sim);
						} else {
							tipoCampoIesDeGrupo.setValor(nao);
						}
						valoresCampos.add(tipoCampoIesDeGrupo);
					}
				}
			}

			atualizarProcesso(processo, usuario, valoresCampos);

			concluirSituacao(processo, usuario, novaSituacao, null, null);
		});
		tw.runNewThread();
		Exception exception = tw.getException();
		if (exception != null) {
			TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
			tw2.setRunnable(() -> {
				Long processoId = processo.getId();
				processoRepository.deleteById(processoId);
			});
			tw2.runNewThread();
			throw exception;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void copiarDocumentosIsencaoDisciplinas(Usuario usuario, Processo processoFilho, Processo processo) throws Exception {
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentosProcessoIsencao = documentoService.findByFiltro(filtro, null, null);

		List<Long> codOrigensReaproveitar = new ArrayList<>();
		HashMap<Long, Documento> mapCodOrigemDocumento = new HashMap<>();
		boolean isMedicina = TipoProcesso.MEDICINA_IDS.contains(processoFilho.getTipoProcesso().getId());

		for (Documento doc : documentosProcessoIsencao) {
			TipoDocumento tipoDocumento = doc.getTipoDocumento();
			Long codOrigem = tipoDocumento.getCodOrigem();
			codOrigensReaproveitar.add(codOrigem);
			mapCodOrigemDocumento.put(codOrigem, doc);
		}

		filtro = new DocumentoFiltro();
		filtro.setProcesso(processoFilho);
		filtro.setCodsOrigem(codOrigensReaproveitar);

		List<Documento> documentosReaproveitar = documentoService.findByFiltro(filtro, null, null);

		for (Documento documentoParaReaproveitar : documentosReaproveitar) {

			Long documentoReaproveitarId = documentoParaReaproveitar.getId();
			TipoDocumento tipoDocumento = documentoParaReaproveitar.getTipoDocumento();
			Long codOrigem = tipoDocumento.getCodOrigem();
			Documento documentoIsencao = mapCodOrigemDocumento.get(codOrigem);
			StatusDocumento status = documentoParaReaproveitar.getStatus();
			Integer versaoAtual = documentoParaReaproveitar.getVersaoAtual();
			Integer versaoAtualDocIsencao = documentoIsencao.getVersaoAtual();

			if(isMedicina) {
				if(StatusDocumento.DIGITALIZADO.equals(status)) {
						throw new RuntimeException("Os documentos de Isenção devem estar aprovados");
				}
			}

			int i = !versaoAtual.equals(versaoAtualDocIsencao) ? versaoAtualDocIsencao + 1 : 1;
			for (; i <= versaoAtual; i++) {

				List<Imagem> imagens = imagemService.findByDocumentoVersao(documentoReaproveitarId, i);
				documentoService.copiarImagensIsencaoDisciplina(imagens, documentoIsencao, usuario, true, i);
				documentoIsencao.setVersaoAtual(i);
			}

			documentoIsencao.setStatus(status);
			documentoService.saveOrUpdate(documentoIsencao);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void digitalizarImagens(Usuario usuario, DigitalizacaoVO digitalizacaoVO) throws Exception {

		ImagemTransaction imagemTransaction = new ImagemTransaction();

		Processo processo = digitalizacaoVO.getProcesso();
		Long processoId = processo.getId();
		processo = get(processoId);
		Origem origem = digitalizacaoVO.getOrigem();
		boolean podeUsarDocumentoOutros = digitalizacaoVO.isPodeUsarDocumentoOutros();

		boolean permiteTipificacao = false;
		Situacao situacao = processo.getSituacao();
		if (situacao != null) {

			Long situacaoId = situacao.getId();
			situacao = situacaoService.get(situacaoId);
			TipoProcesso tipoProcesso = processo.getTipoProcesso() != null ? processo.getTipoProcesso() : null;
			Long tipoProcessoId = tipoProcesso.getId();
			Situacao situacaoAluno = situacaoService.getByFinalNome(tipoProcessoId, Situacao.ALUNO);
			if(situacao.equals(situacaoAluno)) {
				throw new MessageKeyException("processoSituacaoAluno.error");
			}
			permiteTipificacao = situacao.isPermiteTipificacao();
		}

		Documento documento = digitalizacaoVO.getDocumento();
		List<FileVO> arquivos = digitalizacaoVO.getArquivos();
		Map<Long, List<FileVO>> arquivosMap = digitalizacaoVO.getArquivosMap();

		if(arquivos != null && !arquivos.isEmpty()) {
			Long documentoId = documento != null ? documento.getId() : null;
			arquivosMap = new HashMap<>();
			arquivosMap.put(documentoId, arquivos);
		}

		Collection<List<FileVO>> values = arquivosMap.values();
		List<FileVO> list = new ArrayList<>();
		for(List<FileVO> list2 : values) {
			list.addAll(list2);
		}
		if(list.isEmpty()) {
			throw new MessageKeyException("digitalizacao.imagensVazias.error");
		}

		//pode ser o "TIPIFICANDO..." ou o "OUTROS"
		Long docCoringaId = null;

		Set<Long> documentosIds = arquivosMap.keySet();
		List<Long> documentosIdsList = new ArrayList<>(documentosIds);
		for(Long documentoId : documentosIdsList) {

			List<FileVO> list2 = arquivosMap.get(documentoId);
			if(documentoId == null) {

				if(docCoringaId == null) {
					Documento tipificando = permiteTipificacao ? documentoService.makeTipificando(processo) : null;
					if(tipificando != null) {
						docCoringaId = tipificando.getId();
					}
					else {
						Documento outros = permiteTipificacao || podeUsarDocumentoOutros ? documentoService.makeOutros(processo) : null;
						if(outros != null) {
							docCoringaId = outros.getId();
						}
					}
				}

				arquivosMap.remove(documentoId);

				if(docCoringaId != null) {
					List<FileVO> list3 = arquivosMap.get(docCoringaId);
					list3 = list3 != null ? list3 : new ArrayList<>();
					arquivosMap.put(docCoringaId, list3);
					list3.addAll(list2);
				}
			}
		}

		digitalizarImagens(usuario, processo, arquivosMap, origem, imagemTransaction);
		imagemTransaction.isDigitalizadasExistem();

		TipoDocumento tipoDocumento = documento != null ? documento.getTipoDocumento() : null;
		tipoDocumento = tipoDocumento != null ? tipoDocumentoService.get(tipoDocumento.getId()) : null;

		boolean isAceiteContrato = tipoDocumento != null ? tipoDocumento.getAceiteContrato() : false;

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.CONCLUIDO.equals(status)
				&& digitalizacaoVO.isReenvioAnalise()
				&& !processo.isMestradoDoutoradoMedicina()
				&& !isAceiteContrato) {
			enviarParaAnalise(processo, usuario);
		}

		imagemTransaction.isDigitalizadasExistem();
	}

	@Transactional(rollbackFor = Exception.class)
	public void digitalizarImagens(Usuario usuario, Processo processo, Map<Long, List<FileVO>> imagens, Origem origem, ImagemTransaction transaction) throws Exception {

		transaction = transaction == null ? new ImagemTransaction() : transaction;

		try {
			for(Long documentoId : imagens.keySet()) {

				if(documentoId == null) {
					continue;
				}

				Documento documento = documentoService.get(documentoId);
				List<FileVO> list = imagens.get(documentoId);

				String documentoNome = documento.getNome();
				boolean tipificandoOuOutros = Documento.NOME_OUTROS.equals(documentoNome) || Documento.NOME_TIFICANDO.equals(documentoNome);
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				if(!tipificandoOuOutros && tipoDocumento != null) {
					Long tipoDocumentoId = tipoDocumento.getId();
					tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
					Integer maximoPaginas = tipoDocumento.getMaximoPaginas();
					if(maximoPaginas != null && list.size() > maximoPaginas) {
						throw new MessageKeyException("digitalizacao.maximo.paginas.error", tipoDocumento.getNome(), list.size(), maximoPaginas);
					}
				}

				if(!list.isEmpty()) {
					documentoService.digitalizarImagens(transaction, usuario, documento, list, Origem.GETDOC_ALUNO.equals(origem) ? AcaoDocumento.REAPROVEITADO : AcaoDocumento.DIGITALIZOU, origem);
				}
			}

			agendarFacialRecognition(processo);

			transaction.commit();
		}
		catch(RuntimeException e) {
			transaction.rollback();
			throw e;
		}
		catch(Exception e) {
			transaction.rollback();
			throw new RuntimeException(e);
		}

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.DIGITALIZOU_DOCUMENTOS);

	}

	@Transactional(rollbackFor = Exception.class)
	public void agendarFacialRecognition(Processo processo) {

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentosList = documentoService.findByFiltro(filtro, null, null);
		Documento documentoBase = null;
		for(Documento documento2 : documentosList) {
			boolean reconhecimentoFacial = documento2.getReconhecimentoFacial();
			if(reconhecimentoFacial) {
				StatusDocumento status = documento2.getStatus();
				if(Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.EXCLUIDO).contains(status)) {
					continue;
				}
				documentoBase = documento2;
				break;
			}
		}

		if(documentoBase != null) {
			String documentoBaseNome = documentoBase.getNome();
			if(!Documento.NOME_OUTROS.equals(documentoBaseNome) && !Documento.NOME_TIFICANDO.equals(documentoBaseNome)) {
				documentoBase.setAguardandoReconhecimentoFacial(true);
				documentoService.saveOrUpdate(documentoBase);
			}
		}
	}

	private String criarNovoNomeGrupoDinamico(TipoCampoGrupo tipoCampoGrupoSelecionado, Processo processo) {

		String novoNomeGrupo;

		String nomeGrupoSelecionado = tipoCampoGrupoSelecionado.getNome();
		Long tipoCampoGrupoId = tipoCampoGrupoSelecionado.getTipoCampoGrupoId();

		Long processoId = processo.getId();
		List<CampoGrupo> grupos = campoGrupoService.findByProcesso(processoId);

		List<CampoGrupo> gruposDoMesmoTipo = grupos.stream().filter(cg -> cg.getTipoCampoGrupoId().equals(tipoCampoGrupoId)).collect(Collectors.toList());

		if (gruposDoMesmoTipo.isEmpty()) {
			novoNomeGrupo = nomeGrupoSelecionado + " (" + 1 + ")";
		}
		else {
			Pattern pattern = Pattern.compile("(\\d+)");

			int maiorNumero = 1;
			for (CampoGrupo campoGrupo : gruposDoMesmoTipo) {

				String nome = campoGrupo.getNome();
				Matcher matcher = pattern.matcher(nome);
				if (matcher.find()) {

					int numero = Integer.valueOf(matcher.group());
					if (numero > maiorNumero) {
						maiorNumero = numero;
					}
				}
			}

			maiorNumero++;
			novoNomeGrupo = nomeGrupoSelecionado + " (" + maiorNumero + ")";
		}

		return novoNomeGrupo;
	}

	@Transactional(rollbackFor = Exception.class)
	public void removerGrupoDinamicoAndDocumento(CampoGrupo grupo, Usuario usuario) {

		boolean temImagensRelacionadas = imagemService.campoGrupoTemImagensRelacionadas(grupo);
		if (temImagensRelacionadas) {
			throw new MessageKeyException("exclusaoGrupoAndDocumentosComImagens.error");
		}

		Processo processo = grupo.getProcesso();
		ProcessoLog logGrupo = criaProcessoLogDeGrupoDinamico(grupo, usuario, AcaoProcesso.REMOVEU_GRUPO);

		campoGrupoService.removerGrupoDinamico(grupo, usuario);

		ImagemTransaction transaction = new ImagemTransaction();
		List<Documento> documentosRemovidos = documentoService.deletarDocumentosDinamicos(transaction, grupo);

		ProcessoLog logDocumentos = criarProcessoLogDeDocumentosDinamicos(processo, usuario, documentosRemovidos, AcaoProcesso.DELETOU_DOCUMENTO);

		processoLogService.saveOrUpdate(logGrupo);
		processoLogService.saveOrUpdate(logDocumentos);

		transaction.commit();
	}

	public ProcessoLog criarProcessoLogDeDocumentosDinamicos(Processo processo, Usuario usuario, List<Documento> documentos, AcaoProcesso acaoProcesso) {

		String descricao = "Documentos <br>";
		for(Documento documento : documentos) {
			String nome = documento.getNome();
			descricao += nome + "<br>";
		}

		ProcessoLog log = new ProcessoLog();
		log.setObservacao(descricao);
		log.setAcao(acaoProcesso);
		log.setUsuario(usuario);
		log.setProcesso(processo);
		log.setData(new Date());

		return log;
	}

	private ProcessoLog criaProcessoLogDeGrupoDinamico(CampoGrupo grupo, Usuario usuario, AcaoProcesso acaoProcesso) {

		Processo processo = grupo.getProcesso();
		String grupoNome = grupo.getNome();
		String valores = "";
		Long grupoId = grupo.getId();
		CampoGrupo campoGrupo = campoGrupoService.get(grupoId);
		Set<Campo> campos = campoGrupo.getCampos();
		if(campos != null){
			for(Campo campo : campos) {
				String campoNome = campo.getNome();
				String valor = campo.getValor();
				valores += campoNome + ": " + valor + "<br>";
			}
		}

		String descricao = grupoNome + "<br>" + valores;

		ProcessoLog log = new ProcessoLog();
		log.setObservacao(descricao);
		log.setAcao(acaoProcesso);
		log.setUsuario(usuario);
		log.setProcesso(processo);
		log.setData(new Date());

		return log;
	}

	public CampoGrupo criarGrupoDinamico(Long processoId, Long tipoCampoGrupoId, int novoIndice, AtomicLong sequenciaIdsCampos) {

		TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(tipoCampoGrupoId);
		int maxOrdem = campoGrupoService.getMaxOrdemGrupoByProcessoAndTipoCampoGrupo(processoId, tipoCampoGrupoId);

		String novoNomeGrupo = tipoCampoGrupo.getNome() + " (" + novoIndice + ")";

		CampoGrupo grupoClone = campoGrupoService.criaGrupo(null, tipoCampoGrupo);
		grupoClone.setId(null);
		grupoClone.setNome(novoNomeGrupo);
		grupoClone.setGrupoDinamico(true);
		grupoClone.setOrdem((novoIndice > maxOrdem ? novoIndice : maxOrdem + 1));

		List<TipoCampo> campos = tipoCampoService.findByTipoCampoGrupo(tipoCampoGrupo, true);

		Set<CampoAbstract> camposCriados = new LinkedHashSet<>();
		for(TipoCampo tipoCampo : campos) {

			Campo campo = campoService.criaCampo(grupoClone, tipoCampo);
			campo.setId(sequenciaIdsCampos.decrementAndGet());
			campo.setGrupo(grupoClone);
			campo.setValor(null);

			camposCriados.add(campo);
		}

		grupoClone.setCampos(camposCriados);

		return grupoClone;
	}

	public List<GrupoSuperiorVO> getGrupoSuperiorVOS(Set<CampoGrupo> campoGrupos) {

		List<GrupoSuperiorVO> vos = new ArrayList<>();

		Set<Long> campoGrupoIdsAdicionados = new HashSet<>();
		for (CampoGrupo campoGrupo : campoGrupos) {

			Long campoGrupoId = campoGrupo.getId();
			if (campoGrupoIdsAdicionados.contains(campoGrupoId)) {
				continue;
			}

			GrupoSuperiorVO vo = new GrupoSuperiorVO();
			Boolean grupoDinamico = campoGrupo.getGrupoDinamico();
			TipoCampoGrupo tipoSubGrupo = campoGrupo.getTipoSubgrupo();
			if (grupoDinamico && tipoSubGrupo != null) {

				List<CampoGrupo> subGrupos = filtrarSubGrupos(campoGrupos, tipoSubGrupo.getId());
				subGrupos.sort(Comparator.comparing(CampoGrupo::getOrdem));

				vo.setGrupoPai(true);
				vo.setTipoCampoGrupoId(tipoSubGrupo.getId());
				vo.setGrupoSuperior(campoGrupo);
				vo.setGruposFilhos(subGrupos);
				vo.setAbertoPadrao(campoGrupo.getAbertoPadrao());

				vos.add(vo);
				Set<Long> idsCamposGrupos = subGrupos.stream().map(CampoGrupo::getId).collect(Collectors.toSet());
				campoGrupoIdsAdicionados.addAll(idsCamposGrupos);
			}
			else if (grupoDinamico) {

				Long tipoCampoGrupoId = campoGrupo.getTipoCampoGrupoId();
				List<CampoGrupo> campoGrupoPai = filtrarGruposPai(campoGrupos, tipoCampoGrupoId);

				if (CollectionUtils.isEmpty(campoGrupoPai)) {

					vo.setGrupoSuperior(campoGrupo);
					vo.setGruposFilhos(Collections.emptyList());
					vo.setAbertoPadrao(campoGrupo.getAbertoPadrao());
					vo.setGrupoPai(true);
					vo.setTipoCampoGrupoId(tipoCampoGrupoId);

					vos.add(vo);
					campoGrupoIdsAdicionados.add(campoGrupoId);
				}
			}
			else {

				Long tipoCampoGrupoId = campoGrupo.getTipoCampoGrupoId();
				List<CampoGrupo> campoGrupoPai = filtrarGruposPai(campoGrupos, tipoCampoGrupoId);

				if (CollectionUtils.isEmpty(campoGrupoPai)) {

					vo.setGrupoSuperior(campoGrupo);
					vo.setGruposFilhos(Collections.emptyList());
					vo.setAbertoPadrao(campoGrupo.getAbertoPadrao());

					vos.add(vo);
					campoGrupoIdsAdicionados.add(campoGrupoId);
				}
			}
		}

		return vos;
	}

	private List<CampoGrupo> filtrarGruposPai(Set<CampoGrupo> campoGrupos, Long tipoCampoGrupoAtualId) {
		return campoGrupos.stream().filter(cg -> {

			TipoCampoGrupo tipoSubgrupo = cg.getTipoSubgrupo();
			if (tipoSubgrupo != null) {
				return tipoSubgrupo.getId().equals(tipoCampoGrupoAtualId);
			}
			return false;
		}).collect(Collectors.toList());
	}

	private List<CampoGrupo> filtrarSubGrupos(Set<CampoGrupo> campoGrupos, Long tipoSubGrupoId) {
		return campoGrupos.stream().filter(cg -> {

			Long tipoCampoGrupoId = cg.getTipoCampoGrupoId();

			if (tipoCampoGrupoId != null) {
				return tipoCampoGrupoId.equals(tipoSubGrupoId) && cg.getTipoSubgrupo() == null;
			}

			return false;
		}).collect(Collectors.toList());
	}

	@Transactional(rollbackFor = Exception.class)
	public List<Documento> criarDocumentosDinamicos(CampoGrupo grupoRelacionado, Processo processo) {

		List<Documento> documentosNovos = new ArrayList<>();

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Long processoId = processo.getId();
		Long grupoRelacionadoId = grupoRelacionado.getId();

		List<Long> idsByGrupoRelacionadoIdAndProcessoId = tipoDocumentoService.findIdsByGrupoRelacionadoIdAndProcessoId(grupoRelacionadoId, processoId);

		int maiorOrdem = documentoService.findMaxOrdemByProcesso(processo);

		List<TipoDocumento> list = new ArrayList<>();
		if (TipoProcesso.SIS_PROUNI.equals(tipoProcessoId)) {
			list = documentoService.getByProcessoTipoDocumento(processoId, TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_PROUNI);
		}
		else if (TipoProcesso.SIS_FIES.equals(tipoProcessoId)){
			list = documentoService.getByProcessoTipoDocumento(processoId, TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_FIES);
		}
		else if (TipoProcesso.TE_PROUNI.equals(tipoProcessoId)){
			list = documentoService.getByProcessoTipoDocumento(processoId, TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_TE_PROUNI);
		}
		else if (TipoProcesso.TE_FIES.equals(tipoProcessoId)){
			list = documentoService.getByProcessoTipoDocumento(processoId, TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_TE_FIES);
		}

		for(TipoDocumento tipoDocumento : list) {

			Long tipoDocumentoId = tipoDocumento.getId();
			if (idsByGrupoRelacionadoIdAndProcessoId.contains(tipoDocumentoId)) {
				continue;
			}

			String nomeDocumentoPadrao = tipoDocumento.getNome();
			String nome = nomeDocumentoPadrao  + " - " + grupoRelacionado.getNome();

			Documento documentoNovo = new Documento();
			documentoNovo.setId(null);
			documentoNovo.setNome(nome);
			documentoNovo.setObrigatorio(true);
			documentoNovo.setProcesso(processo);
			documentoNovo.setTipoDocumento(tipoDocumento);
			documentoNovo.setOrdem(maiorOrdem + 1);
			documentoNovo.setStatus(StatusDocumento.INCLUIDO);
			documentoNovo.setGrupoRelacionado(grupoRelacionado);

			documentoService.saveOrUpdate(documentoNovo);

			systraceThread("Sucesso ao criar o documento: " + documentoNovo.getNome());
			documentosNovos.add(documentoNovo);
		}

		return documentosNovos;
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarEvidencia(EvidenciaVO vo, Processo processo, Usuario usuario) throws Exception {
		salvarEvidencia(vo, processo, usuario, false);
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarEvidencia(EvidenciaVO vo, Processo processo, Usuario usuario, boolean temGruposDinamicos) throws Exception {

		List<? extends CampoAbstract> campos = vo.getCampos();
		if(campos != null && !campos.isEmpty()) {

			if (!temGruposDinamicos) {
				atualizarProcesso(processo, usuario, campos);
			} else {
				atualizarProcessoComGruposDinamicos(processo, new HashSet<>(campos), usuario);
			}
		}

		AcaoProcesso acao = vo.getAcao();
		String observacaoTmp = vo.getObservacaoTmp();
		TipoEvidencia tipoEvidencia = vo.getTipoEvidencia();
		Subarea subarea = vo.getSubarea();
		Usuario analista = processo.getAnalista();
		ProcessoLog log = new ProcessoLog();
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setObservacao(observacaoTmp);
		log.setTipoEvidencia(tipoEvidencia);
		log.setAcao(acao);
		if(AcaoProcesso.REENVIO_ANALISE.equals(vo.getAcao()) || AcaoProcesso.REGISTRO_EVIDENCIA.equals(vo.getAcao())) {
			log.setLido(usuario.equals(analista));
		}

		processoLogService.saveOrUpdate(log);

		List<FileVO> arquivos = vo.getArquivos();
		for(FileVO fileVO : arquivos) {
			processoLogAnexoService.criar(log, fileVO);
		}

		Situacao situacaoVO = vo.getSituacao();
		Long situacaoId = situacaoVO != null ? situacaoVO.getId() : null;
		if(processo.isSisFiesOrSisProuni() && Situacao.CONSULTAR_SIA_FIES_PROUNI_IDS.contains(situacaoId)) {
			Long processoId = processo.getId();
			atualizarProcessoComSiaFiesProuni(processoId, usuario, campos, null, null);
		}

		if(situacaoVO != null) {
			concluirSituacao(processo, usuario, situacaoVO, log, subarea);
		}

		if(AcaoProcesso.CANCELAMENTO.equals(acao)) {
			cancelar(processo, log);
		}
		else if(AcaoProcesso.EM_ACOMPANHAMENTO.equals(acao)) {
			Integer prazoDias = vo.getPrazoDias();
			emAcompanhamento(processo, log, prazoDias);
		}
		else if(AcaoProcesso.ENVIO_PENDENCIA.equals(acao)) {
			pendenciar(processo, log, vo);
		}
		else if(AcaoProcesso.RESPOSTA_PENDENCIA.equals(acao)) {
			respostaPendencia(processo, log, vo);
		}
		else if(AcaoProcesso.REENVIO_ANALISE.equals(acao)) {
			enviarParaAnalise(processo, usuario, log, true);
		}

		boolean showEmail = vo.getShowEmail();
		List<String> destinatariosList = vo.getDestinatariosList();
		if(showEmail && destinatariosList != null && !destinatariosList.isEmpty()) {

			EmailVO emailVO = new EmailVO();
			emailVO.setDestinatariosList(destinatariosList);
			emailVO.setLogCriacao(log);
			emailVO.setObservacaoTmp(observacaoTmp);

			List<FileVO> arquivos2 = emailVO.getArquivos();
			for(FileVO fileVO : arquivos) {
				arquivos2.add(fileVO);
			}

			emailEnviadoService.enviarEmail(emailVO, processo, usuario);
		}

		verificarFluxoTrabalho(processo, usuario, acao);
	}

	public void atualizarProcessoComSiaFiesProuni(Long processoId, Usuario usuario, List<? extends CampoAbstract> campos, AlunoFiltro alunoFiltro, Boolean existeFinanciamento) throws Exception {

		Processo processo = get(processoId);

		String numCandidato = "";
		String numInscricao = "";
		if(campos != null && !campos.isEmpty()) {
			for(CampoAbstract campo : campos) {
				String nome = campo.getNome();
				String valor = campo.getValor();
				if(CampoMap.CampoEnum.NUM_CANDIDATO.getNome().equals(nome)) {
					numCandidato = valor;
				}
			}
		}
		else if(alunoFiltro != null) {
			numCandidato = alunoFiltro.getNumCandidato();
		}

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		ConsultaInscricoesVO consultaInscricoesVO = siaService.consultaInscricao(alunoFiltro);
		if(consultaInscricoesVO == null) {
			consultaInscricoesVO = consultaCandidatoService.getConsultaInscricoesVO(numInscricao, numCandidato);
		}
		if(consultaInscricoesVO == null) {
			throw new MessageKeyException("candidatoNaoEncontrado.error");
		}

		if(tipoProcessoId.equals(TipoProcesso.SIS_PROUNI) || tipoProcessoId.equals(TipoProcesso.SIS_FIES)) {
			String cpfImportacao = getCampoProcessoValor(processo, CampoMap.CampoEnum.CPF_IMPORTACAO);
			if(cpfImportacao != null) {
				cpfImportacao = getCpfCnpjDesformatado(cpfImportacao);
				cpfImportacao = DummyUtils.removerEspacos(cpfImportacao);
				if (cpfImportacao.length() > 11) {
					acrescentaCaracterCPF(cpfImportacao);
				}

				String cpfConsultaInscricao = consultaInscricoesVO.getCpf();

				if(!cpfImportacao.equals(cpfConsultaInscricao)) {
					throw new MessageKeyException("cpfDivergenteCandidato.error");
				}
			}
		}

		String tipoBolsaVinculadorBaseInterna = "";
		String tipoBolsaProcessoTelaBaseInterna = "";

		List<Processo> processoVinculadorList = verificaProcessoVinculo(tipoProcessoId, numCandidato);

		if (!processoVinculadorList.isEmpty()) {
			Processo processoVinculador = processoVinculadorList.get(0);
			TipoProcesso tipoProcessoVinculador = processoVinculador.getTipoProcesso();
			Long tipoProcessoIdVinculador = tipoProcessoVinculador.getId();

			if (TipoProcesso.SIS_PROUNI.equals(tipoProcessoId) && TipoProcesso.SIS_PROUNI.equals(tipoProcessoIdVinculador)) {

				String tipoBolsaProcessoVinculador = getCampoProcessoValor(processoVinculador, CampoMap.CampoEnum.TIPO_BOLSA);
				String tipoBolsaProcessoTela = getCampoProcessoValor(processo, CampoMap.CampoEnum.TIPO_BOLSA);

				boolean possuiBolsaProcessoVinculador = StringUtils.startsWith(tipoBolsaProcessoVinculador, "BOLSA");
				boolean possuiBolsaProcessoTela = StringUtils.startsWith(tipoBolsaProcessoTela, "BOLSA");

				if (possuiBolsaProcessoTela && possuiBolsaProcessoVinculador) {
					String tipoBolsaVinculadorBaseInternaFormatada = DummyUtils.formatarChaveUnicidadeBaseInterna(tipoBolsaProcessoVinculador);
					tipoBolsaVinculadorBaseInterna = baseRegistroService.getLabel(BaseInterna.TIPO_DE_BOLSA, tipoBolsaVinculadorBaseInternaFormatada);

					String tipoBolsaProcessoTelaBaseInternaFormatada = DummyUtils.formatarChaveUnicidadeBaseInterna(tipoBolsaProcessoTela);
					tipoBolsaProcessoTelaBaseInterna = baseRegistroService.getLabel(BaseInterna.TIPO_DE_BOLSA, tipoBolsaProcessoTelaBaseInternaFormatada);
				}
			}
		}
		boolean existsProcessoDuplicado = existsProcessoDuplicado(processo, tipoProcessoId, numCandidato);

		if(!tipoBolsaVinculadorBaseInterna.equals(tipoBolsaProcessoTelaBaseInterna) &&
			(StringUtils.isNotBlank(tipoBolsaVinculadorBaseInterna) && StringUtils.isNotBlank(tipoBolsaProcessoTelaBaseInterna))) {

			existsProcessoDuplicado = false;

		}
		if(existsProcessoDuplicado) {
			throw new MessageKeyException("processoDuplicado.error");
		}

		AlunoFiltro alunoFiltro2 = new AlunoFiltro();
		alunoFiltro2.setNumCandidato(numCandidato);

		Long codFormaIngresso = consultaInscricoesVO.getCodFormaIngresso();

		boolean isTe = TipoProcessoPortal.TRANSFERENCIA_EXTERNA.getClassificacao().equals(codFormaIngresso);
		boolean isFinanciamento = TipoProcesso.SIS_FIES.equals(tipoProcessoId) || TipoProcesso.SIS_PROUNI.equals(tipoProcessoId);
		if(isTe && isFinanciamento) {
			validarFinanciamentoTransferenciaExterna(processo, existeFinanciamento, numCandidato, consultaInscricoesVO);
		}

		Date data = new Date();
		consultaInscricoesVO.setDataVinculoSia(data);

		consultaCandidatoService.atualizaProcesso(processo, usuario, consultaInscricoesVO);

		StringBuilder logObs = new StringBuilder();
		logObs.append(CampoMap.CampoEnum.NUM_CANDIDATO.getNome()).append(": " + numCandidato).append("<br> ");
		processoLogService.criaLog(processo, usuario, AcaoProcesso.ATUALIZOU_DADOS_SIA, logObs.toString());
	}

	public List <Processo> verificaProcessoVinculo(Long tipoProcessoId, String numCandidato){

		List<Long> processosIdsVinculo = findProcessoIdByNumCandidatoNumInscricao(numCandidato, null);
		List<Processo> processosPossuiVinculo = new ArrayList<>();

		for(Long processoIdVinculo : processosIdsVinculo) {
			Processo processoVinculo = get(processoIdVinculo);
			TipoProcesso tipoProcessoVinculador = processoVinculo.getTipoProcesso();
			Long tipoProcessoIdVinculador = tipoProcessoVinculador.getId();

			if(tipoProcessoId.equals(tipoProcessoIdVinculador)){
					processosPossuiVinculo.add(processoVinculo);
			}
		}
		if(processosPossuiVinculo.size() > 1){
			throw new MessageKeyException("processoJaExistenteComVinculo.error", processosPossuiVinculo);
		}else{
			return processosPossuiVinculo;
		}
	}

	private void validarFinanciamentoTransferenciaExterna(Processo processo, Boolean existeFinanciamento, String numCandidato, ConsultaInscricoesVO consultaInscricoesVO) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		if(existeFinanciamento == null) {
			String finc = null;
			finc = TipoProcesso.SIS_FIES.equals(tipoProcessoId) ? "Fies" : finc;
			finc = TipoProcesso.SIS_PROUNI.equals(tipoProcessoId) ? "Prouni" : finc;
			throw new MessageKeyException("candidatoTE.campoPergunta.required." + finc + ".error");
		}
		else if(existeFinanciamento) {
			String finc = null;
			finc = TipoProcesso.SIS_FIES.equals(tipoProcessoId) ? "Fies" : finc;
			finc = TipoProcesso.SIS_PROUNI.equals(tipoProcessoId) ? "Prouni" : finc;
			throw new MessageKeyException("candidatoTE.vinculoNegado.possuiVinculoOrigem." + finc + ".error", numCandidato);
		}
		else if (!existeFinanciamento) {
			boolean periodoIngressoValido = isPeriodoIngressoTEFinanciamentoValido(processo, consultaInscricoesVO);
			if(!periodoIngressoValido) {
				String finc = null;
				finc = TipoProcesso.SIS_FIES.equals(tipoProcessoId) ? "Fies" : finc;
				finc = TipoProcesso.SIS_PROUNI.equals(tipoProcessoId) ? "Prouni" : finc;
				throw new MessageKeyException("candidatoTE.vinculoNegado.periodoAntigo." + finc + ".error");
			}
		}
	}

	private String acrescentaCaracterCPF(String cpfImportacao) {
		do {
			cpfImportacao = ("0" + cpfImportacao);
		} while (cpfImportacao.length() < 11);

		return cpfImportacao;
	}

	private void respostaPendencia(Processo processo, ProcessoLog log, EvidenciaVO vo) throws Exception {

		Usuario usuario = log.getUsuario();
		Long processoId = processo.getId();
		ProcessoPendenciaVO pp = processoPendenciaService.getLastPendenciaByProcesso(processoId);
		if(pp != null) {
			ProcessoPendencia pendencia = pp.getPendencia();
			Date data = log.getData();
			pendencia.setDataFinalizacao(data);
			processoPendenciaService.saveOrUpdate(pendencia);
		}

		enviarParaAnalise(processo, usuario);
	}

	private void pendenciar(Processo processo, ProcessoLog log, EvidenciaVO vo) {

		StatusProcesso statusOld = processo.getStatus();

		processo.setStatus(StatusProcesso.PENDENTE);
		processo.setPrazoLimiteAnalise(null);
		processo.setDataFinalizacao(null);
		List<StatusProcesso> statusFimAnalise = StatusProcesso.getStatusFimAnalise();
		if(!statusFimAnalise.contains(statusOld)) {
			processo.setDataFinalizacaoAnalise(null);
		}
		processoRepository.saveOrUpdate(processo);

		ProcessoPendencia pp = new ProcessoPendencia();
		Date data = log.getData();
		pp.setProcesso(processo);
		pp.setDataCriacao(data);

		Integer prazoDias = vo.getPrazoDias();
		if(prazoDias != null) {

			Situacao situacao = processo.getSituacao();
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = buildHUC(situacaoId);

			String[] expedienteArray = parametroService.getExpediente();
			Expediente expediente = new Expediente(expedienteArray);
			BigDecimal horasExpediente = expediente.getHoras();
			BigDecimal prazoDiasBD = new BigDecimal(prazoDias);
			BigDecimal prazoHoras = horasExpediente.multiply(prazoDiasBD);

			Date prazoLimite = huc.addHoras(data, prazoHoras);
			processo.setPrazoLimitePendente(prazoLimite);
			pp.setPrazoLimite(prazoLimite);
		}

		processoPendenciaService.saveOrUpdate(pp);

		log.setPendencia(pp);
		log.setStatusProcesso(StatusProcesso.PENDENTE);
		processoLogService.saveOrUpdate(log);

		//emailSmtpService.enviarNotificacaoPendencia(processo, log);
	}

	private void cancelar(Processo processo, ProcessoLog log) {

		Date data = log.getData();

		log.setStatusProcesso(StatusProcesso.CANCELADO);
		processoLogService.saveOrUpdate(log);

		processo.setStatus(StatusProcesso.CANCELADO);
		processo.setDataFinalizacao(data);

		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		if(dataFinalizacaoAnalise == null) {
			processo.setDataFinalizacaoAnalise(data);
		}

		processoRepository.saveOrUpdate(processo);
	}

	private void emAcompanhamento(Processo processo, ProcessoLog log, Integer prazoDias) {

		Date data = log.getData();

		StatusProcesso statusOld = processo.getStatus();
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		processo.setStatus(StatusProcesso.EM_ACOMPANHAMENTO);
		log.setStatusProcesso(StatusProcesso.EM_ACOMPANHAMENTO);
		processoLogService.saveOrUpdate(log);

		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		if(dataFinalizacaoAnalise == null && !StatusProcesso.PENDENTE.equals(statusOld)) {
			processo.setDataFinalizacaoAnalise(data);
		}

		if(prazoDias != null) {

			HorasUteisCalculator huc = buildHUC(situacaoId);

			Expediente expediente = huc.getExpediente();
			BigDecimal horasExpediente = expediente.getHoras();
			BigDecimal prazoDiasBD = new BigDecimal(prazoDias);
			BigDecimal prazoHoras = horasExpediente.multiply(prazoDiasBD);

			Date prazoLimite = huc.addHoras(data, prazoHoras);
			//EXCEÇÃO PARA JOGAR PARA O FINAL DO DIA O PRAZO LIMITE
			//PQ OS ANALISTAS CSC DEVEM PODER CONSEGUIR ANALISAR ATÉ O FIM DO DIA
			if(Situacao.ANALISE_ISENCAO_IDS.contains(situacaoId) || Situacao.ANALISE_ISENCAO_IBMEC_IDS.contains(situacaoId)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(prazoLimite.getTime());
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				prazoLimite = cal.getTime();
			}
			processo.setPrazoLimiteEmAcompanhamento(prazoLimite);
		}

		processo.setDataFinalizacao(null);
		processoRepository.saveOrUpdate(processo);
	}

	private void encaminhar(Processo processo, Usuario usuario, ProcessoLog log0, ProcessoLog log, Subarea subarea) throws Exception {

		//processo.setStatus(StatusProcesso.ENCAMINHADO);
		log.setStatusProcesso(processo.getStatus());
		processoLogService.saveOrUpdate(log);

		processoRepository.saveOrUpdate(processo);

		SolicitacaoVO vo = new SolicitacaoVO();
		vo.setSolicitacao(new Solicitacao());
		vo.setAcao(AcaoProcesso.SOLICITACAO_CRIACAO);
		vo.getSolicitacao().setSubarea(subarea);
		vo.setObservacaoTmp(log0.getObservacao());
		vo.setLogCriacao(log0);

		Set<ProcessoLogAnexo> anexos = log0.getAnexos();
		for(ProcessoLogAnexo anexo : anexos) {
			String path = anexo.getPath();
			String extensao = DummyUtils.getExtensao(path);
			try {
				File fileTmp = File.createTempFile("anexo_solicitacao_", "." + extensao);
				FileUtils.copyFile(new File(path), fileTmp);
				vo.addAnexo(anexo.getNome(), fileTmp);
			}
			catch(IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		solicitacaoService.salvarSolicitacao(vo, processo, usuario);
	}

	public List<Object[]> findToRelatorioGeral(Date dataInicio, Date dataFim, Long tipoProcessoId) {

		Stopwatch stopwatch = Stopwatch.createStarted();

		List<Object[]> toRelatorioGeral = processoRepository.findToRelatorioGeral(dataInicio, dataFim, tipoProcessoId);

		systraceThread("Tempo=" + stopwatch.elapsed(TimeUnit.SECONDS) + "s.");
		return toRelatorioGeral;
	}

	public List<Processo> findByIds(List<Long> processosIds) {
		return processoRepository.findByIds(processosIds);
	}

	@Transactional(rollbackFor = Exception.class)
	public void concluirEmMassa(List<Processo> processos, Usuario usuario, Situacao novaSituacao, String observacao) throws Exception {
		for(Processo processo : processos) {
			EvidenciaVO evidenciaVO = new EvidenciaVO();
			evidenciaVO.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);
			evidenciaVO.setSituacao(novaSituacao);
			evidenciaVO.setObservacaoTmp(observacao);
			salvarEvidencia(evidenciaVO, processo, usuario);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void concluirSituacao(Processo processo, Usuario usuario, Situacao novaSituacao, ProcessoLog log0, Subarea subarea) throws Exception {
		concluirSituacaoComLock(processo, usuario, novaSituacao, log0, subarea);
	}

	private void concluirSituacaoComLock(Processo processo, Usuario usuario, Situacao novaSituacao, ProcessoLog log0, Subarea subarea) throws Exception {

		Long processoId = processo.getId();
		KeyLock lock = new KeyLock(DummyUtils.getCurrentMethodName() + "-" + processoId);

		lock.lock2();

		try {
			boolean duploClickTrocaSituacao = isDuploClickTrocaSituacao(processo, novaSituacao);
			if(duploClickTrocaSituacao) {
				return;
			}

			concluirSituacaoSemLock(processo, usuario, novaSituacao, log0, subarea);
		}
		finally {
			lock.unlock();
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void alterarSituacaoMudancaDeFluxoRegra(Processo processo, Usuario usuario, Situacao novaSituacao, ProcessoLog log0, Subarea subarea) throws Exception {
		concluirSituacaoSemLock(processo, usuario, novaSituacao, log0, subarea);
	}

	private void concluirSituacaoSemLock(Processo processo, Usuario usuario, Situacao novaSituacao, ProcessoLog log0, Subarea subarea) throws Exception {

		Long processoId = processo.getId();
		Situacao situacaoOld = processo.getSituacao();
		StatusProcesso statusOld = processo.getStatus();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		ProcessoLog log = trocarSituacao2(processo, novaSituacao, usuario);

		atualizaEtapaNoLog(novaSituacao, log);

		StatusProcesso novoStatus = novaSituacao.getStatus();

		BigDecimal horasPrazo = novaSituacao.getHorasPrazo();
		if(novaSituacao.isEncaminhado() && subarea != null) {
			Area area = subarea.getArea();
			Integer horasPrazo1 = area.getHorasPrazo();
			horasPrazo = new BigDecimal(horasPrazo1);
		}

		Long novaSituacaoId = novaSituacao != null ? novaSituacao.getId() : null;
		HorasUteisCalculator huc = buildHUC(novaSituacaoId);

		Date prazoLimite = null;
		if(horasPrazo != null) {
			prazoLimite = huc.addHoras(new Date(), horasPrazo);
			//EXCEÇÃO PARA JOGAR PARA O FINAL DO DIA O PRAZO LIMITE
			//PQ OS ANALISTAS CSC DEVEM PODER CONSEGUIR ANALISAR ATÉ O FIM DO DIA
			if(Situacao.ANALISE_ISENCAO_IDS.contains(novaSituacaoId) || Situacao.ANALISE_ISENCAO_IBMEC_IDS.contains(novaSituacaoId)){
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(prazoLimite.getTime());
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				prazoLimite = cal.getTime();
				processo.setPrazoLimiteEmAcompanhamento(prazoLimite);
			}
		}

		systraceThread("setPrazoLimiteSituacao=" + prazoLimite + ", horasPrazo=" + horasPrazo);

		processo.setHorasPrazoSituacao(horasPrazo);
		processo.setPrazoLimiteSituacao(prazoLimite);
		processo.setStatus(novoStatus);

		log.setHorasPrazoSituacao(horasPrazo);
		log.setPrazoLimiteSituacao(prazoLimite);
		log.setSituacaoAnterior(situacaoOld);
		log.setSituacao(novaSituacao);
		log.setStatusProcesso(novoStatus);
		processoLogService.saveOrUpdate(log);

		if(novaSituacao.isEncaminhado()) {
			//processo.setStatus(StatusProcesso.ENCAMINHADO);
			encaminhar(processo, usuario, log0, log, subarea);
		}
		else if(!statusOld.equals(novoStatus)) {

			if(StatusProcesso.EM_ANALISE.equals(novoStatus)) {
				enviarParaAnalise(processo, usuario, StatusProcesso.EM_ANALISE, log, novaSituacao, true, true);
			}
			else if(StatusProcesso.PENDENTE.equals(novoStatus)) {

				EvidenciaVO vo = new EvidenciaVO();
				BigDecimal horasPrazo2 = novaSituacao.getHorasPrazo();
				if(horasPrazo2 != null) {
					Expediente expediente = huc.getExpediente();
					BigDecimal horasExpediente = expediente.getHoras();
					BigDecimal diasPrazo = horasPrazo2.divide(horasExpediente, RoundingMode.CEILING);
					vo.setPrazoDias(diasPrazo.intValue());
				}
				ProcessoLog log2 = processoLogService.criaLog(processo, usuario, AcaoProcesso.ENVIO_PENDENCIA);
				pendenciar(processo, log2, vo);
			}
			else if(StatusProcesso.EM_ACOMPANHAMENTO.equals(novoStatus)) {

				BigDecimal horasPrazo2 = novaSituacao.getHorasPrazo();
				Expediente expediente = huc.getExpediente();
				BigDecimal horasExpediente = expediente.getHoras();
				BigDecimal diasPrazo = horasPrazo2.divide(horasExpediente, RoundingMode.CEILING);

				ProcessoLog log2 = processoLogService.criaLog(processo, usuario, AcaoProcesso.EM_ACOMPANHAMENTO);
				emAcompanhamento(processo, log2, diasPrazo.intValue());
			}
			else if(StatusProcesso.CONCLUIDO.equals(novoStatus)) {
				concluir(processo, usuario, null);
			}
			else if(StatusProcesso.CANCELADO.equals(novoStatus)) {
				ProcessoLog log2 = processoLogService.criaLog(processo, usuario, AcaoProcesso.CANCELAMENTO);
				cancelar(processo, log2);
			}
		}

		Usuario analista = processo.getAnalista();
		if(analista != null && !TipoProcesso.ISENCAO_DISCIPLINAS.equals(tipoProcessoId)) {
			Long analistaId = analista.getId();
			boolean atendeSituacao = usuarioService.atendeSituacao(analistaId, novaSituacaoId, processo);
			if (!atendeSituacao) {
				processo.setDataUltimaAcaoAnalista(null);
			}

			if(situacaoOld != null) {
				Long situacaoOldId = situacaoOld.getId();
				removerAnalista(processo, situacaoOldId, novaSituacaoId);
			}
		}

		Boolean usaTermo = processo.getUsaTermo() != null ? processo.getUsaTermo() : false;
		if(usaTermo != null) {
			documentoService.atualizarUsaTermo(null, null, processoId);
		}

		validacaoNotificacaoAtila(processo);

		processoRepository.saveOrUpdate(processo);

		AcaoProcesso acao = log0 == null ? AcaoProcesso.REPROCESSAMENTO_REGRAS : log0.getAcao();
		String erros = processoRegraService.criarRegras(processo, novaSituacao, usuario, true, true);
		if(isNotBlank(erros) && situacaoOld != null && !acao.equals(AcaoProcesso.CRIACAO)) {
			if(usuario != null) {
				usuario.setProcessoAtualId(processoId);
				usuario.setDataProcessoAtual(new Date());
			}
			throw new MessageKeyException("concluirSituacao.regras.error", erros);
		} else if (isNotBlank(erros) && TipoProcesso.ISENCAO_DISCIPLINAS.equals(tipoProcessoId)) {
			throw new MessageKeyException("concluirSituacao.regras.error", erros);
		}

	}

	private void validacaoNotificacaoAtila(Processo processo) {
		Long processoId = processo.getId();

		boolean exists = documentoService.existsToNotificarAtila(processoId);

		if(exists) {
			ProcessoNotificarAtila processoNotificarAtila = processoNotificarAtilaService.findByProcessoId(processoId);
			if(processoNotificarAtila != null) {
				Boolean notificarAtila = processoNotificarAtila.getNotificarAtila();
				if(!notificarAtila) {
					processoNotificarAtila.setNotificarAtila(true);
					processoNotificarAtilaService.saverOrUpdate(processoNotificarAtila);
				}
			} else {
				processoNotificarAtila = new ProcessoNotificarAtila();
				processoNotificarAtila.setProcesso(processo);
				processoNotificarAtila.setNotificarAtila(true);
				processoNotificarAtilaService.saverOrUpdate(processoNotificarAtila);
			}
		}
	}

	private void atualizaEtapaNoLog(Situacao novaSituacao, ProcessoLog log) {

		Etapa etapa = novaSituacao.getEtapa();
		if(etapa != null) {
			Date data = log.getData();
			Date prazoLimiteEtapa = calculaPrazoLimiteEtapa(novaSituacao, data);
			etapa = etapaService.get(etapa.getId());
			BigDecimal horasPrazoEtapa = etapa != null ? etapa.getHorasPrazo() : null;

			log.setPrazoLimiteEtapa(prazoLimiteEtapa);
			log.setHorasPrazoEtapa(horasPrazoEtapa);
			log.setEtapa(etapa);
		}
	}

	private Date recalcularPrazoLimiteEtapa(Date data, Date prazoLimiteEtapa, Etapa etapa, HorasUteisCalculator huc) {

		Calendar dataEtapa = Calendar.getInstance();
		Calendar dataLog = Calendar.getInstance();
		dataLog.setTime(data);
		dataEtapa.setTime(prazoLimiteEtapa);

		boolean recalcularFinalDeSemana = etapa.getRecalcularFinalDeSemana();
		boolean considerarFinalDoDia = etapa.getConsiderarFinalDoDia();
		if(recalcularFinalDeSemana) {
			boolean isDiaUtil = huc.isDiaUtil(data);
			if(Arrays.asList(Calendar.SUNDAY, Calendar.SATURDAY, Calendar.MONDAY).contains(dataLog.get(Calendar.DAY_OF_WEEK)) && !isDiaUtil) {
				dataEtapa.add(Calendar.DAY_OF_MONTH, 1);
				prazoLimiteEtapa = dataEtapa.getTime();
			}
		}
		if (considerarFinalDoDia) {
			dataEtapa.set(Calendar.HOUR_OF_DAY, 23);
			dataEtapa.set(Calendar.MINUTE, 59);
			dataEtapa.set(Calendar.SECOND, 59);
			prazoLimiteEtapa = dataEtapa.getTime();
		}

		return prazoLimiteEtapa;
	}

	private void removerAnalista(Processo processo, Long situacaoOldId, Long novaSituacaoId) {

		if(Situacao.DOCUMENTO_PENDENTE_IDS.contains(situacaoOldId) &&
				!Situacao.DOCUMENTACAO_APROVADA_IDS.contains(novaSituacaoId) &&
				!Situacao.DOCUMENTO_PENDENTE_IDS.contains(novaSituacaoId)) {
			processo.setAnalista(null);
			processo.setDataUltimaAcaoAnalista(null);
		}
	}

	private boolean isDuploClickTrocaSituacao(Processo processo, Situacao novaSituacao) throws Exception {

		Boolean lockEnabled = LOCK_ENABLED.get();
		if(lockEnabled != null && !lockEnabled) {
			return false;
		}

		//tempo para a transação anterior dar commit após o unlock
		DummyUtils.sleep(100);

		Long processoId = processo.getId();
		Long novaSituacaoId = novaSituacao.getId();

		Bolso<Long> minutosBolso = new Bolso<>();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			ProcessoLog lastLogSituacao = processoLogService.findLastLogByProcesso(processoId, novaSituacaoId);
			Date lastEnvio = lastLogSituacao != null ? lastLogSituacao.getData() : null;
			Long minutos = lastEnvio != null ? DummyUtils.getMinutosEntre(lastEnvio, new Date()) : null;
			minutosBolso.setObjeto(minutos);
		});
		tw.runNewThread();
		tw.throwException();

		Long minutos = minutosBolso.getObjeto();
		if(minutos != null && minutos <= 1) {
			return true;
		}

		return false;
	}

	@Transactional(rollbackFor = Exception.class)
	public void concluir(Processo processo, Usuario usuario, Situacao novaSituacao) throws Exception {

		if(StatusProcesso.CANCELADO.equals(processo.getStatus())) {
			return;
		}

		if(novaSituacao != null) {
			trocarSituacao2(processo, novaSituacao, usuario);
		}

		processo.setStatus(StatusProcesso.CONCLUIDO);
		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.CONCLUSAO);

		Long processoId = processo.getId();
		solicitacaoService.finalizarByProcesso(processoId);

		Date data = log.getData();
		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		if(dataFinalizacaoAnalise == null) {
			processo.setDataFinalizacaoAnalise(data);
		}

		processo.setDataFinalizacao(data);
		processoRepository.saveOrUpdate(processo);

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.CONCLUSAO);
	}

	@Transactional(rollbackFor = Exception.class)
	public void enviarParaAnalise(Processo processo, Usuario usuario) throws Exception {
		enviarParaAnalise(processo, usuario, null, true);
	}

	@Transactional(rollbackFor = Exception.class)
	public void enviarParaAnalise(Processo processo, Usuario usuario, ProcessoLog log, boolean validarObrigatorio) throws Exception {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Situacao situacaoInicial = tipoProcesso.getSituacaoInicial();
		StatusProcesso status = situacaoInicial.getStatus();

		enviarParaAnalise(processo, usuario, status, log, null, validarObrigatorio, true);
	}

	public void enviarParaAnalise(Processo processo, Usuario usuario, StatusProcesso status, ProcessoLog log, Situacao situacaoDestino, boolean validarObrigatorio, boolean validarRegrasParaEnvio) throws Exception {

		Long processoId = processo.getId();
		KeyLock lock = new KeyLock(DummyUtils.getCurrentMethodName() + "-" + processoId);

		systraceThread("lock... processo: " + processo.getId() + " lock: " + lock + ", situacao: " + processo.getSituacao() + ", status: " + processo.getStatus());
		long tempoEspera = lock.lock2();
		systraceThread("continuando lock... processo: " + processo.getId() + " lock: " + lock + " tempoEspera: " + tempoEspera);

		try {
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();

			Situacao situacaoAnalise = situacaoDestino != null ? situacaoDestino : tipoProcesso.getSituacaoInicial();

			boolean duploClickTrocaSituacao = isDuploClickTrocaSituacao(processo, situacaoAnalise);
			if(duploClickTrocaSituacao) {
				return;
			}

			List<CampoAbstract> campos = validaCampos(processo, validarObrigatorio);

			validaUnicidade(tipoProcesso, processoId, campos);

			Situacao situacao = processo.getSituacao();
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = buildHUC(situacaoId);

			if(situacao == null || !situacao.equals(situacaoAnalise)) {
				atualizarSituacaoInicialAnalise(processo, usuario, tipoProcesso, huc);

				systraceThread("prazoLimiteSituacao=" + processo.getPrazoLimiteAnalise() + ", horasPrazo=" + processo.getHorasPrazoAnalise());

				status = processo.getStatus();
			}

			processo.setStatus(status);
			log = log != null ? log : processoLogService.criaLog(processo, usuario, AcaoProcesso.ENVIO_ANALISE);

			if(log != null) {
				log.setStatusProcesso(status);
				processoLogService.saveOrUpdate(log);
			}
			else {
				log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ENVIO_ANALISE);
			}

			Date data = log.getData();
			processo.setDataEnvioAnalise(data);
			BigDecimal horasPrazo = tipoProcesso.getHorasPrazo();
			if(Situacao.ISENCAO_DISCIPLINAS_IDS.contains(situacaoId)) {
				horasPrazo = situacao.getHorasPrazo();
			}
			processo.setHorasPrazoAnalise(horasPrazo);

			Date prazoLimite = huc.addHoras(data, horasPrazo);
			if(Situacao.ISENCAO_DISCIPLINAS_IDS.contains(situacaoId)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(prazoLimite.getTime());
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				prazoLimite = cal.getTime();
			}
			processo.setPrazoLimiteAnalise(prazoLimite);

			processo.setDataFinalizacao(null);
			processo.setDataFinalizacaoAnalise(null);

			if(!Situacao.ISENCAO_DISCIPLINAS_IDS.contains(situacaoId)) {
				if(validarRegrasParaEnvio) {
					validarRegrasParaEnvio(processo, usuario, situacaoAnalise);
				}
				else {
					//Cria regras imediatas da situação e executa
					processoRegraService.criarRegras(processo, situacaoAnalise, usuario, true, false);

					//criar regras agendadas da situação
					processoRegraService.criarRegras(processo, situacaoAnalise, usuario, false, true);
				}
			}

			Usuario autor = processo.getAutor();
			if(usuario != null && autor == null) {
				autor = usuario;
				processo.setAutor(autor);
			}

			Usuario analista = processo.getAnalista();
			if(analista != null && !TipoProcesso.ISENCAO_DISCIPLINAS.equals(tipoProcessoId)) {
				processo.setAnalista(null);
				processo.setDataUltimaAcaoAnalista(null);
			}

			processoRepository.saveOrUpdate(processo);

			verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ENVIO_ANALISE);

			if(usuario != null) {
				Long usuarioId = usuario.getId();
				bloqueioProcessoService.desbloquearByUsuario(usuarioId);
			}
		}
		finally {
			lock.unlock();
		}
	}

	private void validarRegrasParaEnvio(Processo processo, Usuario usuario, Situacao situacaoInicial) throws Exception {

		//Cria regras imediatas da situação e executa
		processoRegraService.criarRegras(processo, situacaoInicial, usuario, true, false);

		Long processoId = processo.getId();
		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setProcessoId(processoId);
		List<ProcessoRegra> lasts2 = getLastsRegrasVerificarEnvio(filtro, processo);
		if(!lasts2.isEmpty()) {
			for(ProcessoRegra pr : lasts2) {
				Regra regra = pr.getRegra();
				Situacao regraSituacao = regra.getSituacao();
				TipoExecucaoRegra tipoExecucao = regra.getTipoExecucao();
				//processa regras das situações anteriores
				if(!regraSituacao.equals(situacaoInicial) || TipoExecucaoRegra.AGENDADA.equals(tipoExecucao)) {
					processoRegraService.reprocessarRegra(processo, regra, usuario);
				}
			}
		}
		lasts2 = getLastsRegrasComErroOuPendentes(filtro, processo);
		if(!lasts2.isEmpty()) {

			StringBuilder regrasNomes = new StringBuilder();
			for(ProcessoRegra pr : lasts2) {
				Regra regra = pr.getRegra();
				String regraNome = regra.getNome();
				regrasNomes.append(regraNome).append(", ");
			}
			String regrasNomesStr = regrasNomes.toString();
			regrasNomesStr = regrasNomesStr.substring(0, regrasNomesStr.length() - 2);

			throw new MessageKeyException("regra-envioProcesso.error", regrasNomesStr);
		}

		List<ProcessoRegra> processoRegras = getLastsRegrasVerificarEnvio(filtro, processo);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		String permissoeseEnvioFarolRegra = tipoProcesso.getPermissoeseEnvioFarolRegra();

		for(ProcessoRegra pr : processoRegras) {

			Regra regra = pr.getRegra();
			Integer decisaoFluxo = regra.getDecisaoFluxo();
			boolean isDecisaoFluxo = new Integer(1).equals(decisaoFluxo);
			if(isDecisaoFluxo) {
				continue;
			}
			FarolRegra farol = pr.getFarol();

			if(!permissoeseEnvioFarolRegra.contains(farol.name())) {
				throw new ProcessoRestException("farolRegra-envioProcesso.error", DummyUtils.capitalize(farol.name()), regra.getNome());
			}
		}

		//criar regras agendadas da situação
		processoRegraService.criarRegras(processo, situacaoInicial, usuario, false, true);
	}

	private List<ProcessoRegra> getLastsRegrasVerificarEnvio(ProcessoRegraFiltro filtro, Processo processo) {
		List<ProcessoRegra> lasts = processoRegraService.findLasts(filtro);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Situacao situacaoInicialAnalise = tipoProcesso.getSituacaoInicial();
		Situacao situacaoInicial = situacaoService.getFirstByTipoProcesso(tipoProcessoId);

		List<ProcessoRegra> lasts2 = new ArrayList<>();
		for(ProcessoRegra last : lasts) {
			Regra regra = last.getRegra();
			Situacao situacaoRegra = regra.getSituacao();
			TipoExecucaoRegra tipoExecucao = regra.getTipoExecucao();

			if(situacaoInicial.equals(situacaoRegra) || (situacaoInicialAnalise.equals(situacaoRegra) && TipoExecucaoRegra.IMEDIATA.equals(tipoExecucao))) {
				lasts2.add(last);
			}
		}
		return lasts2;
	}

	private List<ProcessoRegra> getLastsRegrasComErroOuPendentes(ProcessoRegraFiltro filtro, Processo processo) {
		List<ProcessoRegra> lasts = getLastsRegrasVerificarEnvio(filtro, processo);
		List<ProcessoRegra> lasts2 = new ArrayList<>();
		for(ProcessoRegra last : lasts) {
			StatusProcessoRegra status = last.getStatus();
			if(Arrays.asList(StatusProcessoRegra.PENDENTE, StatusProcessoRegra.ERRO).contains(status)) {
				lasts2.add(last);
			}
		}
		return lasts2;
	}

	private void atualizarSituacaoInicialAnalise(Processo processo, Usuario usuario, TipoProcesso tipoProcesso, HorasUteisCalculator huc) throws Exception {

		Situacao situacaoOld = processo.getSituacao();
		Situacao situacaoInicial = tipoProcesso.getSituacaoInicial();
		Long tipoProcessoId = tipoProcesso.getId();
		Long processoId = processo.getId();

		boolean validarOCR = configuracaoOCRService.isFluxoAprovacaoTipificacaoAtivo(processoId, tipoProcessoId);
		if (validarOCR) {
			situacaoInicial = situacaoService.getByNome(tipoProcessoId, Situacao.AGUARDANDO_TIPIFICACAO);
		}

		processo.setSituacao(situacaoInicial);

		StatusProcesso statusProcesso = situacaoInicial.getStatus();
		processo.setStatus(statusProcesso);

		ProcessoLog processoLog = new ProcessoLog();
		processoLog.setData(new Date());
		BigDecimal horasPrazoSituacao = situacaoInicial.getHorasPrazo();
		if(horasPrazoSituacao != null) {
			Date data = processoLog.getData();
			Date prazoLimiteSituacao = huc.addHoras(data, horasPrazoSituacao);
			processo.setPrazoLimiteSituacao(prazoLimiteSituacao);
			processoLog.setPrazoLimiteSituacao(prazoLimiteSituacao);
			processoLog.setHorasPrazoSituacao(horasPrazoSituacao);
		}

		Situacao situacao = processo.getSituacao();

		atualizaEtapaNoLog(situacao, processoLog);

		processoLog.setAcao(AcaoProcesso.ALTERACAO_SITUACAO);
		processoLog.setSituacao(situacao);
		processoLog.setSituacaoAnterior(situacaoOld);
		processoLog.setUsuario(usuario);
		processoLog.setObservacao("para: " + processo.getSituacao().getNome());
		processoLog.setStatusProcesso(processo.getStatus());
		processoLog.setProcesso(processo);
		processoLogService.saveOrUpdate(processoLog);
	}

	public Date calculaPrazoLimiteEtapa(Situacao situacao, Date date) {

		Date prazoLimiteEtapa = null;

		Etapa etapa = situacao.getEtapa();
		etapa = etapa != null ? etapaService.get(etapa.getId()) : null;

		if(etapa != null && !etapa.getEtapaFinal()) {
			Long situacaoId = situacao.getId();
			HorasUteisCalculator huc = buildHUC(situacaoId);
			BigDecimal horasPrazo = etapa.getHorasPrazo();
			prazoLimiteEtapa = huc.addHoras(date, horasPrazo);

			prazoLimiteEtapa = recalcularPrazoLimiteEtapa(date, prazoLimiteEtapa, etapa, huc);
		}

		return prazoLimiteEtapa;
	}

	@Transactional(rollbackFor = Exception.class)
	public void iniciarAnalise(Processo processo, Usuario analista) throws Exception {

		if(!analista.isAnalistaRole()) {
			return;
		}

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(status)) {
			enviarParaAnalise(processo, analista);
		}
		else if(!StatusProcesso.AGUARDANDO_ANALISE.equals(status)) {
			return;
		}

		processo.setAnalista(analista);
		processoRepository.saveOrUpdate(processo);

		processoLogService.criaLog(processo, analista, AcaoProcesso.INICIO_ANALISE);
		ProcessoLog log1 = processoLogService.criaLog(processo, analista, AcaoProcesso.REGISTRO_ANALISTA);

		Long analistaId = analista.getId();
		String analistaNome = analista.getNome();
		log1.setObservacao("Analista: " + analistaNome + " #" + analistaId);

		processoLogService.saveOrUpdate(log1);

		Long processoAtualId2 = processo.getId();
		usuarioService.atualizarProcessoAtualId(analista, processoAtualId2, true);
	}

	public Processo proximoProcessoComLock(Usuario usuario) throws Exception {

		long inicio = System.currentTimeMillis();

		Processo proximoProcesso;

		List<UsuarioSubperfil> subperfisUsuario = usuarioSubperfilService.findByAnalista(usuario.getId());
		List<Subperfil> subperfis = subperfisUsuario.stream().map(UsuarioSubperfil::getSubperfil).collect(toList());

		boolean isLockEnabled = isProximoProcessoLockEnabled();
		if (isLockEnabled) {

			try {
				proximoProcesso = bulkhead.executeCallable(buscarProximoProcessoComLock(usuario, subperfis));
			}
			catch (BulkheadFullException bfe) {

				String msg = "Erro ao buscar próximo processo. Bulkhead atingiu o limite devido muitos acessos simultâneos.";
				systraceThread(msg, LogLevel.ERROR);
				bfe.printStackTrace();
				emailSmtpService.enviarEmailException(msg, bfe);

				throw new MessageKeyException("proximoProcessoBuscasSimultaneas.error");
			}
		}
		else {
			proximoProcesso = proximoProcessoService.buscar(usuario, subperfis);
		}

		if(proximoProcesso == null) {
			enviarEmailAnalistaSemDemanda(usuario);
		}

		systraceThread("Tempo total=" + (System.currentTimeMillis() - inicio) + "ms. Lock habilitado=" + isLockEnabled);
		return proximoProcesso;
	}

	private void enviarEmailAnalistaSemDemanda(Usuario usuario) {

		Date dataUltimaNotificacaoSemDemanda = usuario.getDataUltimaNotificacaoSemDemanda();
		if(dataUltimaNotificacaoSemDemanda != null) {
			long time1 = dataUltimaNotificacaoSemDemanda.getTime();
			long time2 = new Date().getTime();
			long tempo = time2 - time1;
			long tempoMin = tempo / 1000 / 60;
			Integer minutos = parametroService.getValor(P.INTERVALO_NOTIFICACAO_SEM_DEMANDA, Integer.class);
			if(minutos != null && tempoMin < minutos) {
				return;
			}
		}

		UsuarioFiltro usuarioFiltro = new UsuarioFiltro();
		usuarioFiltro.setRole(RoleGD.GD_GESTOR);
		usuarioFiltro.setEmailLike("neobpo.com.br");
		List<Usuario> usuarios = usuarioService.findByFiltro(usuarioFiltro);
		Map<String, String> emailsGestoresNeo = new LinkedHashMap<>();
		for (Usuario u : usuarios) {
			String nome = u.getNome();
			String email = u.getEmail();
			emailsGestoresNeo.put(email, nome);
		}

		if(DummyUtils.isDev() || DummyUtils.isHomolog()) {
			emailsGestoresNeo.clear();
			String emailPadrao = parametroService.getValor(P.EMAIL_PADRAO);
			emailsGestoresNeo.put(emailPadrao, DummyUtils.getMode());
		}

		//FIXME temporariamente mandando email apenas para a Patrícia
		emailsGestoresNeo.clear();
		emailsGestoresNeo.put("karina.kobayashi@neobpo.com.br", "Karina Kobayashi");
		emailsGestoresNeo.put("ruhan.eliseo@neobpo.com.br", "Ruhan Eliseo da Silva");
		emailsGestoresNeo.put("luciano.ferreira@neobpo.com.br", "Luciano Ferreira");

		emailSmtpService.enviarEmailAnalistaSemDemanda(usuario, emailsGestoresNeo);

		usuarioService.atualizarDataUltimaNotificacaoSemDemanda(usuario, new Date());
	}

	private boolean isProximoProcessoLockEnabled() {

		String habilitarLockProximoProcesso = parametroService.getValorCache(P.HABILITAR_LOCK_PROXIMO_PROCESSO);
		return isNotBlank(habilitarLockProximoProcesso) && Boolean.parseBoolean(habilitarLockProximoProcesso);
	}

	private Callable<Processo> buscarProximoProcessoComLock(Usuario usuario, List<Subperfil> subperfis) {
		return () -> {

			Processo result = null;

			systraceThread(bulkhead.metricsToString());
			try {
				result = proximoProcessoService.buscarComLock(usuario, subperfis);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			return result;
		};
	}

	@Transactional(rollbackFor = Exception.class)
	public void avancarProcesso(Long processoOldId, Usuario usuario) {

		Processo processo = get(processoOldId);
		processoLogService.criaLog(processo, usuario, AcaoProcesso.AVANCOU_PROCESSO);
	}

	public boolean isAcessoBloqueado(Long processoId, Usuario usuario) {

		if(usuario.isAnalistaRole() || usuario.isGestorRole()) {
			return isAcessoBloqueadoAnalistaGestor(processoId, usuario);
		}
		else if(usuario.isAreaRole()) {
			return isAcessoBloqueadoArea(processoId, usuario);
		}
		else if(usuario.isComercialRole()) {
			return isAcessoBloqueadoComercial(processoId, usuario);
		}
		else if(usuario.isRequisitanteRole()) {
			return isAcessoBloqueadoRequisitante(processoId, usuario);
		}

		return false;
	}

	private boolean isAcessoBloqueadoRequisitante(Long processoId, Usuario usuario) {

		Processo processo = get(processoId);
		Usuario autor = processo.getAutor();
		return !usuario.equals(autor);
	}

	private boolean isAcessoBloqueadoArea(Long processoId, Usuario usuario) {

		Processo processo = get(processoId);
		Usuario autor = processo.getAutor();
		if(usuario.equals(autor)) {
			return false;
		}

		Area area = usuario.getArea();
		Long areaId = area.getId();

		boolean exists = solicitacaoService.existsByAreaProcesso(processoId, areaId);
		return !exists;
	}

	private boolean isAcessoBloqueadoAnalistaGestor(Long processoId, Usuario usuario0) {

		Processo processo = get(processoId);
		Long usuarioId = usuario0.getId();
		Usuario usuario = usuarioService.get(usuarioId);

		if(usuario.isAnalistaRole()){
			boolean ordemAtividadesFixa = usuario.getOrdemAtividadesFixa();
			if(!ordemAtividadesFixa) {
				return false;
			}

			Usuario autor = processo.getAutor();
			if(usuario.equals(autor)) {
				return false;
			}

			Long processoAtualId = usuario.getProcessoAtualId();

			if(processoAtualId != null && processoAtualId.equals(processoId)) {
				Situacao situacao = processo.getSituacao();
				Long situacaoId = situacao.getId();
				boolean atendeSituacao = usuarioService.atendeSituacao(usuarioId, situacaoId, processo);
				if(!atendeSituacao) {
					usuarioService.atualizarProcessoAtualId(usuario, null, true);
					processoAtualId = null;
					usuario0.setProcessoAtualId(null);
					usuario0.setDataProcessoAtual(null);
				}
			}

			boolean podeTrocarProcessoAtual = usuario.getPodeTrocarProcessoAtual();
			if(processoAtualId != null && !podeTrocarProcessoAtual) {
				return !processoId.equals(processoAtualId);
			}
		}

		List<Long> regionais = usuarioService.findRegionais(usuario);
		List<Long> campus = usuarioService.findCampus(usuario);
		String regional = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.REGIONAL);
		regional = DummyUtils.limparCharsChaveUnicidade(regional);
		String cp = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CAMPUS);
		cp = DummyUtils.limparCharsChaveUnicidade(cp);
		if (isNotBlank(regional) && !"null".equals(regional) && !regionais.isEmpty() && !regionais.contains(new Long(regional))) {
			return true;
		}
		if (isNotBlank(cp) && isNumeric(cp) && !campus.isEmpty() && !campus.contains(new Long(cp))) {
			return true;
		}

		ProcessoFiltro filtro = proximoProcessoService.getFiltroProximo(usuario);

		int count = processoRepository.countByFiltro(filtro);
		return count > 0;
	}

	public boolean isAcessoBloqueadoComercial(Long processoId, Usuario usuario) {
		Processo processo = get(processoId);
		Usuario autor = processo.getAutor();
		if(usuario.equals(autor)) {
			return false;
		}

		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	public void verificarFluxoTrabalho(Processo processo, Usuario usuario, AcaoProcesso acao) throws Exception {

		Long processoId = processo.getId();

		if(usuario == null || !usuario.isAnalistaRole()) {
			Usuario processoAnalista = processo.getAnalista();
			if(processoAnalista != null) {
				Long processoAtualId = processoAnalista.getProcessoAtualId();
				if(processoAtualId != null && processoAtualId.equals(processoId)) {
					usuarioService.atualizarProcessoAtualId(processoAnalista, null, true);
				}
			}
			return;
		}

		Long processoAtualId = usuario.getProcessoAtualId();
		if(!processoId.equals(processoAtualId)) {
			return;
		}

		processoRepository.atualizarUltimaAcao(processoId, new Date());

		if(acao == null) {
			usuarioService.atualizarProcessoAtualId(usuario, processoAtualId, true);
		}
		else {
			switch(acao) {
				case EM_ACOMPANHAMENTO:
					usuarioService.atualizarProcessoAtualId(usuario, null, true);
					break;
				case CONCLUSAO:
					usuarioService.atualizarProcessoAtualId(usuario, null, true);
					break;
				case ALTERACAO_SITUACAO:
					Long usuarioId = usuario.getId();
					Situacao situacao = processo.getSituacao();
					Long situacaoId = situacao.getId();
					boolean atendeProximaSituacao = usuarioService.atendeSituacao(usuarioId, situacaoId, processo);
					if(atendeProximaSituacao) {
						usuarioService.atualizarProcessoAtualId(usuario, processoAtualId, true);
					} else {
						usuarioService.atualizarProcessoAtualId(usuario, null, true);
					}
					break;
				case ENVIO_PENDENCIA:
					usuarioService.atualizarProcessoAtualId(usuario, null, true);
					break;
				case ATUALIZACAO_CAMPOS:
					usuarioService.atualizarProcessoAtualId(usuario, processoAtualId, false);
					break;
				case DIGITALIZOU_DOCUMENTOS:
					usuarioService.atualizarProcessoAtualId(usuario, processoAtualId, false);
					break;
				default:
					usuarioService.atualizarProcessoAtualId(usuario, processoAtualId, true);
			}
		}
	}

	public void notificarAtrasosAnalistas() {

		Date dataCorte = new Date();
		List<Processo> processos = processoRepository.findAtrasosAnalistas(dataCorte);

		if(processos.isEmpty()) {
			systraceThread("nenhum processo atrasado.");
		}

		List<ProcessoVO> vos = buildVOs(processos, null, null);

		Map<Usuario, List<ProcessoVO>> map = new HashMap<>();
		for(ProcessoVO vo : vos) {

			Processo processo = vo.getProcesso();
			Usuario analista = processo.getAnalista();
			List<ProcessoVO> list = map.get(analista);
			list = list != null ? list : new ArrayList<ProcessoVO>();
			map.put(analista, list);

			list.add(vo);
		}

		Set<Usuario> analistas = map.keySet();
		for(Usuario analista : analistas) {
			if(analista != null) {
				List<ProcessoVO> list = map.get(analista);
				if(!list.isEmpty() && analista.getNotificarAtrasoRequisicoes()) {
					emailSmtpService.enviarNotificacaoAtrasoAnalista(analista, list);
				}
			}
		}
	}

	public void notificarAtrasosGestores() {

		UsuarioFiltro filtro = new UsuarioFiltro();
		filtro.setStatus(StatusUsuario.ATIVO);
		filtro.setRole(RoleGD.GD_GESTOR);
		filtro.setNotificarAtrasoRequisicoes(true);
		List<Usuario> gestores = usuarioService.findByFiltro(filtro);

		if(gestores.isEmpty()) {
			systraceThread("ProcessoService.notificarAtrasosGestores() nenhum gestor habilitado para receber notificações de atraso.");
			return;
		}

		Date dataCorte = new Date();
		List<Processo> processos = processoRepository.findAtrasosAnalistas(dataCorte);

		if(processos.isEmpty()) {
			systraceThread("ProcessoService.notificarAtrasosGestores() nenhum processo atrasado.");
			return;
		}

		List<ProcessoVO> vos = buildVOs(processos, null, null);

		emailSmtpService.enviarNotificacaoAtrasoGestores(gestores, vos);
	}

	@Transactional(rollbackFor = Exception.class)
	public void trocarAnalistas(List<Long> processosIds, Usuario novoAnalista, Usuario usuario) {

		for(Long processoId : processosIds) {

			Processo processo = get(processoId);

			trocarAnalista(processo, novoAnalista, usuario);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void trocarAnalista(Processo processo, Usuario novoAnalista, Usuario usuario) {

		Usuario analistaOld = processo.getAnalista();

		if(analistaOld != null) {

			analistaOld.setPodeTrocarProcessoAtual(true);
			analistaOld.setProcessoAtualId(null);
			usuario.setDataProcessoAtual(null);
			usuarioService.atualizarProcessoAtualId(analistaOld, null, true);

			Long analistaId = novoAnalista.getId();
			String analistaNome = novoAnalista.getNome();

			Long analistaOldId = analistaOld.getId();
			String analistaOldNome = analistaOld.getNome();

			String descricaoLog = "De: " + analistaOldNome + " #" + analistaOldId + "\npara: " + analistaNome + " #" + analistaId;

			ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ATUALIZACAO_ANALISTA);

			log.setObservacao(descricaoLog);
			processoLogService.saveOrUpdate(log);
		}
		else {

			StatusProcesso status = processo.getStatus();
			if(StatusProcesso.AGUARDANDO_ANALISE.equals(status)) {
				processo.setStatus(StatusProcesso.EM_ANALISE);
			}

			processoLogService.criaLog(processo, usuario, AcaoProcesso.REGISTRO_ANALISTA);
		}

		processo.setAnalista(novoAnalista);
		processo.setDataUltimaAcaoAnalista(null);
		processoRepository.saveOrUpdate(processo);
	}

	@Transactional(rollbackFor = Exception.class)
	public void desvincularAnalistaProcesso(Usuario analista, Usuario usuario) {

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setAnalista(analista);
		filtro.setStatusList(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE));
		List<Processo> processos = findByFiltro(filtro, 0, 0);

		for(Processo processo : processos) {

			Usuario analistaOld = processo.getAnalista();

			if (analistaOld != null) {

				usuarioService.atualizarProcessoAtualId(analistaOld, null, true);

				String analistaOldId = analistaOld.getLogin();
				String analistaOldNome = analistaOld.getNome();

				String descricaoLog = "De: " + analistaOldNome + " #" + analistaOldId + "\npara: Vazio #Distribuição Automática";

				ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ATUALIZACAO_ANALISTA);

				log.setObservacao(descricaoLog);
				processoLogService.saveOrUpdate(log);
			}

			processo.setAnalista(null);
			processo.setDataUltimaAcaoAnalista(null);
			processoRepository.saveOrUpdate(processo);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo trocarTipoProcesso(Processo processo, TipoProcesso novoTipoProcesso, Usuario usuario) throws Exception {

		TipoProcesso tipoProcessoOld = processo.getTipoProcesso();
		processo.setTipoProcesso(novoTipoProcesso);

		Long usuarioId = usuario.getId();
		Long novoTipoProcessoId = novoTipoProcesso.getId();
		if(usuario.isAnalistaRole() && !usuarioTipoProcessoService.usuarioAtendeTipoProcesso(usuarioId, novoTipoProcessoId)) {

			processo.setStatus(StatusProcesso.AGUARDANDO_ANALISE);
			processo.setAnalista(null);
			processo.setDataUltimaAcaoAnalista(null);
			processoRepository.saveOrUpdate(processo);

			proximoProcessoComLock(usuario);
		}

		String tipoProcessoNome = novoTipoProcesso.getNome();
		String tipoProcessoOldNome = tipoProcessoOld.getNome();

		String descricaoLog = "De: " + tipoProcessoOldNome + "\npara: " + tipoProcessoNome;

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ATUALIZACAO_TIPO_PROCESSO);

		log.setObservacao(descricaoLog);
		processoLogService.saveOrUpdate(log);

		processoRepository.saveOrUpdate(processo);

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ATUALIZACAO_TIPO_PROCESSO);

		return processo;
	}

	private ProcessoLog trocarSituacao2(Processo processo, Situacao novaSituacao, Usuario usuario) throws Exception {

		Situacao situacaoOld = processo.getSituacao();
		String situacaoNome = novaSituacao.getNome();

		String descricaoLog = "";
		if(situacaoOld != null) {
			String situacaoOldNome = situacaoOld.getNome();
			descricaoLog = "De: " + situacaoOldNome + "\npara: ";
		}

		descricaoLog += situacaoNome;

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ALTERACAO_SITUACAO);
		boolean verificarLogAtendimento = usuario != null ? usuario.isAnalistaRole() : false;
		if(verificarLogAtendimento) {
			LogAtendimento ultimoLogAtendimento = logAtendimentoService.getUltimoLogAtendimentoByAnalistaAndProcesso(usuario, processo);
			if (ultimoLogAtendimento != null) {
				logAtendimentoService.atualizarSituacaoFinal(ultimoLogAtendimento, novaSituacao);
			}
		}

		log.setStatusProcesso(null);
		log.setObservacao(descricaoLog);
		processoLogService.saveOrUpdate(log);

		processo.setSituacao(novaSituacao);
		processoRepository.saveOrUpdate(processo);

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ALTERACAO_SITUACAO);

		return log;
	}

	@Transactional(rollbackFor = Exception.class)
	public void adicionarDocumento(Processo processo, List<Long> novosDocumentoId, Usuario usuario) throws Exception {
		documentoService.adicionarDocumento(novosDocumentoId, usuario);
		Long id = processo.getId();
		processo = get(id);
		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.INCLUSAO_DOCUMENTO);
	}

	@Transactional(rollbackFor = Exception.class)
	public void excluirDocumento(Processo processo, Documento documento, Usuario usuario) throws Exception {

		ImagemTransaction imagemTransaction = new ImagemTransaction();

		documentoService.excluirDocumento(imagemTransaction, documento, usuario);

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.EXCLUSAO_DOCUMENTO);
		imagemTransaction.commit();
	}

	@Transactional(rollbackFor = Exception.class)
	public void marcarEmailRecebidoComoLido(Processo processo, EmailRecebido emailRecebido, Usuario usuario) {

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.LEU_EMAIL_RECEBIDO);

		String emailFrom = emailRecebido.getEmailFrom();
		Date sentDate = emailRecebido.getSentDate();
		String sentDateStr = DummyUtils.formatDateTime(sentDate);
		Long emailRecebidoId = emailRecebido.getId();
		log.setObservacao("De: " + emailFrom + " " + sentDateStr + " #" + emailRecebidoId);

		processoLogService.saveOrUpdate(log);

		emailRecebido.setDataLeitura(new Date());
		emailRecebidoService.saveOrUpdate(emailRecebido);
	}

	@Transactional(rollbackFor = Exception.class)
	public void marcarEmailRecebidoComoNaoLido(Processo processo, EmailRecebido emailRecebido, Usuario usuario) {

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.MARCOU_EMAIL_RECEBIDO_NAO_LIDO);

		String emailFrom = emailRecebido.getEmailFrom();
		Date sentDate = emailRecebido.getSentDate();
		String sentDateStr = DummyUtils.formatDateTime(sentDate);
		Long emailRecebidoId = emailRecebido.getId();
		log.setObservacao("De: " + emailFrom + " " + sentDateStr + " #" + emailRecebidoId);

		processoLogService.saveOrUpdate(log);

		emailRecebido.setDataLeitura(null);
		emailRecebidoService.saveOrUpdate(emailRecebido);
	}

	@Transactional(rollbackFor = Exception.class)
	public void subirNivelPrioridade(Processo processo, Usuario usuario) throws Exception {

		Integer nivelPrioridade = processo.getNivelPrioridade();
		processo.setNivelPrioridade(nivelPrioridade + 1);
		processoRepository.saveOrUpdate(processo);

		processoLogService.criaLog(processo, usuario, AcaoProcesso.PRIORIZAR_PROCESSO);

		verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ATUALIZACAO_TIPO_PROCESSO);
	}

	public Integer countAbertosByCpfCnpj(Processo processo) {
		Aluno aluno = processo.getAluno();

		if (aluno == null) return 0;

		String cpfCnpj = aluno.getCpf();
		if(Aluno.CPF_ALUNO_GENERICO.equals(cpfCnpj)) {
			return 0;
		}
		return processoRepository.countStatusByCpfCnpj(cpfCnpj, StatusProcesso.getStatusEmAndamento());
	}

	public Integer countFechadosByCpfCnpj(Processo processo) {
		Aluno aluno = processo.getAluno();

		if (aluno == null) return 0;

		String cpfCnpj = aluno.getCpf();
		if(aluno == null || Aluno.CPF_ALUNO_GENERICO.equals(cpfCnpj)) {
			return 0;
		}
		return processoRepository.countStatusByCpfCnpj(cpfCnpj, StatusProcesso.getStatusFechado());
	}

	public TempoStatusVO getTempoAguardandoAnalise(Processo processo) {
		return getTempoStatus(processo, StatusProcesso.AGUARDANDO_ANALISE);
	}

	public TempoStatusVO getTempoRascunho(Processo processo) {
		return getTempoStatus(processo, StatusProcesso.RASCUNHO);
	}

	public TempoStatusVO getTempoPendente(Processo processo) {
		return getTempoStatus(processo, StatusProcesso.PENDENTE);
	}

	public TempoStatusVO getTempoEmAnalise(Processo processo) {
		return getTempoStatus(processo, StatusProcesso.EM_ANALISE);
	}

	public TempoStatusVO getTempoEmAcompanhamento(Processo processo) {
		return getTempoStatus(processo, StatusProcesso.EM_ACOMPANHAMENTO);
	}

	public BigDecimal getTempoAteFinalizacao(Processo processo) {

		Date dataFinalizacao = processo.getDataFinalizacao();
		if(dataFinalizacao == null) {
			return null;
		}

		Long processoId = processo.getId();
		Date data = processoLogService.getDataPrimeiroEnvioAnalise(processoId);
		if(data == null) {
			return null;
		}

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		BigDecimal horas = huc.getHoras(data, dataFinalizacao);
		return horas;
	}

	public BigDecimal getTempoAteFinalizacaoAnalise(Processo processo) {

		Date dataFinalizacaoAnalise = processo.getDataFinalizacaoAnalise();
		if(dataFinalizacaoAnalise == null) {
			return null;
		}

		Long processoId = processo.getId();
		Date data = processoLogService.getDataPrimeiroEnvioAnalise(processoId);
		if(data == null) {
			return null;
		}

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		BigDecimal horas = huc.getHoras(data, dataFinalizacaoAnalise);
		return horas;
	}

	public BigDecimal getTempoSlaCriacao(Processo processo) {

		Long processoId = processo.getId();
		Date dataEnvioAnalise = processoLogService.getDataPrimeiroEnvioAnalise(processoId);
		if(dataEnvioAnalise == null) {
			return null;
		}

		Date dataFinalizacao = processo.getDataFinalizacao();
		if(dataFinalizacao == null) {
			dataFinalizacao = new Date();
		}

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		BigDecimal horas = huc.getHoras(dataEnvioAnalise, dataFinalizacao);
		return horas;
	}

	public BigDecimal getTempoSlaTratativa(Processo processo) {

		Long processoId = processo.getId();
		ProcessoLog lastLog = processoLogService.findLastLogByProcesso(processoId, null);
		Date dataUltimoLog = lastLog != null ? lastLog.getData() : null;

		Date dataFinalizacao = processo.getDataFinalizacao();
		if(dataFinalizacao == null) {
			dataFinalizacao = new Date();
		}

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		BigDecimal horas = dataUltimoLog != null ? huc.getHoras(dataUltimoLog, dataFinalizacao) : null;
		return horas;
	}

	private TempoStatusVO getTempoStatus(Processo processo, StatusProcesso status) {

		TempoStatusVO vo = new TempoStatusVO();
		Long processoId = processo.getId();
		List<ProcessoLog> logs = processoLogService.findStatusByProcesso(processoId);

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		Date dataInicio = null;

		for(ProcessoLog log : logs) {

			StatusProcesso statusProcesso = log.getStatusProcesso();
			Date data = log.getData();

			if(status.equals(statusProcesso) && dataInicio == null) {
				dataInicio = data;
			}

			if(dataInicio != null && !status.equals(statusProcesso)) {

				Date dataFim = data;
				BigDecimal horas = huc.getHoras(dataInicio, dataFim);
				vo.addTempo(horas);
				dataInicio = null;
			}
		}

		return vo;
	}

	public Date getLastDataStatus(Processo processo, Date dataMinima, StatusProcesso status) {
		ProcessoLog log = getLogStatus(processo, dataMinima, status, false);
		return log != null ? log.getData() : null;
	}

	public Date getFirstDataStatus(Processo processo, Date dataMinima, StatusProcesso status) {
		ProcessoLog log = getLogStatus(processo, dataMinima, status, true);
		return log != null ? log.getData() : null;
	}

	public ProcessoLog getFirstLogStatus(Processo processo, Date dataMinima, StatusProcesso status) {
		ProcessoLog log = getLogStatus(processo, dataMinima, status, true);
		return log;
	}

	private ProcessoLog getLogStatus(Processo processo, Date dataMinima, StatusProcesso status, boolean first) {

		Long processoId = processo.getId();
		List<ProcessoLog> logs = processoLogService.findStatusByProcesso(processoId);

		StatusProcesso ultimoStatus = null;
		ProcessoLog logStatus = null;

		for(ProcessoLog log : logs) {

			StatusProcesso statusProcesso = log.getStatusProcesso();
			Date data = log.getData();

			if(dataMinima != null && data.before(dataMinima)) {
				continue;
			}

			if(status.equals(statusProcesso) && !statusProcesso.equals(ultimoStatus)) {

				logStatus = log;

				if(first) {
					return logStatus;
				}
			}

			ultimoStatus = statusProcesso;
		}

		return logStatus;
	}

	public Date getDataFinalizacaoAnalise(Processo processo) {

		Date dataEnvioAnalise = processo.getDataEnvioAnalise();
		if(dataEnvioAnalise == null) return null;
		Long processoId = processo.getId();
		List<ProcessoLog> logs = processoLogService.findStatusByProcesso(processoId);

		for(ProcessoLog log : logs) {

			Date data = log.getData();
			if(data.after(dataEnvioAnalise)) {

				AcaoProcesso acao = log.getAcao();
				if(AcaoProcesso.ENVIO_PENDENCIA.equals(acao)) {
					return data;
				}
				if(Arrays.asList(AcaoProcesso.CONCLUSAO, AcaoProcesso.CANCELAMENTO, AcaoProcesso.EM_ACOMPANHAMENTO).contains(acao)) {
					return data;
				}
			}
		}

		return null;
	}

	public Date getDataFinalizacaoEmAcompanhamento(Long processoId, Date dataEmAcompanhamento) {

		if(dataEmAcompanhamento == null) return null;
		List<ProcessoLog> logs = processoLogService.findStatusByProcesso(processoId);

		for(ProcessoLog log : logs) {

			Date data = log.getData();
			if(data.after(dataEmAcompanhamento)) {

				AcaoProcesso acao = log.getAcao();
				String observacao = log.getObservacao();
				if(AcaoProcesso.ALTERACAO_SITUACAO.equals(acao) && observacao.contains(Situacao.ANALISE_ISENCAO_CONCLUIDA)){
					return data;
				}
				if(AcaoProcesso.ENVIO_PENDENCIA.equals(acao)) {
					return data;
				}
				if(Arrays.asList(AcaoProcesso.CONCLUSAO, AcaoProcesso.CANCELAMENTO, AcaoProcesso.ENVIO_ANALISE).contains(acao)) {
					return data;
				}
			}
		}

		return null;
	}

	public ItemPendente getItemPendenteResponderPendencia(Processo processo, Usuario usuario) {

		List<Long> processosIds = findProcessosIdsRelacionados(processo);

		boolean temPendente = documentoService.temPendente(processosIds);
		if(temPendente) {
			return ItemPendente.PENDENCIA_DOCUMENTO;
		}

		Object roleGD = usuario == null ? null : usuario.getRoleGD();
		if(roleGD == null) {

			boolean temPendenteDigitalizado = documentoService.temPendenteDigitalizado(processosIds);
			if(temPendenteDigitalizado) {
				return ItemPendente.DIGITALIZACAO;
			}
		}

		return null;
	}

	public ItemPendente getItemPendenteEnviarProcesso(Processo processo, Usuario usuario) {

		RoleGD roleGD = usuario == null ? null : usuario.getRoleGD();
		if(roleGD == null) {

			List<Long> processosIds = findProcessosIdsRelacionados(processo);
			boolean temPendenteDigitalizado = documentoService.temPendenteDigitalizado(processosIds);
			if(temPendenteDigitalizado) {
				return ItemPendente.DIGITALIZACAO;
			}
		}

		return null;
	}

	public List<Long> findProcessosIdsRelacionados(Processo processo){

		List<Long> processosId = new ArrayList<>();
		Long processoId = processo.getId();
		processosId.add(processoId);
		return processosId;
	}

	public ItemPendente getItemPendenteConclusao(Processo processo, Usuario usuario) {
		return null;
	}

	public Map<StatusProcesso, Long> getStatusAnalise(Usuario usuario, ProcessoFiltro filtro) {

		if(!usuario.isAdminRole() && !usuario.isGestorRole() && !usuario.isAnalistaRole()) {
			return null;
		}

		if (usuario.isAnalistaRole()) {
			boolean distribuirDemandaAutomaticamente = usuario.getDistribuirDemandaAutomaticamente();
			if (distribuirDemandaAutomaticamente) {
				return null;
			}
		}

		Map<StatusProcesso, Long> map = processoRepository.countStatusByFiltro(filtro);
		return map;
	}

	public List<Processo> testeCemProcessos() {
		return processoRepository.testeCemProcessos();
	}

	public void atualizarStatusPrazo() {

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setStatusList(StatusProcesso.getStatusEmAndamento());

		List<Long> ids = processoRepository.findIdsByFiltro(filtro);

		do {
			final List<Long> ids2 = new ArrayList<Long>();
			for(int i = 0; i < 50 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				ProcessoService processoService = applicationContext.getBean(ProcessoService.class);
				processoService.atualizarStatusPrazo(ids2);

				systraceThread("atualizando... restam " + ids.size(), LogLevel.INFO);
			});
			tw.runNewThread();

			Exception exception = tw.getException();
			if(exception != null) {
				emailSmtpService.enviarEmailException("Erro ao atualizar statusPrazo", exception);
			}
		}
		while(!ids.isEmpty());
	}

	@Transactional(rollbackFor = Exception.class)
	public void atualizarStatusPrazo(List<Long> ids2) {

		List<Processo> list = processoRepository.findByIds(ids2);
		int count = 0;

		for(Processo processo : list) {

			StatusPrazo statusPrazoOld = processo.getStatusPrazo();
			StatusPrazo statusPrazo = getStatusPrazo(processo);
			count++;

			if(!statusPrazo.equals(statusPrazoOld)) {
				processoRepository.atualizarStatusPrazo(processo.getId(), statusPrazo);
			}

			if(count % 1000 == 0) {
				systraceThread(count + " processos alterado o StatusPrazo.", LogLevel.INFO);
			}
		}

		processoRepository.flush();
	}

	@Transactional(rollbackFor = Exception.class)
	public void respostaEncaminhamento(Processo processo, Usuario usuario, Solicitacao solicitacao) throws Exception {

		Situacao situacaoAtual = processo.getSituacao();
		if(situacaoAtual.isEncaminhado()) {

			Long processoId = processo.getId();
			Long solicitacaoId = solicitacao.getId();
			Map<Long, String> areasPendentesStr = solicitacaoService.getAreasPendentesStr(Arrays.asList(processoId), null, solicitacaoId);
			if(areasPendentesStr.isEmpty()) {

				Situacao situacaoAnterior = null;
				Situacao situacaoRetorno = situacaoAtual.getSituacaoRetorno();

				if(situacaoRetorno != null) {
					situacaoRetorno = situacaoService.get(situacaoRetorno.getId());
					situacaoAnterior = situacaoRetorno;
				}
				else {
					List<Situacao> situacoes = processoLogService.findSituacoesProcesso(processoId);
					Collections.reverse(situacoes);

					for(Situacao situacao : situacoes) {
						StatusProcesso status = situacao.getStatus();
						if(!StatusProcesso.ENCAMINHADO.equals(status)) {
							situacaoAnterior = situacao;
							break;
						}
					}
				}

				concluirSituacao(processo, usuario, situacaoAnterior, null, null);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void respostaNaoAceite(Processo processo, Usuario usuario, Solicitacao solicitacao) throws Exception {

		Long processoId = processo.getId();
		Situacao situacaoAnterior = null;

		List<Situacao> situacoes = processoLogService.findSituacoesProcesso(processoId);
		Collections.reverse(situacoes);

		for(Situacao situacao : situacoes) {
			if(situacao.isEncaminhado()) {
				situacaoAnterior = situacao;
				break;
			}
		}

		if(situacaoAnterior != null) {
			concluirSituacao(processo, usuario, situacaoAnterior, null, null);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarStatusOcr(Processo processo) {

		StatusOcr statusOcrProcesso = StatusOcr.TUDO_OK;
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

		for(Documento documento : documentos) {
			StatusOcr statusOcr = documento.getStatusOcr();
			if(statusOcr != null) {
				if(StatusOcr.PROCESSANDO.equals(statusOcr)) {
					statusOcrProcesso = StatusOcr.PROCESSANDO;
				}
				else if(StatusOcr.ERRO.equals(statusOcr)) {
					statusOcrProcesso = StatusOcr.ERRO;
				}
				else if(StatusOcr.INCONSISTENTE.equals(statusOcr)) {
					statusOcrProcesso = StatusOcr.INCONSISTENTE;
				}
			}
		}

		processo.setStatusOcr(statusOcrProcesso);
		processoRepository.saveOrUpdate(processo);
	}

	public Boolean existPendenciaByProcesso(Processo processo) {
		StatusProcesso status = processo.getStatus();

		if(StatusProcesso.RASCUNHO.equals(status) || StatusProcesso.PENDENTE.equals(status) || StatusProcesso.CANCELADO.equals(status)) {
			return null;
		}

		Long processoId = processo.getId();
		return processoLogService.existPendenciaByProcesso(processoId);
	}

	public Boolean isProcessoOriginal(Processo processo) {
		Long processoId = processo.getId();
		return processoRepository.isProcessoOriginal(processoId);
	}

	public List<Processo> findProcessosOriginados(Processo processo) {
		Long processoId = processo.getId();
		return processoRepository.findProcessosOriginados(processoId);
	}

	public List<Object[]> findRelatorioLicenciamento(RelatorioLicenciamentoFiltro filtro) {
		return processoRepository.findRelatorioLicenciamento(filtro);
	}

	public Map<String, List<Long>> montarColunaPersonalizada(Usuario usuario) {

		Subperfil subperfil = usuario.getSubperfilAtivo();

		FilaConfiguracao filaConfiguracao;
		if (subperfil != null) {
			filaConfiguracao = subperfil.getFilaConfiguracao();
		} else {
			filaConfiguracao = filaConfiguracaoService.getPadrao();
		}

		return montarColunaPersonalizada(filaConfiguracao);
	}

	public Map<String, List<Long>> montarColunaPersonalizada(FilaConfiguracao filaConfiguracao) {

		Map<String, List<Long>> colunasPersonalizadas = new LinkedHashMap<>();
		if (filaConfiguracao != null) {
			String colunasConfigJson = filaConfiguracao.getColunas();

			if (colunasConfigJson == null){
				return colunasPersonalizadas;
			}

			List<ColunaConfigVO> colunasConfig;
			ObjectMapper mapper = new ObjectMapper();
			try {
				colunasConfig = mapper.readValue(colunasConfigJson, new TypeReference<List<ColunaConfigVO>>(){});
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			for(ColunaConfigVO colunaConfigVO : colunasConfig){
				String nomeColuna = colunaConfigVO.getNome();
				List<String> tipoCampoIdsStr = colunaConfigVO.getCampos();

				List<Long> tipoCampoIds = tipoCampoIdsStr.stream()
						.map(s -> Long.valueOf(s))
						.collect(Collectors.toList());

				colunasPersonalizadas.put(nomeColuna, tipoCampoIds);
			}
		}

		return colunasPersonalizadas;
	}

	public List<Campo> findCamposColunaPersonalizada(List<Long> tipoCampoIds, Long processoId){
		if(tipoCampoIds.size() > 0){
			return campoService.findByProcessoTipoCampos(processoId, tipoCampoIds);
		}
		return new ArrayList<>();
	}

	public String getValorBaseInternaLabel(Long baseInternaId, String chaveUnicidade) {
		return baseRegistroService.getLabel(baseInternaId, chaveUnicidade);
	}

	public List<Long> fintToNotificarAprovacao(Date dataCorte) {
		return processoRepository.fintToNotificarAprovacao(dataCorte);
	}

	public List<Long> findToNotificarPendencia() {
		return processoRepository.findToNotificarPendencia();
	}

    public List<Long> findToNotificarPendenciaRascunho(Date dataCorte) {
        return processoRepository.findToNotificarPendenciaRascunho(dataCorte);
    }

	public List<Long> findIdsProcessoParaEnvioDeEmailSisFiesAndSisProuni(List<Long> tiposProcessosIds) {
		String mode = DummyUtils.getMode();
		Integer max = Arrays.asList("dev", "homolog").contains(mode) ? 1 : null;
		return processoRepository.findIdsProcessoParaEnvioDeEmailSisFiesAndSisProuni(max, tiposProcessosIds);
	}

	public List<Long> findIdsProcessoParaEnvioDeEmailPendenteSisProuni() {
		String mode = DummyUtils.getMode();
		Integer max = Arrays.asList("dev", "homolog").contains(mode) ? 1 : null;
		return processoRepository.findIdsProcessoParaEnvioDeEmailPendenteSisProuni(max);
	}

	public List<Long> findProcessoAnaliseDeIsencaoParaEnvioDeEmail() {
		String mode = DummyUtils.getMode();
		Integer max = Arrays.asList("dev", "homolog").contains(mode) ? 1 : null;
		return processoRepository.findProcessoAnaliseDeIsencaoParaEnvioDeEmail(max);
	}

	public boolean existsProcessoDuplicado(Processo processo, Long tipoProcessoId, String numCandidato) {
		return processoRepository.existsProcessoDuplicado(processo, tipoProcessoId, numCandidato);
	}

	public void iniciarNotificacaoCadidatoSisFiesAndSisProuni(Long processoId, String email, Usuario usuario, boolean verificarTentativas) {

		Processo processo = get(processoId);
		try {
			notificarCanditadoSisFiesSisProuni(processo, email, usuario);
		}
		catch(Exception e) {
			String message = DummyUtils.getExceptionMessage(e);
			systraceThread("Erro: " + message + " processoId: " + processoId, LogLevel.ERROR);
			e.printStackTrace();
			processoLogService.criaLog(processo, null, AcaoProcesso.ERRO_ENVIO_EMAIL_NOTIFICACAO_CANDIDATO_SISFIES_SISPROUNI, message);
			if(verificarTentativas) {
				verificarNumeroDeTentativasNotificacaoCandidatoFiesProuni(processoId);
			}
		}
	}

	public void iniciarNotificacaoPendenciaCadidatoSisProuni(Long processoId, String email, Usuario usuario, boolean verificarTentativas) {

		Processo processo = get(processoId);
		try {
			notificarPendenciaSisProuni(processo, email, usuario);
		}
		catch(Exception e) {
			String message = DummyUtils.getExceptionMessage(e);
			systraceThread("Erro: " + message + " processoId: " + processoId, LogLevel.ERROR);
			e.printStackTrace();
			processoLogService.criaLog(processo, null, AcaoProcesso.ERRO_ENVIO_EMAIL_NOTIFICACAO_CANDIDATO_SISFIES_SISPROUNI, message);
			if(verificarTentativas) {
				verificarNumeroDeTentativasNotificacaoCandidatoFiesProuni(processoId);
			}
		}
	}

	private void verificarNumeroDeTentativasNotificacaoCandidatoFiesProuni(Long processoId) {

		ProcessoLogFiltro filtro = new ProcessoLogFiltro();
		filtro.setProcessoId(processoId);
		filtro.setFiltrarRoles(false);
		filtro.setAcaoList(Arrays.asList(AcaoProcesso.ERRO_ENVIO_EMAIL_NOTIFICACAO_CANDIDATO_SISFIES_SISPROUNI));
		Integer tentativas = processoLogService.countByFiltro(filtro);
		if(tentativas >= GetdocConstants.NUMERO_TENTATIVAS_NOTIFICACAO_CANDIDATO) {
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				Processo processo = get(processoId);
				Situacao situacaoAtual = processo.getSituacao();
				Set<ProximaSituacao> proximas = situacaoAtual.getProximas();
				for(ProximaSituacao proximaSituacao : proximas) {
					Situacao situacao = proximaSituacao.getProxima();
					Long situacaoId = situacao.getId();
					if(Situacao.ERRO_NOTIFICACAO_CANDIDATO_IDS.contains(situacaoId)) {
						processoLogService.criaLog(processo, null, AcaoProcesso.NOTIFICACAO_CANDIDATO_SISFIS_SISPROUNI_CANCELADO, "Excedeu o número de tentativas (" + GetdocConstants.NUMERO_TENTATIVAS_NOTIFICACAO_CANDIDATO + ")");
						Situacao novaSituacao = situacaoService.get(situacaoId);
						Processo processo1 = get(processoId);
						trocarSituacao2(processo1, novaSituacao, null);
					}
				}
			});
			tw.runNewThread();
		}
	}

	private void notificarCanditadoSisFiesSisProuni(Processo processo, String email, Usuario usuario) throws Exception {

		Long processoId = processo.getId();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();

		if(isBlank(email)) {

			email = getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL_NOTIFICACAO);
			if(StringUtils.isBlank(email)) {
				throw new MessageKeyException("nenhumEmailInformado.error");
			}

			email = verificarAmbienteParaNotificarCanditato(processoId, email);
		}

		systraceThread("Enviando de email para: " + email + " processo: " + processoId + " ambiente: " + DummyUtils.getMode());

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

		Hibernate.initialize(processo);
		String campoProcessoValor = getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO);
		String curso;
		if(StringUtils.isBlank(campoProcessoValor)) {
			curso = getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO_NOME_IMPORTACAO);
		}
		else {
			curso = getValorBaseInternaLabel(BaseInterna.CURSO_ID, campoProcessoValor);
		}

		curso = StringUtils.isBlank(curso) ? " " : curso;

		ProcessoLog log = new ProcessoLog();
		log.setProcesso(processo);
		log.setObservacao(email);
		log.setUsuario(usuario);
		log.setAcao(AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_CANDIDATO_SISFIES_SISPROUNI);

		Long tipoProcessoId = tipoProcesso.getId();
		if(TipoProcesso.SIS_FIES.equals(tipoProcessoId)) {
			emailSmtpService.enviarNotificacaoCandidatoSisFies(documentos, email, curso, log);
		}
		else {
			emailSmtpService.enviarNotificacaoCandidatoSisProuni(documentos, email, curso, log);
		}
	}

	private void notificarPendenciaSisProuni(Processo processo, String email, Usuario usuario) throws Exception {

		Long processoId = processo.getId();

		if(isBlank(email)) {

			email = getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL_NOTIFICACAO);
			if(StringUtils.isBlank(email)) {
				throw new MessageKeyException("nenhumEmailInformado.error");
			}

			email = verificarAmbienteParaNotificarCanditato(processoId, email);
		}

		systraceThread("Enviando de email para: " + email + " processo: " + processoId + " ambiente: " + DummyUtils.getMode());

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

		Hibernate.initialize(processo);
		String campoProcessoValor = getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO);
		String curso;
		if(StringUtils.isBlank(campoProcessoValor)) {
			curso = getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO_NOME_IMPORTACAO);
		}
		else {
			curso = getValorBaseInternaLabel(BaseInterna.CURSO_ID, campoProcessoValor);
		}

		curso = StringUtils.isBlank(curso) ? " " : curso;

		ProcessoLog log = new ProcessoLog();
		log.setProcesso(processo);
		log.setObservacao(email);
		log.setUsuario(usuario);
		log.setAcao(AcaoProcesso.ENVIO_EMAIL_PENDENCIA_CANDIDATO_SISPROUNI);

		emailSmtpService.enviarPendenciaCandidatoSisProuni(documentos, email, curso, log);
	}

	private String verificarAmbienteParaNotificarCanditato(Long processoId, String email) {

		String mode = getMode();
		if(Arrays.asList("dev", "homolog").contains(mode)) {
			email = parametroService.getValor(P.EMAIL_PADRAO);

			if(isBlank(email)) {
				throw new MessageKeyException("emailPadraoNaoConfigurado.error", processoId);
			}
		}

		return email;
	}

	public Map<CampoGrupo, List<Campo>> mapearCamposListParaMap(List<Campo> camposSituacao, Long processoId) {

		Map<CampoGrupo, List<Campo>> camposSituacaoMap = new LinkedHashMap<>();

		if (camposSituacao != null) {
			for (Campo campo : camposSituacao) {

				CampoGrupo grupo = campo.getGrupo();

				List<Campo> campos = camposSituacaoMap.get(grupo);
				campos = campos == null ? new ArrayList<>() : campos;

				campos.add(campo);

				camposSituacaoMap.put(grupo, campos);
			}

			List<CampoGrupo> gruposSemCampos = campoGrupoService.findGruposSemCamposByProcessoId(processoId);
			for (CampoGrupo grupoSemCampo : gruposSemCampos) {
				camposSituacaoMap.put(grupoSemCampo, new ArrayList<>());
			}
		}

		return camposSituacaoMap;
	}

	public List<String> getLocalDeOferta() {
		return campoService.getLocalDeOferta();
	}

	public List<Long> findIdsProcessosGraduacaoComDocumentosDigitalizado() {
		return processoRepository.findIdsProcessosGraduacaoComDocumentosDigitalizado();
	}

	public void enviarProcessoGraduacaoParaConferencia(Long processoId) throws Exception {

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			Processo processo = get(processoId);
			enviarParaAnalise(processo, null, null, false);
			gravarLogEnvioParaConferenciaGraduacao(processo);
		});
		tw.runNewThread();
		tw.throwException();
	}

	private void gravarLogEnvioParaConferenciaGraduacao(Processo processo) {

		StringBuilder observacao = new StringBuilder();

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		filtro.setStatusDocumentoList(Arrays.asList(StatusDocumento.DIGITALIZADO));
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
		for(Documento documento : documentos) {
			String nome = documento.getNome();
			Processo pd = documento.getProcesso();
			TipoProcesso tipoProcesso = pd.getTipoProcesso();
			String tipoProcessoNome = tipoProcesso.getNome();
			observacao.append(" <br>");
			observacao.append(nome);
			observacao.append(" - ");
			observacao.append("(");
			observacao.append(tipoProcessoNome);
			observacao.append(")");
		}
		String obsLog = messageService.getValue("envioAnalisePorDocumentoDigitalizado.label", observacao);

		processoLogService.criaLog(processo, null, AcaoProcesso.ENVIO_ANALISE_AUTOMATICO, obsLog);
	}

	public List<Long> findIdsToEnvioParaAnalise() {
		return processoRepository.findIdsToEnvioParaAnalise();
	}

	public String getModalidade(Long processoId) {
		return processoRepository.getModalidade(processoId);
	}

	public void gravarErroDeNotificacaoAluno(Processo processo, String email) {
		StringBuilder observacao = new StringBuilder();
		observacao.append("E-mail invalido: ").append(email);

		processoLogService.criaLog(processo, null, AcaoProcesso.ERRO_NOTIFICAR_POR_EMAIL, observacao.toString());
	}

	public List<Long> findIdsToEnvioParaAnaliseSisFiesProuniConcluido() {
		return processoRepository.findIdsToEnvioParaAnaliseSisFiesProuniConcluido();
	}

	public void bloquearProcessosReaproveitados(List<Processo> processos, String matricula, Long getdocAlunoProcessoId) throws Exception {

		systraceThread("Bloqueando processos=" + processos);

		for (Processo processo : processos) {
			bloquearProcessoReaproveitado(matricula, getdocAlunoProcessoId, processo);
		}
	}

	private void bloquearProcessoReaproveitado(String matricula, Long getdocAlunoProcessoId, Processo processo) throws Exception {

		systraceThread("Bloqueando processo=" + processo);

		Long processoId = processo.getId();

		processo.setAlunoProcessoId(getdocAlunoProcessoId);
		CampoAbstract campoMatricula = DummyUtils.getCampoProcesso(processo, CampoMap.CampoEnum.MATRICULA);
			campoMatricula.setValor(matricula);

		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		Map<Long, String> valores = new LinkedHashMap<>();
		for (CampoGrupo cg : gruposCampos) {
			Set<Campo> campos = cg.getCampos();
			for (Campo campo : campos) {
				Long campoId = campo.getId();
				String valor = campo.getValor();
				valores.put(campoId, valor);
			}
		}

		EditarProcessoVO vo = new EditarProcessoVO();
		vo.setProcessoId(processoId);
		vo.setValores(valores);
		vo.setGrupos((new ArrayList<>(gruposCampos)));
		vo.setValidarCampos(false);
		atualizarProcesso(vo);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Situacao situacaoAluno = situacaoService.getByFinalNome(tipoProcessoId, Situacao.ALUNO);

		if (situacaoAluno == null) {
			throw new MessageKeyException("situacaoAlunoNaoEncontrado.error", tipoProcesso);
		}

		try {
			concluirSituacao(processo, null, situacaoAluno, null, null);
		}
		catch (Exception e) {

			e.printStackTrace();
			emailSmtpService.enviarEmailException("Não foi possível enviar processo (" + processoId +" ) para situação \"Aluno\"", e);
			throw new MessageKeyException("erroInesperado.error", e.getMessage());
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo criaNovoProcessoModalidade(Processo processo, TipoProcesso tipoProcesso, Usuario usuario) throws Exception {
		CriacaoProcessoVO vo = new CriacaoProcessoVO();
		Long processoId = processo.getId();
		Aluno aluno = processo.getAluno();
		Usuario analista = processo.getAnalista();
		Set<CampoAbstract> valoresCampos = new HashSet<CampoAbstract>();
		List<CampoGrupo> camposGrupos = campoGrupoService.findByProcesso(processoId);

		for (CampoGrupo campoGrupo : camposGrupos) {
			Set<Campo> campos = campoGrupo.getCampos();
			valoresCampos.addAll(campos);
		}

		vo.setAluno(aluno);
		vo.setAnalista(analista);
		vo.setValoresCampos(valoresCampos);
		vo.setTipoProcesso(tipoProcesso);
		vo.setUsuario(usuario);
		vo.setProcessoOriginal(processo);

		Processo novoProcesso = criaProcesso(vo);

		return novoProcesso;
	}

	public List<Long> findIdsEnvioProcessoProuni(CalendarioCriterio calendarioCriterio) {
		Calendario calendario = calendarioCriterio.getCalendario();
		TipoProcesso tipoProcesso = calendario.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		String periodoIngresso = calendario.getPeriodoIngresso();
		TipoParceiro tipoParceiro = TipoParceiro.POLO_PARCEIRO;
		TipoProuni tipoProuni = calendario.getTipoProuni();
		ListaChamada chamada = calendarioCriterio.getChamada();
		return processoRepository.findIdsEnvioProcessoProuni(tipoProcessoId, periodoIngresso, tipoParceiro, tipoProuni, chamada);
	}

	@Transactional(rollbackFor = Exception.class)
	public void inserirArquivoNoDocumento(Processo processo, TipoDocumento tipoDocumentoParaAtualizar, File arquivo, Usuario usuarioLogado, boolean aprovarDocumento) {

		systraceThread("Inserindo arquivo. processo=" + processo + ", tipoDocumento=" + tipoDocumentoParaAtualizar + ", arquivo=" + arquivo + ", usuario=" + usuarioLogado);

		Long processoId = processo.getId();
		Long tipoDocumentoId = tipoDocumentoParaAtualizar.getId();

		Documento documentoParaAtualizar = documentoService.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);

		if (documentoParaAtualizar == null) {

			systraceThread("Nenhum documento foi encontrado. Será criado um novo documento. processo=" + processo + ", tipoDocumento=" + tipoDocumentoParaAtualizar);
			documentoParaAtualizar = criaDocumento(processo, tipoDocumentoParaAtualizar, usuarioLogado);
		}

		systraceThread("Documento encontrado=" + documentoParaAtualizar);

		Long documentoId = documentoParaAtualizar.getId();
		List<Imagem> versaoAtualByDocumento = imagemService.findVersaoAtualByDocumento(documentoId);

		String hashChecksumNovoArquivo = DummyUtils.getHashChecksum(arquivo);
		if (!isArquivoAdicionadoAnteriormente(hashChecksumNovoArquivo, versaoAtualByDocumento)) {

			ImagemTransaction imagemTx = new ImagemTransaction();

			String fileSize = DummyUtils.toFileSize(arquivo.length());

			FileVO fileVO = new FileVO();
			fileVO.setFile(arquivo);
			fileVO.setLength(fileSize);
			fileVO.setHash(hashChecksumNovoArquivo);
			fileVO.setOrigem(Origem.WEB);
			fileVO.setName(arquivo.getName());

			try {
				systraceThread("Digitalizando imagem.");
				documentoService.digitalizarImagens(imagemTx, usuarioLogado, documentoParaAtualizar, singletonList(fileVO), Origem.WEB);

				if (aprovarDocumento) {

					systraceThread("Aprovando documento=" + documentoId);
					documentoService.aprovar(documentoParaAtualizar, null, usuarioLogado);
				}

				imagemTx.commit();
			}
			catch (IOException e) {
				e.printStackTrace();
				imagemTx.rollback();
			}
		}
	}

	private boolean isArquivoAdicionadoAnteriormente(String hashChecksumNovoArquivo, List<Imagem> versaoAtualByDocumento) {

		boolean arquivoFoiAdicionadoAnteriormente = false;
		for (Imagem imagem : versaoAtualByDocumento) {

			String hashChecksumArquivoArmazenado = imagem.getHashChecksum();

			if (hashChecksumNovoArquivo.equals(hashChecksumArquivoArmazenado)) {
				arquivoFoiAdicionadoAnteriormente = true;
				break;
			}
		}

		systraceThread("Arquivo já foi adicionado?=" + arquivoFoiAdicionadoAnteriormente);
		return arquivoFoiAdicionadoAnteriormente;
	}

	public Processo getProcessoIsencaoDisciplinas(Processo processoPai){
		TipoProcesso tipoProcessoIsencao = tipoProcessoService.get(TipoProcesso.ISENCAO_DISCIPLINAS);
		if(tipoProcessoIsencao == null) {
			return null;
		}
		String numCandidato = processoPai.getNumCandidato();
		String numInscricao = processoPai.getNumInscricao();

		if(StringUtils.isBlank(numCandidato) && StringUtils.isBlank(numInscricao)){
			return null;
		}

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setTiposProcesso(Arrays.asList(tipoProcessoIsencao));
		if (StringUtils.isNotBlank(numCandidato)) {
			filtro.setNumCandidatoInscricao(numCandidato);
		} else {
			filtro.setNumCandidatoInscricao(numInscricao);
		}
		return getLastByFiltro(filtro);
	}

	public List<ProcessoVO> montarProcessoVO(ProcessoFiltro filtro, Integer inicio, Integer max) {
		List<Processo> processos = processoRepository.findByFiltro(filtro, inicio, max);
		List<ProcessoVO> processoVOS = new ArrayList<>();

		for (Processo processo : processos) {
			String chamada = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUMERO_CHAMADA);
			String periodo = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.PERIODO_DE_INGRESSO);
			String unidade = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CAMPUS);
			String labelUnidade = baseRegistroService.getLabel(BaseInterna.CAMPUS_ID, unidade);
			String curso = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO);
			String labelCurso = baseRegistroService.getLabel(BaseInterna.CURSO_ID, curso);

			ProcessoVO processoVO = new ProcessoVO(processo);
			processoVO.setChamada(chamada);
			processoVO.setPeriodo(periodo);
			processoVO.setUnidade(labelUnidade);
			processoVO.setCurso(labelCurso);

			processoVOS.add(processoVO);
		}
		return processoVOS;
	}

	public int atualizarDataUltimaAtualizacao(Long processoId, Date dataUltimaAtualizacao) {
		return processoRepository.atualizarDataUltimaAtualizacao(processoId, dataUltimaAtualizacao);
	}

	public void ajustePrazoLimiteSituacao(ProcessoLog log, Situacao situacao) {

		Long processoLogId = log.getId();
		ProcessoLog log1 = processoLogService.get(processoLogId);

		Etapa etapa = log1.getEtapa();
		BigDecimal horasPrazo1 = etapa.getHorasPrazo();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = buildHUC(situacaoId);
		Date data = log1.getData();

		BigDecimal horasPrazoSituacao = situacao.getHorasPrazo();
		if(horasPrazoSituacao != null) {
			Date prazoLimiteSituacao = huc.addHoras(data, horasPrazoSituacao);
			log1.setPrazoLimiteSituacao(prazoLimiteSituacao);
			log1.setHorasPrazoSituacao(horasPrazoSituacao);
		}

		Date prazoLimiteEtapa = calculaPrazoLimiteEtapa(situacao, data);

		log1.setPrazoLimiteEtapa(prazoLimiteEtapa);
		log1.setHorasPrazoEtapa(horasPrazo1);
		processoLogService.saveOrUpdate(log1);

	}

	public void alterarSituacaoEmMassaIsencao(List<Processo> processos, List<Situacao> novaSituacao, Usuario usuario, String observacao) {

		if (processos.isEmpty() || novaSituacao.isEmpty()) {
			return;
		}

		int length = processos.size();
		int count = 1;
		List<Long> processosIdsNaoAtualizados = new ArrayList<>();
		systraceThread("Processando alteracao em massa de situacao qtd: " + length + " processos...");

		for (Situacao situacao : novaSituacao) {
			for (Processo processo : processos) {

				Situacao processoSituacao = processo.getSituacao();
				Long processoSituacaoId = processoSituacao.getId();
				processoSituacao = situacaoService.get(processoSituacaoId);
				Set<ProximaSituacao> proximas = processoSituacao.getProximas();
				List<Situacao> proximasSituacoes = proximas.stream().map(p -> p.getProxima()).collect(Collectors.toList());

				if(proximasSituacoes.contains(situacao)) {
					Long processoId = processo.getId();
					systraceThread("Processo " + processoId + " situacao " + situacao.getNome() + " " + count + " de " + length);

					TipoProcesso tipoProcessoSituacao = situacao.getTipoProcesso();
					TipoProcesso tipoProcesso = processo.getTipoProcesso();
					if (tipoProcessoSituacao.equals(tipoProcesso)) {
						try {
							concluirEmMassa(singletonList(processo), usuario, situacao, observacao);
						}
						catch (Exception e) {
							processosIdsNaoAtualizados.add(processoId);
							e.printStackTrace();
						}
					}
					else {
						systraceThread("Processo não é do mesmo tipo_processo da situação. processo.tipo_processo=" + tipoProcesso + ", situacao.tipo_processo=" + tipoProcessoSituacao);
						processosIdsNaoAtualizados.add(processoId);
					}

					count++;
				}
			}
		}

		systraceThread("Finalizado. Processos não atualizados=" + processosIdsNaoAtualizados);
	}

	public List<Processo> findProcessoFormaDeIngressoVestibular() {
		return processoRepository.findProcessoFormaDeIngressoVestibular();
	}

	public List<Long> findProcessosIds(Long processoId, int limit) {
		return processoRepository.findProcessosIds(processoId, limit);
	}

	public List<Processo> findProcessosFinanciamentoNovoComAlteracao(Long tipoProcessoId, List<Long> situacaoList) {
		return processoRepository.findProcessosFinanciamentoNovoComAlteracao(tipoProcessoId, situacaoList);
	}

	public List<Processo> findProcessosFinanciamentoProcessadosComAlteracao(Long tipoProcessoId, List<Long> situacaoList) {
		return processoRepository.findProcessosFinanciamentoProcessadosComAlteracao(tipoProcessoId, situacaoList);
	}

	public List<Long> findProcessoIdByNumCandidatoNumInscricao(String numCandidato, String numInscricao) {
		return processoRepository.findProcessoIdByNumCandidatoNumInscricao(numCandidato, numInscricao);
	}

	public List<Processo> findProcessosPendentesComDocsAprovados() {
		return processoRepository.findProcessosPendentesComDocsAprovados();
	}
}
