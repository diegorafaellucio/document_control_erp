package net.wasys.util.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.ContentCachingRequestWrapper;

import net.wasys.util.other.Bolso;

public class HttpServletRequestLogAcesso extends ContentCachingRequestWrapper {

	private Bolso<String> forward = new Bolso<String>();

	public HttpServletRequestLogAcesso(HttpServletRequest request) {
		super(request);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {

		final RequestDispatcher requestDispatcher = super.getRequestDispatcher(path);

		return new RequestDispatcher() {

			@Override
			public void include(ServletRequest paramServletRequest, ServletResponse paramServletResponse) throws ServletException, IOException {
				requestDispatcher.include(paramServletRequest, paramServletResponse);
			}

			@Override
			public void forward(ServletRequest paramServletRequest, ServletResponse paramServletResponse) throws ServletException, IOException {
				requestDispatcher.forward(paramServletRequest, paramServletResponse);
				forward.setObjeto(path);
			}
		};
	}

	public String getForward() {
		return forward.getObjeto();
	}
}
