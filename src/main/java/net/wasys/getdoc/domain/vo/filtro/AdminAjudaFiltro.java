package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Usuario;
import org.primefaces.model.SortOrder;

import java.util.Date;

public class AdminAjudaFiltro implements Cloneable {

	private Date dataInicio;
	private Date dataFim;
	private String campoOrdem;
	private SortOrder ordem;
	private Usuario analista;

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

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public void setCampoOrdem(String campoOrdem) { this.campoOrdem = campoOrdem; }

	public SortOrder getOrdem() { return ordem;	}

	public void setOrdem(SortOrder ordem) {	this.ordem = ordem;	}

	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	public AdminAjudaFiltro clone() {
		try {
			return (AdminAjudaFiltro) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
