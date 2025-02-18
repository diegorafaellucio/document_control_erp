package net.wasys.getdoc.domain.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.TipoCampoGrupoRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class TipoCampoGrupoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoCampoGrupoRepository tipoCampoGrupoRepository;

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		TipoCampoGrupo campo = get(id);
		logAlteracaoService.registrarAlteracao(campo, usuario, TipoAlteracao.EXCLUSAO);

		try {
			tipoCampoGrupoRepository.deleteById(id);
		}
		catch(RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoCampoGrupo grupo, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(grupo);

		TipoCampoGrupo subgrupo = grupo.getSubgrupo();
		if (subgrupo != null) {

			List<TipoCampoGrupo> gruposSuperiores = findGruposSuperiores(grupo);
			Set<String> nomesGruposSuperiores = gruposSuperiores.stream().map(TipoCampoGrupo::getNome).collect(Collectors.toSet());

			if (!gruposSuperiores.isEmpty()) {
				throw new MessageKeyException("subgrupoNaoPodeConterOutroSubgrupo.error", nomesGruposSuperiores);
			}
		}

		Integer ordem = grupo.getOrdem();
		if(ordem == null) {

			ordem = 1;

			TipoProcesso tipoProcesso = grupo.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			List<TipoCampoGrupo> grupos = tipoCampoGrupoRepository.findByTipoProcesso(tipoProcessoId);

			for (TipoCampoGrupo grupo2 : grupos) {
				Integer ordem2 = grupo2.getOrdem();
				ordem = Math.max(ordem, ordem2);
			}

			ordem++;
			grupo.setOrdem(ordem);
		}

		try {
			tipoCampoGrupoRepository.saveOrUpdate(grupo);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(grupo, usuario, tipoAlteracao);
	}

	private List<TipoCampoGrupo> findGruposSuperiores(TipoCampoGrupo grupo) {
		return tipoCampoGrupoRepository.findGruposSuperiores(grupo);
	}

	public TipoCampoGrupo get(Long id) {
		return tipoCampoGrupoRepository.get(id);
	}

	public List<TipoCampoGrupo> findByTipoProcesso(Long tipoProcessoId) {
		return tipoCampoGrupoRepository.findByTipoProcesso(tipoProcessoId);
	}

	public List<TipoCampoGrupo> findByTipoProcessoAndNaoAdicionado(Long tipoProcessoId) {
		return tipoCampoGrupoRepository.findByTipoProcessoAndNaoAdicionado(tipoProcessoId);
	}

	public TipoCampoGrupo getByTipoProcessoAndGrupoNome(Long tipoProcessoId, String nomeGrupo) {
		return tipoCampoGrupoRepository.getByTipoProcessoAndGrupoNome(tipoProcessoId, nomeGrupo);
	}

	public Set<String> findNomesByRegraId(Long regraId) {
		return tipoCampoGrupoRepository.findNomesByRegraId(regraId);
	}

	public void subirOrdem(TipoCampoGrupo grupo, Usuario usuario) {

		Integer ordem1 = grupo.getOrdem();
		TipoProcesso tipoProcesso = grupo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		TipoCampoGrupo anterior = tipoCampoGrupoRepository.getAnterior(tipoProcessoId, ordem1);

		if(anterior == null) {
			return;
		}

		Integer ordem2 = anterior.getOrdem();

		anterior.setOrdem(ordem1);
		grupo.setOrdem(ordem2);

		tipoCampoGrupoRepository.saveOrUpdate(anterior);
		tipoCampoGrupoRepository.saveOrUpdate(grupo);

		logAlteracaoService.registrarAlteracao(anterior, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(grupo, usuario, TipoAlteracao.ATUALIZACAO);
	}

	public void descerOrdem(TipoCampoGrupo grupo, Usuario usuario) {

		Integer ordem1 = grupo.getOrdem();
		TipoProcesso tipoProcesso = grupo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		TipoCampoGrupo proximo = tipoCampoGrupoRepository.getProximo(tipoProcessoId, ordem1);

		if(proximo == null) {
			return;
		}

		Integer ordem2 = proximo.getOrdem();

		proximo.setOrdem(ordem1);
		grupo.setOrdem(ordem2);

		tipoCampoGrupoRepository.saveOrUpdate(proximo);
		tipoCampoGrupoRepository.saveOrUpdate(grupo);

		logAlteracaoService.registrarAlteracao(proximo, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(grupo, usuario, TipoAlteracao.ATUALIZACAO);
	}
}
