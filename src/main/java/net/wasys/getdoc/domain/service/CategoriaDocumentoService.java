package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.CategoriaDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.CategoriaDocumentoRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaDocumentoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private CategoriaDocumentoRepository categoriaDocumentoRepository;

	public CategoriaDocumento get(Long id) {
		return categoriaDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CategoriaDocumento categoriaDocumento, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(categoriaDocumento);
		try {
			categoriaDocumentoRepository.saveOrUpdate(categoriaDocumento);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(categoriaDocumento, usuario, tipoAlteracao);
	}

	public List<CategoriaDocumento> findAll(Integer inicio, Integer max) {
		return categoriaDocumentoRepository.findAll(inicio, max);
	}

	public int count() {
		return categoriaDocumentoRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long categoriaDocumentoId, Usuario usuario) throws MessageKeyException {

		CategoriaDocumento feriado = get(categoriaDocumentoId);

		logAlteracaoService.registrarAlteracao(feriado, usuario, TipoAlteracao.EXCLUSAO);

		try {
			categoriaDocumentoRepository.deleteById(categoriaDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<CategoriaDocumento> findAtivos() {
		return categoriaDocumentoRepository.findAtivos();
	}
}
