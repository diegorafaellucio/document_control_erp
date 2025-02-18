package net.wasys.getdoc.domain.service;

import java.util.List;

import net.wasys.getdoc.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.TipoEvidenciaRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class TipoEvidenciaService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoEvidenciaRepository tipoEvidenciaRepository;
	@Autowired private TipoEvidenciaRoleService tipoEvidenciaRoleService;

	public TipoEvidencia get(Long id) {
		return tipoEvidenciaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoEvidencia tipoEvidencia, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoEvidencia);

		try {
			tipoEvidenciaRepository.saveOrUpdate(tipoEvidencia);

			Long tipoEvidenciaId = tipoEvidencia.getId();
			List<TipoEvidenciaRole> tiposEvidenciasRoles = tipoEvidencia.getTiposEvidenciasRoles();

			tiposEvidenciasRoles.forEach( t ->{
				t.setTipoEvidencia(tipoEvidencia);
				tipoEvidenciaRoleService.saveOrUpdate(t, usuario);
			});

			List<TipoEvidenciaRole> salvos = tipoEvidenciaRoleService.findByTipoEvidencia(tipoEvidenciaId);
			if (salvos != null) {
				salvos.removeAll(tiposEvidenciasRoles);
				salvos.forEach(s -> {
					Long id = s.getId();
					tipoEvidenciaRoleService.delete(id);
				});
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(tipoEvidencia, usuario, tipoAlteracao);
	}

	public List<TipoEvidencia> findAll(Integer inicio, Integer max) {
		return tipoEvidenciaRepository.findAll(inicio, max);
	}

	public int count() {
		return tipoEvidenciaRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long tipoEvidenciaId, Usuario usuario) throws MessageKeyException {

		TipoEvidencia tipoEvidencia = get(tipoEvidenciaId);

		logAlteracaoService.registrarAlteracao(tipoEvidencia, usuario, TipoAlteracao.EXCLUSAO);

		try {
			tipoEvidenciaRepository.deleteById(tipoEvidenciaId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<TipoEvidencia> findAtivas() {
		return tipoEvidenciaRepository.findAtivas();
	}
}
