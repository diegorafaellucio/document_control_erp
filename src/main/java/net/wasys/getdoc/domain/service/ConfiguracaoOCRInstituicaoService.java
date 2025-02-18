package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCRInstituicao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ConfiguracaoOCRInstituicaoRepository;
import net.wasys.getdoc.domain.repository.ConfiguracaoOCRRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfiguracaoOCRInstituicaoService {

	@Autowired private ConfiguracaoOCRInstituicaoRepository configuracaoOCRInstituicaoRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public ConfiguracaoOCRInstituicao get(Long id) {
		return configuracaoOCRInstituicaoRepository.get(id);
	}

	public List<ConfiguracaoOCRInstituicao> findAll() {
		return configuracaoOCRInstituicaoRepository.findAll();
	}

	public int countAll() {
		return configuracaoOCRInstituicaoRepository.countAll();
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ConfiguracaoOCRInstituicao configuracaoOCR, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(configuracaoOCR);

		try {
			configuracaoOCRInstituicaoRepository.saveOrUpdate(configuracaoOCR);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(configuracaoOCR, usuario, tipoAlteracao);
	}

	public int countByConfiguracaoOCR(Long configuracaoOCRId) {
		return configuracaoOCRInstituicaoRepository.countByConfiguracaoOCR(configuracaoOCRId);
	}

    public List<ConfiguracaoOCRInstituicao> findByConfiguracaoOCR(Long configuracaoOCRId) {
		return configuracaoOCRInstituicaoRepository.findByConfiguracaoOCR(configuracaoOCRId);
    }

	public void excluir(Long configuracaoOCRInstituicaoId, Usuario usuarioLogado) {
		ConfiguracaoOCRInstituicao configuracaoOCRInstituicao = get(configuracaoOCRInstituicaoId);

		logAlteracaoService.registrarAlteracao(configuracaoOCRInstituicao, usuarioLogado, TipoAlteracao.EXCLUSAO);

		try {
			configuracaoOCRInstituicaoRepository.deleteById(configuracaoOCRInstituicaoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}
}
