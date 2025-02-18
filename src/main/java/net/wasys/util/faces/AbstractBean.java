package net.wasys.util.faces;

import net.wasys.getdoc.bean.LoginBean;
import net.wasys.getdoc.bean.MenuBean;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.Funcionalidade;
import net.wasys.getdoc.domain.service.BloqueioProcessoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.Entity;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;
import net.wasys.util.menu.Item;
import net.wasys.util.menu.Menu;
import net.wasys.util.other.LoginJwtUtils;
import net.wasys.util.servlet.HttpServletResponseLogAcesso;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.omnifaces.util.Faces;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

public abstract class AbstractBean implements Serializable {

	public static final String CAMPOS_MAP = "campos_map";
	public static final String USUARIO_SESSION_KEY = "usuario_session_key";
	public static final String USUARIO_AZURE = "usuario_azure";
	public static final String URL_LOGOFF_AZURE = "url_logoff_zure";
	private static final String SECURITY_CHECK_KEY = "security_check_key";
	private List<String> EXCECOES_MENU = Arrays.asList("/download.xhtml",
			"/processos/processo-visualizar.xhtml",
			"/processos/processo-digitalizarTwain.xhtml");

	public AbstractBean() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	public final void init() {
		initBean();
		verificarBloqueioProcesso();
	}

	protected abstract void initBean();

	public boolean isSecurityOk() {

		Map<String, Object> viewMap = getViewMap();
		Object ok = viewMap.get(SECURITY_CHECK_KEY);
		return ok != null && (Boolean) ok;
	}

	@PostConstruct
	public void securityCheck() {

		Map<String, Object> viewMap = getViewMap();
		Object ok = viewMap.get(SECURITY_CHECK_KEY);
		if(ok != null) {
			return;
		}

		String viewId = FacesUtil.getViewId();
		if(viewId != null && (
				viewId.startsWith("/cliente/processo-cliente")
				|| viewId.contains("/mobile") 
				|| viewId.contains("/login.xhtml")
				|| viewId.contains("/recuperacao-senha.xhtml")
				|| viewId.contains("/trocar-senha.xhtml")
				|| viewId.contains("/download.xhtml")
				|| viewId.contains("/home/home.xhtml")
				|| viewId.contains("/reply-azure-oauth.xhtml")
				|| viewId.contains("/erro/")
				)) {
			viewMap.put(SECURITY_CHECK_KEY, true);
			return;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario != null) {

			if(usuario.isAdminRole() && viewId.contains("/admin/util/") || EXCECOES_MENU.contains(viewId)) {
				viewMap.put(SECURITY_CHECK_KEY, true);
				return;
			}

			Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
			if(subperfilAtivo != null){
				Long id = subperfilAtivo.getId();
				if(Subperfil.CSC_ADM_ID.equals(id)){
					viewMap.put(SECURITY_CHECK_KEY, true);
					return;
				}
			}


			Date agora = new Date();
			Date dataExpiracaoSenha = usuario.getDataExpiracaoSenha();
			Date dataExpiracao = usuario.getDataExpiracao();
			String login = usuario.getLogin();
			if(dataExpiracaoSenha == null || dataExpiracaoSenha.before(agora)) {
				redirect("/trocar-senha/");
				return;
			}

			final ServletContext servletContext = getServletContext();
			WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			if(dataExpiracao != null && dataExpiracao.before(agora)){
				try {
					UsuarioService usuarioService = appContext.getBean(UsuarioService.class);
					usuarioService.bloquear(login);
				} catch (Exception e) {
					e.printStackTrace();
					String message = DummyUtils.getExceptionMessage(e);
					throw new RuntimeException(message);
				}
			}

			Menu menu = MenuBean.getMenuEstatico();
			Item activeItem = menu.getActiveItem();
			if(activeItem == null) {
				throw new RuntimeException("activeItem está null; talvez vc tenha esquecido de configurar <subItens> no menu.xml");
			}

			String id = activeItem.getId();
			if(StringUtils.isBlank(id)) {
				Item parent = activeItem.getParent();
				id = parent != null ? parent.getId() : null;
			}

			if(StringUtils.isNotBlank(id)) {
				Funcionalidade funcionalidade = Funcionalidade.valueOf(id);
				if(funcionalidade != null && !funcionalidade.podeAcessar(activeItem, usuario)) {
					DummyUtils.systraceThread("usuário " + login + " tentou acessar " + funcionalidade + " e foi impedido. (Erro 403)", LogLevel.ERROR);
					sendError(403);
					return;
				}
			}

			UsuarioService usuarioService = appContext.getBean(UsuarioService.class);
			try {
				usuarioService.validarHorarioAcesso(usuario);
			}
			catch (MessageKeyException e) {

				String key = e.getKey();
				Object[] args = e.getArgs();
				String message = getMessage(key, args);
				DummyUtils.systraceThread("horário de acesso não permitido. " + message, LogLevel.ERROR);
				setRequestAttribute("mensagemErro403", message);

				setRequestAttribute(LoginBean.FORCAR_LOGOFF, true);
				sendError(403);
				return;
			}

			viewMap.put(SECURITY_CHECK_KEY, true);
		}
		else {

			DummyUtils.systraceThread("usuário logado está null (Erro 403)", LogLevel.ERROR);
			sendError(403);
			return;
		}
	}

	public void verificarBloqueioProcesso() {

		Usuario usuario = getUsuarioLogado();
		if(usuario == null) {
			return;
		}

		ServletContext servletContext = getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		BloqueioProcessoService bloqueioProcessoService = applicationContext.getBean(BloqueioProcessoService.class);

		Long usuarioId = usuario.getId();
		bloqueioProcessoService.desbloquearByUsuario(usuarioId);
	}

	protected void sendError(int errorCode) {

		HttpServletResponse response = getResponse();
		try {
			response.sendError(errorCode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<String, Object> viewMap = getViewMap();
		viewMap.put(SECURITY_CHECK_KEY, false);
	}

	protected void addMessageError(Throwable e) {

		if(e instanceof ProcessoService.ProcessoCriadoException) {
			Throwable cause = e.getCause();
			addMessageError(cause);
			return;
		}
		else if(e instanceof MessageKeyException) {
			addMessageError((MessageKeyException) e);
			return;
		}
		else if(e instanceof MessageKeyListException) {
			List<MessageKeyException> messageKeyExceptions = ((MessageKeyListException) e).getMessageKeyExceptions();
			for (MessageKeyException mke : messageKeyExceptions) {
				addMessageError(mke);
			}
		}
		else if(e instanceof ValidatorException) {
			addMessageError((ValidatorException) e);
			return;
		}

		addMessageErrorInesperado(e);
	}

	private void addMessageErrorInesperado(Throwable e) {

		e.printStackTrace();
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		String stackTrace = ExceptionUtils.getStackTrace(e);
		logAcesso.setException(stackTrace);
		String exceptionMessage = DummyUtils.getExceptionMessage(e);
		DummyUtils.systraceThread(exceptionMessage, LogLevel.ERROR);

		String message = getMessage("erroInesperado.error", "LA: #" + logAcesso.getId());

		addFaceMessage(FacesMessage.SEVERITY_ERROR, message, null);
	}

	protected void addMessageError(MessageKeyException e) {

		try {
			String key = e.getKey();
			Object[] args = e.getArgs();

			String message = getMessage(key, args);

			addFaceMessage(FacesMessage.SEVERITY_ERROR, message, null);
		}
		catch (Exception e2) {
			e2.printStackTrace();
			addMessageErrorInesperado(e);
		}
	}

	protected void addMessageError(ValidatorException e) {

		try {
			FacesMessage facesMessage = e.getFacesMessage();
			addFaceMessage(facesMessage, null);
		}
		catch (Exception e2) {
			e2.printStackTrace();
			addMessageErrorInesperado(e);
		}
	}

	protected void addMessageWarn(String key) {
		addMessageWarnToComponent(key, null, (Object[]) null);
	}

	protected void addMessageWarn(String key, Object... args) {
		addMessageWarnToComponent(key, null, args);
	}

	protected void addMessageWarnToComponent(String key, String componentId) {
		addMessageWarnToComponent(key, componentId, (Object[]) null);
	}

	protected void addMessageWarnToComponent(String key, String componentId, Object... args) {
		String message = getMessage(key, args);
		addFaceMessage(FacesMessage.SEVERITY_WARN, message, componentId);
	}

	protected void addMessageError(String key) {
		addMessageErrorToComponent(key, null, (Object[]) null);
	}

	protected void addMessageError(String key, Object... args) {
		addMessageErrorToComponent(key, null, args);
	}

	protected void addMessageErrorToComponent(String key, String componentId) {
		addMessageErrorToComponent(key, componentId, (Object[]) null);
	}

	protected void addMessageErrorToComponent(String key, String componentId, Object... args) {
		String message = getMessage(key, args);
		addFaceMessage(FacesMessage.SEVERITY_ERROR, message, componentId);
	}

	protected String getMessage(String key) {
		return getMessage(key, (Object[]) null);
	}

	protected String getMessage(String key, Object... args) {

		ResourceBundle bundle = FacesUtil.getMessages();

		String message = bundle.getString(key);

		if(args != null) {
			message = MessageFormat.format(message, args);
		}

		return message;
	}

	protected void addMessage(String key) {
		addMessageToComponent(key, null);
	}

	protected void addMessage(String key, Object... args) {
		addMessageToComponent(key, null, args);
	}

	protected void addMessageToComponent(String key, String componentId, Object... args) {
		String message = getMessage(key, args);
		addFaceMessage(FacesMessage.SEVERITY_INFO, message, componentId);
	}

	protected void addFaceMessage(Severity severity, String message) {
		addFaceMessage(severity, message, null);
	}

	protected void addFaceMessage(Severity severity, String message, String componentId) {

		addFaceMessage(new FacesMessage(severity, message, null), componentId);
	}

	protected void addFaceMessage(FacesMessage facesMessage, String componentId) {

		FacesContext facesContext = getFacesContext();
		facesContext.addMessage(componentId, facesMessage);

		ExternalContext externalContext = getExternalContext();
		Flash flash = externalContext.getFlash();
		flash.setKeepMessages(true);
	}

	protected boolean isInsert(Entity entity) {
		Long id = entity.getId();
		return id == null;
	}

	protected FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	protected HttpServletResponse getResponse() {
		ExternalContext externalContext = getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		return response;
	}

	protected HttpServletRequest getRequest() {
		ExternalContext externalContext = getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		return request;
	}

	protected ServletContext getServletContext() {
		HttpServletRequest request = getRequest();
		ServletContext servletContext = request.getServletContext();
		return servletContext;
	}

	protected HttpSession getSession() {
		ExternalContext externalContext = getExternalContext();
		HttpSession session = (HttpSession) externalContext.getSession(true);
		return session;
	}

	protected ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	protected String getContextPath() {
		return getExternalContext().getRequestContextPath();
	}

	protected void setFlashAttribute(String key, Object value) {
		if (StringUtils.isNotBlank(key) && value != null) {
			Flash flash = getFlash();
			flash.put(key, value);
		}
	}

	protected Flash getFlash() {
		ExternalContext externalContext = getExternalContext();
		return externalContext.getFlash();
	}

	protected Object getFlashAttribute(String key) {
		Flash flash = getFlash();
		return flash.get(key);
	}

	protected void redirect(String path) {

		String contextPath = getContextPath();
		if(path == null || !path.startsWith(contextPath)) {
			if (path == null || path.equals("null")) {
				path = "";
			}
			path = contextPath + path;
		}

		ExternalContext externalContext = getExternalContext();
		try {
			externalContext.redirect(path);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		HttpServletResponseLogAcesso.registrarRedirect(path);

		Map<String, Object> viewMap = getViewMap();
		viewMap.put(SECURITY_CHECK_KEY, false);
	}

	protected String getPath() {
		HttpServletRequest request = getRequest();
		StringBuffer requestURL = request.getRequestURL();
		return requestURL.toString();
	}

	public Usuario getUsuarioLogado() {

		HttpSession session = getSession();
		Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION_KEY);
		return usuario;
	}

	protected Map<String, Object> getViewMap() {

		FacesContext facesContext = getFacesContext();
		UIViewRoot viewRoot = facesContext.getViewRoot();
		Map<String, Object> viewMap = viewRoot.getViewMap();

		return viewMap;
	}

	protected void logoff() {

		LoginJwtUtils.invalidarCookie(getResponse());
		HttpSession session = getSession();
		boolean usuarioAzure = (boolean) session.getAttribute(USUARIO_AZURE);
		String logoffEndPoint = (String) session.getAttribute(URL_LOGOFF_AZURE);
		session.invalidate();
		if(usuarioAzure) {
			try {
				ExternalContext externalContext = getExternalContext();
				externalContext.redirect(logoffEndPoint);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			redirect("/");
		}
	}

	protected void sendFile(File file, String nome) {

		FileInputStream fis = null;

		try {
			fis = new FileInputStream(file);
			Faces.sendFile(fis, nome, true);
		}
		catch (IOException e) {
			addMessageError(e);
		}
		finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void sendFile(byte[] file, String nome) {

		try {
			Faces.sendFile(file, nome, true);
		}
		catch (IOException e) {
			addMessageError(e);
		}
	}

	protected void setRequestAttribute(String name, Object value) {
		HttpServletRequest request = getRequest();
		request.setAttribute(name, value);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getRequestAttribute(String name) {
		HttpServletRequest request = getRequest();
		Object result = request.getAttribute(name);
		return (T) result;
	}
}
