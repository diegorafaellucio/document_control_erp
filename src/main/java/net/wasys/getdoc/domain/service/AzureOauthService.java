package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsAzureVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;
import static org.omnifaces.util.Faces.getRequest;

@Service
public class AzureOauthService {

	@Autowired private ParametroService parametroService;
	@Autowired private ResourceService resourceService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ConfiguracaoLoginAzureService configuracaoLoginAzureService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BaseRegistroValorService baseRegistroValorService;
	@Autowired private UsuarioCampusService usuarioCampusService;

	public Usuario autenticacaoAzureInicio(String code) throws Exception {

		ConsultaExterna ce = new ConsultaExterna();
		ce.setData(new Date());
		Stopwatch stopwatch = Stopwatch.createStarted();

		ConfiguracoesWsAzureVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_AZURE, ConfiguracoesWsAzureVO.class);

		Usuario usuario = null;
		String memberOfStr = null;
		String usersStr = null;
		String nomeUsuario = "";
		try {
			Map<?, ?> jsonStringToMap = doPostToken(code, configuracoesVO);

			String accessToken = (String) jsonStringToMap.get("access_token");

			usuario = getUserByAccessToken(accessToken);
			String login = usuario != null ? usuario.getLogin() : null;
			LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
			if(logAcesso != null) {
				logAcesso.setRemoteUser(login);
			}

			String memberOfEndpoint = configuracoesVO.getMemberOfEndpoint();
			memberOfStr = doPostMemberOf(accessToken, memberOfEndpoint);

			Map<?, ?> memberOf = DummyUtils.jsonStringToMap(memberOfStr);
			List<Object> value = (List<Object>) memberOf.get("value");

			Set<String> rolesStr = new HashSet<>();
			List<String> subperfisStr = new ArrayList<>();
			List<String> gruposAD = new ArrayList<>();
			for (Object obj1 : value) {
				Map<?, ?> map1 = (Map<?, ?>) obj1;
				String displayName = (String) map1.get("displayName");
				gruposAD.add(displayName);

				nomeUsuario =  usuario != null ? (usuario.getLogin() + " - " + usuario.getNome()) : " Usuario Novo";
				systraceThread("### displayName: " + displayName + " usuario: " + nomeUsuario);
				ConfiguracaoLoginAzure configuracaoLoginAzure = configuracaoLoginAzureService.findByGrupoAD(displayName);
				if(configuracaoLoginAzure != null) {
					systraceThread("### retornou: " + configuracaoLoginAzure.getId() + " role: " + configuracaoLoginAzure.getRole() + " usuario: " + nomeUsuario);
					RoleGD role = configuracaoLoginAzure.getRole();
					rolesStr.add(role.name());

					Subperfil subPerfil = configuracaoLoginAzure.getSubperfil();
					if(subPerfil != null) {
						systraceThread("### retornou perfil: " + configuracaoLoginAzure.getId() + " role: " + configuracaoLoginAzure.getSubperfil().getDescricao()  + " usuario: " + nomeUsuario);
						Long subPerfilId = subPerfil.getId();
						if(usuario != null) {
							usuario.setSubperfilAtivo(subPerfil);
						}
						subperfisStr.add(subPerfilId.toString());
						systraceThread("### encontrou o perfil: " + subPerfil.getDescricao() + " usuario: " + nomeUsuario);
					}
					break;
				}
			}

			Object[] result = getAzureCampus(configuracoesVO, usuario, accessToken);
			String extensionAttribute8 = result != null ? (String) result[0] : null;
			List<String> campusList = result != null ? (List<String>) result[1] : null;

			if(logAcesso != null) {
				try {
					String parameters = logAcesso.getParameters();
					Map<Object, Object> paramsMap = (Map<Object, Object>) DummyUtils.jsonStringToMap(parameters);
					paramsMap = paramsMap != null ? paramsMap : new LinkedHashMap<>();

					paramsMap.put("gruposAD", gruposAD);
					paramsMap.put("extensionAttribute8", extensionAttribute8);
					paramsMap.put("campus", campusList);
					String parameters2 = DummyUtils.objectToJson(paramsMap);
					logAcesso.setParameters(parameters2);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if(rolesStr == null || rolesStr.isEmpty()) {
				String msgErro = "### usuário não autorizado: " + (usuario != null ? usuario.getLogin() : "???.") + " roles null.";
				systraceThread(msgErro) ;
				throw new MessageKeyException("acessoNaoPermitiroAzure.error");
			}

			usuarioService.atualizaRoles(usuario, new ArrayList<>(rolesStr));

			List<String> tiposProcessoStr = new ArrayList<>();
			Set<UsuarioTipoProcesso> tiposProcessos = usuario.getTiposProcessos();
			for (UsuarioTipoProcesso utp : tiposProcessos) {
				TipoProcesso tipoProcesso = utp.getTipoProcesso();
				Long tipoProcessoId = tipoProcesso.getId();
				tiposProcessoStr.add(tipoProcessoId.toString());
			}

			StatusUsuario status = usuario.getStatus();
			if(!StatusUsuario.ATIVO.equals(status)) {
				//se não estava ativo, por inatividade, por exemplo, ativa novamente, já que no AD ele ainda está ativo (se não não teria chego até aqui)
				usuario.setStatus(StatusUsuario.ATIVO);
			}

			usuarioService.saveOrUpdate(usuario, tiposProcessoStr, null, subperfisStr);

			return usuario;
		}
		catch (Exception e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("### Erro ao logar usuario: " + exceptionMessage + "memberOfStr" + memberOfStr, LogLevel.ERROR);
			e.printStackTrace();
			String stackTrace = DummyUtils.getStackTrace(e);
			ce.setStackTrace(stackTrace);
			ce.setMensagem(exceptionMessage);
			throw e;
		}
		finally {

			stopwatch.stop();
			long tempo = stopwatch.elapsed(TimeUnit.MILLISECONDS);
			String stackTrace = ce.getStackTrace();
			ce.setStatus(org.apache.commons.lang.StringUtils.isNotBlank(stackTrace) ? StatusConsultaExterna.ERRO : StatusConsultaExterna.SUCESSO);
			ce.setParametros("{\"usuario\": \"" + nomeUsuario + "\"}");
			ce.setTipo(TipoConsultaExterna.LOGIN_AZURE);
			ce.setTempoExecucao(tempo);

			try {
				consultaExternaService.saveOrUpdate(ce);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Object[] getAzureCampus(ConfiguracoesWsAzureVO configuracoesVO, Usuario usuario, String accessToken) throws Exception {

		if(true) {
			usuario.setCampus(new HashSet<>());
			return null;
		}

		if(!usuario.isSalaMatriculaRole()) {
			return null;
		}

		systraceThread("----- INICIANDO BUSCA PELO ATRIBUTO 8 -----");

		String usersEndpoint = configuracoesVO.getUsersEndpoint();

		String oidAzure = usuario.getOidAzure();
		usersEndpoint = usersEndpoint.replace("{oidAzure}", oidAzure);

		String usersStr = doPostUsers(accessToken, usersEndpoint);
		Map<?, ?> users = DummyUtils.jsonStringToMap(usersStr);

		Map<String, String> opea = (Map<String, String>) users.get("onPremisesExtensionAttributes");
		String attribute8 = opea != null ? opea.get("extensionAttribute8") : null;
		String[] campus = attribute8 != null ? attribute8.split(",") : null;

		systraceThread("CAMPUS ATRIBUTO 8: " + attribute8);
		if(campus == null) {
			return new Object[]{attribute8, null};
		}

		List<String> campusList = new ArrayList<>();

		Set<UsuarioCampus> usuarioCampus = new HashSet<>();
		for (String nomeCampus : campus) {
			BaseRegistroValor baseRegistroValor = baseRegistroValorService.getByNomeAndBaseInterna(nomeCampus, BaseInterna.CAMPUS_ID);
			if(baseRegistroValor != null) {
				BaseRegistro baseRegistro = baseRegistroValor.getBaseRegistro();

				Long usuarioId = usuario.getId();
				Long campusId = baseRegistro.getId();
				String chaveUnicidade = baseRegistro.getChaveUnicidade();
				campusList.add(chaveUnicidade + "-" + nomeCampus);

				UsuarioCampus usuarioCampus1 = usuarioCampusService.findByUsuarioIdAndCampusId(usuarioId, campusId);
				usuarioCampus1 = usuarioCampus1 != null ? usuarioCampus1 : new UsuarioCampus();
				usuarioCampus1.setCampus(baseRegistro);
				usuarioCampus1.setUsuario(usuario);

				usuarioCampus.add(usuarioCampus1);
			}
		}

		usuario.setCampus(usuarioCampus);
		return new Object[]{attribute8, campusList};
	}

	private Usuario getUserByAccessToken(String accessToken) {

		String[] jwtSplit = accessToken.split("\\.");
		//String base64Header = jwtSplit[0];
		String base64Body = jwtSplit[1];
		Base64.Decoder decoder = Base64.getDecoder();
		String accessTokenDecoded = new String(decoder.decode(base64Body));
		systraceThread("### getUserByAccessToken: "+ accessTokenDecoded);

		Map<?, ?> map = DummyUtils.jsonStringToMap(accessTokenDecoded);

		String nome = (String) map.get("name");
		String login = (String) map.get("unique_name");
		String oidAzure = (String) map.get("oid");

		login = StringUtils.isNotBlank(login) ? login.toLowerCase() : login;

		Usuario usuario = usuarioService.getByLogin(login);

		if(usuario == null) {
			usuario = new Usuario();
			usuario.setStatus(StatusUsuario.ATIVO);
			String senha = DummyUtils.gerarSenhaAleatoria();
			usuario.setSenha(senha);
			usuario.setLogin(login);
			usuario.setEmail(login);
			usuario.setTelefone(null);
			usuario.setGeralId(null);//vai sincronizar com o geral?
			usuario.setNotificarAtrasoRequisicoes(true);
			usuario.setNotificarAtrasoSolicitacoes(true);
			usuario.setGestorArea(false);
			usuario.setRegionais(null);
			usuario.setCampus(null);
			//nunca vai usar a senha do getdoc... então não precisa expirar
			Date dataExpiracaoSenha = DummyUtils.parseDate("01/01/2050");
			usuario.setDataExpiracaoSenha(dataExpiracaoSenha);
		}

		usuario.setNome(nome);
		usuario.setEmail(login);
		usuario.setOidAzure(oidAzure);

		systraceThread("### login Azure, sucesso getUserByAccessToken");

		return usuario;
	}

	private Map<?, ?> doPostToken(String code, ConfiguracoesWsAzureVO configuracoesVO)throws Exception {

		String tokenEndpoint = configuracoesVO.getTokenEndpoint();
		String clientId = configuracoesVO.getClientId();
		String clientSecret = configuracoesVO.getClientSecret();

		Map<String, String> headers = new HashMap<>();
		headers.put("grant_type", "authorization_code");
		headers.put("Content-Type","application/x-www-form-urlencoded");

		String replyUrl = getReplyUrl(true);

		RestClient rc = new RestClient();
		rc.setRepeatTimes(1);
		rc.setHeaders(headers);
		rc.setUrl(tokenEndpoint);
		ProxyManager proxyManager = resourceService.getProxyManager();
		rc.setProxyManager(proxyManager);

		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
		parameters.add(new BasicNameValuePair("code", code));
		parameters.add(new BasicNameValuePair("client_id", clientId));
		parameters.add(new BasicNameValuePair("client_secret", clientSecret));
		if(StringUtils.isNotBlank(replyUrl)) {
			parameters.add(new BasicNameValuePair("redirect_uri", replyUrl));
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);

		try {
			String release = rc.execute(entity, String.class);
			Map<?, ?> jsonStringToMap = DummyUtils.jsonStringToMap(release);

			systraceThread("### login Azure, sucesso ao fazer post");
			return jsonStringToMap;
		}
		catch (RestClient.RestException e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("### erro ao buscar token: " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();
			int statusCode = e.getStatusCode();
			String release = e.getRelease();
			if(statusCode == 400 && StringUtils.isNotBlank(release) && release.contains("invalid_grant")) {
				Map<?, ?> jsonStringToMap = doPostTokenRefresh(code, configuracoesVO);
				return jsonStringToMap;
			}
			throw e;
		}
	}

	public String getReplyUrl(boolean onlyRedirect) {
		try {
			String urlRedirect = Faces.getRequestParameter("varUrl");
			HttpServletRequest request = getRequest();
			String contextPath = request.getContextPath() + "/";
			if(StringUtils.isBlank(urlRedirect)) {
				String protocolo = request.getHeader("X-Forwarded-Proto");
				String server = request.getHeader("X-Forwarded-Server");
				String port = request.getHeader("X-Forwarded-Port");
				if(StringUtils.isBlank(server)) {
					protocolo = request.getProtocol();
					protocolo = protocolo.toUpperCase().startsWith("HTTPS") ? "https" : "http";
					server = request.getServerName();
					port = String.valueOf(request.getServerPort());
				}

				urlRedirect = protocolo + "://" + server;
				if(!"80".equals(port) && !"443".equals(port)) {
					urlRedirect += ":" +port;
				}

				urlRedirect += contextPath;

			} else {
				int posicao = urlRedirect.lastIndexOf(contextPath);
				int length = contextPath.length();
				urlRedirect = urlRedirect.substring(0, posicao + length);
			}

			String mode = DummyUtils.getMode();
			if("dev".equals(mode)) {
				urlRedirect = "https://digitaliza.estacio.br/getdoc_aluno_hml/";
			}

			AtomicReference<String> azureURL = new AtomicReference<>();
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				ConfiguracoesWsAzureVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_AZURE, ConfiguracoesWsAzureVO.class);
				azureURL.set(configuracoesVO.getLinkAutenticacao());
			});
			tw.runNewThread();

			if(onlyRedirect) {
				String s = urlRedirect + "reply-azure-oauth.xhtml";
				return s;
			} else {
				urlRedirect = URLEncoder.encode(urlRedirect, StandardCharsets.UTF_8.toString());
				String s = azureURL + "&redirect_uri=" + urlRedirect + "reply-azure-oauth.xhtml";
				return s;
			}

		} catch (UnsupportedEncodingException e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("### Erro ao carregar url da azure: " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();
			throw new UnsupportedOperationException();
		}
	}

	private Map<?, ?> doPostTokenRefresh(String code, ConfiguracoesWsAzureVO configuracoesVO) throws Exception {

		systraceThread("tentando fazer refresh do token. code: " + code);

		String tokenEndpoint = configuracoesVO.getTokenEndpoint();
		String clientId = configuracoesVO.getClientId();
		String clientSecret = configuracoesVO.getClientSecret();

		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type","application/x-www-form-urlencoded");
		headers.put("grant_type", "authorization_code");

		RestClient rc = new RestClient();
		rc.setHeaders(headers);
		rc.setUrl(tokenEndpoint);
		ProxyManager proxyManager = resourceService.getProxyManager();
		rc.setProxyManager(proxyManager);

		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
		parameters.add(new BasicNameValuePair("refresh_token", code));
		parameters.add(new BasicNameValuePair("client_id", clientId));
		parameters.add(new BasicNameValuePair("client_secret", clientSecret));

		String replyUrl = getReplyUrl(true);
		if(StringUtils.isNotBlank(replyUrl)) {
			parameters.add(new BasicNameValuePair("redirect_uri", replyUrl));
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
		String release = rc.execute(entity, String.class);
		return DummyUtils.jsonStringToMap(release);
	}

	protected String doPostMemberOf(String accessToken, String memberOfEndpoint)throws Exception{

		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization",  "Bearer " + accessToken);

		RestClient rc = new RestClient();
		rc.setHeaders(headers);
		rc.setUrl(memberOfEndpoint);
		ProxyManager proxyManager = resourceService.getProxyManager();
		rc.setProxyManager(proxyManager);

		String execute = rc.execute(String.class);
		return execute;
	}

	protected String doPostUsers(String accessToken, String usersEndpoint)throws Exception{

		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization",  "Bearer " + accessToken);

		RestClient rc = new RestClient();
		rc.setHeaders(headers);
		rc.setUrl(usersEndpoint);
		ProxyManager proxyManager = resourceService.getProxyManager();
		rc.setProxyManager(proxyManager);

		String execute = rc.execute(String.class);
		return execute;
	}
}
