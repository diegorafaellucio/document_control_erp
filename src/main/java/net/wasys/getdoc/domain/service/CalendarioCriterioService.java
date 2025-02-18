package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.repository.CalendarioCriterioRepository;
import net.wasys.getdoc.domain.vo.filtro.CalendarioCriterioFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CalendarioCriterioService {

	@Autowired private CalendarioCriterioRepository calendarioCriterioRepository;
	@Autowired private CalendarioCriterioSituacaoService calendarioCriterioSituacaoService;

	public CalendarioCriterio get(Long id) {
		return calendarioCriterioRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CalendarioCriterio calendarioCriterio, List<Situacao> calendarioCriterioSituacaoSelecionado) throws MessageKeyException {
		try {
			calendarioCriterioRepository.saveOrUpdate(calendarioCriterio);

			if(calendarioCriterioSituacaoSelecionado != null) {
				List<Situacao> situacaoCalendarioCriterioAtuaisList = calendarioCriterioSituacaoService.findSituacaoByCalendarioCriterio(calendarioCriterio);
				for (Situacao situacao : calendarioCriterioSituacaoSelecionado) {
					if(!situacaoCalendarioCriterioAtuaisList.remove(situacao)) {
						CalendarioCriterioSituacao ccs = new CalendarioCriterioSituacao();
						ccs.setCalendarioCriterio(calendarioCriterio);
						ccs.setSituacao(situacao);

						calendarioCriterioSituacaoService.saveOrUpdate(ccs);
					}
				}

				for (Situacao situacao : situacaoCalendarioCriterioAtuaisList) {
					CalendarioCriterioSituacao ccs = calendarioCriterioSituacaoService.findBySituacaoAndCalendarioCriterio(situacao, calendarioCriterio);
					Long calendarioCriterioSituacaoId = ccs.getId();
					calendarioCriterioSituacaoService.deleteById(calendarioCriterioSituacaoId);
				}
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void deleteById(Long calendarioCriterioId) {
		calendarioCriterioRepository.deleteById(calendarioCriterioId);
	}

	public List<CalendarioCriterio> findAll(Integer inicio, Integer max) {
		return calendarioCriterioRepository.findAll(inicio, max);
	}

	public int count() {
		return calendarioCriterioRepository.count();
	}

	public List<CalendarioCriterio> findByFiltro(CalendarioCriterioFiltro filtro) {
		return calendarioCriterioRepository.findByFiltro(filtro);
	}

	public CalendarioCriterio getByFiltro(CalendarioCriterioFiltro filtro) {
		return calendarioCriterioRepository.getByFiltro(filtro);
	}

	public Date getFirstDataFimEmissaoTermo(){
		return calendarioCriterioRepository.getFirstDataFimEmissaoTermo();
	}

	public List<CalendarioCriterio> findByDataFim(Date dataFim){
		return calendarioCriterioRepository.findByDataFim(dataFim);
	}
}
