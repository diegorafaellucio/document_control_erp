package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ConsultaExternaDataModel;
import net.wasys.getdoc.bean.datamodel.ConsultaExternaLogDataModel;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.service.ConsultaExternaLogService;
import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.domain.vo.StatusWSVO;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaFiltro;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaLogFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioConsultaExternaBean extends AbstractBean {

	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private ConsultaExternaLogService consultaExternaLogService;

	private ConsultaExternaFiltro filtro;
	private ConsultaExterna consultaExternaVisualizacao;
	private ConsultaExternaDataModel consultaExternaDataModel;
	private ConsultaExternaLogDataModel consultaExternaLogDataModel;
	private HorizontalBarChartModel horizontalBarChartModel;
	private List<StatusWSVO> statusWsVos;

	protected void initBean(){
		consultaExternaDataModel = new ConsultaExternaDataModel();
		consultaExternaDataModel.setConsultaExternaService(consultaExternaService);
		ConsultaExternaFiltro filtro = getFiltro();
		filtro.setValido(false);
		Date hoje = DummyUtils.truncateInicioDia(new Date());
		Date hoje2 = DummyUtils.truncateFinalDia(hoje);
		filtro.setDataInicio(hoje);
		filtro.setDataFim(hoje2);
		consultaExternaDataModel.setFiltro(filtro);
		carregarChart();
	}

	private void carregarChart() {
		try {
			List<Integer> listCel = consultaExternaLogService.total();
			Double total = 0.0;
			Double repetido = 0.0;
			for (Integer value : listCel) {
				repetido += value - 1;
				total += value;
			}

			Double repetidoPercent = (repetido * 100) / total;
			Double totalPercent = 100.0 - repetidoPercent;
			setHorizontalBarChartModel(new HorizontalBarChartModel());
			ChartSeries totalChart = new ChartSeries("Total");
			totalChart.set("%", totalPercent);

			ChartSeries repetidoChart = new ChartSeries("Repetido");
			repetidoChart.set("%", repetidoPercent);

			getHorizontalBarChartModel().addSeries(totalChart);
			getHorizontalBarChartModel().addSeries(repetidoChart);

			getHorizontalBarChartModel().setTitle("Consultas Externas - Total e Repetido");
			getHorizontalBarChartModel().setLegendPosition("e");
			getHorizontalBarChartModel().setStacked(true);

			Axis xAxis = getHorizontalBarChartModel().getAxis(AxisType.X);
			xAxis.setMin(0);
			xAxis.setMax(100);
			xAxis.setTickFormat("%.0f");
			int consultasExternas = total.intValue() - repetido.intValue();
			int consultasLog = repetido.intValue();
			xAxis.setLabel("Total de Consultas Externas: " + consultasExternas + " - Consultas Repetidas: " + consultasLog);
			xAxis.setTickInterval("10");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void carregarConsultaExternaLog(ConsultaExterna ce) {
		try {
			setConsultaExternaLogDataModel(new ConsultaExternaLogDataModel());
			getConsultaExternaLogDataModel().setFiltro(new ConsultaExternaLogFiltro(ce));
			getConsultaExternaLogDataModel().setConsultaExternaLogService(consultaExternaLogService);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public ConsultaExternaFiltro getFiltro() {
		if(filtro == null) {
			filtro = new ConsultaExternaFiltro();
		}
		return filtro;
	}

	public void setFiltro(ConsultaExternaFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoConsultaExterna> getListaTipoConsultaExterna(){
		return Arrays.asList(TipoConsultaExterna.values());
	}

	public List<StatusConsultaExterna> getListaStatus(){
		return Arrays.asList(StatusConsultaExterna.values());
	}

	public ConsultaExternaDataModel getConsultaExternaDataModel() {
		return consultaExternaDataModel;
	}

	public HorizontalBarChartModel getHorizontalBarChartModel() {
		return horizontalBarChartModel;
	}

	public void setHorizontalBarChartModel(HorizontalBarChartModel horizontalBarChartModel) {
		this.horizontalBarChartModel = horizontalBarChartModel;
	}

	public ConsultaExterna getConsultaExternaVisualizacao() {
		if(consultaExternaVisualizacao == null) {
			consultaExternaVisualizacao = new ConsultaExterna();
		}
		return consultaExternaVisualizacao;
	}

	public void setConsultaExternaVisualizacao(ConsultaExterna consultaExternaVisualizacao) {
		this.consultaExternaVisualizacao = consultaExternaVisualizacao;
	}

	public ConsultaExternaLogDataModel getConsultaExternaLogDataModel() {
		return consultaExternaLogDataModel;
	}

	public void setConsultaExternaLogDataModel(ConsultaExternaLogDataModel consultaExternaLogDataModel) {
		this.consultaExternaLogDataModel = consultaExternaLogDataModel;
	}

	public void buscar() {
		filtro.setValido(true);

		statusWsVos = consultaExternaService.findWsStatus();
	}

	public List<StatusWSVO> getStatusWsVos() {
		return statusWsVos;
	}
}
