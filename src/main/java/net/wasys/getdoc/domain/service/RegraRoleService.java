package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.RegraRole;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.RegraRoleRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegraRoleService {

	@Autowired private RegraRoleRepository regraRoleRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RegraRole regraRole, Usuario usuario) throws MessageKeyException {
		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(regraRole);
		try {
			regraRoleRepository.saveOrUpdate(regraRole);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long id) {
		regraRoleRepository.deleteById(id);
	}

	public List<RegraRole> findByRegra(Long regraId){
		return regraRoleRepository.findByRegra(regraId);
	}

	public void deleteByRegra(Long regraId){
		regraRoleRepository.deleteByRegra(regraId);
	}

}