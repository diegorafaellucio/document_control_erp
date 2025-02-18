package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;

import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoRegistro;

public class LogAlteracaoFiltro {

	private Date dataInicio;
	private Date dataFim;
	private Long usuarioId;
	private TipoRegistro tipoRegistro;
	private TipoAlteracao tipoAlteracao;
	private Long registroId;
	private boolean valid;
	private String campoOrdem;
	private SortOrder ordem;

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

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public TipoRegistro getTipoRegistro() {
		return tipoRegistro;
	}

	public void setTipoRegistro(TipoRegistro tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}

	public TipoAlteracao getTipoAlteracao() {
		return tipoAlteracao;
	}

	public void setTipoAlteracao(TipoAlteracao tipoAlteracao) {
		this.tipoAlteracao = tipoAlteracao;
	}

	public Long getRegistroId() {
		return registroId;
	}

	public void setRegistroId(Long registroId) {
		this.registroId = registroId;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
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
}
