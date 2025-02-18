package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusLogOcr;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.repository.LogOcrRepository;
import net.wasys.getdoc.domain.vo.RelatorioOcrVO;
import net.wasys.getdoc.domain.vo.filtro.LogOcrFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.image.ImageResizer;
import net.wasys.util.ocrws.dto.callback.CampoOcrDTO;
import net.wasys.util.ocrws.dto.callback.NotificacaoDTO;
import net.wasys.util.ocrws.dto.callback.StatusAgendamento;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LogOcrService {

	private static final String TIPO_ERRO_AGENDAMENTO_NAO_ENCONTRADO = "AGENDAMENTO_NAO_ENCONTRADO";

	@Autowired private LogOcrRepository logOcrRepository;
	@Autowired private ImagemService imagemService;
	@Autowired private OcrWsService ocrWsService;
	@Autowired private DocumentoService documentoService;
	@Autowired private CampoOcrService campoOcrService;
	@Autowired private ProcessoService processoService;
	@Autowired private ApplicationContext applicationContext;
	@Resource(name="resource") private MessageSource resource;

	public LogOcr get(Long id) {
		return logOcrRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(LogOcr logOcr) {
		logOcrRepository.saveOrUpdate(logOcr);
	}

	public LogOcr getLastLog(Long documentoId) {
		return logOcrRepository.getLastLog(documentoId);
	}

	public List<Long> findIdsByFiltro(LogOcrFiltro filtro) {
		return logOcrRepository.findIdsByFiltro(filtro);
	}

	public List<LogOcr> findByIds(List<Long> ids) {
		return logOcrRepository.findByIds(ids);
	}

	@Transactional(rollbackFor=Exception.class)
	public void agendarOcr(Usuario usuario, ModeloOcr modeloOcr, Imagem imagem) {

		String extensao = imagem.getExtensao();
		if(!GetdocConstants.IMAGEM_EXTENSOES.contains(extensao)) {
			return;
		}

		LogOcr logOcr = criaLogOcr(usuario, modeloOcr, imagem);

		agendarOcr(logOcr);
	}

	private LogOcr criaLogOcr(Usuario usuario, ModeloOcr modeloOcr, Imagem imagem) {

		String extensao = imagem.getExtensao();
		Documento documento = imagem.getDocumento();
		Long documentoId = documento.getId();
		Processo processo = documento.getProcesso();
		Long processoId = processo.getId();
		Long imagemId = imagem.getId();

		documento.setStatusOcr(StatusOcr.PROCESSANDO);
		documentoService.saveOrUpdate(documento);

		processoService.atualizarStatusOcr(processo);

		String caminho = imagem.getCaminho();
		if(!new File(caminho).exists()) {
			imagemService.atualizaCaminho(imagem);
			caminho = imagem.getCaminho();
		}

		LogOcr logOcr = new LogOcr();
		logOcr.setStatusOcr(StatusLogOcr.PRE_AGENDADO);
		logOcr.setData(new Date());
		logOcr.setUsuario(usuario);
		logOcr.setCaminhoImagem(caminho);
		logOcr.setExtensaoImagem(extensao);
		logOcr.setDocumentoId(documentoId);
		logOcr.setModeloOcr(modeloOcr);
		logOcr.setImagemId(imagemId);
		logOcr.setProcessoId(processoId);

		logOcrRepository.saveOrUpdate(logOcr);
		return logOcr;
	}

	public void agendarOcr(Long logOcrId) {
		LogOcr logOcr = get(logOcrId);
		agendarOcr(logOcr);
	}

	public void agendarOcr(final LogOcr logOcr) {

		ModeloOcr modeloOcr = logOcr.getModeloOcr();
		Hibernate.initialize(modeloOcr);

		Long imagemId = logOcr.getImagemId();
		final Long documentoId = logOcr.getDocumentoId();
		List<Imagem> imagens = imagemService.findVersaoAtualByDocumento(documentoId);
		final List<Imagem> imagensSecundarias = new ArrayList<Imagem>();
		for (Imagem imagem : imagens) {
			Long imagemId2 = imagem.getId();
			if(!imagemId.equals(imagemId2)) {
				imagensSecundarias.add(imagem);
			}
		}

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			Long agendamentoId = ocrWsService.agendarOcr(logOcr, imagensSecundarias);
			if(agendamentoId != null) {
				logOcr.setStatusOcr(StatusLogOcr.AGENDADO);
				logOcr.setAgendamentoId(agendamentoId);
				logOcrRepository.saveOrUpdate(logOcr);
			}
		});
		tw.setExceptionHandler((Exception e, String stackTrace) -> {

			String message = ExceptionUtils.getRootCauseMessage(e);
			message = message != null ? message : e.getMessage();
			logOcr.setMensagemErro("Erro ao agendar OCR");
			logOcr.setStackTrace(stackTrace);
			logOcr.setStatusOcr(StatusLogOcr.ERRO);

			LogOcrService logOcrService = applicationContext.getBean(LogOcrService.class);
			logOcrService.saveOrUpdate(logOcr);

			documentoService.atualizaStatusOcr(documentoId);
		});

		tw.startThread();
	}

	@Transactional(rollbackFor=Exception.class)
	public void processarNotificacao(NotificacaoDTO dto) {

		if(dto == null) {
			systraceThread("erro, dto is null", LogLevel.ERROR);
			return;
		}

		Date inicioOcr = dto.getInicioOcr();
		Date fimOcr = dto.getFimOcr();
		Long tempoOcr = dto.getTempoOcr();
		String mensagemErro = dto.getMensagemErro();
		String stackTrace = dto.getStackTrace();
		String resultado = dto.getResultado();
		Date fimPreparacaoImagem = dto.getFimPreparacaoImagem();
		StatusAgendamento statusAgendamento = dto.getStatusAgendamento();
		StatusLogOcr statusLogOcr = null;
		switch (statusAgendamento) {
			case CADASTRADO:
			case PREPARAR:
			case PROCESSAR_OCR:
				statusLogOcr = StatusLogOcr.AGENDADO;
				break;
			case CONCLUIDO_ERRO:
				statusLogOcr = StatusLogOcr.ERRO;
				break;
			case CONCLUIDO_OK:
				statusLogOcr = StatusLogOcr.CONCLUIDO;
				break;
		}
		Long idRegistro = dto.getIdRegistro();

		LogOcr logOcr = logOcrRepository.get(idRegistro);
		StatusLogOcr statusLogOcr2 = logOcr != null ? logOcr.getStatusOcr() : null;
		Long documentoId = logOcr != null ? logOcr.getDocumentoId() : null;
		LogOcr lastLog = documentoId != null ? logOcrRepository.getLastLog(documentoId) : null;
		if(logOcr == null || statusLogOcr == null || statusLogOcr.equals(statusLogOcr2)) {
			systraceThread("retorno ignorado: logOcr=" + logOcr + " lastLog=" + lastLog + " statusLogOcr=" + statusLogOcr + " statusLogOcr2=" + statusLogOcr2);
			return;
		}

		logOcr.setInicioProcessamento(inicioOcr);
		logOcr.setFimProcessamento(fimOcr);
		logOcr.setTempoProcessamento(tempoOcr);
		logOcr.setMensagemErro(mensagemErro);
		logOcr.setResultado(resultado);
		logOcr.setStackTrace(stackTrace);
		logOcr.setStatusOcr(statusLogOcr);
		logOcrRepository.saveOrUpdate(logOcr);

		if(!logOcr.equals(lastLog)) {
			systraceThread("retorno não é do último log: logOcr=" + logOcr + " lastLog=" + lastLog + " statusLogOcr=" + statusLogOcr + " statusLogOcr2=" + statusLogOcr2);
			return;
		}

		try {
			Long imagemId = logOcr.getImagemId();
			CampoOcrDTO[] camposOcr = dto.getCamposOcr();
			if(camposOcr != null) {
				campoOcrService.criarCampos(imagemId, camposOcr);

				Long processoId = logOcr.getProcessoId();
				campoOcrService.preencheProcesso(processoId, documentoId, null);
			}

			if(fimPreparacaoImagem != null) {
				imagemService.atualizaPreparacao(documentoId, true);
			}

			documentoService.atualizaStatusOcr(documentoId);

			if(StatusLogOcr.CONCLUIDO.equals(statusLogOcr) || StatusLogOcr.ERRO.equals(statusLogOcr)) {

				List<Imagem> imagens = imagemService.findByDocumento(documentoId);

				Imagem primeiraImagem = imagens.get(0);
				if(StatusLogOcr.CONCLUIDO.equals(statusLogOcr) && primeiraImagem != null) {
					Documento documento = documentoService.get(documentoId);
					ModeloOcr modeloOcr = documento.getModeloOcr();
					if(modeloOcr != null) {
						Integer newWidth = modeloOcr.getLargura();
						Integer newHeight = modeloOcr.getAltura();

						File file = imagemService.getFile(primeiraImagem);
						if(file != null && file.exists()) {
							try {
								ImageResizer ir = new ImageResizer(file);
								BufferedImage img = ir.getBufferedImage();
								if(img != null) {
									ir.resizeProporcionalW(newWidth, newHeight);
									File img2 = ir.getFile();
									FileUtils.copyFile(img2, file);
								}
							}
							catch (IOException e) {
								String exceptionMessage = DummyUtils.getExceptionMessage(e);
								throw new RuntimeException("Falha ao redimencionar a imagem " + file.getAbsolutePath() + ": " + exceptionMessage, e);
							}
						}
					}
				}

				for (Imagem imagem : imagens) {
					imagemService.atualizarInfoImagem(imagem);
				}
			}

			String tipoErroStr = dto.getTipoErro();
			if(TIPO_ERRO_AGENDAMENTO_NAO_ENCONTRADO.equals(tipoErroStr)) {
				Usuario usuario = logOcr.getUsuario();
				ModeloOcr modeloOcr = logOcr.getModeloOcr();
				Imagem imagem = imagemService.get(imagemId);
				LogOcr logOcr2 = criaLogOcr(usuario, modeloOcr, imagem);

				agendarOcr(logOcr2);
			}
		}
		catch (Exception e) {

			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			String stackTrace2 = ExceptionUtils.getStackTrace(e);

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {

				LogOcr logOcr2 = logOcrRepository.get(idRegistro);
				logOcr2.setMensagemErro("Erro ao processar resultado: " + exceptionMessage);
				logOcr2.setStackTrace(stackTrace2);
				logOcr2.setStatusOcr(StatusLogOcr.ERRO);
				logOcrRepository.saveOrUpdate(logOcr2);

			});
			tw.runNewThread();

			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void verificar(Long logOcrId) {
		NotificacaoDTO dto = ocrWsService.verificar(logOcrId);
		processarNotificacao(dto);
	}

	public List<RelatorioOcrVO> findRelatorioByFiltro(LogOcrFiltro filtro, Integer inicio, Integer max, boolean trazerCampos) {
		return logOcrRepository.findRelatorioByFiltro(filtro, inicio,  max, trazerCampos);
	}
}
