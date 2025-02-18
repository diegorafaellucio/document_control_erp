package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class TipificacaoFluxoAprovacaoService {

	@Autowired private DocumentoService documentoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ExceptionService exceptionService;

	@Transactional(rollbackFor=Exception.class)
	public void tipificarDocumentosByProcesso(Processo processo, boolean reprocessamento) throws Exception {
		Usuario usuarioAdmin = usuarioService.getByLogin(Usuario.LOGIN_ADMIN);
		Long processoId = processo.getId();
		Long tipoProcessoId = processo.getTipoProcesso().getId();
		MultiValueMap<Long, Long> idsToTipificacaoFluxoAprovacao = documentoService.findIdsToTipificacaoFluxoAprovacao(processoId);

		systraceThread("Tipificacao - Processo Id: " + processoId);
		systraceThread("Tipificacao - Processo possui documentos para Tipificação: " + idsToTipificacaoFluxoAprovacao.size());

		if(idsToTipificacaoFluxoAprovacao.isEmpty() && !reprocessamento) {
			ProcessoLogFiltro processoLogFiltroNaoHaDocumentos = new ProcessoLogFiltro();
			processoLogFiltroNaoHaDocumentos.setProcessoId(processoId);
			processoLogFiltroNaoHaDocumentos.setAcaoList(Collections.singletonList(AcaoProcesso.NAO_HA_DOCUMENTOS_PARA_TIPIFICAR));
			Integer totalDeTentativasDeTipificacao = processoLogService.countByFiltro(processoLogFiltroNaoHaDocumentos);
			systraceThread(" Tipificacao - Total de Tentativas de Envio Para Tipificacao: " + totalDeTentativasDeTipificacao);

			processoLogService.criaLog(processo, null, AcaoProcesso.NAO_HA_DOCUMENTOS_PARA_TIPIFICAR);

			Situacao situacao = situacaoService.getByNome(tipoProcessoId, Situacao.AGUARDANDO_ANALISE_IA);
			processoService.concluirSituacao(processo, null, situacao, null, null);

			return;
		}

		systraceThread("Tipificacao - Total de Documentos a serem processados: " + idsToTipificacaoFluxoAprovacao.size() + " do processo " + processoId);

		for (Long documentoId : idsToTipificacaoFluxoAprovacao.keySet()) {
			systraceThread("Tipificacao - Tipificando Documento id: " + documentoId);
			Documento documento = documentoService.get(documentoId);
			try{
				documentoService.tipificarFluxoAprovacao(processo, documento);
			}
			catch (Exception e){
				e.printStackTrace();
				String message = exceptionService.getMessage(e);

				LogAcesso log = LogAcessoFilter.getLogAcesso();
				String exception = log.getException();
				exception = exception != null? exception + "\n\n" : "";
				exception += message + "\n";
				String stackTrace = ExceptionUtils.getStackTrace(e);
				exception += stackTrace;
				log.setException(exception);

				processoLogService.criaLogErro(processo, null, AcaoProcesso.ERRO_TIPIFICACAO, null, "Erro ao tipificar documento: " + message + " #c-" + log.getId());
			}

		}

		if (!reprocessamento) {
			Situacao situacao = situacaoService.getByNome(tipoProcessoId, Situacao.AGUARDANDO_OCR);
			processoService.concluirSituacao(processo, usuarioAdmin, situacao, null, null);
			processoService.agendarFacialRecognition(processo);
		}
	}
 }
