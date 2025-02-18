package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.repository.RelatorioGeralEtapaRepository;
import net.wasys.getdoc.domain.vo.EtapaVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.HorasUteisCalculator;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatorioGeralEtapaService {

	protected ApplicationContext applicationContext;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private RelatorioGeralEtapaRepository relatorioGeralEtapaRepository;
	@Autowired private RelatorioGeralService relatorioGeralService;

	public RelatorioGeralEtapa get(Long id) {
		return relatorioGeralEtapaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RelatorioGeralEtapa rg) throws MessageKeyException {
		relatorioGeralEtapaRepository.saveOrUpdate(rg);
	}

	public void criaRelatorioGeral(RelatorioGeral rg, HorasUteisCalculator huc) {

		deletarRgesByRelatorioGeral(rg);

		List<EtapaVO> etapaVOS = criaEtapaVoList(rg, huc);

		criarRelatorioGeralEtapa(rg, etapaVOS);
	}

	@Transactional(rollbackFor=Exception.class)
	public void deletarRgesByRelatorioGeral(RelatorioGeral rg) {

		Long relatorioGeralId = rg.getId();
		RelatorioGeralFiltro filtro = new RelatorioGeralFiltro();
		filtro.setRelatorioGeralId(relatorioGeralId);

		List<RelatorioGeralEtapa> rges = relatorioGeralEtapaRepository.findByFiltro(filtro, null, null);
		for (RelatorioGeralEtapa rge : rges) {
			Long relatorioGeralEtapaId = rge.getId();
			relatorioGeralEtapaRepository.deleteById(relatorioGeralEtapaId);
		}
	}

	private List<EtapaVO> criaEtapaVoList(RelatorioGeral rg, HorasUteisCalculator huc) {

		List<EtapaVO> etapaVOS = new ArrayList<>();
		Map<Etapa, EtapaVO> map = new LinkedHashMap<>();
		Etapa etapaAnterior = null;
		EtapaVO etapaVOAnterior = null;

		List<ProcessoLog> logs = getProcessoLogs(rg);
		for (ProcessoLog log : logs) {

			Situacao situacao = log.getSituacao();
			Etapa etapa = log.getEtapa();
			if(etapa == null && situacao != null) continue;

			Date data = log.getData();
			boolean etapaFinal = etapa.getEtapaFinal();
			if(etapaFinal) {
				Usuario usuario = log.getUsuario();
				finalizarUltimaEtapa(map, etapaAnterior, data, usuario);
				etapaAnterior = null;
				if (etapaVOAnterior != null) {
					etapaVOAnterior.setSituacaoDe(log.getSituacaoAnterior());
					if (etapaVOAnterior.getFim() != null) {
						etapaVOAnterior.setSituacaoPara(log.getSituacao());
					}
				}
				etapaVOAnterior = null;
				continue;
			}

			EtapaVO etapaVO = map.get(etapa);
			boolean isNovaEtapa = (etapa != etapaAnterior && etapaVO != null) || (etapaVO == null);
			if(isNovaEtapa) {
				Date prazoLimiteEtapa = log.getPrazoLimiteEtapa();
				Long processoLogId = log.getId();

				etapaVO = new EtapaVO();
				etapaVO.setHuc(huc);
				etapaVO.setEtapa(etapa);
				etapaVO.setInicio(data);
				etapaVO.setPrazoLimite(prazoLimiteEtapa);
				etapaVO.setProcessoLogIdInicial(processoLogId);
				etapaVO.setProcessoLogCorrente(log);
				etapaVO.setSituacaoDe(log.getSituacao());

				etapaVOS.add(etapaVO);

				Usuario usuario = log.getUsuario();
				finalizarUltimaEtapa(map, etapaAnterior, data, usuario);

				if (etapaVOAnterior != null) {
					etapaVOAnterior.setSituacaoDe(log.getSituacaoAnterior());
					if (etapaVOAnterior.getFim() != null) {
						etapaVOAnterior.setSituacaoPara(log.getSituacao());
					}
				}
				etapaVOAnterior = etapaVO;
			}

			boolean contarTempoSlaEtapa = situacao.getContarTempoSlaEtapa();
			if(contarTempoSlaEtapa) {
				map.put(etapa, etapaVO);
			}
			else {
				etapaVO.addTratativasNaoContada();
			}

			etapaAnterior = etapa;
		}

		return etapaVOS;
	}

	private List<ProcessoLog> getProcessoLogs(RelatorioGeral rg) {
		Long processoId = rg.getProcessoId();
		ProcessoLogFiltro filtro = new ProcessoLogFiltro();
		filtro.setProcessoId(processoId);
		filtro.setOrdenar("pl.id", SortOrder.ASCENDING);
		filtro.setFiltrarRoles(false);
		filtro.setApenasComEtapa(true);
		return processoLogService.findByFiltro(filtro, null, null);
	}

	private void registrarUsuarioReferenteAhEtapa(EtapaVO etapaVO, Usuario usuario) {

		if(usuario == null) return;

		Map<Long, String> usuariosMap = etapaVO.getUsuariosMap();
		if(usuariosMap == null) usuariosMap = new LinkedHashMap<>();

		Long usuarioId = usuario.getId();
		String login = usuario.getLogin();
		String nome = usuario.getNome();
		usuariosMap.put(usuarioId, login + " - " + nome);
	}

	private boolean calcularPrazo(EtapaVO etapaVO) {
		ProcessoLog log = etapaVO.getProcessoLogCorrente();
		BigDecimal horasPrazo = log.getHorasPrazoEtapa();
		return horasPrazo.compareTo(new BigDecimal(0)) != 0;
	}

	private void criarRelatorioGeralEtapa(RelatorioGeral rg, List<EtapaVO> etapaVOList) {

		for (EtapaVO etapaVO : etapaVOList) {

			Etapa etapa = etapaVO.getEtapa();
			Date inicio = etapaVO.getInicio();
			Date fim = etapaVO.getFim();
			BigDecimal sla = etapaVO.getSla();
			Date prazoLimite = etapaVO.getPrazoLimite();
			int tratativasNaoContada = etapaVO.getTratativasNaoContada();
			boolean prazoOk = etapaVO.getSlaAtendido();
			Long processoLogIdInicial = etapaVO.getProcessoLogIdInicial();
			Map<Long, String> usuariosMap = etapaVO.getUsuariosMap();
			String usuariosJson = DummyUtils.objectToJson(usuariosMap);

			Situacao situacaoDe = etapaVO.getSituacaoDe();
			Situacao situacaoPara = etapaVO.getSituacaoPara();

			RelatorioGeralEtapa rge = new RelatorioGeralEtapa();
			rge.setTratativasNaoContada(tratativasNaoContada);
			rge.setProcessoLogInicialId(processoLogIdInicial);
			rge.setPrazoLimite(prazoLimite);
			rge.setUsuarios(usuariosJson);
			rge.setRelatorioGeral(rg);
			rge.setDataInicio(inicio);
			rge.setPrazoOk(prazoOk);
			rge.setDataFim(fim);
			rge.setEtapa(etapa);
			rge.setTempo(sla);

			rge.setSituacaoDe(situacaoDe);
			rge.setSituacaoPara(situacaoPara);

			relatorioGeralEtapaRepository.saveOrUpdateWithoutFlush(rge);
		}
	}

	private void finalizarUltimaEtapa(Map<Etapa, EtapaVO> map, Etapa etapaAnterior, Date data, Usuario usuario) {

		EtapaVO etapaVO = map.get(etapaAnterior);
		if(etapaVO != null) {
			etapaVO.addTimeSla(data);
			boolean calcularPrazo = calcularPrazo(etapaVO);
			if(calcularPrazo) {
				Date fim = etapaVO.getFim();
				fim = DummyUtils.parse(DummyUtils.formatDateTime5(fim), "yyyy-MM-dd HH:mm");
				Date prazoLimite = etapaVO.getPrazoLimite();
				prazoLimite = DummyUtils.parse(DummyUtils.formatDateTime5(prazoLimite), "yyyy-MM-dd HH:mm");
				Boolean prazoOk = (fim.before(prazoLimite) || fim.equals(prazoLimite));

				etapaVO.setSlaAtendido(prazoOk);
			}
			else {
				etapaVO.setSlaAtendido(true);
			}

			registrarUsuarioReferenteAhEtapa(etapaVO, usuario);

			map.remove(etapaAnterior);
		}
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralEtapaRepository.findIdsByFiltro(filtro);
	}

	public int countByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralEtapaRepository.countByFiltro(filtro);
	}

	public List<RelatorioGeralEtapa> findByFiltro(RelatorioGeralFiltro filtro, Integer first, Integer pageSize) {
		return relatorioGeralEtapaRepository.findByFiltro(filtro, first, pageSize);
	}

	public List<RelatorioGeralEtapa> findByIds(List<Long> ids){
		return relatorioGeralEtapaRepository.findByIds(ids);
	}

	public String getNomesUsuarios(RelatorioGeralEtapa rge) {

		StringBuilder usuarios = new StringBuilder();

		String usuariosJson = rge.getUsuarios();
		if(StringUtils.isBlank(usuariosJson)) return usuariosJson;

		Map<String, String> usuarioMap = (Map<String, String>) DummyUtils.jsonStringToMap(usuariosJson);
		if(usuarioMap != null) {
			Set<String> usuariosIds = usuarioMap.keySet();
			for (String usuarioIdStr : usuariosIds) {
				String usuario = usuarioMap.get(usuarioIdStr);
				usuarios.append(usuario).append(", ");
			}
		}

		return usuarios.toString();
	}

	public List<Long> findEtapasPendetesDeProcessamento() {
		return relatorioGeralEtapaRepository.findEtapasPendetesDeProcessamento();
	}
}