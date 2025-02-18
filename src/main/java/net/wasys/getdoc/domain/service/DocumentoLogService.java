package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.DocumentoLog;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.repository.DocumentoLogRepository;
import net.wasys.getdoc.domain.vo.filtro.DocumentoLogFiltro;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DocumentoLogService {

	@Autowired private DocumentoLogRepository documentoLogRepository;
	@Autowired private ApplicationContext applicationContext;

	public DocumentoLog get(Long id) {
		return documentoLogRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public DocumentoLog criaLog(Pendencia pendencia, Usuario usuario, AcaoDocumento acao) {

		Documento documento = pendencia.getDocumento();

		DocumentoLog log = new DocumentoLog();
		log.setPendencia(pendencia);
		log.setDocumento(documento);
		log.setUsuario(usuario);
		log.setAcao(acao);

		documentoLogRepository.saveOrUpdate(log);

		return log;
	}

	@Transactional(rollbackFor=Exception.class)
	public DocumentoLog criaLog(Documento documento, Usuario usuario, AcaoDocumento acao) {
		return criaLog(documento, usuario, acao, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public DocumentoLog criaLog(Documento documento, Usuario usuario, AcaoDocumento acao, String observacao) {

		DocumentoLog log = new DocumentoLog();
		log.setDocumento(documento);
		log.setUsuario(usuario);
		log.setAcao(acao);
		log.setObservacao(observacao);

		documentoLogRepository.saveOrUpdate(log);

		return log;
	}

	public DocumentoLog criaLogErro(Documento documento, Usuario usuario, AcaoDocumento acao, String observacao) {
		Bolso<DocumentoLog> bolso = new Bolso<>();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			DocumentoLogService documentoLogService = applicationContext.getBean(DocumentoLogService.class);
			DocumentoLog log = documentoLogService.criaLog(documento, usuario, acao, observacao);
			bolso.setObjeto(log);
		});
		tw.runNewThread();
		return bolso.getObjeto();
	}

	public List<DocumentoLog> findByProcesso(Long processoId) {
		return documentoLogRepository.findByProcesso(processoId);
	}

	public List<Documento> findDocumentoByFiltro(DocumentoLogFiltro filtro, Integer inicio, Integer max) {
		return documentoLogRepository.findDocumentoByFiltro(filtro, inicio, max);
	}

	public Date getLastDataAlteracao(Long documentoId){
		return documentoLogRepository.getLastDataAlteracao(documentoId);
	}

	public DocumentoLog findLastByDocumentoAndAcaoDocumento(Long documentoId, List<AcaoDocumento> acoesDocumentos) {
		return documentoLogRepository.findLastByDocumentoAndAcaoDocumento(documentoId, acoesDocumentos);
	}
}
