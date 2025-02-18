package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="LOGIN_LOG")
public class LoginLog extends net.wasys.util.ddd.Entity {

	private Long id;
	private Usuario usuario;
	private Date dataAcesso;
	private Date dataFimAcesso;
	private String server;
	private String locale;
	private String protocol;
	private String remoteHost;
	private String scheme;
	private String serverName;
	private Integer serverPort;
	private String contextPath;
	private String servletPath;
	private String remoteUser;
	private String userAgent;
	private String headers;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID", nullable=false)
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ACESSO")
	public Date getDataAcesso() {
		return dataAcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_FIM_ACESSO")
	public Date getDataFimAcesso() {
		return dataFimAcesso;
	}

	public void setDataFimAcesso(Date dataFimAcesso) {
		this.dataFimAcesso = dataFimAcesso;
	}

	public void setDataAcesso(Date dataAcesso) {
		this.dataAcesso = dataAcesso;
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

	@Column(name="USER_AGENT")
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
}




