package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.CalendarioCriterio;
import net.wasys.getdoc.domain.entity.CalendarioCriterioSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.repository.CalendarioCriterioSituacaoRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalendarioCriterioSituacaoService {

	@Autowired private CalendarioCriterioSituacaoRepository calendarioCriterioSituacaoRepository;

	public CalendarioCriterioSituacao get(Long id) {
		return calendarioCriterioSituacaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CalendarioCriterioSituacao calendarioCriterioSituacao) throws MessageKeyException {
		try {
			calendarioCriterioSituacaoRepository.saveOrUpdate(calendarioCriterioSituacao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void deleteById(Long calendarioCriterioSituacaoId) {
		calendarioCriterioSituacaoRepository.deleteById(calendarioCriterioSituacaoId);
	}

	public List<CalendarioCriterioSituacao> findAll(Integer inicio, Integer max) {
		return calendarioCriterioSituacaoRepository.findAll(inicio, max);
	}

	public List<Situacao> findSituacaoByCalendarioCriterio(CalendarioCriterio calendarioCriterio) {
		return calendarioCriterioSituacaoRepository.findSituacaoByCalendarioCriterio(calendarioCriterio);
	}

	public CalendarioCriterioSituacao findBySituacaoAndCalendarioCriterio(Situacao situacao, CalendarioCriterio calendarioCriterio) {
		return calendarioCriterioSituacaoRepository.findBySituacaoAndCalendarioCriterio(situacao, calendarioCriterio);
	}

}
