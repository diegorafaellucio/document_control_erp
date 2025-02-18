package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.RegraSubperfil;
import net.wasys.getdoc.domain.repository.RegraSubperfilRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegraSubperfilService {

	@Autowired private RegraSubperfilRepository regraSubperfilRepository;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RegraSubperfil regraSubperfil) throws MessageKeyException {

		try {
			regraSubperfilRepository.saveOrUpdate(regraSubperfil);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long id) {
		regraSubperfilRepository.deleteById(id);
	}

	public List<RegraSubperfil> findByRegra(Long regraId){
		return regraSubperfilRepository.findByRegra(regraId);
	}

	public void deleteByRegra(Long regraId){
		regraSubperfilRepository.deleteByRegra(regraId);
	}

}