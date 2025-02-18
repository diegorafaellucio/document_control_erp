package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.Origem;

import java.util.Date;
import java.util.List;

public class DashboardMensalFiltro {

	private Date dataInicioDia;
	private	Date dataFimDia;
	private	Date dataInicioMes;
	private	Date dataFimMes;
	private long diasUteis;
	private String mes;
	private String ano;
	private Integer diasMes;

	private List<Long> tiposProcessoId;
	private Origem origemProcesso;
	private String regional;
	private String campus;
	private String curso;
	private boolean tratados = false;

	public Date getDataInicioDia() {
		return dataInicioDia;
	}

	public void setDataInicioDia(Date dataInicioDia) {
		this.dataInicioDia = dataInicioDia;
	}

	public Date getDataFimDia() {
		return dataFimDia;
	}

	public void setDataFimDia(Date dataFimDia) {
		this.dataFimDia = dataFimDia;
	}

	public Date getDataInicioMes() {
		return dataInicioMes;
	}

	public void setDataInicioMes(Date dataInicioMes) {
		this.dataInicioMes = dataInicioMes;
	}

	public Date getDataFimMes() {
		return dataFimMes;
	}

	public void setDataFimMes(Date dataFimMes) {
		this.dataFimMes = dataFimMes;
	}

	public long getDiasUteis() {
		return diasUteis;
	}

	public void setDiasUteis(long diasUteis) {
		this.diasUteis = diasUteis;
	}

	public List<Long> getTiposProcessoId() {
		return tiposProcessoId;
	}

	public void setTiposProcessoId(List<Long> tiposProcessoId) {
		this.tiposProcessoId = tiposProcessoId;
	}

	public Origem getOrigemProcesso() {
		return origemProcesso;
	}

	public void setOrigemProcesso(Origem origemProcesso) {
		this.origemProcesso = origemProcesso;
	}

	public String getRegional() {
		return regional;
	}

	public void setRegional(String regional) {
		this.regional = regional;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public boolean isTratados() {
		return tratados;
	}

	public void setTratados(boolean tratados) {
		this.tratados = tratados;
	}

	public String getMes(){return this.mes;}

	public void setMes(String mes){this.mes = mes;}

	public String getAno(){return this.ano;}

	public void setAno(String ano){this.ano = ano;}

	public Integer getDiasMes() {
		return diasMes;
	}

	public void setDiasMes(Integer diasMes) {
		this.diasMes = diasMes;
	}

	@Override public String toString() {
		return "DashboardMensalFiltro{" +
				"dataInicioDia=" + dataInicioDia +
				", dataFimDia=" + dataFimDia +
				", dataInicioMes=" + dataInicioMes +
				", dataFimMes=" + dataFimMes +
				", diasUteis=" + diasUteis +
				", mes='" + mes + '\'' +
				", ano='" + ano + '\'' +
				", diasMes=" + diasMes +
				", tiposProcessoId=" + tiposProcessoId +
				", origemProcesso=" + origemProcesso +
				", regional='" + regional + '\'' +
				", campus='" + campus + '\'' +
				", curso='" + curso + '\'' +
				", tratados=" + tratados +
				'}';
	}
}
