package net.wasys.getdoc.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.SubperfilRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class SubperfilService {

	@Autowired private SubperfilRepository repository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public Subperfil get(Long id) {
		return repository.get(id);
	}

	public List<Subperfil> findAll(Integer inicio, Integer max) {
		return repository.findAll(inicio, max);
	}

	public int count() {
		return repository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Subperfil subperfil, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(subperfil);

		try {
			repository.saveOrUpdate(subperfil);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(subperfil, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long entityId, Usuario usuario) throws MessageKeyException {

		Subperfil tipoEvidencia = get(entityId);

		logAlteracaoService.registrarAlteracao(tipoEvidencia, usuario, TipoAlteracao.EXCLUSAO);

		try {
			repository.deleteById(entityId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Subperfil> findAll() {
		return repository.findAll(0, Integer.MAX_VALUE);
	}
}
