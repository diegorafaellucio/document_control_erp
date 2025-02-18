package net.wasys.getdoc.domain.service.webservice.sia;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.ConsultaComprovanteInscricaoVO;
import net.wasys.getdoc.domain.vo.ConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.ConsultaLinhaTempoSiaVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.SiaAtualizaDocumentoVO;
import net.wasys.getdoc.domain.vo.SiaConsultaComprovanteInscricaoVO;
import net.wasys.getdoc.domain.vo.SiaConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.SiaConsultaLinhaTempoVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.rest.jackson.ObjectMapper;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class SiaService {

	@Autowired private DocumentoService documentoService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private SiaConsultaLinhaTempoService siaConsultaLinhaTempoService;
	@Autowired private SiaConsultaComprovanteInscricaoService siaConsultaComprovanteInscricaoService;
	@Autowired private SiaConsultaInscricoesService siaConsultaInscricoesService;
	@Autowired private SiaAtualizaDocumentoService siaAtualizaDocumentoService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;

	public void notificarDocumentos() {

		List<Long> documentosIds = documentoService.findToNotificacaoSia();
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(logAcesso, "documentos", documentosIds.size());
		if(documentosIds.isEmpty()) {
			return;
		}

		int qtdeThreads = 20;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(qtdeThreads);
		ExecutorTimeoutUtil etu = new ExecutorTimeoutUtil(executor);
		etu.configurar(40 * 1000, qtdeThreads, documentosIds.size());

		AtomicInteger notificados = new AtomicInteger(0);
		AtomicInteger naoNotificados = new AtomicInteger(0);

		for (Long documentoId : documentosIds) {

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				Documento documento = documentoService.get(documentoId);
				Processo processo = documento.getProcesso();
				Long processoId = processo.getId();
				DummyUtils.mudarNomeThread("thdpool-poolSiaNotificar-" + documentoId + "-" + processoId);

				boolean notificado = notificarDocumento(documento);
				if(notificado) {
					notificados.incrementAndGet();
				} else {
					naoNotificados.incrementAndGet();
				}
			});
			etu.submit(tw, documentoId);
			DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
		}

		etu.esperarTerminarFuturesOuCancelar(false);

		List<Object> referenciaThreadsTimeouts = etu.getReferenciaThreadsTimeouts();
		Map<Runnable, Object> referenciaRunnableMap = etu.getReferenciaRunnableMap();

		DummyUtils.addParameter(logAcesso, "timeouts", referenciaThreadsTimeouts.size());

		int erros = 0;
		StringBuilder exceptionsMessages = new StringBuilder();
		for (Map.Entry<Runnable, Object> entry : referenciaRunnableMap.entrySet()) {

			TransactionWrapper tw = (TransactionWrapper) entry.getKey();
			Exception exception = tw.getException();
			if(exception == null) continue;

			erros++;
			String message = exceptionService.getMessage(exception);
			Long documentoId = (Long) entry.getValue();
			Documento documento = documentoService.get(documentoId);
			Processo processo = documento.getProcesso();
			Long processoId = processo.getId();
			String documentoNome = documento.getNome();
			exceptionsMessages.append("ProcessoId: ").append(processoId).append(". Documento: ").append(documentoNome).append(". ").append(message).append("\n");
			exceptionsMessages.append("\n");
		}

		systraceThread("FIM! " + notificados.get() + " notificados, " + naoNotificados.get() + " não notificados e " + erros + " erros.");

		if(logAcesso != null) {
			DummyUtils.addParameter(logAcesso, "notificados", notificados.get());
			DummyUtils.addParameter(logAcesso, "naoNotificados", naoNotificados.get());
			DummyUtils.addParameter(logAcesso, "erros", erros);
		}

		if(exceptionsMessages.length() > 0) {
			emailSmtpService.enviarEmailException("Erros excecutando notificação de documentos ao SIA: ", exceptionsMessages.toString(), "");
			throw new RuntimeException(exceptionsMessages.toString());
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public boolean notificarDocumento(Documento documento) {

		Processo processo = documento.getProcesso();
		String numInscricao = processo.getNumInscricao();
		String numCandidato = processo.getNumCandidato();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		Long codOrigem = tipoDocumento.getCodOrigem();
		String codOrigemStr = codOrigem != null ? codOrigem.toString() : null;

		if(StringUtils.isBlank(numCandidato)){

			AlunoFiltro alunoFiltro = new AlunoFiltro();
			alunoFiltro.setNumCandidato(numCandidato);
			alunoFiltro.setNumInscricao(numInscricao);

			ConsultaInscricoesVO consultaInscricoesVO = consultaInscricao(alunoFiltro);

			if(consultaInscricoesVO == null) {
				consultaCandidatoService.getConsultaInscricoesVO(numInscricao, numCandidato);
			}

			numCandidato = consultaInscricoesVO != null ? consultaInscricoesVO.getNumCandidato() : null;

			if(StringUtils.isBlank(numCandidato)){
				//não da pra fazer marcação no SIA sem numero de candidato, pq inscrito nem tem documentação no SIA.
				return false;
			}
		}

		validarParametrosDeNotificacaoDocumentosSia(documento, processo, numCandidato, codOrigemStr);

		SiaAtualizaDocumentoVO vo = new SiaAtualizaDocumentoVO();
		vo.setNumeroInscricao(null);
		vo.setNumeroCandidato(numCandidato);
		vo.setCodOrigem(codOrigemStr);
		ConsultaExterna ce = notificarDocumento(vo);

		documentoService.atualizarNotificadoSia(documento.getId(), true);

		String resultado = ce.getResultado();
		documentoLogService.criaLog(documento, null, AcaoDocumento.NOTIFICACAO_SIA, resultado);

		return true;
	}

	public ConsultaExterna notificarDocumento(SiaAtualizaDocumentoVO vo) {

		ConsultaExterna ce = siaAtualizaDocumentoService.consultarInvalidandoConsultaAnterior(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			throw new MessageKeyException("erroNotificacaoSiaDocumento.error", mensagem);
		}

		return ce;
	}

	private void validarParametrosDeNotificacaoDocumentosSia(Documento documento, Processo processo, String numCandidato, String codOrigemStr) {
		if(StringUtils.isBlank(numCandidato) && StringUtils.isBlank(codOrigemStr)) {
			Long processoId = processo.getId();
			String documentoNome = documento.getNome();

			StringBuilder erro = new StringBuilder();
			if(StringUtils.isBlank(numCandidato)) {
				erro.append(" N. Candidato não preenchidos para o processo. ");
			}
			if(StringUtils.isBlank(codOrigemStr)) {
				erro.append(" Cod Origem não preenchidos para o documento. ");
			}
			erro.append("Processo: ").append(processoId).append(". Documento: ").append(documentoNome).append(".");

			throw new MessageKeyException("erroNotificacaoSiaDocumento.error", erro.toString());
		}
	}

	public ConsultaLinhaTempoSiaVO consultaLinhaTempo(String numInscricao, String numCandidato) {

		SiaConsultaLinhaTempoVO vo = new SiaConsultaLinhaTempoVO();
		vo.setNumeroInscricao(numInscricao);
		vo.setNumeroCandidato(numCandidato);
		ConsultaExterna ce = siaConsultaLinhaTempoService.consultar(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		String resultado = ce.getResultado();
		Map<?, ?> result = DummyUtils.jsonStringToMap(resultado);
		Boolean success = result != null ? (Boolean) result.get("Success") : null;
		if(result == null || success == null || !success) {
			String mensagem = resultado;
			if(result != null) {
				Object code = result.get("Code");
				mensagem = code + " - ";
				mensagem += (String) result.get("Message");
			}
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		Map<?, ?> mapValue = (Map<?, ?>) result.get("Value");
		String textoRetorno = mapValue != null ? (String) mapValue.get("TXT_RETORNO") : null;
		if ("NENHUM REGISTRO FOI ENCONTRADO!".equals(textoRetorno)) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(mapValue, ConsultaLinhaTempoSiaVO.class);
	}

	public ConsultaComprovanteInscricaoVO consultaComprovanteInscricao(String numInscricao, String numCadidato){

		SiaConsultaComprovanteInscricaoVO vo = new SiaConsultaComprovanteInscricaoVO();
		vo.setNumeroInscricao(numInscricao);
		vo.setNumeroCandidato(numCadidato);
		ConsultaExterna ce = siaConsultaComprovanteInscricaoService.consultar(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		String resultado = ce.getResultado();

		Map<?, ?> result = DummyUtils.jsonStringToMap(resultado);

		Boolean success = result != null ? (Boolean) result.get("Success") : null;
		if(result == null || success == null || !success) {
			String mensagem = resultado;
			if(result != null) {
				mensagem = result.get("Code") + " - ";
				mensagem += (String) result.get("Message");
			}
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		Map<?, ?> mapValue = (Map<?, ?>) result.get("Value");
		String textoRetorno = mapValue.get("TXT_RETORNO") != null ? (String) mapValue.get("TXT_RETORNO") : "";
		if ("NENHUM REGISTRO FOI ENCONTRADO!".equals(textoRetorno)) {
			throw new MessageKeyException("candidatoNaoEncontrado.error");
		}

		ObjectMapper mapper = new ObjectMapper();
		ConsultaComprovanteInscricaoVO vo2 = mapper.convertValue(mapValue, ConsultaComprovanteInscricaoVO.class);
		return vo2;
	}

	public List<ConsultaInscricoesVO> consultaInscricaoPorCpf(AlunoFiltro filtro) {

		SiaConsultaInscricoesVO vo = new SiaConsultaInscricoesVO();
		String cpf = filtro.getCpf();
		vo.setCpfAluno(cpf);
		ConsultaExterna ce = siaConsultaInscricoesService.consultar(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			if (mensagem == null) {
				mensagem = "ERRO NÃO IDENTIFICADO!";
			}
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		String resultado = ce.getResultado();

		Map<?, ?> result = DummyUtils.jsonStringToMap(resultado);

		Boolean success = result != null ? (Boolean) result.get("Success") : null;
		if(result == null || success == null || !success || result.get("Value") == null) {
			String mensagem = resultado;
			if(result != null) {
				mensagem = result.get("Code") + " - ";
				mensagem += (String) result.get("Message");
			}
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(result.get("Value"), mapper.getTypeFactory().constructCollectionType(List.class, ConsultaInscricoesVO.class));
	}

	public ConsultaInscricoesVO consultaInscricao(AlunoFiltro filtro) {

		SiaConsultaInscricoesVO vo = new SiaConsultaInscricoesVO();
		String numInscricao = filtro.getNumInscricao();
		String numCandidato = filtro.getNumCandidato();
		vo.setNumeroInscricao(numInscricao);
		vo.setNumeroCandidato(numCandidato);
		ConsultaExterna ce = siaConsultaInscricoesService.consultar(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			if (mensagem == null) {
				mensagem = "ERRO NÃO IDENTIFICADO!";
			}
			throw new MessageKeyException("erroConsultaSia.error", "Num Inscrição: "+ numInscricao + " Num Candidato: " + numCandidato + "\n" + mensagem);
		}

		String resultado = ce.getResultado();

		Map<?, ?> result = DummyUtils.jsonStringToMap(resultado);

		Boolean success = result != null ? (Boolean) result.get("Success") : null;
		if(result == null || success == null || !success || result.get("Value") == null) {
			String mensagem = resultado;
			if(result != null) {
				mensagem = result.get("Code") + " - ";
				mensagem += (String) result.get("Message");
			}
			throw new MessageKeyException("erroConsultaSia.error", mensagem);
		}

		ObjectMapper mapper = new ObjectMapper();
		List<ConsultaInscricoesVO> consultaInscricoesVO = mapper.convertValue(result.get("Value"), mapper.getTypeFactory().constructCollectionType(List.class, ConsultaInscricoesVO.class));
		if(consultaInscricoesVO.size() == 1 || consultaInscricoesVO.isEmpty()){
			return consultaInscricoesVO.isEmpty() ? null : consultaInscricoesVO.get(0);
		} else {
			return getMaisRecente(consultaInscricoesVO);
		}
	}

	private ConsultaInscricoesVO getMaisRecente(List<ConsultaInscricoesVO> consultaInscricoesVO) {
		ConsultaInscricoesVO maisRecenteVo = new ConsultaInscricoesVO();
		for(ConsultaInscricoesVO inscricoesVO : consultaInscricoesVO){
			String perspectiva = inscricoesVO.getPerspectiva();
			if (perspectiva.equals(ConsultaInscricoesVO.Perspectivas.ALUNO.name())){
				return inscricoesVO;
			} else if (perspectiva.equals(ConsultaInscricoesVO.Perspectivas.INSCRITO.name())){
				maisRecenteVo = inscricoesVO;
			}
		}

		for(ConsultaInscricoesVO inscricoesVO : consultaInscricoesVO){
			String perspectiva = inscricoesVO.getPerspectiva();
			if(ConsultaInscricoesVO.Perspectivas.CANDIDATO.name().equals(perspectiva)){
				String nomCandidato = inscricoesVO.getNomCandidato();
				String periodoIngresso = inscricoesVO.getPeriodoIngresso();
				Long codSituacaoAluno = inscricoesVO.getCodSituacaoAluno();
				String nomSituacaoAluno = inscricoesVO.getNomSituacaoAluno();
				Date dataInscricao = inscricoesVO.getDataInscricao();
				Long codCurso = inscricoesVO.getCodCurso();
				Long codCampus = inscricoesVO.getCodCampus();

				Long codInstituicao = inscricoesVO.getCodInstituicao();
				String nomInstituicao = inscricoesVO.getNomInstituicao();
				String nomCampus = inscricoesVO.getNomCampus();
				String nomCurso = inscricoesVO.getNomCurso();
				Long codRegional = inscricoesVO.getCodRegional();
				String nomRegional = inscricoesVO.getNomRegional();
				String codMatricula = inscricoesVO.getCodMatricula();
				String email = inscricoesVO.getEmail();
				Long codTurno = inscricoesVO.getCodTurno();
				String nomTurno = inscricoesVO.getNomTurno();
				Long codTipoCurso = inscricoesVO.getCodTipoCurso();
				String nomTipoCurso = inscricoesVO.getNomTipoCurso();
				Long codFormaIngresso = inscricoesVO.getCodFormaIngresso();
				String formaIngresso = inscricoesVO.getFormaIngresso();

				maisRecenteVo.setNomCandidato(nomCandidato);
				maisRecenteVo.setPeriodoIngresso(periodoIngresso);
				maisRecenteVo.setCodSituacaoAluno(codSituacaoAluno);
				maisRecenteVo.setNomSituacaoAluno(nomSituacaoAluno);
				maisRecenteVo.setDataInscricao(dataInscricao);
				maisRecenteVo.setCodCurso(codCurso);
				maisRecenteVo.setCodCampus(codCampus);
				maisRecenteVo.setCodInstituicao(codInstituicao);
				maisRecenteVo.setNomInstituicao(nomInstituicao);
				maisRecenteVo.setNomCampus(nomCampus);
				maisRecenteVo.setNomCurso(nomCurso);
				maisRecenteVo.setCodRegional(codRegional);
				maisRecenteVo.setNomRegional(nomRegional);
				maisRecenteVo.setCodMatricula(codMatricula);
				maisRecenteVo.setEmail(email);
				maisRecenteVo.setCodTurno(codTurno);
				maisRecenteVo.setNomTurno(nomTurno);
				maisRecenteVo.setCodTipoCurso(codTipoCurso);
				maisRecenteVo.setNomTipoCurso(nomTipoCurso);
				maisRecenteVo.setCodFormaIngresso(codFormaIngresso);
				maisRecenteVo.setFormaIngresso(formaIngresso);
			}
		}
		return maisRecenteVo;
	}

	public String getRelacional(Long baseInternaId, String campoValor, String tipoCampoFiltro, String tipoCampoValor) {
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		BaseInterna baseInterna = baseInternaService.get(baseInternaId);
		filtro.setBaseInterna(baseInterna);
		campoValor = DummyUtils.limparCharsChaveUnicidade(campoValor);
		filtro.addCampoFiltro(tipoCampoFiltro, campoValor);
		List<RegistroValorVO> valorVo = baseRegistroService.findByFiltro(filtro, null, null);

		if(valorVo.isEmpty()){
			return "[\""+null+"\"]";
			/*throw new MessageKeyException("consultaRelacionalPortal.error", tipoCampoFiltro, campoValor);*/
		}

		RegistroValorVO registroValorVO = valorVo.get(0);
		String chaveUnicidade = registroValorVO.getValor(tipoCampoValor);

		return "[\""+chaveUnicidade+"\"]";
	}
}
