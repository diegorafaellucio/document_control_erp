package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.EtapaRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EtapaService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private EtapaRepository etapaRepository;

	public Etapa get(Long id) {
		return etapaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Etapa etapa, Usuario usuario) throws MessageKeyException {

		Long etapaId = etapa.getId();
		logAlteracaoService.registrarAlteracao(etapa, usuario, TipoAlteracao.EXCLUSAO);

		try {
			etapaRepository.deleteById(etapaId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Etapa etapas, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(etapas);

		try {
			etapaRepository.saveOrUpdate(etapas);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(etapas, usuario, tipoAlteracao);
	}

	public int countByTipoProcesso(Long tipoProcessoId) {
		return etapaRepository.countByTipoProcesso(tipoProcessoId);
	}

	public List<Etapa> findByTipoProcesso(Long tipoProcessoId) {
		return etapaRepository.findByTipoProcesso(tipoProcessoId);
	}

	public List<String> findNomesAtivas() {
		return etapaRepository.findNomesAtivas();
	}
}
