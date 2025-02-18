package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.UsuarioRepository;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.getdoc.restws.dto.RoleDTO;
import net.wasys.getdoc.restws.dto.UsuarioCampusDTO;
import net.wasys.getdoc.restws.dto.UsuarioDTO;
import net.wasys.getdoc.restws.dto.UsuarioRegionalDTO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.Criptografia;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class UsuarioService {

	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private ResourceService resourceService;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private EmailSmtpService emailService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioTipoProcessoService usuarioTipoProcessoService;
	@Autowired private UsuarioSubperfilService usuarioSubperfilService;
	@Autowired private UsuarioCampusService usuarioCampusService;
	@Autowired private UsuarioRegionalService usuarioRegionalService;
	@Autowired private FeriadoService feriadoService;
	@Autowired private ParametroService parametroService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private AreaService areaService;
	@Autowired private RoleService roleService;
	@Autowired private LoginLogService loginLogService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private GeralService geralService;
	@Autowired private BloqueioProcessoService bloqueioProcessoService;
	@Autowired private StatusLaboralService statusLaboralService;
	@Autowired private LogAtendimentoService logAtendimentoService;

	public Usuario get(Long id) {
		return usuarioRepository.get(id);
	}

	public Usuario getByLogin(String login) {
		return usuarioRepository.getByLogin(login);
	}

	public Usuario autenticar(String login, String senha) {
		senha = Criptografia.encrypt(Criptografia.SENHA, senha);
		return usuarioRepository.autenticar(login, senha);
	}

	@Transactional(rollbackFor=Exception.class)
	public void delDevicePushToken(Long id) {
		usuarioRepository.delDevicePushToken(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void addDevicePushToken(Long id, DeviceSO deviceSO, String devicePushToken) {
		usuarioRepository.addDevicePushToken(id, deviceSO, devicePushToken);
	}

	@Transactional(rollbackFor=Exception.class)
	public void merge(Usuario usuario) throws MessageKeyException {
		try {
			usuarioRepository.merge(usuario);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Usuario usuario, List<String> tiposProcessosSelecionados, Usuario usuarioLogado, List<String> subperfilsSelecionados) throws MessageKeyException {
		saveOrUpdate(usuario, tiposProcessosSelecionados, usuarioLogado, subperfilsSelecionados, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Usuario usuario, List<String> tiposProcessosSelecionados, Usuario usuarioLogado, List<String> subperfilsSelecionados, Map<Long, Integer> niveis) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(usuario);

		Set<Long> presentes = new HashSet<>();
		Set<UsuarioTipoProcesso> tiposProcessos = usuario.getTiposProcessos();
		List<UsuarioTipoProcesso> list = new ArrayList<>(tiposProcessos);
		for (UsuarioTipoProcesso utp : list) {
			TipoProcesso tipoProcesso = utp.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			String tipoProcessoIdStr = tipoProcessoId.toString();
			if(!tiposProcessosSelecionados.contains(tipoProcessoIdStr)) {
				tiposProcessos.remove(utp);
				Long utpId = utp.getId();
				usuarioTipoProcessoService.delete(utpId);
			}
			presentes.add(tipoProcessoId);
		}
		for (String tipoProcessoIdStr : tiposProcessosSelecionados) {
			Long tipoProcessoId = new Long(tipoProcessoIdStr);
			TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			if(!presentes.contains(tipoProcessoId)) {
				UsuarioTipoProcesso utp = new UsuarioTipoProcesso();
				utp.setTipoProcesso(tipoProcesso);
				utp.setUsuario(usuario);
				tiposProcessos.add(utp);
			}
		}

		Map<Long, UsuarioSubperfil> subperfisAtuais = new HashMap<>();
		Set<UsuarioSubperfil> subperfils = usuario.getSubperfils();
		List<UsuarioSubperfil> usList = new ArrayList<>(subperfils);
		for (UsuarioSubperfil usp : usList) {
			Subperfil subperfil = usp.getSubperfil();
			Long subperfilID = subperfil.getId();
			if(!subperfilsSelecionados.contains(subperfilID.toString())) {
				subperfils.remove(usp);
				Long uspId = usp.getId();
				usuarioSubperfilService.delete(uspId);
			}
			subperfisAtuais.put(subperfilID, usp);
		}
		for (String subperfilIdStr : subperfilsSelecionados) {
			Long subperfilId = new Long(subperfilIdStr);
			UsuarioSubperfil usp = subperfisAtuais.get(subperfilId);
			if(usp == null) {
				usp = new UsuarioSubperfil();
				Subperfil subperfil = subperfilService.get(subperfilId);
				usp.setSubperfil(subperfil);
				usp.setUsuario(usuario);
				subperfils.add(usp);
			}
			Integer nivel = niveis != null ? niveis.get(subperfilId) : usp.getNivel();
			nivel = nivel != null ? nivel : 1;
			usp.setNivel(nivel);
		}

		Date agora = new Date();
		usuario.setDataAtualizacao(agora);
		String senha = usuario.getSenha();
		if(StringUtils.isEmpty(senha)) {
			String login = usuario.getLogin();
			usuario.setSenha(login);
			usuario.setDataExpiracaoSenha(agora);

			Calendar cal = Calendar.getInstance();
			cal.setTime(agora);
			cal.add(Calendar.MONTH, 6);
			usuario.setDataExpiracao(cal.getTime());
		}
		StatusUsuario status = usuario.getStatus();
		if(status == null) {
			usuario.setStatus(StatusUsuario.ATIVO);
		}
		Date dataCadastro = usuario.getDataCadastro();
		if(dataCadastro == null) {
			usuario.setDataCadastro(agora);
		}

		RoleGD roleGD = usuario.getRoleGD();
		if(roleGD != null && !roleGD.equals(RoleGD.GD_AREA)){
			usuario.setArea(null);
		}

		if(RoleGD.GD_GESTOR.equals(roleGD)){
			usuario.setSubperfilAtivo(null);
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null && subperfils.isEmpty()){
			usuario.setSubperfilAtivo(null);
		}
		else if(subperfilAtivo == null && !subperfils.isEmpty()) {
			UsuarioSubperfil next = subperfils.iterator().next();
			Subperfil subperfil = next.getSubperfil();
			usuario.setSubperfilAtivo(subperfil);
		}

		try {

			usuario.setDataAlteracao(new Date());
			usuarioRepository.saveOrUpdate(usuario);

			boolean distribuirDemandaAutomaticamente = usuario.getDistribuirDemandaAutomaticamente();

			if(!distribuirDemandaAutomaticamente){
				logAtendimentoService.encerrarUltimoLog(usuario);
				StatusLaboral pausaSistema = statusLaboralService.getFixo(StatusAtendimento.PAUSA_SISTEMA);
				usuario.setStatusLaboral(pausaSistema);
				usuarioRepository.saveOrUpdate(usuario);
			}

			if(distribuirDemandaAutomaticamente){
				processoService.desvincularAnalistaProcesso(usuario, usuarioLogado);
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}

		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, tipoAlteracao);
	}

	public List<Usuario> findByFiltroToSelect(UsuarioFiltro filtro) {
		return usuarioRepository.findByFiltroToSelect(filtro, null, null);
	}

	public List<Usuario> findByFiltro(UsuarioFiltro filtro) {
		return usuarioRepository.findByFiltro(filtro, null, null);
	}

	public List<Usuario> findByFiltro(UsuarioFiltro filtro, Integer inicio, Integer max) {
		return usuarioRepository.findByFiltro(filtro, inicio, max);
	}

	public int countByFiltro(UsuarioFiltro filtro) {
		return usuarioRepository.countByFiltro(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario reiniciarSenha(Usuario usuario, Usuario usuarioLogado) throws Exception {

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			String login = usuario.getLogin();
			String login2 = usuarioLogado != null ? usuarioLogado.getLogin() : null;
			UsuarioDTO retorno = geralService.reiniciarSenha(login, login2);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}
		else {
			return reiniciarSenhaLocal(usuario, usuarioLogado);
		}
	}

	private Usuario reiniciarSenhaLocal(Usuario usuario, Usuario usuarioLogado) {
		Date agora = new Date();
		String login = usuario.getLogin();
		usuario.setSenha(login);
		usuario.setDataExpiracaoSenha(agora);
		usuario.setDataAtualizacao(agora);

		usuarioRepository.saveOrUpdate(usuario);

		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		emailSmtpService.enviarNotificacaoResetSenha(usuarioLogado, usuario, urlSistema);
		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, TipoAlteracao.ATUALIZACAO);
		return usuario;
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario desativarUsuario(Usuario usuario, Usuario usuarioLogado) throws Exception {

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			String login = usuario.getLogin();
			String login2 = usuarioLogado != null ? usuarioLogado.getLogin() : null;
			UsuarioDTO retorno = geralService.desativarUsuario(login, login2, MotivoBloqueioUsuario.TENTATIVAS);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}
		else {
			return desativarUsuarioLocal(usuario, usuarioLogado);
		}
	}

	private Usuario desativarUsuarioLocal(Usuario usuario, Usuario usuarioLogado) {
		Date agora = new Date();
		usuario.setDataBloqueio(agora);
		usuario.setStatus(StatusUsuario.INATIVO);
		usuario.setDataExpiracaoBloqueio(null);
		usuario.setMotivoBloqueio(null);
		usuario.setDataAtualizacao(agora);

		usuarioRepository.saveOrUpdate(usuario);

		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, TipoAlteracao.ATUALIZACAO);

		return usuario;
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario ativarUsuario(Usuario usuario, Usuario usuarioLogado) throws Exception {

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			String login = usuario.getLogin();
			String login2 = usuarioLogado != null ? usuarioLogado.getLogin() : null;
			UsuarioDTO retorno = geralService.ativarUsuario(login, login2);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}
		else {
			return ativarUsuarioLocal(usuario, usuarioLogado);
		}
	}

	private Usuario ativarUsuarioLocal(Usuario usuario, Usuario usuarioLogado) {
		usuario.setDataBloqueio(null);
		usuario.setMotivoBloqueio(null);
		usuario.setStatus(StatusUsuario.ATIVO);
		usuario.setMotivoDesativacao(null);
		usuario.setDataExpiracaoBloqueio(null);
		usuario.setDataAtualizacao(new Date());

		usuarioRepository.saveOrUpdate(usuario);

		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, TipoAlteracao.ATUALIZACAO);

		return usuario;
	}

	public Usuario loginApi(String email) {
		Usuario usuario = usuarioRepository.getByLogin(email, true);
		return usuario;
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario login(String email) throws MessageKeyException {

		Usuario usuario = usuarioRepository.getByLogin(email, false);

		if(usuario == null) {
			return null;
		}

		Area area = usuario.getArea();
		Hibernate.initialize(area);

		Set<UsuarioSubperfil> uss = usuario.getSubperfils();
		Hibernate.initialize(uss);
		if(CollectionUtils.isNotEmpty(uss)) {
			for (UsuarioSubperfil us : uss) {
				Hibernate.initialize(us);
				Subperfil subperfil = us.getSubperfil();
				Hibernate.initialize(subperfil);
			}
		}

		StatusUsuario status = usuario.getStatus();
		if(StatusUsuario.BLOQUEADO.equals(status)) {
			boolean ativado = false;
			Date dataExpiracaoBloqueio = usuario.getDataExpiracaoBloqueio();
			if(dataExpiracaoBloqueio != null && dataExpiracaoBloqueio.before(new Date())) {
				try {
					ativarUsuario(usuario, null);
					ativado = true;
				} catch (Exception e) {
					String message = e.getMessage();
					emailSmtpService.enviarEmailException(message, e);
				}
			}

			if(!ativado) {
				if(dataExpiracaoBloqueio != null) {
					throw new MessageKeyException("acessoBloqueadoTemporariamente.error");
				} else {
					throw new MessageKeyException("acessoBloqueado.error");
				}
			}
		}
		else if(StatusUsuario.INATIVO.equals(status)) {
			throw new MessageKeyException("acessoInativo.error");
		}
		else if(usuario.isAreaRole()) {
			if(area == null) {
				throw new MessageKeyException("acessoAreaInatia.error");
			}
			else if(!area.getAtivo()) {
				throw new MessageKeyException("acessoAreaNaoEspecificada.error");
			}
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null) {
			Hibernate.initialize(subperfilAtivo);
			Set<SubperfilSituacao> situacoes = subperfilAtivo.getSituacoes();
			for (SubperfilSituacao ss : situacoes) {
				Hibernate.initialize(ss);
				Situacao situacao = ss.getSituacao();
				Hibernate.initialize(situacao);
			}
		}

		String login = usuario.getLogin();
		if(!Usuario.PORTAL_POS_GRADUACAO.equals(login)){

			LoginLog loginLog = loginLogService.criarLog(usuario);
			if(usuario.isAnalistaRole()) {
				loginLogService.verificarUltimoLoginLogMenorQueDataDoLoginLogAtual(loginLog);
			}

			Long usuarioId = usuario.getId();
			Long loginLogId = loginLog.getId();

			usuario.setLogoffListener(() -> {
				bloqueioProcessoService.desbloquearByUsuario(usuarioId);
				loginLogService.registarLogoff(loginLogId);
			});
		}

		usuario.setDataUltimoAcesso(new Date());
		usuarioRepository.saveOrUpdate(usuario);

		initUsuario(usuario);

		return usuario;
	}

	public void initUsuario(Usuario usuario) {

		Set<Role> roles = usuario.getRoles();
		for (Role role : roles) {
			Hibernate.initialize(role);
			usuarioRepository.deatach(role);
		}

		Subperfil subperfil = usuario.getSubperfilAtivo();
		if (subperfil != null) {
			usuarioRepository.deatach(subperfil);
			FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
			if (filaConfiguracao != null) {
				Hibernate.initialize(filaConfiguracao);
				usuarioRepository.deatach(filaConfiguracao);
			}
		}

		Set<UsuarioRegional> regionais = usuario.getRegionais();
		if(regionais != null) {
			for (UsuarioRegional ur : regionais) {
				Hibernate.initialize(ur);
				BaseRegistro regional = ur.getRegional();
				Hibernate.initialize(regional);
				usuarioRepository.deatach(ur);
			}
		}

		Set<UsuarioCampus> campus = usuario.getCampus();
		if(campus != null) {
			for (UsuarioCampus uc : campus) {
				Hibernate.initialize(uc);
				BaseRegistro campus1 = uc.getCampus();
				Hibernate.initialize(campus1);
				usuarioRepository.deatach(uc);
			}
		}

		usuarioRepository.deatach(usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario bloquear(String login) throws Exception {

		if(geralService.isSincronismoHabilitado()) {
			UsuarioDTO retorno = geralService.bloquearUsuario(login, MotivoBloqueioUsuario.TENTATIVAS);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}
		else {
			return bloquearUsuarioLocal(login);
		}
	}

	private Usuario bloquearUsuarioLocal(String login) throws MessageKeyException {

		Usuario usuario = usuarioRepository.getByLogin(login);

		if(usuario != null) {

			int tempoBloqueioMin = 10;
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, tempoBloqueioMin);
			Date dataDesbloqueio = c.getTime();

			Date agora = new Date();
			usuario.setDataBloqueio(agora);
			usuario.setStatus(StatusUsuario.BLOQUEADO);
			usuario.setDataExpiracaoBloqueio(dataDesbloqueio);
			usuario.setMotivoBloqueio(MotivoBloqueioUsuario.TENTATIVAS);
			usuario.setDataAtualizacao(agora);

			usuarioRepository.saveOrUpdate(usuario);
		}

		return usuario;
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario enviarRedefinicaoSenha(String login) throws Exception {
		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			UsuarioDTO retorno = geralService.enviarRedefinicaoSenha(login);
			if(retorno == null) {
				throw new MessageKeyException("usuarioInvalido.error");
			}
			return atualizar(retorno, null);
		}
		else {
			return enviarRedefinicaoSenhaLocal(login);
		}
	}

	private Usuario enviarRedefinicaoSenhaLocal(String login) {

		Usuario usuario = usuarioRepository.getByLogin(login);

		if(usuario == null) {
			throw new MessageKeyException("conta-nao-encontrada.error");
		}

		String senha = usuario.getSenha();

		String aux = encodeLogin(login, senha);

		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);

		String link = urlSistema + "/trocar-senha/" + aux;

		emailService.enviarRedefinicaoSenha(usuario, link);

		return usuario;
	}

	public static String encodeLogin(String login, String senha) {
		String aux = login + "\n\t\n" + senha;
		byte[] encodeBase64 = Base64.encodeBase64(aux.getBytes());
		aux = new String(encodeBase64);
		return aux;
	}

	public static String[] decodeLogin(String str) {
		byte[] decodeBase64 = Base64.decodeBase64(str.getBytes());
		String aux = new String(decodeBase64);

		int indexOf = aux.indexOf("\n\t\n");
		if(indexOf <= 0) {
			return null;
		}

		String login = aux.substring(0, indexOf);
		String senha = aux.substring(indexOf + 3);
		return new String[]{login, senha};
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario atualizarSenha(String login, String novaSenha) throws Exception {
		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			UsuarioDTO retorno = geralService.atualizarSenha(login, novaSenha);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}
		else {
			return atualizarSenhaLocal(login, novaSenha);
		}
	}

	private Usuario atualizarSenhaLocal(String login, String novaSenha) {

		Usuario usuario = usuarioRepository.getByLogin(login);
		if(usuario == null) {
			throw new MessageKeyException("usuarioSenhaInvalido.error");
		}

		String senhaAnterior = usuario.getSenha();
		if(senhaAnterior.equals(novaSenha)) {
			throw new MessageKeyException("usuarioSenhaRepetido.error");
		}

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 90);
		Date dataExpiracaoSenha = c.getTime();
		dataExpiracaoSenha = DateUtils.truncate(dataExpiracaoSenha, Calendar.DAY_OF_MONTH);
		Date agora = new Date();
		usuario.setDataExpiracaoSenha(dataExpiracaoSenha);
		usuario.setDataAtualizacao(agora);
		usuario.setSenha(novaSenha);

		logAlteracaoService.registrarAlteracao(usuario, usuario, TipoAlteracao.ATUALIZACAO);
		usuarioRepository.saveOrUpdate(usuario);
		return usuario;
	}

	public Date getUltimaDataSincronizacao() {
		return usuarioRepository.getUltimaDataSincronizacao();
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarProcessoAtualId(Usuario usuario, Long processoAtualId, boolean podeTrocarProcessoAtual) {

		Long processoOldId = usuario.getProcessoAtualId();

		usuario.setProcessoAtualId(processoAtualId);
		usuario.setDataProcessoAtual(new Date());
		usuario.setPodeTrocarProcessoAtual(podeTrocarProcessoAtual);

		Long usuarioId = usuario.getId();
		usuarioRepository.atualizarProcessoAtualId(usuarioId, processoAtualId, podeTrocarProcessoAtual);

		if(processoOldId != null && !processoOldId.equals(processoAtualId)) {
			processoService.avancarProcesso(processoOldId, usuario);
		}
	}

	public Map<Long, Date> getDatasUltimosAcessos(Date dataCorte) {
		return usuarioRepository.getDatasUltimosAcessos(dataCorte);
	}

	public void validarHorarioAcesso(Usuario usuario) throws MessageKeyException {

		RoleGD roleGD = usuario.getRoleGD();
		if (RoleGD.GD_ADMIN.equals(roleGD)) {
			return;
		}

		Boolean pafsf = parametroService.getValorCache(P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO, Boolean.class);
		String[] expedienteArray = parametroService.getExpedienteAcesso();
		Expediente expediente = new Expediente(expedienteArray);
		Date agora = new Date();

		if (!pafsf) {

			Calendar c = DateUtils.toCalendar(agora);
			boolean diaUtil = c.get(Calendar.DAY_OF_WEEK) != 1 && c.get(Calendar.DAY_OF_WEEK) != 7;
			if(!diaUtil) {
				throw new MessageKeyException("acessoBloqueadoFimSemanaFeriado.error");
			}

			List<Date> feriados = feriadoService.findAllDatas();
			Date hoje = DateUtils.truncate(agora, Calendar.DAY_OF_MONTH);

			if (feriados.contains(hoje)) {
				throw new MessageKeyException("acessoBloqueadoFimSemanaFeriado.error");
			}
		}

		if(!expediente.isHorarioExpediente(agora, !pafsf)) {
			throw new MessageKeyException("acessoBloqueadoHorario.error", expedienteArray[0], expedienteArray[3]);
		}
	}

	public Usuario getUsuarioCliente() {
		Usuario usuario = new Usuario();
		usuario.setId(Usuario.CLIENTE_ID);
		return usuario;
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long usuarioId, Usuario usuarioLogado) throws MessageKeyException {

		Usuario usuario = usuarioRepository.get(usuarioId);
		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, TipoAlteracao.EXCLUSAO);
		try {
			usuarioRepository.deleteById(usuarioId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario trocarSubperfil(Usuario usuario, Long subperfilId) {

		Date agora = new Date();
		usuario.setDataAtualizacao(agora);
		Subperfil subperfil = null;
		Set<UsuarioSubperfil> subperfils = usuario.getSubperfils();
		for (UsuarioSubperfil us : subperfils) {
			Subperfil subperfil2 = us.getSubperfil();
			Long subperfilId2 = subperfil2.getId();
			if(subperfilId.equals(subperfilId2)) {
				subperfil = subperfil2;
			}
		}
		if(subperfil == null) {
			throw new MessageKeyException("subperfilNaoHabilitadoUsuario.error", subperfilId);
		}

		usuario.setSubperfilAtivo(subperfil);
		usuarioRepository.saveOrUpdate(usuario);
		logAlteracaoService.registrarAlteracao(usuario, usuario, TipoAlteracao.ATUALIZACAO);
		return usuario;
	}

	public boolean atendeSituacao(Long analistaId, Long situacaoId, Processo processo) {

		Usuario usuario = usuarioRepository.get(analistaId);
		if(usuario.isAdminRole()) {
			return true;
		}

		boolean atendeSituacao = usuarioRepository.atendeSituacao(analistaId, situacaoId);
		return atendeSituacao;
	}

	public List<Usuario> findAnalistasAtivos() {
		UsuarioFiltro f = new UsuarioFiltro();
		f.setRole(RoleGD.GD_ANALISTA);
		f.setStatus(StatusUsuario.ATIVO);
		return findByFiltro(f);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizar(UsuarioDTO[] usuarios) throws MessageKeyException {

		systraceThread("usuarios: " + (usuarios != null ? usuarios.length : "null"));
		if(usuarios == null) {
			return;
		}

		for (UsuarioDTO dto : usuarios) {
			atualizar(dto, null, true);
		}
	}

	private Usuario atualizar(UsuarioDTO dto, Usuario usuario) throws MessageKeyException {
		return atualizar(dto, usuario, false);
	}

	private Usuario atualizar(UsuarioDTO dto, Usuario usuario, boolean isSincronismo) throws MessageKeyException {

		Long geralId = dto.getId();
		Long areaId = dto.getAreaId();
		Date dataAtualizacao = dto.getDataAtualizacao();
		Date dataBloqueio = dto.getDataBloqueio();
		Date dataCadastro = dto.getDataCadastro();
		Date dataExpiracaoBloqueio = dto.getDataExpiracaoBloqueio();
		Date dataExpiracaoSenha = dto.getDataExpiracaoSenha();
		Date dataUltimoAcesso = dto.getDataUltimoAcesso();
		String email = dto.getEmail();
		String login = dto.getLogin();
		String motivoBloqueioStr = dto.getMotivoBloqueio();
		MotivoBloqueioUsuario motivoBloqueio = StringUtils.isNotBlank(motivoBloqueioStr) ? MotivoBloqueioUsuario.valueOf(motivoBloqueioStr) : null;
		String motivoDesativacaoStr = dto.getMotivoDesativacao();
		MotivoDesativacaoUsuario motivoDesativacao = StringUtils.isNotBlank(motivoDesativacaoStr) ? MotivoDesativacaoUsuario.valueOf(motivoDesativacaoStr) : null;
		String nome = dto.getNome();
		RoleDTO[] roles = dto.getRoles();
		String senha = dto.getSenha();
		String senhasAnteriores = dto.getSenhasAnteriores();
		String statusStr = dto.getStatus();
		StatusUsuario status = StringUtils.isNotBlank(statusStr) ? StatusUsuario.valueOf(statusStr) : null;
		String telefone = dto.getTelefone();
		Date dataExpiracao = dto.getDataExpiracao();
		UsuarioRegionalDTO[] regionais = dto.getRegionais();
		UsuarioCampusDTO[] campus = dto.getCampus();

		systraceThread(nome + " " + Arrays.asList(roles));

		String mode = DummyUtils.getMode();
		if("homolog".equals(mode) || "dev".equals(mode)) {
			if(usuario == null)
				usuario = usuarioRepository.getByLogin(login);
			if(usuario == null)
				usuario = usuarioRepository.getByGeralId(geralId);
		}
		else {
			if(usuario == null)
				usuario = usuarioRepository.getByGeralId(geralId);
			if(usuario == null)
				usuario = usuarioRepository.getByLogin(login);
		}

		usuario = usuario != null ? usuario : new Usuario();
		Date dataUltimoAcesso1 = usuario.getDataUltimoAcesso();
		dataUltimoAcesso = DummyUtils.max(dataUltimoAcesso, dataUltimoAcesso1);

		if(isSincronismo) {
			usuario.setDataSincronizacao(dataAtualizacao);
		}
		usuario.setDataAtualizacao(dataAtualizacao);
		usuario.setDataBloqueio(dataBloqueio);
		usuario.setDataCadastro(dataCadastro);
		usuario.setDataExpiracaoBloqueio(dataExpiracaoBloqueio);
		usuario.setDataExpiracaoSenha(dataExpiracaoSenha);
		usuario.setDataUltimoAcesso(dataUltimoAcesso);
		usuario.setEmail(email);
		usuario.setGeralId(geralId);
		usuario.setLogin(login);
		usuario.setMotivoBloqueio(motivoBloqueio);
		usuario.setMotivoDesativacao(motivoDesativacao);
		usuario.setNome(nome);
		usuario.setSenha(senha);
		usuario.setSenhasAnteriores(senhasAnteriores);
		usuario.setStatus(status);
		usuario.setTelefone(telefone);
		usuario.setDataExpiracao(dataExpiracao);

		Set<String> rolesStr = new LinkedHashSet<>();
		for (RoleDTO roleDTO : roles) {
			String nome2 = roleDTO.getNome();
			nome2 = nome2.replace("CAPTACAO_", "GD_");
			rolesStr.add(nome2);
		}

		Area area = null;
		if (areaId != null) {
			area = areaService.getByGeralId(areaId);
		}
		usuario.setArea(area);

		List<Long> regionaisList = new ArrayList<>();
		if(regionais != null) {
			for (UsuarioRegionalDTO urDTO : regionais) {
				Long codReginal = urDTO.getCodRegional();
				regionaisList.add(codReginal);
			}
		}

		List<Long> campusList = new ArrayList<>();
		if(campus != null) {
			for (UsuarioCampusDTO ucDTO : campus) {
				Long codCampus = ucDTO.getCodCampus();
				campusList.add(codCampus);
			}
		}

		saveOrUpdate(usuario, new ArrayList<>(rolesStr), regionaisList, campusList, null);

		return usuario;
	}

	private void saveOrUpdate(Usuario usuario, List<String> rolesNovar, List<Long> regionais, List<Long> campus, Usuario usuarioLogado) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(usuario);

		atualizaRoles(usuario, rolesNovar);
		atualizarUsuarioRegionais(usuario, regionais);
		atualizarUsuarioCampus(usuario, campus);

		try {
			String login = usuario.getLogin();
			//pra evitar not-null property references a null...
			if(StringUtils.isBlank(login)) {
				usuario.setLogin("aux");
			}

			usuarioRepository.saveOrUpdate(usuario);

			//define o login só agora para evitar contar a sequence desnecessáriamente, em caso de erro na linha anterior
			Date dataCadastro = usuario.getDataCadastro();
			if(dataCadastro == null) {

				usuario.setDataCadastro(new Date());
				usuario.setDataExpiracao(new Date());

				usuarioRepository.saveOrUpdate(usuario);
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(usuario, usuarioLogado, tipoAlteracao);
	}

	public void atualizaRoles(Usuario usuario, List<String> rolesStr) {

		if(rolesStr == null) {
			return;
		}

		Set<Role> roles = usuario.getRoles();
		List<Role> roles2 = new ArrayList<>(roles);
		for (Role role : roles2) {

			String roleNome = role.getNome();
			boolean existe = rolesStr.remove(roleNome);
			if(!existe) {
				Long roleId = role.getId();
				roleService.delete(roleId);
				roles.remove(role);
			}
		}

		for (String nomeRole : rolesStr) {

			Role role2 = new Role();
			role2.setUsuario(usuario);
			role2.setNome(nomeRole);
			roles.add(role2);
		}
	}

	private void atualizarUsuarioRegionais(Usuario usuario, List<Long> regionais) {

		if(regionais == null) {
			return;
		}

		BaseInterna baseInterna = baseInternaService.get(BaseInterna.REGIONAL_ID);
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(baseInterna);
		List<RegistroValorVO> regionalVO = baseRegistroService.findByFiltro(filtro, null, null);

		Set<UsuarioRegional> regional = usuario.getRegionais();
		List<UsuarioRegional> regionais2 = new ArrayList<>(regional);
		for (UsuarioRegional rg : regionais2) {

			BaseRegistro baseRegistro = rg.getRegional();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
            chaveUnicidade = chaveUnicidade.replaceAll("\\[\"(.*)\"\\]", "$1");
			boolean existe = regionais.remove(new Long(chaveUnicidade));
			if(!existe) {
				Long rgId = rg.getId();
				usuarioRegionalService.delete(rgId);
				regional.remove(rg);
			}
		}

		for (Long chaveUnicidade : regionais) {
			String chaveUnicidadeStr = String.valueOf(chaveUnicidade);
			for(RegistroValorVO vo : regionalVO) {
				String codRegional = vo.getValor(TipoCampo.COD_REGIONAL);
				if(codRegional.equals(chaveUnicidadeStr)) {
					BaseRegistro baseRegistro = vo.getBaseRegistro();
					UsuarioRegional usuarioRegional = new UsuarioRegional();
					usuarioRegional.setUsuario(usuario);
					usuarioRegional.setRegional(baseRegistro);
					regional.add(usuarioRegional);
				}
			}
		}
	}

	private void atualizarUsuarioCampus(Usuario usuario, List<Long> campus) {

		if(campus == null) {
			return;
		}

		BaseInterna baseInterna = baseInternaService.get(BaseInterna.CAMPUS_ID);
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(baseInterna);
		List<RegistroValorVO> campusVO = baseRegistroService.findByFiltro(filtro, null, null);

		Set<UsuarioCampus> campusSet = usuario.getCampus();
		List<UsuarioCampus> campus2 = new ArrayList<>(campusSet);
		for (UsuarioCampus cp : campus2) {

			BaseRegistro baseRegistro = cp.getCampus();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
            chaveUnicidade = chaveUnicidade.replaceAll("\\[\"(.*)\"\\]", "$1");
            boolean existe = campus.remove(new Long(chaveUnicidade));
			if(!existe) {
				Long cpId = cp.getId();
				usuarioCampusService.delete(cpId);
				campusSet.remove(cp);
			}
		}

		for (Long chaveUnicidade : campus) {
			String chaveUnicidadeStr = String.valueOf(chaveUnicidade);
			for(RegistroValorVO vo : campusVO) {
				String codRegional = vo.getValor(TipoCampo.COD_CAMPUS);
				if(codRegional.equals(chaveUnicidadeStr)) {
					BaseRegistro baseRegistro = vo.getBaseRegistro();
					UsuarioCampus usuarioCampus = new UsuarioCampus();
					usuarioCampus.setUsuario(usuario);
					usuarioCampus.setCampus(baseRegistro);
					campusSet.add(usuarioCampus);
				}
			}
		}
	}

	public Usuario getByGeralId(Long geralId) {
		return usuarioRepository.getByGeralId(geralId);
	}

	public boolean estaEmAnalise(Usuario analista) {
		StatusLaboral statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		return StatusAtendimento.EM_ANALISE.equals(statusAtendimento);
	}

	public void programarSituacaoAtendimento(Usuario analista, StatusLaboral statusLaboral) {
		analista.setProgramouStatusLaboral(true);
		analista.setStatusLaboralProgramado(statusLaboral);
		merge(analista);
	}

	@Transactional(rollbackFor=Exception.class)
	public Usuario prorrogarAcesso(Usuario usuarioSalvo, Usuario usuario) throws Exception {
		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		if(StringUtils.isNotBlank(geralEndpoint)) {
			String login = usuarioSalvo.getLogin();
			String loginAutor = usuario.getLogin();
			UsuarioDTO retorno = geralService.prorrogarAcesso(login, loginAutor);
			if(retorno != null) {
				return atualizar(retorno, null);
			}
			return null;
		}else{
			return prorrogarAcessoLocal(usuarioSalvo, usuario);
		}
	}

	public Usuario prorrogarAcessoLocal(Usuario usuarioSalvo, Usuario usuario) {

		return null;
	}

	public String validaChaveRedefinicaoSenha(String key) {

		String[] decode = decodeLogin(key);

		if(decode == null) {
			throw new MessageKeyException("esqueciSenhaLinkInvalido.error");
		}

		String login = decode[0];
		String senha = decode[1];

		Usuario usuario = getByLogin(login);
		String senha2 = usuario.getSenha();
		senha2 = Criptografia.decrypt(Criptografia.SENHA, senha2);

		if(!senha2.equals(senha)) {
			throw new MessageKeyException("esqueciSenhaLinkInvalido.error");
		}

		return login;
	}

	public Boolean verificarAcessoPoc(String login, String contextPath){

		if(StringUtils.isBlank(login)){
			return false;
		}

		if(Usuario.ADMIN_POC_LOGIN.equals(login)){
			List<String> poc = Arrays.asList(DummyUtils.getMode(), contextPath);
			for(String pocList : poc){
				if(pocList.contains("poc")){
					return true;
				}
			}
		}
		return false;
	}

	public String getSenhaPoc() {
		return usuarioRepository.getSenhaPoc();
	}

	public List<Long> findRegionais(Usuario usuario){
		List<Long> regionalLong = new ArrayList<>();
		Long usuarioId = usuario.getId();
		usuario = get(usuarioId);
		Set<UsuarioRegional> regionalSet = usuario.getRegionais();

		for(UsuarioRegional regionais : regionalSet){
			BaseRegistro baseRegistro = regionais.getRegional();
			baseRegistro = baseRegistroService.get(baseRegistro.getId());
			String valor = baseRegistro.getChaveUnicidade();
			valor = valor.replaceAll("\\[\"(.*)\"\\]", "$1");
			Long valorLong = new Long(valor);
			regionalLong.add(valorLong);
		}
		return  regionalLong;
	}

	public List<Long> findCampus(Usuario usuario){
		List<Long> campusLong = new ArrayList<>();
		Long usuarioId = usuario.getId();
		usuario = get(usuarioId);
		Set<UsuarioCampus> campusSet = usuario.getCampus();

		for(UsuarioCampus campus : campusSet){
			BaseRegistro baseRegistro = campus.getCampus();
			baseRegistro = baseRegistroService.get(baseRegistro.getId());
			String valor = baseRegistro.getChaveUnicidade();
			valor = valor.replaceAll("\\[\"(.*)\"\\]", "$1");
			Long valorLong = new Long(valor);
			campusLong.add(valorLong);
		}
		return campusLong;
	}

	public RoleGD getRoleGD(Usuario usuario) {
		return usuarioRepository.getRoleGD(usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarSubperfil(Usuario usuario, Subperfil subperfil) {
		Set<SubperfilSituacao> situacoes = subperfil.getSituacoes();
		Hibernate.initialize(situacoes);
		FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
		Hibernate.initialize(filaConfiguracao);
		Set<SubperfilTipoDocumento> tipoDocumentos = subperfil.getTipoDocumentos();
		Hibernate.initialize(tipoDocumentos);
		usuario.setSubperfilAtivo(subperfil);
		usuarioRepository.update(usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarDataUltimaNotificacaoSemDemanda(Usuario usuario, Date date) {
		usuario.setDataUltimaNotificacaoSemDemanda(date);
		Long usuarioId = usuario.getId();
		usuarioRepository.atualizarDataUltimaNotificacaoSemDemanda(usuarioId, date);
	}

	@Transactional(rollbackFor=Exception.class)
	public void verificarAcessos() {

		Date agora = new Date();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -3);
		Date fim = c.getTime();

		List<Usuario> usuarios = usuarioRepository.findByDataAcesso(null, fim, true);

		for (Usuario usuario : usuarios) {

			usuario.setDataBloqueio(new Date());
			usuario.setStatus(StatusUsuario.BLOQUEADO);
			usuario.setMotivoBloqueio(MotivoBloqueioUsuario.INATIVIDADE);
			usuario.setDataAtualizacao(agora);
			usuarioRepository.saveOrUpdate(usuario);

			logAlteracaoService.registrarAlteracao(usuario, null, TipoAlteracao.ATUALIZACAO);
		}
	}
}
