package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ConfiguracaoOCRRepository;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfiguracaoOCRService {

	@Autowired private ConfiguracaoOCRRepository configuracaoOCRRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ProcessoService processoService;
	@Autowired private CampoService campoService;

	public ConfiguracaoOCR get(Long id) {
		return configuracaoOCRRepository.get(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void excluir(Long ocrId, Usuario usuario) throws MessageKeyException {

		ConfiguracaoOCR ocr = get(ocrId);
		logAlteracaoService.registrarAlteracao(ocr, usuario, TipoAlteracao.EXCLUSAO);

		try {
			configuracaoOCRRepository.deleteById(ocrId);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<ConfiguracaoOCR> findAll() {
		return configuracaoOCRRepository.findAll();
	}

	public int countAll() {
		return configuracaoOCRRepository.countAll();
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ConfiguracaoOCR configuracaoOCR, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(configuracaoOCR);

		try {
			configuracaoOCRRepository.saveOrUpdate(configuracaoOCR);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(configuracaoOCR, usuario, tipoAlteracao);
	}

    public boolean isFluxoAprovacaoTipificacaoAtivo(Long processoId, Long tipoProcessoId) {
		boolean retorno = false;

		try {
			String instituicao = campoService.findValorByProcessoId(processoId, CampoMap.CampoEnum.INSTITUICAO);

			instituicao = DummyUtils.removerJsonTags(instituicao);

			retorno = configuracaoOCRRepository.isFluxoAprovacaoTipificacaoAtivo(tipoProcessoId, instituicao);
		} catch (Exception e){
			e.printStackTrace();
		}

		return retorno;
	}

    public boolean isFluxoAprovacaoIAAtivo(Long processoId, Long tipoProcessoId) {
		boolean retorno;

		try {
			String instituicao = campoService.findValorByProcessoId(processoId, CampoMap.CampoEnum.INSTITUICAO);

			instituicao = DummyUtils.removerJsonTags(instituicao);

			retorno = configuracaoOCRRepository.isFluxoAprovacaoIAAtivo(tipoProcessoId, instituicao);
		} catch (Exception e){
			retorno = false;
		}

		return retorno;
    }

	public boolean isFluxoAprovacaoOCRAtivo(Long processoId, Long tipoProcessoId) {
		boolean retorno;

		try {
			String instituicao = campoService.findValorByProcessoId(processoId, CampoMap.CampoEnum.INSTITUICAO);

			instituicao = DummyUtils.removerJsonTags(instituicao);

			retorno = configuracaoOCRRepository.isFluxoAprovacaoOCRAtivo(tipoProcessoId, instituicao);
		} catch (Exception e){
			retorno = false;
		}

		return retorno;
	}

	public boolean isFluxoAprovacaoDocumentos(Long processoId, Long tipoProcessoId) {
		boolean retorno;

		try {
			String instituicao = campoService.findValorByProcessoId(processoId, CampoMap.CampoEnum.INSTITUICAO);

			instituicao = DummyUtils.removerJsonTags(instituicao);

			retorno = configuracaoOCRRepository.isFluxoAprovacaoDocumentos(tipoProcessoId, instituicao);
		} catch (Exception e){
			retorno = false;
		}

		return retorno;
	}
}
