package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Usuario;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;

public class LogAcessoFiltro {

	private Long id;
	private String servletPath;
	private String campoOrdem;
	private SortOrder ordem;
	private Date dataInicio;
	private Date dataFim;
	private Usuario usuario = new Usuario();
	private List<String> tipoDoRegistro;
	private boolean apenasErros;
	private boolean semFim;
	private Integer intervalo = 1;
	private String parameters;
	private String headers;
	private String threadName;
	private String server;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getParameters() {
		return parameters;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public List<String> getTipoDoRegistro() {
		return tipoDoRegistro;
	}

	public void setTipoDoRegistro(List<String> tipoDoRegistro) {
		this.tipoDoRegistro = tipoDoRegistro;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public boolean getApenasErros() {
		return apenasErros;
	}

	public void setApenasErros(boolean apenasErros) {
		this.apenasErros = apenasErros;
	}

	public boolean isSemFim() {
		return semFim;
	}

	public void setSemFim(boolean semFim) {
		this.semFim = semFim;
	}

	public Integer getIntervalo() {
		return intervalo;
	}

	public void setIntervalo(Integer intervalo) {
		this.intervalo = intervalo;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
