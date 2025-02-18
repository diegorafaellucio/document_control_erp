package net.wasys.getdoc.domain.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Regra;
import net.wasys.getdoc.domain.entity.RegraLinha;
import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.repository.RegraLinhaRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class RegraLinhaService {

	@Autowired private RegraLinhaRepository regraLinhaRepository;
	@Autowired private SubRegraService subRegraService;

	public RegraLinha get(Long id) {
		return regraLinhaRepository.get(id);
	}

	public RegraLinha getRaiz(Long regraId) {
		return regraLinhaRepository.getRaiz(regraId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RegraLinha linha) {
		regraLinhaRepository.saveOrUpdate(linha);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long regraLinhaId) throws MessageKeyException {

		try {
			regraLinhaRepository.deleteById(regraLinhaId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public void clearSession() {
		regraLinhaRepository.clear();
	}

	@Transactional(rollbackFor=Exception.class)
	public void duplicar(Regra regra, Regra regra2, Usuario usuario) throws MessageKeyException {
		
		Long regraId = regra.getId();
		RegraLinha raiz = getRaiz(regraId);
		duplicar(regra2, raiz, null, usuario);
	}

	private void duplicar(Regra regra2, RegraLinha linha, RegraLinha linhaPai, Usuario usuario) throws MessageKeyException {
		
		RegraLinha linha2 = new RegraLinha();
		linha2.setLinhaPai(linhaPai);
		linha2.setRegra(regra2);
		regraLinhaRepository.saveOrUpdate(linha2);
		
		Set<SubRegra> subRegras = linha.getSubRegras();
		for (SubRegra subRegra : subRegras) {
			subRegraService.duplicar(subRegra, linha2, usuario);
		}
		
		RegraLinha filha = linha.getFilha();
		if(filha != null) {
			duplicar(regra2, filha, linha2, usuario);
		}
	}
}