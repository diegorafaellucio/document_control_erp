package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class ConsultaExternaFiltro implements Cloneable{

	private Long id;
	private Long processoId;
	private TipoConsultaExterna tipo;
	private Date data;
	private Date dataInicio;
	private Date dataFim;
	private String parametros;
	private String resultado;
	private String stackTrace;
	private StatusConsultaExterna status;
	private boolean valido = true;

	private Usuario usuario;

	@Override
	public String toString() {
		return "ConsultaExterna [id=" + id + ", tipo=" + tipo + ", data=" + data + ", parametros="
				+ parametros + ", resultado=" + resultado + ", stackTrace=" + stackTrace + ", status=" + status
				+ "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public TipoConsultaExterna getTipo() {
		return tipo;
	}

	public void setTipo(TipoConsultaExterna tipo) {
		this.tipo = tipo;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getParametros() {
		return parametros;
	}

	public void setParametros(String parametros) {
		this.parametros = parametros;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public StatusConsultaExterna getStatus() {
		return status;
	}

	public void setStatus(StatusConsultaExterna status) {
		this.status = status;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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

	public boolean isValido() {
		return valido;
	}

	public void setValido(boolean valido) {
		this.valido = valido;
	}
}
