package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.ProcessoRepository;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
public class ProximoProcessoService {

	@Autowired private SessionFactory sessionFactory;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ProcessoRepository processoRepository;
	@Autowired private UsuarioTipoProcessoService usuarioTipoProcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ParametroService parametroService;
	@Autowired private SituacaoLockService situacaoLockService;

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public Processo buscarComLock(Usuario usuario, List<Subperfil> subperfis) throws Exception {

		String value = parametroService.getValor(ParametroService.P.VERIFICAR_LOCK_SITUACAO_PROXIMO_PROCESSO);
		boolean aplicarLockNasSituacoes = StringUtils.isNotBlank(value) && Boolean.valueOf(value);
		if(aplicarLockNasSituacoes) {
			aplicarLockNasSituacoes(subperfis);
		}

		return buscar(usuario, subperfis);
	}

	@Transactional(rollbackFor = Exception.class)
	public Processo buscar(Usuario usuario, List<Subperfil> subperfisUsuario) throws Exception {

		Long analistaId = usuario.getId();
		Usuario analista = usuarioService.get(analistaId);
		Long processoAtualId = analista.getProcessoAtualId();

		systraceThread("buscando processo para o usuario=" + analista + ", subperfilAtivo=" + analista.getSubperfilAtivo() + ", processoAtualId=" + analista.getProcessoAtualId() + ", podeTrocarProcessoAtual=" + analista.getPodeTrocarProcessoAtual());

		if (processoAtualId != null) {

			boolean podeTrocarProcessoAtual = !analista.getOrdemAtividadesFixa();
			podeTrocarProcessoAtual |= analista.getPodeTrocarProcessoAtual();
			if (!podeTrocarProcessoAtual) {

				Processo processo = processoRepository.get(processoAtualId);
				StatusProcesso status = processo.getStatus();
				List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();

				if (statusEmAndamento.contains(status)) {

					TipoProcesso tipoProcesso = processo.getTipoProcesso();
					Long tipoProcessoId = tipoProcesso.getId();
					boolean usuarioAtendeTipoProcesso = usuarioTipoProcessoService.usuarioAtendeTipoProcesso(analistaId, tipoProcessoId);

					if (usuarioAtendeTipoProcesso) {
						throw new MessageKeyException("proximoProcessoBloqueado.error");
					}
				}
			}

			Processo processo = processoService.get(processoAtualId);
			Usuario analista2 = processo.getAnalista();
			if (analista.equals(analista2)) {
				processo.setDataUltimaAcaoAnalista(new Date());
				processoRepository.saveOrUpdate(processo);
				systraceThread("Analista continua com o mesmo processo.");
			}
		}

		Processo processo = null;
		boolean distribuirDemandaAutomaticamente = analista.getDistribuirDemandaAutomaticamente();
		boolean ordemAtividadesFixa = analista.getOrdemAtividadesFixa();
		if (distribuirDemandaAutomaticamente && ordemAtividadesFixa) {
			processo = getProximoProcessoVinculadoAnalista(usuario);
		}

		if (processo == null) {

			ProcessoFiltro filtro = getFiltroProximo(usuario);

			//busca processo por order de prioridade de subperfil
			processo = getProcessoPorSubperfilPrioridade(usuario, analista, subperfisUsuario, filtro);

			//caso não tenham processos AGUARDANDO_ANALISE nem EM_ANALISE, acrescenta EM_ACOMPANHAMENTO na busca.
			if (processo == null) {

				filtro.setProximoPrazoFim(null);
				filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.PENDENCIAS_ACOMPANHAMENTO);
				filtro.setStatusList(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE, StatusProcesso.EM_ACOMPANHAMENTO));

				processo = getProcessoPorSubperfilPrioridade(usuario, analista, subperfisUsuario, filtro);
			}
		}

		if (processo == null) {
			systraceThread("Nenhum processo encontrado.");
			return null;
		}

		processoService.iniciarAnalise(processo, analista);

		Usuario analista2 = processo.getAnalista();
		if (analista2 == null) {
			processo.setAnalista(analista);
			processoRepository.saveOrUpdate(processo);
		}
		else if (!analista2.equals(analista)) {
			systraceThread("Já existe um analista vinculado ao processo=" + processo + ". Analista vinculado=" + analista2);
			return null;
		}

		Long processoAtualId2 = processo.getId();
		usuarioService.atualizarProcessoAtualId(analista, processoAtualId2, false);

		usuario.setProcessoAtualId(processoAtualId2);
		usuario.setDataProcessoAtual(new Date());
		usuario.setPodeTrocarProcessoAtual(false);

		systraceThread("processo encontrado=" + processo);
		return processo;
	}

	private Processo getProcessoPorSubperfilPrioridade(Usuario usuario, Usuario analista, List<Subperfil> subperfisUsuario, ProcessoFiltro filtro) {

		Processo processo = null;

		for (Subperfil subperfil : subperfisUsuario) {

			filtro.setSubperfil(subperfil);

			systraceThread("Buscando pelo subperfil=" + subperfil);
			List<Processo> result = processoRepository.findByFiltro(filtro, 0, 1);

			if (!result.isEmpty()) {

				processo = result.get(0);

				Set<SubperfilSituacao> situacoes = subperfil.getSituacoes();
				for (SubperfilSituacao situacoe : situacoes) {
					Hibernate.initialize(situacoe);
				}

				usuario.setSubperfilAtivo(subperfil);
				usuarioService.atualizarSubperfil(analista, subperfil);

				break;
			}
		}

		return processo;
	}

	private Processo getProximoProcessoVinculadoAnalista(Usuario analista) {

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setAnalista(analista);
		filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.PENDENCIAS_ANALISTA);
		filtro.setStatusList(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE));

		Processo processo = null;
		List<Processo> processosVinculadosAnalista = processoRepository.findByFiltro(filtro, 0, 1);
		if (!processosVinculadosAnalista.isEmpty()) {
			processo = processosVinculadosAnalista.get(0);
		}

		return processo;
	}

	private void aplicarLockNasSituacoes(List<Subperfil> subperfisUsuario) {

		Stopwatch stopwatch = Stopwatch.createStarted();

		List<SituacaoLock> situacoesLock = situacaoLockService.findSituacoesLockBySubperfis(subperfisUsuario);

		situacoesLock.sort(new SuperBeanComparator<>("id asc")); //importante garantir a ordem para não ocorrer deadlock

		Session session = sessionFactory.getCurrentSession();
		if (isNotEmpty(situacoesLock)) {

			for (SituacaoLock sl : situacoesLock) {
				session.lock(sl, LockMode.PESSIMISTIC_WRITE);
			}
		}

		systraceThread("Tempo de espera=" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
	}

	public ProcessoFiltro getFiltroProximo(Usuario analista) {

		ProcessoFiltro filtro = new ProcessoFiltro();

		filtro.setAnalistaProx(analista);

		Date hoje = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
		hoje = DateUtils.addDays(hoje, 1);
		DateUtils.addMilliseconds(hoje, -1);
		filtro.setProximoPrazoFim(hoje);

		filtro.setStatusList(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE));

		filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.PENDENCIAS_ANALISTA);

		Integer intervaloDistribuicao = parametroService.getValor(ParametroService.P.INTERVALO_DISTRIBUICAO, Integer.class);
		Date dataUltimaAlteracaoAnalistaFim = DateUtils.addMinutes(new Date(), -1 * intervaloDistribuicao);
		filtro.setDataUltimaAcaoAnalistaFim(dataUltimaAlteracaoAnalistaFim);

		return filtro;
	}
}
