package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.SituacaoRepository;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SituacaoService {

	@Autowired private SituacaoRepository situacaoRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoLockService situacaoLockService;

	public Situacao get(Long id) {
		return situacaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Situacao situacao, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(situacao);

		BigDecimal horasPrazo = situacao.getHorasPrazo();
		if (horasPrazo != null && horasPrazo.equals(new BigDecimal(0))) {
			situacao.setHorasPrazo(null);
		}

		try {
			situacaoRepository.saveOrUpdate(situacao);

			if (TipoAlteracao.CRIACAO.equals(tipoAlteracao)) {
				situacaoLockService.criarSituacaoLock(situacao);
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(situacao, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void espelharConfiguracao(Situacao situacao, Usuario usuario) throws MessageKeyException {

		Set<ProximaSituacao> proximasEspelho = situacao.getProximas();

		SituacaoFiltro filtro = null;
		filtro = new SituacaoFiltro();
		filtro.setNome(situacao.getNome());
		List<Situacao> situacoesPorNome = findByFiltro(filtro, null, null);
		situacoesPorNome.remove(situacao);

		filtro = new SituacaoFiltro();
		List<Situacao> todasSituacaoes = findByFiltro(filtro, null, null);
		Map<String, Situacao> mapTipoNome = new HashMap<>();
		for(Situacao s: todasSituacaoes) {
			mapTipoNome.put(s.getTipoProcesso().getId()+"_" + s.getNome(), s);
		}

		// cria a situacao, se nao existir ainda
		List<TipoProcesso> tipoProcessos = tipoProcessoService.findAll(null, null);
		for (TipoProcesso tp: tipoProcessos) {
			if (mapTipoNome.get(tp.getId() + "_" + situacao.getNome()) == null) {
				Situacao situacao1 = new Situacao();
				situacao1.setAtiva(situacao.getAtiva());
				situacao1.setDistribuicaoAutomatica(situacao.isDistribuicaoAutomatica());
				situacao1.setEncaminhado(situacao.isEncaminhado());
				situacao1.setHorasPrazo(situacao.getHorasPrazo());
				situacao1.setNome(situacao.getNome());
				situacao1.setNotificarAutor(situacao.isNotificarAutor());
				situacao1.setStatus(situacao.getStatus());
				situacao1.setTipoPrazo(situacao.getTipoPrazo());
				situacao1.setSituacaoRetorno(situacao.getSituacaoRetorno());
				situacao1.setTipoProcesso(tp);
				saveOrUpdate(situacao1, usuario);
				mapTipoNome.put(situacao.getTipoProcesso().getId()+"_" + situacao1.getNome(), situacao1);
			}
		}

		Set<String> proximasEspelhosNomeSet = new HashSet<>();
		for (ProximaSituacao ps: proximasEspelho) {
			proximasEspelhosNomeSet.add(ps.getProxima().getNome());
		}

		for(Situacao s: situacoesPorNome) {
			Set<ProximaSituacao> proximas = s.getProximas();
			Map<String, Situacao> mapTipoNomeProximas = new HashMap<>();
			for(ProximaSituacao ps: proximas) {
				mapTipoNomeProximas.put(s.getTipoProcesso().getId() + "_" + ps.getProxima().getNome(), ps.getProxima());
			}

			boolean salvar = false;

			if (!s.getHorasPrazo().equals(situacao.getHorasPrazo())) {
				salvar = true;
				s.setHorasPrazo(situacao.getHorasPrazo());
			}
			if (!s.getStatus().equals(situacao.getStatus())) {
				salvar = true;
				s.setStatus(situacao.getStatus());
			}
			if (!s.getTipoPrazo().equals(situacao.getTipoPrazo())) {
				salvar = true;
				s.setTipoPrazo(situacao.getTipoPrazo());
			}
			if (!s.getAtiva() == situacao.getAtiva()) {
				salvar = true;
				s.setAtiva(situacao.getAtiva());
			}
			if (!s.isDistribuicaoAutomatica() == situacao.isDistribuicaoAutomatica()) {
				salvar = true;
				s.setDistribuicaoAutomatica(situacao.isDistribuicaoAutomatica());
			}
			if (!s.isEncaminhado() == situacao.isEncaminhado()) {
				salvar = true;
				s.setEncaminhado(situacao.isEncaminhado());
			}
			if (!s.isNotificarAutor() == situacao.isNotificarAutor()) {
				salvar = true;
				s.setEncaminhado(situacao.isEncaminhado());
			}

			// remove
			for (Iterator<ProximaSituacao> iterator = proximas.iterator(); iterator.hasNext(); ) {
				ProximaSituacao next = iterator.next();
				if (!proximasEspelhosNomeSet.contains(next.getProxima().getNome())) {
					salvar = true;
					iterator.remove();
				}
			}

			for (ProximaSituacao espelho: proximasEspelho) {
				if (mapTipoNomeProximas.get(s.getTipoProcesso().getId() + "_" + espelho.getProxima().getNome()) == null) {
					Situacao situacao1 = mapTipoNome.get(s.getTipoProcesso().getId() + "_" + espelho.getProxima().getNome());
					if (situacao1 == null) {
						continue;
					}
					ProximaSituacao ps = new ProximaSituacao();
					ps.setAtual(s);
					ps.setProxima(situacao1);
					s.getProximas().add(ps);

					salvar = true;
				}
			}
			if (salvar) {
				saveOrUpdate(s, usuario);
			}
		}

	}

	public List<Situacao> findByTipoProcesso(Long tipoProcessoId) {
		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setTipoProcessoId(tipoProcessoId);
		return situacaoRepository.findByFiltro(filtro, 0, Integer.MAX_VALUE);
	}

	public List<Situacao> findByFiltro(SituacaoFiltro filtro, Integer inicio, Integer max) {
		return situacaoRepository.findByFiltro(filtro, inicio, max);
	}

	public int count(SituacaoFiltro filtro) {
		return situacaoRepository.count(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long situacaoId, Usuario usuario) throws MessageKeyException {

		Situacao situacao = get(situacaoId);

		logAlteracaoService.registrarAlteracao(situacao, usuario, TipoAlteracao.EXCLUSAO);

		try {
			situacaoRepository.deleteById(situacaoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Situacao> findByNome(String nome) {
		return situacaoRepository.findByNome(nome);
	}

	public List<Situacao> findAtivasToSelect(StatusProcesso status) {
		return findAtivasToSelect(status, null);
	}

	public List<Situacao> findAtivasToSelect(StatusProcesso status, Long tipoProcessoId) {
		return situacaoRepository.findAtivasToSelect(status, tipoProcessoId);
	}

	public List<Situacao> findAtivas(StatusProcesso status) {
		return findAtivas(status, null);
	}

	public List<Situacao> findAtivas(StatusProcesso status, Long tipoProcessoId) {
		List<Situacao> list = situacaoRepository.findAtivas(status, tipoProcessoId);
		for (Situacao situacao : list) {
			Hibernate.initialize(situacao);
		}
		return list;
	}

	public List<Situacao> findByIds(List<Long> ids) {
		return situacaoRepository.findByIds(ids);
	}

	public Situacao getFirstByTipoProcesso(Long tipoProcessoId) {

		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setAtiva(true);
		filtro.setTipoProcessoId(tipoProcessoId);

		return getFirst(filtro);
	}

	public Situacao getFirst(SituacaoFiltro filtro) {
		List<Situacao> list = situacaoRepository.findByFiltro(filtro, 0, 1);
		Situacao situacao = list.isEmpty() ? null : list.get(0);
		return situacao;
	}

	public List<Situacao> getProximasByAtual(Long situacaoAtualId){
		return situacaoRepository.getProximasByAtual(situacaoAtualId);
	}

	public Situacao getByNome(Long tipoProcessoId, String nome) {
		return situacaoRepository.getByNome(tipoProcessoId, nome);
	}

	public Situacao getByFinalNome(Long tipoProcessoId, String nome) {
		return situacaoRepository.getByFinalNome(tipoProcessoId, nome);
	}
}
