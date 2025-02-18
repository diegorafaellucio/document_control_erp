package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusImportacao;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import org.primefaces.model.SortOrder;

import java.util.Date;

public class LogImportacaoFiltro {

	private String usuario;
	private TipoImportacao tipoImportacao;
	private String campoOrdem;
	private SortOrder ordem;
	private TipoProcesso tipoProcesso;
	private Date dataInicio;
	private Date dataFim;
	private StatusImportacao status;
	private String nomeArquivo;

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public TipoImportacao getTipoImportacao() {
		return tipoImportacao;
	}

	public void setTipoImportacao(TipoImportacao tipoImportacao) {
		this.tipoImportacao = tipoImportacao;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public void setOrdem(SortOrder ordem) {
		this.ordem = ordem;
	}

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public void setCampoOrdem(String campoOrdem) {
		this.campoOrdem = campoOrdem;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
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

	public StatusImportacao getStatus() {
		return status;
	}

	public void setStatus(StatusImportacao status) {
		this.status = status;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}
}
