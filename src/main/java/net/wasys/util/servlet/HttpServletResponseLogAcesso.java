package net.wasys.util.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

public class HttpServletResponseLogAcesso extends HttpServletResponseWrapper {

	private ServletOutputStream outputStream;
	private PrintWriter writer;
	private ServletOutputStreamSizeMeter sizeMeter;
	private String redirect;
	private static ThreadLocal<String> redirectTL = new ThreadLocal<>();

	public HttpServletResponseLogAcesso(HttpServletResponse response) {
		super(response);
		redirectTL.set(null);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		this.redirect = location;
		super.sendRedirect(location);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called on this response.");
		}

		if (outputStream == null) {
			outputStream = getResponse().getOutputStream();
			sizeMeter = new ServletOutputStreamSizeMeter(outputStream);
		}

		return sizeMeter;
	}

	@Override
	public PrintWriter getWriter() throws IOException {

		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() has already been called on this response.");
		}

		if (writer == null) {
			ServletResponse response = getResponse();
			ServletOutputStream outputStream2 = response.getOutputStream();
			String characterEncoding = response.getCharacterEncoding();
			sizeMeter = new ServletOutputStreamSizeMeter(outputStream2);
			writer = new PrintWriter(new OutputStreamWriter(sizeMeter, characterEncoding), true);
		}

		return writer;
	}

	public int getSizeCount() {
		if (sizeMeter != null) {
			return sizeMeter.getSizeCount();
		} else {
			return 0;
		}
	}

	public String getRedirect() {

		if(StringUtils.isNotBlank(redirect)) {
			return redirect;
		}

		return redirectTL.get();
	}

	public void flushAndClose() {

		if(writer != null) {

			writer.flush();
			writer.close();
		}
	}

	public static void registrarRedirect(String path) {
		redirectTL.set(path);
	}
}