package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.DashboardCampos;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class DashboardFiltro {

	private Date dataInicio;
	private Date dataFim;
	private String campus;
	private RegistroValorVO regional;
	private DashboardCampos.SituacaoEnum situacao;
	private DashboardCampos.SituacaoEnum situacaoCompara;
	private DashboardCampos.IntervaloEnum interval;
	private DashboardCampos.TipoAgrupamentoEnum tipoArupamento;

	public Date getDataInicio() {
		return DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
	}

	public Date getDataFim() {
		return DummyUtils.truncateFinalDia(dataFim);
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = DummyUtils.truncateFinalDia(dataFim);
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public RegistroValorVO getRegional() {
		return regional;
	}

	public void setRegional(RegistroValorVO regional) {
		this.regional = regional;
	}

	public DashboardCampos.SituacaoEnum getSituacao() {
		return situacao;
	}

	public void setSituacao(DashboardCampos.SituacaoEnum situacao) {
		this.situacao = situacao;
	}

	public DashboardCampos.SituacaoEnum getSituacaoCompara() {
		return situacaoCompara;
	}

	public void setSituacaoCompara(DashboardCampos.SituacaoEnum situacaoCompara) {
		this.situacaoCompara = situacaoCompara;
	}

	public DashboardCampos.IntervaloEnum getInterval() {
		return interval;
	}

	public void setInterval(DashboardCampos.IntervaloEnum interval) {
		this.interval = interval;
	}

	public DashboardCampos.TipoAgrupamentoEnum getTipoAgrupamento() {
		return tipoArupamento;
	}

	public void setTipoAgrupamento(DashboardCampos.TipoAgrupamentoEnum tipoArupamento) {
		this.tipoArupamento = tipoArupamento;
	}

}
