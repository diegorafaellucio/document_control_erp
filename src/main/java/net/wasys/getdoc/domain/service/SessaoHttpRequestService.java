package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.repository.SessaoHttpRequestRepository;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestLogin;
import net.wasys.getdoc.rest.service.SuperServiceRest;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.Criptografia;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Date;

@Service
public class SessaoHttpRequestService extends SuperServiceRest {

	@Autowired private SessaoHttpRequestRepository repository;
	@Autowired private UsuarioService usuarioService;

	public SessaoHttpRequest get(Long id) {
		return repository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public SessaoHttpRequest criaSessao(HttpServletRequest httpServletRequest, Usuario usuario) throws MessageKeyException {
		SessaoHttpRequest sessaoHttpRequest = new SessaoHttpRequest();
		sessaoHttpRequest.setAtiva(true);
		sessaoHttpRequest.setData(new Date());
		sessaoHttpRequest.setJsessionId(httpServletRequest.getSession().getId());
		sessaoHttpRequest.setUsuario(usuario);

		String login = usuario.getLogin();
		if(Usuario.PORTAL_POS_GRADUACAO.equals(login)){
			repository.saveOrUpdate(sessaoHttpRequest);
		}

		return sessaoHttpRequest;
	}

	@Transactional(rollbackFor=Exception.class)
	public void invalidaSessoesAnteriores(Usuario usuario) {
		//repository.mataSessaoUsuario(usuario);
	}

	/**
	 * Recupera uma sessão salva no banco através do JSESSIONID obtido o login.
	 * @param request
	 * @return
	 */
	public SessaoHttpRequest findByJSessionId(HttpServletRequest request) throws HTTP401Exception {

		String sessionId = request.getRequestedSessionId();

		if (!request.isRequestedSessionIdValid() || !request.isRequestedSessionIdFromCookie()) {

			SessaoHttpRequest shr2 = checkPortalPos(request);
			if (shr2 != null) return shr2;

			throw new HTTP401Exception("http401.exception");
		}

		SessaoHttpRequest shr = repository.findByJSessionId(sessionId);
		if(shr == null) {
			SessaoHttpRequest shr2 = checkPortalPos(request);
			if (shr2 != null) return shr2;
		}

		return shr;
	}

	/** exeção gambi pro postal de pós... pq lá o acesso é feito via javascript e a configuração de cors não funcionou */
	private SessaoHttpRequest checkPortalPos(HttpServletRequest request) {

		String cookie = request.getHeader("Set-Cookie");
		if(StringUtils.isNotBlank(cookie) && cookie.startsWith("JSESSIONID")) {
			String[] split = cookie.split(";");
			if(split != null && split.length > 0) {
				String jsessionid = split[0];
				if(StringUtils.isNotBlank(jsessionid)) {
					jsessionid = jsessionid.replace("JSESSIONID=", "");
					SessaoHttpRequest shr2 = repository.findByJSessionId(jsessionid);
					if(shr2 == null && jsessionid.contains(".")) {
						jsessionid = jsessionid.substring(0, jsessionid.indexOf("."));
						shr2 = repository.findByJSessionId(jsessionid);
					}
					if(shr2 != null) {
						Usuario usuario = shr2.getUsuario();
						String login = usuario.getLogin();
						if(Usuario.PORTAL_POS_GRADUACAO.equals(login)) {
							HttpSession session = request.getSession();
							session.setAttribute(AbstractBean.USUARIO_SESSION_KEY, usuario);
							return shr2;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Faz login no próprio container web através do Login e Senha do usuário.
	 * @param request
	 * @param requestLogin
	 * @return
	 * @throws ServletException
	 * @throws MessageKeyException
	 */
	public SessaoHttpRequest login(HttpServletRequest request, RequestLogin requestLogin) throws Exception {

		HttpSession session = request.getSession();
		try {
			//validaRequestParameters(requestLogin);

			Principal userPrincipal = request.getUserPrincipal();
			Usuario usuario = (Usuario) session.getAttribute(AbstractBean.USUARIO_SESSION_KEY);
			if(userPrincipal == null || usuario == null) {

				//Faz o login no container web.
				String login = requestLogin.getLogin();
				try {
					String senha = requestLogin.getSenha();
					senha = Criptografia.encrypt(Criptografia.SENHA, senha);
					request.login(login, senha);
				} catch (ServletException e) {
					e.printStackTrace();
					throw new HTTP401Exception("http401.loginSenha.exception");
				}

				usuario = usuarioService.loginApi(login);

				session.setAttribute(AbstractBean.USUARIO_SESSION_KEY, usuario);
				session.setAttribute(AbstractBean.USUARIO_AZURE, false);

				//invalidaSessoesAnteriores(usuario);

				if(Usuario.PORTAL_GRADUCAO.equals(login)) {
					session.setMaxInactiveInterval(2 * 60);
				} else {
					session.setMaxInactiveInterval(15 * 60);
				}
			}

			SessaoHttpRequest result = criaSessao(request, usuario);
			return result;
		}
		catch (Exception e) {
			session.invalidate();
			throw e;
		}
	}
}
