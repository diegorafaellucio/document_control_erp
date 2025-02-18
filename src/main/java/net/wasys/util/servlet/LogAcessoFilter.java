package net.wasys.util.servlet;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.other.Criptografia;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

public class LogAcessoFilter implements Filter {

	private static ThreadLocal<LogAcesso> threadLocalLogAcesso = new ThreadLocal<>();

	@Override
	public void init(FilterConfig arg0) {

		DummyUtils.setLogLevel(DummyUtils.getLogLevel());

		ServletContext servletContext = arg0.getServletContext();
		WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		ResourceService resourceService = appContext.getBean(ResourceService.class);
		String senhaCriptografada = resourceService.getValue(ResourceService.SENHA_CRIPTOGRAFADA);
		if(!"true".equals(senhaCriptografada)) {
			Criptografia.desabilitar();
		}
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;

		LogAcesso logAcesso = null;
		boolean logarAcesso = getLogarAcesso(request);

		String servletPath0 = request.getServletPath();
		boolean telaErro = servletPath0 != null && servletPath0.contains("/erro/");
		if(telaErro) {
			logarAcesso = false;
		}

		if(logarAcesso) {
			logAcesso = criaLogAcesso(request);
			threadLocalLogAcesso.set(logAcesso);
			response = new HttpServletResponseLogAcesso(response);
			request = new HttpServletRequestLogAcesso(request);

			Principal userPrincipal = request.getUserPrincipal();
			String userPrincipalName = userPrincipal != null ? userPrincipal.getName() : "";
			//DummyUtils.systraceThread("login: " + userPrincipalName + " " + logAcesso, LogLevel.INFO);

			String threadName = logAcesso.getThreadName();
			Thread thread = Thread.currentThread();
			thread.setName(threadName);
		}
		else if(!telaErro) {
			String servletPath = request.getServletPath();
			DummyUtils.mudarNomeThread("thdreqoff-" + servletPath);
		}

		try {
			arg2.doFilter(request, response);
		}
		catch (Exception e) {
			if(logAcesso != null) {
				String stackTrace = ExceptionUtils.getStackTrace(e);
				logAcesso.setException(stackTrace);
			}
			throw e;
		}
		finally {
			if(logAcesso != null) {
				finalizaLogAcesso(logAcesso, (HttpServletRequestLogAcesso) request, (HttpServletResponseLogAcesso) response);
				((HttpServletResponseLogAcesso) response).flushAndClose();
			}
		}
	}

	private void finalizaLogAcesso(LogAcesso logAcesso, HttpServletRequestLogAcesso request, HttpServletResponseLogAcesso response) {

		ServletContext servletContext = request.getServletContext();
		WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		LogAcessoService logAcessoService = appContext.getBean(LogAcessoService.class);

		logAcessoService.finalizaLogAcesso(logAcesso, request, response);
	}

	private LogAcesso criaLogAcesso(HttpServletRequest request) {

		ServletContext servletContext = request.getServletContext();
		WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		LogAcessoService logAcessoService = appContext.getBean(LogAcessoService.class);

		LogAcesso log = logAcessoService.criaLogAcesso(request);

		return log;
	}

	private boolean getLogarAcesso(HttpServletRequest request) {

		String requestURI = request.getRequestURI();
		boolean logarAcesso = !requestURI.contains("/resources/")
				&& !requestURI.contains("/javax.faces.resource/")
				&& !requestURI.contains("/imagem_layout/");

		return logarAcesso;
	}

	public static LogAcesso getLogAcesso() {
		return threadLocalLogAcesso.get();
	}

	public static void setLogAcesso(LogAcesso logAcesso) {
		threadLocalLogAcesso.set(logAcesso);
	}

	@Override
	public void destroy() { }
}
