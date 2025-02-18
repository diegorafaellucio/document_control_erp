package net.wasys.getdoc.domain.service;

import java.util.Arrays;
import java.util.List;

import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoAjuda;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.repository.ProcessoAjudaRepository;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class ProcessoAjudaService {

	@Autowired private DocumentoService documentoService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private ProcessoAjudaRepository repository;

	public ProcessoAjuda get(Long id) {
		return repository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void anularResposta(ProcessoAjuda ajuda) {
		ProcessoAjuda proximo = ajuda.getProximo();
		proximo.setMarcado(false);
		repository.saveOrUpdate(proximo);
		ajuda.setMarcado(false);
		ajuda.setProximo(null);
		repository.saveOrUpdate(ajuda);

		ProcessoAjuda superior = ajuda.getSuperior();
		if (superior == null) {
			superior = ajuda;
		}
		while (superior.getSuperior() != null) {
			superior = superior.getSuperior();
		}

		boolean pendencia = proximo.isPendencia();

		Processo processo = ajuda.getProcesso();

		TipoDocumento tipoDocumento = superior.getTipoDocumento();
		if(tipoDocumento != null && pendencia) {
			Long tipoDocumentoId = tipoDocumento.getId();
			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setProcesso(processo);
			filtro.setTipoDocumentoIdList(Arrays.asList(tipoDocumentoId));
			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
			if (!documentos.isEmpty()) {
				for(Documento documento : documentos) {
					Long documentoId = documento.getId();
					//tirar a pendencia do documento
					Pendencia pendenciaDocumento = pendenciaService.getLast(documentoId);
					if (pendenciaDocumento != null) {
						pendenciaService.excluir(pendenciaDocumento.getId());
						documento.setStatus(StatusDocumento.DIGITALIZADO);
						documentoService.saveOrUpdate(documento);
					}
				}
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarResposta(ProcessoAjuda ajuda, ProcessoAjuda proximo, Usuario usuario) throws MessageKeyException {

		ajuda.setMarcado(true);
		ajuda.setProximo(proximo);
		repository.saveOrUpdate(ajuda);

		boolean pendencia = ajuda.isPendencia();
		List<ProcessoAjuda> inferiores = ajuda.getInferiores();
		if(inferiores.isEmpty() || pendencia) {

			ProcessoAjuda superior = ajuda.getSuperior();
			superior = superior != null ? superior : ajuda;
			while (superior.getSuperior() != null) {
				superior = superior.getSuperior();
			}
			TipoDocumento tipoDocumento = superior.getTipoDocumento();
			if(tipoDocumento != null) {

				Processo processo = ajuda.getProcesso();

				Long tipoDocumentoId = tipoDocumento.getId();
				DocumentoFiltro filtro = new DocumentoFiltro();
				filtro.setProcesso(processo);
				filtro.setTipoDocumentoIdList(Arrays.asList(tipoDocumentoId));
				List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
				if (!documentos.isEmpty()) {
					for(Documento documento : documentos) {
						Long documentoId = documento.getId();
						if(pendencia) {
							//pendenciar esse documento
							String texto = ajuda.getTexto();
							String solucao = ajuda.getSolucao();
							String observacao = DummyUtils.stringToHTML(texto + "<br/><br/>" + solucao);
							documentoService.rejeitar(documentoId, null, usuario, observacao);
						} else {
							Pendencia pendenciaDocumento = pendenciaService.getLast(documentoId);
							if(pendenciaDocumento != null) {
								Long pendenciaDocumentoId = pendenciaDocumento.getId();
								pendenciaService.excluir(pendenciaDocumentoId);
								//tirar a pendencia do documento
								documento.setStatus(StatusDocumento.DIGITALIZADO);
								documentoService.saveOrUpdate(documento);
							}
						}
					}
				}
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarSimples(ProcessoAjuda ajuda) {
		repository.saveOrUpdate(ajuda);
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvar(ProcessoAjuda ajuda) {
		repository.saveOrUpdate(ajuda);
		List<ProcessoAjuda> inferiores = ajuda.getInferiores();
		for (ProcessoAjuda processoAjuda : inferiores) {
			salvar(processoAjuda);
		}
	}

	public void excluir(Long id) {
		repository.deleteById(id);
	}

	public ProcessoAjuda getSuperiorById(Long id) {
		return repository.getSuperiorById(id);
	}

	public List<ProcessoAjuda> getInferioresBySuperior(Long superiorId) {
		return getInferioresBySuperior(superiorId, false);
	}

	public ProcessoAjuda getByProcessoAndObjetivo(Long processoId, Objetivo objetivo) {
		return getByProcessoAndObjetivo(processoId, objetivo, false);
	}

	public List<ProcessoAjuda> getInferioresBySuperior(Long superiorId, boolean recursivo) {
		return repository.getInferioresBySuperior(superiorId, recursivo);
	}

	public ProcessoAjuda getByProcessoAndObjetivo(Long processoId, Objetivo objetivo, boolean recursivo) {
		return repository.getByProcessoAndObjetivo(processoId, objetivo, recursivo);
	}

	public List<ProcessoAjuda> findByProcesso(Long processoId) {
		return repository.findByProcesso(processoId);
	}

	public List<ProcessoAjuda> getByProcesso(Long processoId, Long tipoDocumento) {
		return repository.findByProcesso(processoId, tipoDocumento);
	}

	public int countByFiltro(AjudaFiltro filtro) {
		return repository.countByFiltro(filtro);
	}

	public List<ProcessoAjuda> findByFiltro(AjudaFiltro filtro, Integer inicio, Integer max) {
		return repository.findByFiltro(filtro, inicio, max);
	}

	public List<ProcessoAjuda> getPendencias(Long processoId) {
		return repository.getByPendencia(processoId);
	}
}