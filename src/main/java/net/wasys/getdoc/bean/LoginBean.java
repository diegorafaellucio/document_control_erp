package net.wasys.getdoc.bean;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.AzureOauthService;
import net.wasys.getdoc.domain.service.LogAtendimentoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.faces.FacesUtil;
import net.wasys.util.other.Criptografia;
import net.wasys.util.other.LoginJwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@ViewScoped
public class LoginBean extends AbstractBean {

	private static final String TENTATIVAS_INCORRETAS_LOGIN = "tentativas_incorretas_login";
	public static final String FORCAR_LOGOFF = "forcar_logoff";

	@Autowired private UsuarioService usuarioService;
	@Autowired private AzureOauthService azureOauthService;
	@Autowired private LogAtendimentoService logAtendimentoService;
	@Autowired private ProcessoService processoService;

	private String login;
	private String senha;
	private String azureURL;

	public void securityCheck() { }

	protected void initBean() {

		HttpSession session = getSession();
		String originalURL = (String) session.getAttribute(GetdocConstants.ORIGINAL_URL);
		HttpServletRequest request = getRequest();
		String contextPath = request.getContextPath();
		if(StringUtils.isBlank(originalURL) || originalURL.replace("/", "").equals(contextPath.replace("/", ""))) {
			ExternalContext externalContext = getExternalContext();
			Map<String, Object> requestMap = externalContext.getRequestMap();
			originalURL = (String) requestMap.get(RequestDispatcher.FORWARD_REQUEST_URI);
			if(originalURL == null) {
				originalURL = contextPath;
			}
			else {
				String originalQuery = (String) requestMap.get(RequestDispatcher.FORWARD_QUERY_STRING);
				if(originalQuery != null && !"logoff".equals(originalQuery)) {
					originalURL += "?" + originalQuery;
				}
			}
			if(!originalURL.endsWith("login.xhtml")) {
				session.setAttribute(GetdocConstants.ORIGINAL_URL, originalURL);
			}
		}

		boolean sso = ssoAzure();
		if (sso) return;

		this.azureURL = azureOauthService.getReplyUrl(false);

		String login = LoginJwtUtils.checkLogadoCookie(request);
		if(org.apache.commons.lang.StringUtils.isNotBlank(login)) {
			Usuario usuarioLogando = usuarioService.getByLogin(login);
			if(usuarioLogando != null) {
				try {
					login(usuarioLogando);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean ssoAzure() {

		List<String> loginAzure = FacesUtil.getCookiesValues(GetdocConstants.LOGIN_AZURE);
		if(loginAzure.contains("true") && !loginAzure.contains("false")) {

			HttpServletResponse response = getResponse();
			DummyUtils.setCookieLogin(response, false);

			this.azureURL = azureOauthService.getReplyUrl(false);

			try {
				response.sendRedirect(this.azureURL);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	public void login() throws Exception {
		login(null);
	}

	public void login(Usuario usuario) throws Exception {

		String login, senha;
		if(usuario != null) {
			login = usuario.getLogin();
			senha = usuario.getSenha();
		} else {
			login = this.login;
			senha = Criptografia.encrypt(Criptografia.SENHA, this.senha);
		}

		HttpServletRequest request = getRequest();
		HttpSession session = getSession();

		try {
			request.login(login, senha);
		}
		catch (ServletException e) {

			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("login: " + login + " senha: " + senha + " senhaCripto: " + senha + " exception: " + exceptionMessage, LogLevel.ERROR);

			addMessageError("usuarioSenhaInvalido.error");
			Integer tentativas = getTentativas();
			tentativas++;
			if(tentativas >= 5) {
				try {
					usuarioService.bloquear(login);
					tentativas = 0;
					addMessageError("acessoBloqueadoTemporariamente.error");
				}
				catch (MessageKeyException e2) {
					addMessageError(e2);
				}
			}
			registrarTentativa(tentativas);
			return;
		}

		try {
			Usuario usuarioLogado = usuarioService.login(login);

			if(usuarioLogado == null) {
				throw new RuntimeException("Login feito, mas usuarioService.login() retornou null.");
			}

			registrarTentativa(0);

			RoleGD roleGD = usuarioLogado.getRoleGD();
			if(roleGD != null) {
				session.setAttribute(USUARIO_SESSION_KEY, usuarioLogado);
				session.setAttribute(USUARIO_AZURE, false);

				LoginJwtUtils.criarCookie(usuarioLogado, getResponse());
			}

			if(usuarioLogado.isAnalistaRole()){
				boolean distribuirDemandaAutomaticamente = usuarioLogado.getDistribuirDemandaAutomaticamente();
				if(distribuirDemandaAutomaticamente) {
					Long processoAtualId = usuarioLogado.getProcessoAtualId();
					if (processoAtualId != null  && !usuarioLogado.getPodeTrocarProcessoAtual()) {
						Processo processo = processoService.get(processoAtualId);
						Usuario processoAnalista = processo.getAnalista();
						if(processoAnalista != null) {
							logAtendimentoService.criarEmAnalise(usuarioLogado.getId());
							String contextPath = getContextPath();
							redirect(contextPath + "/requisicoes/fila/edit/" + processoAtualId);
							return;
						} else {
							usuarioLogado.setProcessoAtualId(null);
							usuarioService.merge(usuarioLogado);
							logAtendimentoService.criarPausaSistema(usuarioLogado.getId());
						}
					} else {
						logAtendimentoService.criarPausaSistema(usuarioLogado.getId());
					}
				}
			}

			String originalURL = (String) session.getAttribute(GetdocConstants.ORIGINAL_URL);
			redirect(originalURL);
		}
		catch (MessageKeyException e) {

			session.invalidate();
			addMessageError(e);
		}

		HttpServletResponse response = getResponse();
		DummyUtils.setCookieLogin(response, false);
	}

	@SuppressWarnings("unchecked")
	private Integer getTentativas() {

		ServletContext servletContext = getServletContext();
		Map<String, Integer> map = (Map<String, Integer>) servletContext.getAttribute(TENTATIVAS_INCORRETAS_LOGIN);
		if(map == null) {
			map = new HashMap<String, Integer>();
			servletContext.setAttribute(TENTATIVAS_INCORRETAS_LOGIN, map);
		}
		Integer tentativas = map.get(login);
		tentativas = tentativas != null ? tentativas : 0;

		return tentativas;
	}

	@SuppressWarnings("unchecked")
	private void registrarTentativa(int num) {

		ServletContext servletContext = getServletContext();
		Map<String, Integer> map = (Map<String, Integer>) servletContext.getAttribute(TENTATIVAS_INCORRETAS_LOGIN);
		if(map != null) {
			map.put(login, num);
		}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String email) {
		this.login = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public void invalidarSessaoSeDeslogado() {

		Usuario usuario = getUsuarioLogado();
		if(usuario == null) {
			HttpSession session = getSession();
			session.invalidate();
			return;
		}

		Boolean forcarLogoff = getRequestAttribute(FORCAR_LOGOFF);
		if(forcarLogoff != null && Boolean.TRUE.equals(forcarLogoff)) {
			HttpSession session = getSession();
			session.invalidate();
			return;
		}
	}

	public String getAzureURL() {
		return azureURL;
	}
}
