package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.CampoModeloOcrRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CampoModeloOcrService {


	@Resource(name="resource") private MessageSource resource;
	@Autowired private CampoModeloOcrRepository campoModeloOcrRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public CampoModeloOcr get(Long id) {
		return campoModeloOcrRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CampoModeloOcr campoModeloOcr) {
		campoModeloOcrRepository.saveOrUpdate(campoModeloOcr);
	}

	public int countByModeloOcr(Long modeloOCrId) {
		return campoModeloOcrRepository.countByModeloOcr(modeloOCrId);
	}

	public List<CampoModeloOcr> findByModeloOcr(Long modeloOcrId) {
		return campoModeloOcrRepository.findByModeloOcr(modeloOcrId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long campoModeloOcrId, Usuario usuario) throws MessageKeyException {

		CampoModeloOcr tipoDocumento = get(campoModeloOcrId);

		logAlteracaoService.registrarAlteracao(tipoDocumento, usuario, TipoAlteracao.EXCLUSAO);

		try {
			campoModeloOcrRepository.deleteById(campoModeloOcrId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}
}
