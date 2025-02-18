package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.SolicitacaoRepository;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class SolicitacaoService {

	@Autowired private SolicitacaoRepository solicitacaoRepository;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private FeriadoService feriadoService;
	@Autowired private ParametroService parametroService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private EmailSmtpService emailSmtpService;

	public List<SolicitacaoVO> findVosPendentesByProcesso(Long processoId, RoleGD roleGD) {

		List<SolicitacaoVO> vos = solicitacaoRepository.findVosPendentesByProcesso(processoId, roleGD);

		preencheVOs(vos);

		return vos;
	}

	public void finalizarByProcesso(Long processoId) {
		List<Solicitacao> solicitacoes = solicitacaoRepository.findNaoFinalizadosByProcesso(processoId);
		Date dataFinalizacao = new Date();
		for (Solicitacao solicitacao : solicitacoes) {
			solicitacao.setDataFinalizacao(dataFinalizacao);
			solicitacaoRepository.saveOrUpdate(solicitacao);
		}
	}

	public List<SolicitacaoVO> findVosByProcesso(Long processoId) {

		List<SolicitacaoVO> vos = solicitacaoRepository.findVosByProcesso(processoId);

		preencheVOs(vos);

		return vos;
	}

	private void preencheVOs(List<SolicitacaoVO> vos) {

		Date agora = new Date();

		for (SolicitacaoVO vo : vos) {

			Solicitacao solicitacao = vo.getSolicitacao();
			Date dataFinalizacao = solicitacao.getDataFinalizacao();
			if(dataFinalizacao == null) {

				ProcessoLog logCriacao = vo.getLogCriacao();
				Processo processo = logCriacao.getProcesso();
				Situacao situacao = processo.getSituacao();
				Long situacaoId = situacao.getId();
				Date prazoLimite = solicitacao.getPrazoLimite();
				HorasUteisCalculator huc = processoService.buildHUC(situacaoId);
				BigDecimal horas = huc.getHoras(agora, prazoLimite);
				BigDecimal horasExpediente = huc.getHorasExpediente();
				String horasRestantes = DummyUtils.getHoras(horasExpediente, horas, false);
				vo.setHorasRestantes(horasRestantes);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarSolicitacao(SolicitacaoVO vo, Processo processo, Usuario usuario) throws Exception {

		AcaoProcesso acao = vo.getAcao();
		ProcessoLog log = null;

		if(AcaoProcesso.SOLICITACAO_CRIACAO.equals(acao)) {
			log = criar(vo, processo, usuario);
		}
		else if(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA.equals(acao)) {
			log = registrarSolucao(vo, processo, usuario);
		}
		else if(AcaoProcesso.SOLICITACAO_ACEITE_RESPOSTA.equals(acao)) {
			log = aceitarResposta(vo, processo, usuario);
		}
		else if(AcaoProcesso.SOLICITACAO_NAO_ACEITE_RESPOSTA.equals(acao)) {
			log = naoAceitarResposta(vo, processo, usuario);
			Solicitacao solicitacao = vo.getSolicitacao();
			processoService.respostaNaoAceite(processo, usuario, solicitacao);
		}
		else if(AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO.equals(acao)) {
			log = recusarSolicitacao(vo, processo, usuario);
		}

		if(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA.equals(acao) || AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO.equals(acao)) {
			Solicitacao solicitacao = vo.getSolicitacao();
			processoService.respostaEncaminhamento(processo, usuario, solicitacao);
		}

		processoService.verificarFluxoTrabalho(processo, usuario, AcaoProcesso.SOLICITACAO_ACEITE_RESPOSTA);

		List<FileVO> arquivos = vo.getArquivos();
		for (FileVO fileVO : arquivos) {

			processoLogAnexoService.criar(log, fileVO);
		}

		if(AcaoProcesso.SOLICITACAO_CRIACAO.equals(acao)) {
			emailSmtpService.enviarNovaSolicitacao(log);
		}
	}

	private ProcessoLog aceitarResposta(SolicitacaoVO vo, Processo processo, Usuario usuario) {

		Date agora = new Date();
		Solicitacao solicitacao = vo.getSolicitacao();
		String observacaoTmp = vo.getObservacaoTmp();

		solicitacao.setDataFinalizacao(agora);

		solicitacaoRepository.saveOrUpdate(solicitacao);

		ProcessoLog log = new ProcessoLog();
		log.setSolicitacao(solicitacao);
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setObservacao(observacaoTmp);
		log.setAcao(AcaoProcesso.SOLICITACAO_ACEITE_RESPOSTA);

		processoLogService.saveOrUpdate(log);

		return log;
	}

	// Ao recusar uma resposta da área, a situação tem que mudar novamente pra Encaminhado (1.2 - Em Análise – Encaminhado ou 2.1 - Acompanhamento - Encaminhado).
	private ProcessoLog naoAceitarResposta(SolicitacaoVO vo, Processo processo, Usuario usuario) {

		Solicitacao solicitacao = vo.getSolicitacao();
		String observacaoTmp = vo.getObservacaoTmp();

		solicitacao.setStatus(StatusSolicitacao.ENVIADA);
		solicitacao.setDataResposta(null);

		solicitacaoRepository.saveOrUpdate(solicitacao);

		ProcessoLog log = new ProcessoLog();
		log.setSolicitacao(solicitacao);
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setObservacao(observacaoTmp);
		log.setAcao(AcaoProcesso.SOLICITACAO_NAO_ACEITE_RESPOSTA);

		processoLogService.saveOrUpdate(log);

		return log;
	}

	private ProcessoLog registrarSolucao(SolicitacaoVO vo, Processo processo, Usuario usuario) {

		Date agora = new Date();
		Solicitacao solicitacao = vo.getSolicitacao();
		String observacaoTmp = vo.getObservacaoTmp();

		boolean adminRole = usuario.isAdminRole();
		boolean analistaRole = usuario.isAnalistaRole();
		if(adminRole || analistaRole) {
			solicitacao.setDataFinalizacao(agora);
		}
		solicitacao.setDataResposta(agora);
		solicitacao.setStatus(StatusSolicitacao.RESPONDIDA);

		solicitacaoRepository.saveOrUpdate(solicitacao);

		ProcessoLog log = new ProcessoLog();
		log.setSolicitacao(solicitacao);
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setObservacao(observacaoTmp);
		log.setAcao(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA);

		processoLogService.saveOrUpdate(log);

		return log;
	}

	private ProcessoLog recusarSolicitacao(SolicitacaoVO vo, Processo processo, Usuario usuario) {

		Date agora = new Date();
		Solicitacao solicitacao = vo.getSolicitacao();
		String observacaoTmp = vo.getObservacaoTmp();

		boolean adminRole = usuario.isAdminRole();
		boolean analistaRole = usuario.isAnalistaRole();
		if(adminRole || analistaRole) {
			solicitacao.setDataFinalizacao(agora);
		}
		solicitacao.setDataResposta(agora);
		solicitacao.setStatus(StatusSolicitacao.RECUSADA);

		solicitacaoRepository.saveOrUpdate(solicitacao);

		ProcessoLog log = new ProcessoLog();
		log.setSolicitacao(solicitacao);
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setObservacao(observacaoTmp);
		log.setAcao(AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO);

		processoLogService.saveOrUpdate(log);

		return log;
	}

	private ProcessoLog criar(SolicitacaoVO vo, Processo processo, Usuario usuario) throws MessageKeyException {

		Date agora = new Date();
		String observacaoTmp = vo.getObservacaoTmp();
		Solicitacao solicitacao = vo.getSolicitacao();
		Subarea subarea = solicitacao.getSubarea();
		Area area = subarea.getArea();
		Integer horasPrazo = area.getHorasPrazo();

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		HorasUteisCalculator huc = processoService.buildHUC(situacaoId);
		if (horasPrazo == null) {
			throw new MessageKeyException("areaEstaSemPrazo.error");
		}
		Date prazoLimite = huc.addHoras(agora, new BigDecimal(horasPrazo));
		solicitacao.setPrazoLimite(prazoLimite);

		solicitacao.setDataCriacao(agora);
		solicitacao.setHorasPrazo(horasPrazo);
		solicitacao.setProcesso(processo);
		solicitacao.setStatus(StatusSolicitacao.ENVIADA);

		solicitacaoRepository.saveOrUpdate(solicitacao);

		ProcessoLog log = new ProcessoLog();
		log.setSolicitacao(solicitacao);
		log.setProcesso(processo);
		log.setUsuario(usuario);
		log.setAcao(AcaoProcesso.SOLICITACAO_CRIACAO);
		log.setObservacao(observacaoTmp);

		processoLogService.saveOrUpdate(log);

		return log;
	}

	public Map<Long, String> getAreasPendentesStr(List<Long> processosIds, Area area) {
		return getAreasPendentesStr(processosIds, area, null);
	}

	public Map<Long, String> getAreasPendentesStr(List<Long> processosIds, Area area, Long solicitcaoExcluidaId) {
		return solicitacaoRepository.getAreasPendentesStr(processosIds, area, solicitcaoExcluidaId);
	}

	public Map<Long, Set<StatusSolicitacao>> getStatusPendentes(List<Long> processosIds, Long areaId) {
		return solicitacaoRepository.getStatusPendentes(processosIds, areaId);
	}

	public boolean existsByAreaProcesso(Long processoId, Long areaId) {
		return solicitacaoRepository.existsByAreaProcesso(processoId, areaId);
	}

	public void notificarAtrasos() {

		Date dataCorte = new Date();
		List<SolicitacaoVO> vos = solicitacaoRepository.findAtrasosArea(dataCorte);

		if(vos.isEmpty()) {
			systraceThread("nenhuma solicitação atrasada.");
			return;
		}

		preencheVOs(vos);

		Map<Area, List<SolicitacaoVO>> map = new HashMap<>();
		for (SolicitacaoVO vo : vos) {

			Solicitacao solicitacao = vo.getSolicitacao();
			Subarea subarea = solicitacao.getSubarea();
			Area area = subarea.getArea();

			List<SolicitacaoVO> list = map.get(area);
			list = list != null ? list : new ArrayList<SolicitacaoVO>();
			map.put(area, list);

			list.add(vo);
		}

		Map<Area, List<Usuario>> mapGestoresArea = new HashMap<>();
		Set<Area> areas = map.keySet();
		for (Area area : areas) {

			List<SolicitacaoVO> list = map.get(area);

			if(!list.isEmpty()) {

				UsuarioFiltro filtro = new UsuarioFiltro();
				filtro.setStatus(StatusUsuario.ATIVO);
				filtro.setRole(RoleGD.GD_AREA);
				filtro.setGestorArea(true);
				filtro.setArea(area);
				List<Usuario> gestoresArea = usuarioService.findByFiltro(filtro);

				if(!gestoresArea.isEmpty() && !list.isEmpty()) {
					mapGestoresArea.put(area, gestoresArea);
					emailSmtpService.enviarNotificacaoAtrasoSolicitacoesArea(gestoresArea, list);
				}
			}
		}

		UsuarioFiltro filtro = new UsuarioFiltro();
		filtro.setStatus(StatusUsuario.ATIVO);
		filtro.setRole(RoleGD.GD_GESTOR);
		filtro.setNotificarAtrasoSolicitacoes(true);
		List<Usuario> gestores = usuarioService.findByFiltro(filtro);

		emailSmtpService.enviarNotificacaoAtrasoSolicitacoes(gestores, map, mapGestoresArea);
	}

	public int countAguardandoAcaoByProcesso(Long processoId, RoleGD roleGD) {
		return solicitacaoRepository.countAguardandoAcaoByProcesso(processoId, roleGD);
	}

	public Date getProximoPrazo(Long processoId, Long areaId) {
		return solicitacaoRepository.getProximoPrazo(processoId, areaId);
	}

	public Solicitacao getById(Long solicitacaoId) {
		return solicitacaoRepository.get(solicitacaoId);
	}
}
