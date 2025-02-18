package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.TipoEvidenciaRole;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.TipoEvidenciaRoleRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoEvidenciaRoleService {

	@Autowired private TipoEvidenciaRoleRepository tipoEvidenciaRoleRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoEvidenciaRole tipoEvidenciaRole, Usuario usuario) throws MessageKeyException {
		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoEvidenciaRole);
		try {
			tipoEvidenciaRoleRepository.saveOrUpdate(tipoEvidenciaRole);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long id) {
		tipoEvidenciaRoleRepository.deleteById(id);
	}

	public List<TipoEvidenciaRole> findByTipoEvidencia(Long idTipoEvidencia){
		return tipoEvidenciaRoleRepository.findByTipoEvidencia(idTipoEvidencia);
	}

	public void deleteByTipoEvidencia(Long idTipoEvidencia){
		tipoEvidenciaRoleRepository.deleteByTipoEvidencia(idTipoEvidencia);
	}

}