package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.wasys.getdoc.domain.enumeration.Origem;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.mb.Toast;
import net.wasys.getdoc.mb.Toast.Type;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.FacesUtil;
import net.wasys.util.rest.jackson.ObjectMapper;

public abstract class MobileBean {

	public static String USUARIO_LOGADO_MOBILE_KEY = "usuario_logado_mobile_key";

	protected Usuario usuario;

	@Autowired protected MessageService messageService;

	public MobileBean() {
		usuario = Faces.getRequestAttribute(USUARIO_LOGADO_MOBILE_KEY);
		if (usuario == null) {
			ServletContext servletContext = Faces.getServletContext();
			WebApplicationContext cc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			UsuarioService usuarioService = cc.getBean(UsuarioService.class);
			usuario = usuarioService.get(1074l);
		}
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	protected void showToast(String message) {
		showToast(null, message);
	}

	protected void showToast(Toast toast) {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(toast);
		if (Faces.isAjaxRequest()) {
			Ajax.oncomplete("Toast.show(" + json + ")");
		} else {
			Faces.setRequestAttribute("deviceToast", json);
		}
	}

	protected void sendFile(File file, String nome) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			Faces.sendFile(inputStream, nome, true);
		} catch (IOException e) {
			showToast(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void showToast(Exception exception) {
		exception.printStackTrace();
		if (exception instanceof MessageKeyException) {
			showToast((MessageKeyException) exception);
		} else {
			String rootCauseMessage = exception.getMessage();
			Throwable rootCause = ExceptionUtils.getRootCause(exception);
			if (rootCause != null) {
				rootCauseMessage = exception.getMessage();
			}
			String message = getMessage("erroInesperado.error", rootCauseMessage);
			showToast(Type.DANGER, message);
		}
	}

	protected String getErrorMessage(Exception exception) {
		if (exception instanceof MessageKeyException) {
			MessageKeyException e = (MessageKeyException) exception;
			String key = e.getKey();
			Object[] args = e.getArgs();
			String message = getMessage(key, args);
			return message;
		} else {
			String rootCauseMessage = exception.getMessage();
			Throwable rootCause = ExceptionUtils.getRootCause(exception);
			if (rootCause != null) {
				rootCauseMessage = exception.getMessage();
			}
			String message = getMessage("erroInesperado.error", rootCauseMessage);
			return message;
		}
	}

	protected void showToast(MessageKeyException exception) {
		String key = exception.getKey();
		Object[] args = exception.getArgs();
		showToastByMessageKey(Type.DANGER, key, args);
	}

	protected void showToast(Type type, String message) {
		showToast(new Toast(type, message));
	}

	protected void showToastByMessageKey(String key, Object... args) {
		showToastByMessageKey(null, key, args);
	}

	protected void showToastByMessageKey(Type type, String key, Object... args) {
		String message = getMessage(key, args);
		showToast(type, message);
	}

	protected String getMessage(String key, Object... args) {
		ResourceBundle bundle = FacesUtil.getMessages();
		String message = bundle.getString(key);
		if (args != null) {
			message = MessageFormat.format(message, args);
		}
		return message;
	}

	protected FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	protected ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	protected HttpServletRequest getRequest() {
		ExternalContext externalContext = getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		return request;
	}

	protected Origem getDevice() {
		HttpServletRequest request = getRequest();
		String deviceSo = request.getHeader("Device-SO");
		if ("IOS".equalsIgnoreCase(deviceSo)) {
			return Origem.IOS;
		} else if ("Android".equalsIgnoreCase(deviceSo)) {
			return Origem.ANDROID;
		}
		return null;
	}
}
