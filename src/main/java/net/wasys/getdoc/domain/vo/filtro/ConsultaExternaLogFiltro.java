package net.wasys.getdoc.domain.vo.filtro;

import java.util.Date;

import javax.persistence.Entity;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;

@Entity(name="CONSULTA_EXTERNA_LOG")
public class ConsultaExternaLogFiltro extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;

	private Usuario usuario;
	private Processo processo;
	private ConsultaExterna consultaExterna;
	
	public ConsultaExternaLogFiltro() {}

	public ConsultaExternaLogFiltro(ConsultaExterna ce) {
		this.consultaExterna = ce;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	public Processo getProcesso() {
		return processo;
	}
	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
	public ConsultaExterna getConsultaExterna() {
		return consultaExterna;
	}
	public void setConsultaExterna(ConsultaExterna consultaExterna) {
		this.consultaExterna = consultaExterna;
	}
}
