package net.wasys.getdoc.bean;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.AzureOauthService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsAzureVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@ManagedBean
@ViewScoped
public class ReplyAzureOauthBean extends AbstractBean {

	@Autowired private AzureOauthService azureOauthService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ParametroService parametroService;

	public void securityCheck() { }

	protected void initBean(){
		autenticarAzure();
	}

	private void autenticarAzure() {

		HttpSession session = getSession();
		try {
			Usuario usuario = getUsuarioLogado();
			if(usuario == null) {
				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

				String code = request.getParameter("code");
				usuario = azureOauthService.autenticacaoAzureInicio(code);
				if(usuario == null) {
					sendError(403);
					return;
				}

				String login = usuario.getEmail();
				String senha = usuario.getSenha();

				request.login(login, senha);

				Usuario usuarioLogado = usuarioService.login(login);
				session.setAttribute(USUARIO_SESSION_KEY, usuarioLogado);
				session.setAttribute(USUARIO_AZURE, true);
				ConfiguracoesWsAzureVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_AZURE, ConfiguracoesWsAzureVO.class);
				String logoffEndPoint = configuracoesVO.getLogoffEndPoint();
				session.setAttribute(URL_LOGOFF_AZURE, logoffEndPoint);

				HttpServletResponse response = getResponse();
				DummyUtils.setCookieLogin(response, true);
			}

			String originalURL = (String) session.getAttribute(GetdocConstants.ORIGINAL_URL);
			if(StringUtils.isNotBlank(originalURL)) {
				redirect(originalURL);
			} else {
				redirect("/home/");
			}
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}
}
