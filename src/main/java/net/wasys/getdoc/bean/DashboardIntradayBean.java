package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.DashboardIntradayService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO.TempoOperacao;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO.TipoProcessoPorDia;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.text.DecimalFormat;
import java.util.*;

@ManagedBean
@ViewScoped
public class DashboardIntradayBean extends AbstractBean {

	@Autowired private DashboardIntradayService dashboardIntradayService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private DashboardDiarioFiltro filtro = new DashboardDiarioFiltro();
	private DashboardIntradayVO dashboardIntradayVO;

	private Boolean reloadAutomatico;
	private Boolean modoTelaCheia;

	private List<RegistroValorVO> regionais;
	private List<RegistroValorVO> campusList;
	private List<RegistroValorVO> cursos;
	private List<TipoProcesso> tiposProcesso;

	protected void initBean(){
		reloadAutomatico = false;
		modoTelaCheia = false;

		initFiltros();
		filtro.setDataInicioDia(new Date());
		filtro.setDataFimDia(new Date());
	}

	public void buscar() {
		Date dataInicioDia = DateUtils.getFirstTimeOfDay(filtro.getDataInicioDia());
		Date dataFimDia = DateUtils.getLastTimeOfDay(filtro.getDataFimDia());

		int daysBetween = DateUtils.getDaysBetween(dataFimDia, dataInicioDia);
		if(daysBetween >= 31){
			addMessageError("intervaloData.error", 31);
			return;
		}

		Date dataInicioMes = DateUtils.getActualMinDay(dataInicioDia);
		Date dataFimMes = DateUtils.getActualMaxDay(dataInicioDia);
		dataFimMes = DateUtils.getLastTimeOfDay(dataFimMes);

		filtro.setDataInicioDia(dataInicioDia);
		filtro.setDataFimDia(dataFimDia);
		filtro.setDataInicioMes(dataInicioMes);
		filtro.setDataFimMes(dataFimMes);

		dashboardIntradayVO = dashboardIntradayService.buildVO(filtro);
		long totalPeriodo = dashboardIntradayVO.getTipoProcessoPorDia().getTotalPeriodo();
		if(totalPeriodo == 0){
			addMessageWarn("erroConsultaIntraday.error");
			return;
		}
		geraGraficoSituacaoPorDia();
		geraGraficoTempoOperacao();
		Ajax.oncomplete("start()");
	}

	//CONFGIS
	public void reloadAutomatico(){
		if (reloadAutomatico){
			Ajax.oncomplete("stopReloadAutomatico()");
			setReloadAutomatico(false);
		}
		else {
			Ajax.oncomplete("startReloadAutomatico()");
			setReloadAutomatico(true);
		}
	}

	public void modoTelaCheia(){
		if (modoTelaCheia){
			Ajax.oncomplete("exitFullscreen()");
			setModoTelaCheia(false);
		}
		else {
			Ajax.oncomplete("openFullscreen()");
			setModoTelaCheia(true);
		}
	}

	//FILTROS
	private void initFiltros(){
		BaseInterna baseInterna;
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();

		baseInterna = baseInternaService.get(BaseInterna.REGIONAL_ID);
		filtro.setBaseInterna(baseInterna);
		regionais = baseRegistroService.findByFiltro(filtro, null, null);

		tiposProcesso = tipoProcessoService.findAtivos(null);
	}

	public void findCampusList(){
		String regional = filtro.getRegional();
		campusList = dashboardIntradayService.findRegistroValorVOCombos(regional, BaseInterna.CAMPUS_ID, BaseRegistro.COD_REGIONAL);
	}

	public void findCursos(){
		String campus = filtro.getCampus();
		cursos = dashboardIntradayService.findRegistroValorVOCombos(campus, BaseInterna.CURSO_ID, BaseRegistro.COD_CAMPUS);
	}

	//GERA GRAFICOS
	public void geraGraficoSituacaoPorDia() {

		TipoProcessoPorDia tipoProcessoPorDia = dashboardIntradayVO.getTipoProcessoPorDia();
		TipoProcessoPorDia tipoProcessoPorDiaTratado = dashboardIntradayVO.getTipoProcessoPorDiaTratado();
		List<Long> conferidoPorDiaQtd = dashboardIntradayService.calculaTotal(tipoProcessoPorDia);
		List<Long> tratadosPorDiaQtd = dashboardIntradayService.calculaTotal(tipoProcessoPorDiaTratado);

		tipoProcessoPorDiaTratado.setTotalPorDia(tratadosPorDiaQtd);
		tipoProcessoPorDia.setTotalPorDia(conferidoPorDiaQtd);
		long conferidoTotalDia = tipoProcessoPorDia.getTotalPeriodo();

		double conferidoQtdMax = (double) Collections.max(conferidoPorDiaQtd);

		List<Double> valoresMax = Arrays.asList(conferidoQtdMax);
		double yMax = Collections.max(valoresMax);
		yMax = Math.ceil(yMax);

		Ajax.oncomplete("drawConferidoPorDia(" + conferidoPorDiaQtd + ", " + yMax + "," + conferidoTotalDia +" )");
	}



	public void geraGraficoTempoOperacao() {
		TempoOperacao tempoOperacao = dashboardIntradayVO.getTempoOperacao();
		List<Double> conferido = tempoOperacao.getConferido();

		double tempoMedioConferido = conferido.stream().mapToDouble(val -> val).average().orElse(0.0);
		String formatacaoTempoConferido = tempoMedioConferido > 24 ? "dia" : tempoMedioConferido > 1 ? "hora" : "minuto";

		Ajax.oncomplete("drawTempoOperacaoConferido(" + conferido + ",'" + formatacaoTempoConferido + "')");
	}

	public int getMes(int dia){
		Date dataInicioDia = filtro.getDataInicioDia();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataInicioDia);
		int mountBegin = cal.get(Calendar.MONTH)+1;

		Date dataFimDia = filtro.getDataFimDia();
		cal.setTime(dataInicioDia);
		cal.setTime(dataFimDia);
		int dayEnd = cal.get(Calendar.DAY_OF_MONTH);
		int mountEnd = cal.get(Calendar.MONTH)+1;

		if(dia <= dayEnd){
			return mountEnd;
		}else{
			return mountBegin;
		}

	}

	public String getAgingProcesso(){
		if(dashboardIntradayVO == null){
			return "0.0";
		}
		TempoOperacao tempoOperacao = dashboardIntradayVO.getTempoOperacao();
		List<Double> conferido = tempoOperacao.getConferido();
		Double agingProcesso = conferido.get(12);
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(agingProcesso).replace(",", ".");
	}

	public DashboardDiarioFiltro getFiltro() {
		return filtro;
	}

	public DashboardIntradayVO getDashboardIntradayVO() {
		return dashboardIntradayVO;
	}

	public void setDashboardIntradayVO(DashboardIntradayVO dashboardIntradayVO) {
		this.dashboardIntradayVO = dashboardIntradayVO;
	}

	public Boolean getReloadAutomatico() {
		return reloadAutomatico;
	}

	public void setReloadAutomatico(Boolean reloadAutomatico) {
		this.reloadAutomatico = reloadAutomatico;
	}

	public Boolean getModoTelaCheia() {
		return modoTelaCheia;
	}

	public void setModoTelaCheia(Boolean modoTelaCheia) {
		this.modoTelaCheia = modoTelaCheia;
	}

	public List<RegistroValorVO> getRegionais() {
		return regionais;
	}

	public void setRegionais(List<RegistroValorVO> regionais) {
		this.regionais = regionais;
	}

	public List<RegistroValorVO> getCampusList() {
		return campusList;
	}

	public void setCampusList(List<RegistroValorVO> campusList) {
		this.campusList = campusList;
	}

	public List<RegistroValorVO> getCursos() {
		return cursos;
	}

	public void setCursos(List<RegistroValorVO> cursos) {
		this.cursos = cursos;
	}

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}

	public void setTiposProcesso(List<TipoProcesso> tiposProcesso) {
		this.tiposProcesso = tiposProcesso;
	}
}