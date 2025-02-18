package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.repository.TipoCampoRepository;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TipoCampoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoCampoRepository tipoCampoRepository;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		TipoCampo campo = get(id);
		logAlteracaoService.registrarAlteracao(campo, usuario, TipoAlteracao.EXCLUSAO);

		try {
			tipoCampoRepository.deleteById(id);
		}
		catch(RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoProcesso tipoProcesso, TipoCampo tipoCampo, Usuario usuario) throws MessageKeyException {

		String criterioExibicao = tipoCampo.getCriterioExibicao();
		if(StringUtils.contains(criterioExibicao, "'")) {
			throw new MessageKeyException("criterioExibicao-caracterInvalido.error");
		}

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoCampo);

		Integer ordem = tipoCampo.getOrdem();
		if(ordem == null) {

			ordem = 0;

			TipoCampoGrupo grupo = tipoCampo.getGrupo();
			tipoCampo.setGrupo(grupo);
			Long grupoId = grupo.getId();

			List<TipoCampo> tiposCampos = tipoCampoRepository.findByGrupo(grupoId);

			for (TipoCampo tc : tiposCampos) {
				Integer ordem2 = tc.getOrdem();
				ordem = Math.max(ordem, ordem2);
			}

			ordem++;
			tipoCampo.setOrdem(ordem);
		}

		try {
			tipoCampoRepository.saveOrUpdate(tipoCampo);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(tipoCampo, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void espelhar(TipoCampo tipoCampo, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoCampo);

		String nome = tipoCampo.getNome();
		TipoCampoGrupo grupo = tipoCampo.getGrupo();

		List<TipoCampo> tipoCampos = tipoCampoRepository.findByNome(grupo, nome, tipoCampo.getId());
		Map<String, TipoCampo> mapTipoProcessoGrupoCampo = new HashMap<>();
		for (TipoCampo tc: tipoCampos) {
			String key = tc.getGrupo().getTipoProcesso().getNome() + "_" + tc.getGrupo().getNome() + "_" + tc.getNome();
			mapTipoProcessoGrupoCampo.put(key, tc);
		}

		Map<Long, TipoCampo> mapLongCampoPai = new HashMap<>();
		List<CampoAbstract.CampoPai> filiacoes = tipoCampo.getPaisObject();
		for (CampoAbstract.CampoPai cc: filiacoes) {
			TipoCampo tipoCampo1 = get(cc.getPaiId());
			mapLongCampoPai.put(cc.getPaiId(), tipoCampo1);
		}

		List<TipoProcesso> tipoProcessos = tipoProcessoService.findAll(null , null);
		TipoProcesso tipoProcesso = grupo.getTipoProcesso();
		tipoCampoRepository.deatach(tipoProcesso);
		tipoProcessos.remove(tipoProcesso);
		for (TipoProcesso t: tipoProcessos) {
			String key = t.getNome() + "_" + grupo.getNome() + "_" + nome;
			TipoCampo tp1 = mapTipoProcessoGrupoCampo.get(key);
			if (tp1 == null) {
				tp1 = new TipoCampo();
				TipoCampoGrupo grupo2 = tipoCampoGrupoService.getByTipoProcessoAndGrupoNome(t.getId(), grupo.getNome());
				if (grupo2 == null) {
					continue;
				}
				tp1.setGrupo(grupo2);
				tp1.setNome(nome);
			}

			tp1.setObrigatorio(tipoCampo.getObrigatorio());
			tp1.setEditavel(tipoCampo.getEditavel());
			tp1.setTipo(tipoCampo.getTipo());
			tp1.setBaseInterna(tipoCampo.getBaseInterna());
			tp1.setDica(tipoCampo.getDica());
			tp1.setOpcoes(tipoCampo.getOpcoes());
			tp1.setPais(tipoCampo.getPais());
			tp1.setPais(tipoCampo.getCriterioExibicao());
			tp1.setPais(tipoCampo.getCriterioFiltro());

			List<CampoAbstract.CampoPai> filiacoes1 = new ArrayList<>();

			for (CampoAbstract.CampoPai cc: filiacoes) {
				Long paiId = cc.getPaiId();
				String paiNome = cc.getNome();

				TipoCampo tipoCampo2 = get(paiId);
				String tipoCampoGrupoNome2 = tipoCampo2.getGrupo().getNome();

				TipoCampoGrupo grupo1 = tp1.getGrupo();
				String tipoCampo1Nome = tipoCampo2.getNome();
				TipoCampo campoDestino = tipoCampoRepository.getByGrupoNomeAndNome(grupo1.getTipoProcesso().getId(), tipoCampoGrupoNome2, tipoCampo1Nome);
				if (campoDestino != null) {
					Long campoDestinoId = campoDestino.getId();
					CampoAbstract.CampoPai campoPai = new CampoAbstract.CampoPai();
					campoPai.setPaiId(campoDestinoId);
					campoPai.setNome(paiNome);
					filiacoes1.add(campoPai);
				}
			}

			try {
				String pais = null;
				if(filiacoes1 != null && !filiacoes1.isEmpty()) {
					ObjectMapper objectMapper = new ObjectMapper();
					pais = objectMapper.writeValueAsString(filiacoes1);
				}
				tp1.setPais(pais);
				tp1.setCriterioExibicao(tipoCampo.getCriterioExibicao());
				tp1.setCriterioFiltro(tipoCampo.getCriterioFiltro());

				saveOrUpdate(t, tp1, usuario);
			}
			catch (RuntimeException e) {
				HibernateRepository.verifyConstrantViolation(e);
				throw e;
			}
		}

		logAlteracaoService.registrarAlteracao(tipoCampo, usuario, tipoAlteracao);
	}

	public TipoCampo get(Long id) {
		return tipoCampoRepository.get(id);
	}

	public void subirOrdem(TipoCampo tipoCampo, Usuario usuario) {

		Integer ordem1 = tipoCampo.getOrdem();
		TipoCampoGrupo grupo = tipoCampo.getGrupo();
		Long grupoId = grupo.getId();
		TipoCampo anterior = tipoCampoRepository.getAnterior(grupoId, ordem1);

		if(anterior == null) {
			return;
		}

		Integer ordem2 = anterior.getOrdem();

		anterior.setOrdem(ordem1);
		tipoCampo.setOrdem(ordem2);

		tipoCampoRepository.saveOrUpdate(anterior);
		tipoCampoRepository.saveOrUpdate(tipoCampo);

		logAlteracaoService.registrarAlteracao(anterior, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(tipoCampo, usuario, TipoAlteracao.ATUALIZACAO);
	}

	public void descerOrdem(TipoCampo tipoCampo, Usuario usuario) {

		Integer ordem1 = tipoCampo.getOrdem();
		TipoCampoGrupo grupo = tipoCampo.getGrupo();
		Long grupoId = grupo.getId();
		TipoCampo proximo = tipoCampoRepository.getProximo(grupoId, ordem1);

		if(proximo == null) {
			return;
		}

		Integer ordem2 = proximo.getOrdem();

		proximo.setOrdem(ordem1);
		tipoCampo.setOrdem(ordem2);

		tipoCampoRepository.saveOrUpdate(proximo);
		tipoCampoRepository.saveOrUpdate(tipoCampo);

		logAlteracaoService.registrarAlteracao(proximo, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(tipoCampo, usuario, TipoAlteracao.ATUALIZACAO);
	}

	public Map<TipoCampoGrupo, List<TipoCampo>> findMapByTipoProcesso(Long tipoProcessoId) {
		return findMapByTipoProcesso(tipoProcessoId, true);
	}

	public List<String> findNomesGrupoAndCampo() {
		return tipoCampoRepository.findListByGrupoAndNome(null);
	}

	public List<String> findNomesGrupoAndCampoByIds(List<Long> tipoCampoIds) {
		return tipoCampoRepository.findNomesGrupoAndCampoByIds(tipoCampoIds);
	}

	public List<Long> findIdsByNomesGrupoAndCampo(List<String> tipoCampoNomes) {
		return tipoCampoRepository.findIdsByNomesGrupoAndCampo(tipoCampoNomes);
	}

	public LinkedMultiValueMap<String, String> findListByGrupoAndNome(List<Long> tiposProcessoIds) {

		LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		List<String> list = tipoCampoRepository.findListByGrupoAndNome(tiposProcessoIds);
		for(String e: list) {
			String[] split = e.split(" -> ");
			String grupo = split[0];
			String campo = DummyUtils.capitalize(split[1]);
			result.add(grupo, campo);
		}
		return result;
	}

	public Map<TipoCampoGrupo, List<TipoCampo>> findMapByTipoProcesso(Long tipoProcessoId, boolean carregarOpcoesDinamicas) {

		Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoRepository.findMapByTipoProcesso(tipoProcessoId, null);

		if (carregarOpcoesDinamicas) {

			for (List<TipoCampo> list : map.values()) {
				carregarValoresBaseInterna(list);
			}
		}

		return map;
	}

	private void carregarValoresBaseInterna(List<TipoCampo> list) {

		for (TipoCampo tc : list) {

			TipoEntradaCampo tipo = tc.getTipo();
			BaseInterna baseInterna = tc.getBaseInterna();
			String criterioFiltro = tc.getCriterioFiltro();

			if (TipoEntradaCampo.COMBO_BOX_ID.equals(tipo) && baseInterna != null && StringUtils.isBlank(criterioFiltro)) {
				BaseRegistroFiltro filtro = new BaseRegistroFiltro();
				filtro.setBaseInterna(baseInterna);
				List<RegistroValorVO> opcoes = baseRegistroService.findByFiltro(filtro, null, null);
				tc.setOpcoesBaseInterna(opcoes);
			}
		}
	}

	public Map<TipoCampoGrupo, List<TipoCampo>> findMapByTipoProcessoCriacaoProcesso(Long tipoProcessoId) {
		Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoRepository.findMapByTipoProcesso(tipoProcessoId, true);
		return map;
	}

	public List<TipoCampo> findByTipoProcessoSituacao(Long tipoProcessoId, Long situacaoId) {
		return tipoCampoRepository.findByTipoProcessoSituacao(tipoProcessoId, situacaoId, null);
	}

	public List<TipoCampo> findByTipoCampoGrupo(TipoCampoGrupo tipoCampoGrupo, boolean carregarOpcoesDinamicas) {

		List<TipoCampo> tipoCampos = tipoCampoRepository.findByTipoCampoGrupo(tipoCampoGrupo);

		if (carregarOpcoesDinamicas) {
			carregarValoresBaseInterna(tipoCampos);
		}

		return tipoCampos;
	}

	public List<TipoCampo> findByTipoProcesso(Long tipoProcessoId) {
		return tipoCampoRepository.findByTipoProcessoSituacao(tipoProcessoId, null, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public List<TipoCampo> findCamposComOrigemByTipoProcesso(Long tipoProcessoId) {
		return tipoCampoRepository.findCamposComOrigemByTipoProcesso(tipoProcessoId);
	}

	public List<TipoCampo> findPossiveisPais(TipoCampo tipoCampo) {
		return tipoCampoRepository.findPossiveisPais(tipoCampo);
	}

	public List<TipoCampo> findTipoCamposByTipoProcesso(TipoProcesso tipoProcesso, boolean apenasEditavel) {
		return tipoCampoRepository.findTipoCamposByTipoProcesso(tipoProcesso, apenasEditavel);
	}

	public Map<Long, TipoCampo> findDefinemUnicidade(Long tipoProcessoId){
		List<TipoCampo> tiposCampos = tipoCampoRepository.findDefinemUnicidade(tipoProcessoId);
		Map<Long, TipoCampo> mapTipoCampo = new HashMap<>();
		tiposCampos.forEach(item ->
			mapTipoCampo.put(item.getTipoCampoId(), item)
		);
		return mapTipoCampo;
	}

	public List<Long> findBasesInternas() {
		return tipoCampoRepository.findBasesInternas();
	}
}
