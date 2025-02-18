package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ConfiguracaoLoginAzure;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ConfiguracaoLoginAzureRepository;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoLoginAzureFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfiguracaoLoginAzureService {

	@Autowired private ConfiguracaoLoginAzureRepository configuracaoLoginAzureRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public ConfiguracaoLoginAzure get(Long id) {
		return configuracaoLoginAzureRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ConfiguracaoLoginAzure configuracaoLoginAzure, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(configuracaoLoginAzure);

		try {
			configuracaoLoginAzureRepository.saveOrUpdate(configuracaoLoginAzure);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(configuracaoLoginAzure, usuario, tipoAlteracao);
	}

	public List<ConfiguracaoLoginAzure> findAll(Integer inicio, Integer max) {
		return configuracaoLoginAzureRepository.findAll(inicio, max);
	}

	public int countByFiltro(ConfiguracaoLoginAzureFiltro filtro) {
		return configuracaoLoginAzureRepository.countByFiltro(filtro);
	}

	public List<ConfiguracaoLoginAzure> findByFiltro(ConfiguracaoLoginAzureFiltro filtro, Integer inicio, Integer max) {
		return configuracaoLoginAzureRepository.findByFiltro(filtro, inicio, max);
	}

	public int count() {
		return configuracaoLoginAzureRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long configuracaoLoginAzureId, Usuario usuario) throws MessageKeyException {

		ConfiguracaoLoginAzure configuracaoLoginAzure = get(configuracaoLoginAzureId);

		logAlteracaoService.registrarAlteracao(configuracaoLoginAzure, usuario, TipoAlteracao.EXCLUSAO);

		try {
			Subperfil subperfil = configuracaoLoginAzure.getSubperfil();

			configuracaoLoginAzureRepository.deleteById(configuracaoLoginAzureId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public ConfiguracaoLoginAzure findByGrupoAD(String grupoAD) {
		return configuracaoLoginAzureRepository.findByGrupoAD(grupoAD);
	}
}
