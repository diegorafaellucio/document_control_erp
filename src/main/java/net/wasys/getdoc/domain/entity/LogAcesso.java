package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="LOG_ACESSO")
public class LogAcesso extends net.wasys.util.ddd.Entity {

	private Long id;
	private String server;
	private String locale;
	private String method;
	private String parameters;
	private String protocol;
	private String remoteHost;
	private String scheme;
	private String serverName;
	private Integer serverPort;
	private String contextPath;
	private String servletPath;
	private String remoteUser;
	private Date inicio;
	private String exception;
	private String forward;
	private String redirect;
	private String contentType;
	private Integer status;
	private Date fim;
	private Long tempo;
	private Boolean ajax;
	private String userAgent;
	private Integer contentSize;
	private Integer contentLength;
	private String headers;
	private String threadName;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name="REMOTE_HOST")
	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Column(name="SERVER_NAME")
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Column(name="SERVER_PORT")
	public Integer getServerPort() {
		return serverPort;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	@Column(name="CONTEXT_PATH")
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Column(name="SERVLET_PATH")
	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	@Column(name="REMOTE_USER")
	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	@Column(name="CONTENT_TYPE")
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	public Boolean getAjax() {
		return ajax;
	}

	public void setAjax(Boolean ajax) {
		this.ajax = ajax;
	}

	@Column(name="USER_AGENT")
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Column(name="CONTENT_SIZE")
	public Integer getContentSize() {
		return contentSize;
	}

	public void setContentSize(Integer contentSize) {
		this.contentSize = contentSize;
	}

	@Column(name="CONTENT_LENGTH")
	public Integer getContentLength() {
		return contentLength;
	}

	public void setContentLength(Integer contentLength) {
		this.contentLength = contentLength;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	@Column(name="THREAD_NAME")
	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	@Transient
	public Long getTempoAgora() {
		Long tempo = getTempo();
		if(tempo != null) {
			return tempo;
		}
		Date inicio = getInicio();
		if(inicio == null) {
			return null;
		}
		return System.currentTimeMillis() - inicio.getTime();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{path:" + getServletPath() + ";ajax:" + getAjax() + ";params:" + getParameters() + "}";
	}
}
