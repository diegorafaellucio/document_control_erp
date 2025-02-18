package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.StatusLaboralRepository;
import net.wasys.getdoc.domain.vo.filtro.StatusLaboralFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatusLaboralService {

	@Autowired private StatusLaboralRepository statusLaboralRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public StatusLaboral get(Long id) {
		return statusLaboralRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(StatusLaboral statusLaboral, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(statusLaboral);

		try {
			statusLaboralRepository.saveOrUpdate(statusLaboral);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(statusLaboral, usuario, tipoAlteracao);
	}

	public List<StatusLaboral> findByFiltro(StatusLaboralFiltro filtro, Integer inicio, Integer max) {
		return statusLaboralRepository.findByFiltro(filtro, inicio, max);
	}

	public int countByFiltro(StatusLaboralFiltro filtro) {
		return statusLaboralRepository.countByFiltro(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long situacaoAtendimentoId, Usuario usuario) throws MessageKeyException {

		StatusLaboral statusLaboral = get(situacaoAtendimentoId);

		logAlteracaoService.registrarAlteracao(statusLaboral, usuario, TipoAlteracao.EXCLUSAO);

		try {
			statusLaboralRepository.deleteById(situacaoAtendimentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<StatusLaboral> findAtivas() {
		return statusLaboralRepository.findAtivas();
	}

	public List<StatusLaboral> findAtivasSelecionaveis() {

		StatusLaboralFiltro filtro = new StatusLaboralFiltro();
		filtro.setAtiva(true);
		filtro.setFixo(false);

		return statusLaboralRepository.findByFiltro(filtro, null, null);
	}

	public List<StatusLaboral> findByIds(List<Long> ids) {
		return statusLaboralRepository.findByIds(ids);
	}

	public List<StatusLaboral> findAll() {
		return statusLaboralRepository.findAll();
	}

	public StatusLaboral getFixo(StatusAtendimento statusAtendimento) {
		return statusLaboralRepository.getFixo(statusAtendimento);
	}
}
