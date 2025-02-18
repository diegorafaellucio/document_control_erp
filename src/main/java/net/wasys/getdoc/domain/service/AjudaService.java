package net.wasys.getdoc.domain.service;

import java.util.ArrayList;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.Resposta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Ajuda;
import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.entity.Ajuda.Tipo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.AjudaRepository;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class AjudaService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private AjudaRepository ajudaRepository;

	public Ajuda get(Long id) {
		return ajudaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvar(Ajuda ajuda, Resposta respostaNovoFilho) {

		Long ajudaId = ajuda.getId();
		ajudaRepository.saveOrUpdate(ajuda);

		Ajuda tmpSuperior = ajuda.getSuperior();

		Tipo tipo = ajuda.getTipo();
		List<Ajuda> inferiores = ajuda.getInferiores();
		if(ajudaId == null) {

			// verificar se tem irm�o para a mesma resposta
			if(tmpSuperior != null) {

				// se sim, mudar o superior do irm�o para este registro
				Long superiorId = tmpSuperior.getId();
				Ajuda superior = ajudaRepository.get(superiorId);
				List<Ajuda> brothers = superior.getInferiores();
				ArrayList<Ajuda> brothers2 = new ArrayList<>(brothers);

				Tipo tipo2 = superior.getTipo();
				Resposta resposta = ajuda.getResposta();
				if (tipo == Tipo.PERGUNTA && tipo2 != Tipo.PERGUNTA) {
					for (Ajuda sister : brothers2) {
						if(!ajuda.equals(sister)) {
							sister.setSuperior(ajuda);
							brothers.remove(sister);
							inferiores.add(sister);
							sister.setResposta(respostaNovoFilho);
							ajudaRepository.saveOrUpdate(sister);
						}
					}
					ajuda.setResposta(Resposta.OK);
					ajudaRepository.saveOrUpdate(ajuda);
				}
				else if (resposta == Resposta.OK) {
					for (Ajuda sister : brothers2) {
						if(!ajuda.equals(sister)) {
							sister.setSuperior(ajuda);
							brothers.remove(sister);
							inferiores.add(sister);
							ajudaRepository.saveOrUpdate(sister);
						}
					}
				}

				if ( tipo2 == Tipo.PERGUNTA && resposta == Resposta.OK ) {
					superior.setTipo(Tipo.DICA);
					ajudaRepository.saveOrUpdate(superior);
				}
			}
		}
		else {
			if (Tipo.PERGUNTA.equals(tipo) && !Resposta.OK.equals(respostaNovoFilho)) {
				for (Ajuda filho : inferiores) {
					filho.setResposta(respostaNovoFilho);
					ajudaRepository.saveOrUpdate(filho);
				}
			}
			else if (!inferiores.isEmpty() ) {
				Ajuda filho = inferiores.get(0);
				Resposta resposta = filho.getResposta();
				if (tipo == Tipo.DICA && resposta != Resposta.OK) {
					filho.setResposta(Resposta.OK);
					ajudaRepository.saveOrUpdate(filho);
				}
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Ajuda ajuda, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(ajuda);

		Integer ordem = ajuda.getOrdem();
		if(ordem == null || ordem == 0) {

			TipoProcesso tipoProcesso = ajuda.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			List<Ajuda> ajudas = ajudaRepository.findByTipoProcesso(tipoProcessoId, null, null);

			for (Ajuda aj : ajudas) {
				Integer ordem2 = aj.getOrdem();
				ordem = Math.max(ordem, ordem2);
			}

			ordem++;
			ajuda.setOrdem(ordem);
		}

		try {
			ajudaRepository.saveOrUpdate(ajuda);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
		logAlteracaoService.registrarAlteracao(ajuda, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Ajuda ajuda) {

		Ajuda superior = ajuda.getSuperior();
		List<Ajuda> inferiores = ajuda.getInferiores();
		for (Ajuda inferior : inferiores) {
			inferior.setSuperior(superior);
			ajudaRepository.saveOrUpdate(inferior);
		}
		Tipo tipo = ajuda.getTipo();
		if(Tipo.PERGUNTA.equals(tipo)) {
			superior.setTipo(Tipo.PERGUNTA);
			ajudaRepository.saveOrUpdate(superior);
			List<Ajuda> inferiores2 = superior.getInferiores();
			inferiores2.clear();
			inferiores2.addAll(inferiores);
		}

		if (inferiores.isEmpty()) {
			excluirRecursivo(ajuda);
		}

		List<Ajuda> irmaos = superior.getInferiores();
		irmaos.remove(ajuda);

		Long ajudaId = ajuda.getId();
		ajudaRepository.deleteById(ajudaId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluirRecursivo(Long id) {
		Ajuda ajuda = ajudaRepository.get(id);
		excluirRecursivo(ajuda);
	}

	private void excluirRecursivo(Ajuda ajuda) {
		List<Ajuda> inferiores = ajuda.getInferiores();
		for (Ajuda ajuda2 : inferiores) {
			excluirRecursivo(ajuda2);
		}
		ajudaRepository.deleteById(ajuda.getId());
	}

	public Ajuda getSuperiorById(Long id) {
		return ajudaRepository.getSuperiorById(id);
	}

	public List<Ajuda> getInferioresBySuperior(Long superiorId) {
		return getInferioresBySuperior(superiorId, false);
	}

	public Ajuda getByTipoProcessoAndObjetivo(Long tipoProcessoId, Objetivo objetivo) {
		return getByTipoProcessoAndObjetivo(tipoProcessoId, objetivo, false);
	}

	public List<Ajuda> getInferioresBySuperior(Long superiorId, boolean recursivo) {
		return ajudaRepository.getInferioresBySuperior(superiorId, recursivo);
	}

	public Ajuda getByTipoProcessoAndObjetivo(Long tipoProcessoId, Objetivo objetivo, boolean recursivo) {
		return ajudaRepository.getByTipoProcessoAndObjetivo(tipoProcessoId, objetivo, recursivo);
	}

	public List<Ajuda> getByTipoProcesso(Long tipoProcessoId) {
		return ajudaRepository.getByTipoProcesso(tipoProcessoId);
	}

	public int countByFiltro(AjudaFiltro filtro) {
		return ajudaRepository.countByFiltro(filtro);
	}

	public List<Ajuda> findByFiltro(AjudaFiltro filtro, Integer inicio, Integer max) {
		return ajudaRepository.findByFiltro(filtro, inicio, max);
	}

	@Transactional(rollbackFor=Exception.class)
	public void subirOrdem(Ajuda ajuda, Usuario usuario) {

		Integer ordem1 = ajuda.getOrdem();
		TipoProcesso tipoProcesso = ajuda.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Ajuda anterior = ajudaRepository.getAnterior(tipoProcessoId, ordem1);

		if(anterior == null) {
			return;
		}

		Integer ordem2 = anterior.getOrdem();

		anterior.setOrdem(ordem1);
		ajuda.setOrdem(ordem2);

		ajudaRepository.saveOrUpdate(anterior);
		ajudaRepository.saveOrUpdate(ajuda);

		logAlteracaoService.registrarAlteracao(anterior, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(ajuda, usuario, TipoAlteracao.ATUALIZACAO);
	}

	@Transactional(rollbackFor=Exception.class)
	public void descerOrdem(Ajuda ajuda, Usuario usuario) {

		Integer ordem1 = ajuda.getOrdem();
		TipoProcesso tipoProcesso = ajuda.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Ajuda proximo = ajudaRepository.getProximo(tipoProcessoId, ordem1);

		if(proximo == null) {
			return;
		}

		Integer ordem2 = proximo.getOrdem();

		proximo.setOrdem(ordem1);
		ajuda.setOrdem(ordem2);

		ajudaRepository.saveOrUpdate(proximo);
		ajudaRepository.saveOrUpdate(ajuda);

		logAlteracaoService.registrarAlteracao(proximo, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(ajuda, usuario, TipoAlteracao.ATUALIZACAO);
	}
}