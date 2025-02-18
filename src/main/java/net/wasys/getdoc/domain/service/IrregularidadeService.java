package net.wasys.getdoc.domain.service;

import java.util.*;

import net.wasys.getdoc.domain.vo.filtro.IrregularidadeFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.IrregularidadeRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class IrregularidadeService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private IrregularidadeRepository irregularidadeRepository;

	public Irregularidade get(Long id) {
		return irregularidadeRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Irregularidade irregularidade, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(irregularidade);

		try {
			irregularidadeRepository.saveOrUpdate(irregularidade);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(irregularidade, usuario, tipoAlteracao);
	}

	public List<Irregularidade> findAll(Integer inicio, Integer max) {
		return irregularidadeRepository.findAll(inicio, max);
	}

	public int count() {
		return irregularidadeRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long irregularidadeId, Usuario usuario) throws MessageKeyException {

		Irregularidade irregularidade = get(irregularidadeId);

		logAlteracaoService.registrarAlteracao(irregularidade, usuario, TipoAlteracao.EXCLUSAO);

		try {
			irregularidadeRepository.deleteById(irregularidadeId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Irregularidade> findAtivas() {
		return irregularidadeRepository.findAtivas();
	}

	public int countByFiltro(IrregularidadeFiltro filtro) {
		return irregularidadeRepository.countByFiltro(filtro);
	}

	public List<Irregularidade> findByFiltro(IrregularidadeFiltro filtro, Integer first, Integer pageSize) {
		return irregularidadeRepository.findByFiltro(filtro, first, pageSize);
	}
}