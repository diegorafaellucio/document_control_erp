package net.wasys.getdoc.domain.service.webservice.atila;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class AtilaService {

	@Autowired private ProcessoNotificarAtilaService processoNotificarAtilaService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private AtilaAtualizaDocumentoPendenteService atilaAtualizaDocumentoPendenteService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;


	public void notificarDocumentosPendentes() {

		List<ProcessoNotificarAtila> processosToNotificar = processoNotificarAtilaService.findToNotificar();

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(logAcesso, "processos", processosToNotificar.size());
		if(processosToNotificar.isEmpty()) {
			return;
		}

		String accessToken = atilaAtualizaDocumentoPendenteService.autenticarNoAtila();

		int qtdeThreads = 20;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(qtdeThreads);
		ExecutorTimeoutUtil etu = new ExecutorTimeoutUtil(executor);
		etu.configurar(40 * 1000, qtdeThreads, processosToNotificar.size());

		AtomicInteger notificados = new AtomicInteger(0);
		AtomicInteger naoNotificados = new AtomicInteger(0);

		for (ProcessoNotificarAtila processoNotificarAtila : processosToNotificar) {

			Processo processo = processoNotificarAtila.getProcesso();
			Long processoId = processo.getId();
			Long processoNotificarAtilaId = processoNotificarAtila.getId();

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				DummyUtils.mudarNomeThread("thdpool-poolAtilaNotificar-" + processoId);
				boolean notificado = notificarProcessoPendente(processoNotificarAtilaId, accessToken);
				if(notificado) {
					notificados.incrementAndGet();
				} else {
					naoNotificados.incrementAndGet();
				}

			});
			etu.submit(tw, processoId);
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
			Long processoId = (Long) entry.getValue();
			exceptionsMessages.append("ProcessoId: ").append(processoId).append(message).append("\n");
			exceptionsMessages.append("\n");
		}

		systraceThread("FIM! " + notificados.get() + " notificados, " + naoNotificados.get() + " não notificados e " + erros + " erros.");

		if(logAcesso != null) {
			DummyUtils.addParameter(logAcesso, "erros", erros);
		}

		if(exceptionsMessages.length() > 0) {
			emailSmtpService.enviarEmailException("Erros excecutando notificação de documentos ao SIA: ", exceptionsMessages.toString(), "");
			throw new RuntimeException(exceptionsMessages.toString());
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public boolean notificarProcessoPendente(Long processoNotificarAtilaId, String accessToken) {
		ProcessoNotificarAtila processoNotificarAtila = processoNotificarAtilaService.get(processoNotificarAtilaId);
		Processo processo = processoNotificarAtila.getProcesso();
		Long processoId = processo.getId();

		processo = processoService.get(processoId);

		String numCandidato = processo.getNumCandidato();
		if(StringUtils.isBlank(numCandidato)) {
			processoNotificarAtila.setNotificarAtila(false);
			processoNotificarAtilaService.saverOrUpdate(processoNotificarAtila);
			return false;
		}

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		filtro.setStatusDocumentoList(Arrays.asList(StatusDocumento.PENDENTE));

		Map<Long, Pendencia> toNotificar = pendenciaService.findMapToNotificar(processoId);

		List<Documento> documentoList = documentoService.findByFiltro(filtro, null, null);

		if(toNotificar.isEmpty()) {
			processoNotificarAtila.setNotificarAtila(false);
			processoNotificarAtilaService.saverOrUpdate(processoNotificarAtila);
			return false;
		}

		JSONArray documentosArrayJson = new JSONArray();
		AtilaAtualizaDocumentoVO vo = new AtilaAtualizaDocumentoVO();

		Set<Long> documentoIds = toNotificar.keySet();
		for (Long documentoId : documentoIds) {
			Documento documento = documentoService.get(documentoId);
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) {
				continue;
			}

			tipoDocumento = tipoDocumentoService.get(tipoDocumento.getId());
			Long codOrigem = tipoDocumento.getCodOrigem();
			String documentoNome = documento.getNome();

			Pendencia pendencia = toNotificar.get(documentoId);
			if(pendencia != null) {
				Irregularidade irregularidade = pendencia.getIrregularidade();
				String irregularidadeNome = irregularidade.getNome();
				Date dataCriacao = pendencia.getDataCriacao();
				String data= DummyUtils.format(dataCriacao, "MM/dd/yyyy");
				String hora = DummyUtils.formatTime(dataCriacao);

				vo.setNumeroCandidato(numCandidato);
				vo.setDocumentoId(codOrigem);
				vo.setDocumentoNome(documentoNome);
				vo.setIrregularidadeNome(irregularidadeNome);
				vo.setDataPendencia(data);
				vo.setHoraPendencia(hora);

				JSONObject keyJson = atilaAtualizaDocumentoPendenteService.getJson(vo, true);
				JSONObject valueJson = atilaAtualizaDocumentoPendenteService.getJson(vo, false);
				JSONObject documentoJson = new JSONObject();
				documentoJson.put("keys", keyJson);
				documentoJson.put("values", valueJson);

				documentosArrayJson.put(documentoJson);
			}
		}

		vo.setDocumentosJson(documentosArrayJson.toString());
		vo.setAccessToken(accessToken);

		ConsultaExterna ce = notificarDocumento(vo);
		String resultado = ce.getResultado();
		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.SUCESSO.equals(status)) {

			for (Long documentoId : documentoIds) {
				Pendencia pendencia = toNotificar.get(documentoId);
				pendencia.setNotificadoAtila(true);
				pendenciaService.saveOrUpdate(pendencia);
			}
			processoNotificarAtila.setNotificarAtila(false);
			processoNotificarAtilaService.saverOrUpdate(processoNotificarAtila);
		}

		processoLogService.criaLog(processo, null, AcaoProcesso.NOTIFICACAO_ATILA, resultado);

		DummyUtils.systraceThread(resultado);


		/*Long codOrigem = tipoDocumento.getCodOrigem();
		String codOrigemStr = codOrigem != null ? codOrigem.toString() : null;



		SiaAtualizaDocumentoVO vo = new SiaAtualizaDocumentoVO();
		vo.setNumeroInscricao(null);
		vo.setNumeroCandidato(numCandidato);
		vo.setCodOrigem(codOrigemStr);
		ConsultaExterna ce = notificarDocumento(vo);

		documentoService.atualizarNotificadoSia(documento.getId(), true);*/

		return true;
	}

	public ConsultaExterna notificarDocumento(AtilaAtualizaDocumentoVO vo) {

		ConsultaExterna ce = atilaAtualizaDocumentoPendenteService.consultarInvalidandoConsultaAnterior(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			throw new MessageKeyException("erroNotificacaoqAtilaDocumento.error", mensagem);
		}

		return ce;
	}

	private void validarParametrosDeNotificacaoDocumentosAtila(Processo processo, String numCandidato) {
		if(StringUtils.isBlank(numCandidato)) {
			Long processoId = processo.getId();

			StringBuilder erro = new StringBuilder();
			if(StringUtils.isBlank(numCandidato)) {
				erro.append(" N. Candidato não preenchidos para o processo. ");
			}
			erro.append("Processo: ").append(processoId).append(".");

			throw new MessageKeyException("erroNotificacaoqAtilaDocumento.error", erro.toString());
		}
	}
}
