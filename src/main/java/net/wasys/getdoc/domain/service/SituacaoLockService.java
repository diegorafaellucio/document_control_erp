package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.SituacaoLock;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.repository.SituacaoLockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SituacaoLockService {

	@Autowired private SituacaoLockRepository situacaoLockRepository;

	@Transactional(rollbackFor = Exception.class)
	public void criarSituacaoLock(Situacao situacao) {

		SituacaoLock situacaoLock = new SituacaoLock();
		situacaoLock.setSituacaoId(situacao.getId());

		situacaoLockRepository.saveOrUpdate(situacaoLock);
	}

	public List<SituacaoLock> findSituacoesLockBySubperfis(List<Subperfil> subperfilsAtivos) {
		return situacaoLockRepository.findSituacoesLockBySubperfis(subperfilsAtivos);
	}
}
