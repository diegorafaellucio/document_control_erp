package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import org.primefaces.model.SortOrder;

import java.util.Date;

public class BacalhauFiltro {

	private String servletPath;
	private String campoOrdem;
	private SortOrder ordem;
	private Date dataInicio;
	private Date dataFim;
	private boolean apenasErros;
	private boolean recuperadaDoCache;
	private boolean ahRecuperar;
	private TipoExecucaoBacalhau tipoExecucaoBacalhau;

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public void setCampoOrdem(String campoOrdem) {
		this.campoOrdem = campoOrdem;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public void setOrdem(SortOrder ordem) {
		this.ordem = ordem;
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

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}

	public boolean isApenasErros() {
		return apenasErros;
	}

	public boolean getApenasErros() {
		return apenasErros;
	}

	public void setApenasErros(boolean apenasErros) {
		this.apenasErros = apenasErros;
	}

	public boolean getRecuperadaDoCache() {
		return recuperadaDoCache;
	}

	public void setRecuperadaDoCache(boolean recuperadaDoCache) {
		this.recuperadaDoCache = recuperadaDoCache;
	}

	public boolean getAhRecuperar() {
		return ahRecuperar;
	}

	public void setAhRecuperar(boolean ahRecuperar) {
		this.ahRecuperar = ahRecuperar;
	}

	public TipoExecucaoBacalhau getTipoExecucaoBacalhau() {
		return tipoExecucaoBacalhau;
	}

	public void setTipoExecucaoBacalhau(TipoExecucaoBacalhau tipoExecucaoBacalhau) {
		this.tipoExecucaoBacalhau = tipoExecucaoBacalhau;
	}
}
