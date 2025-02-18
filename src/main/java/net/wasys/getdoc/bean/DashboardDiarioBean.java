package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.HighchartsColor;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.DashboardDiarioService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.*;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.HorasUteisCalculator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class DashboardDiarioBean extends AbstractBean {

	@Autowired private DashboardDiarioService dashboardDiarioService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private DashboardDiarioFiltro filtro = new DashboardDiarioFiltro();
	private DashboardDiarioVO dashboardDiarioVO;

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

		Date dataAtual = new Date();
		String mesSelecionado = DummyUtils.format(dataInicioDia, "MM/yyyy");
		String mesAtual = DummyUtils.format(dataAtual, "MM/yyyy");
		if(mesSelecionado.equals(mesAtual)){
			dataFimMes = dataAtual;
		}

		HorasUteisCalculator huc = new HorasUteisCalculator(null, null);
		long diasUteis = huc.calculaDiasUteis(dataInicioMes, dataFimMes);
		filtro.setDiasUteis(diasUteis);

		dashboardDiarioVO = dashboardDiarioService.buildVO(filtro);
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
		campusList = dashboardDiarioService.findRegistroValorVOCombos(regional, BaseInterna.CAMPUS_ID, BaseRegistro.COD_REGIONAL);
	}

	public void findCursos(){
		String campus = filtro.getCampus();
		cursos = dashboardDiarioService.findRegistroValorVOCombos(campus, BaseInterna.CURSO_ID, BaseRegistro.COD_CAMPUS);
	}

	//GERA GRAFICOS
	public void geraGraficoSituacaoPorHora() {
		SituacaoPorHora inscritoPorHora = dashboardDiarioVO.getInscritoPorHora();
		List<Long> inscritoPorHoraQtd = inscritoPorHora.getQtdPorHora();
		List<Double> inscritoPorHoraMedia = inscritoPorHora.getQtdPorHoraMediaMensal();
		long inscritoTotalDia = inscritoPorHora.getTotalDia();
		long inscritoTotalMes = inscritoPorHora.getTotalMediaMensal();

		SituacaoPorHora emConferenciaPorHora = dashboardDiarioVO.getEmConferenciaPorHora();
		List<Long> emConferenciaPorHoraQtd = emConferenciaPorHora.getQtdPorHora();
		List<Double> emConferenciaPorHoraMedia = emConferenciaPorHora.getQtdPorHoraMediaMensal();
		long emConferenciaTotalDia = emConferenciaPorHora.getTotalDia();
		long emConferenciaTotalMes = emConferenciaPorHora.getTotalMediaMensal();

		SituacaoPorHora pendentePorHora = dashboardDiarioVO.getPendentePorHora();
		List<Long> pendentePorHoraQtd = pendentePorHora.getQtdPorHora();
		List<Double> pendentePorHoraMedia = pendentePorHora.getQtdPorHoraMediaMensal();
		long pendenteTotalDia = pendentePorHora.getTotalDia();
		long pendenteTotalMes = pendentePorHora.getTotalMediaMensal();

		SituacaoPorHora aprovadoPorHora = dashboardDiarioVO.getAprovadoPorHora();
		List<Long> aprovadoPorHoraQtd = aprovadoPorHora.getQtdPorHora();
		List<Double> aprovadoPorHoraMedia = aprovadoPorHora.getQtdPorHoraMediaMensal();
		long aprovadoTotalDia = aprovadoPorHora.getTotalDia();
		long aprovadoTotalMes = aprovadoPorHora.getTotalMediaMensal();

		double inscritoQtdMax = (double) Collections.max(inscritoPorHoraQtd);
		double inscritoMediaMax = Collections.max(inscritoPorHoraMedia);
		double emConferenciaQtdMax = (double) Collections.max(emConferenciaPorHoraQtd);
		double emConferenciaMediaMax = Collections.max(emConferenciaPorHoraMedia);
		double pendenteQtdMax = (double) Collections.max(pendentePorHoraQtd);
		double pendenteMediaMax = Collections.max(pendentePorHoraMedia);
		double aprovadoQtdMax = (double) Collections.max(aprovadoPorHoraQtd);
		double aprovadoMediaMax = Collections.max(aprovadoPorHoraMedia);

		List<Double> valoresMax = Arrays.asList(inscritoQtdMax, inscritoMediaMax, emConferenciaQtdMax, emConferenciaMediaMax, pendenteQtdMax, pendenteMediaMax, aprovadoQtdMax, aprovadoMediaMax);
		double yMax = Collections.max(valoresMax);
		yMax = Math.ceil(yMax);

		Ajax.oncomplete("drawInscritoPorHora(" + inscritoPorHoraQtd + ", " + inscritoPorHoraMedia + "," + yMax + "," + inscritoTotalDia + "," + inscritoTotalMes + ")");
		Ajax.oncomplete("drawEmConferenciaPorHora(" + emConferenciaPorHoraQtd + ", " + emConferenciaPorHoraMedia + "," + yMax + "," + emConferenciaTotalDia + "," + emConferenciaTotalMes + ")");
		Ajax.oncomplete("drawPendentePorHora(" + pendentePorHoraQtd + ", " + pendentePorHoraMedia + "," + yMax + "," + pendenteTotalDia + "," + pendenteTotalMes + ")");
		Ajax.oncomplete("drawAprovadoPorHora(" + aprovadoPorHoraQtd + ", " + aprovadoPorHoraMedia + "," + yMax + "," + aprovadoTotalDia + "," + aprovadoTotalMes + ")");
	}


	public void geraGraficoTempoOperacao() {
		TempoOperacao tempoOperacao = dashboardDiarioVO.getTempoOperacao();
		List<Double> digitalizado = tempoOperacao.getDigitalizado();
		List<Double> conferido = tempoOperacao.getConferido();

		double tempoMedioDigitalizado = digitalizado.stream().mapToDouble(val -> val).average().orElse(0.0);
		String formatacaoTempoDigitalizado = tempoMedioDigitalizado > 24 ? "dia" : tempoMedioDigitalizado > 1 ? "hora" : "minuto";

		double tempoMedioConferido = conferido.stream().mapToDouble(val -> val).average().orElse(0.0);
		String formatacaoTempoConferido = tempoMedioConferido > 24 ? "dia" : tempoMedioConferido > 1 ? "hora" : "minuto";

		Ajax.oncomplete("drawTempoOperacao(" + digitalizado + ", '" + formatacaoTempoDigitalizado + "')");
		Ajax.oncomplete("drawTempoOperacaoConferido(" + conferido + ",'" + formatacaoTempoConferido + "')");
	}

	public void geraGraficoInscritoTipoProcesso() {
		List<Pizza> inscritoTipoProcesso = dashboardDiarioVO.getInscritoTipoProcesso();
		JSONArray jsonArray = new JSONArray();
		long total = 0;

		for(Pizza pizza: inscritoTipoProcesso){
			String nome = pizza.getNome();
			String color = HighchartsColor.getColorForTipoProcesso(nome);
			long qtd = pizza.getQuantidade();
			total += qtd;

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", nome);
			jsonObject.put("y", qtd);
			jsonObject.put("color", color);

			jsonArray.put(jsonObject);
		}

		if(inscritoTipoProcesso.size() == 0){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", "vazio");
			jsonObject.put("y", 0);
			jsonArray.put(jsonObject);
		}
		Ajax.oncomplete("drawInscritoTipoProcesso(" + jsonArray + ", " + total + ")");
	}

	public void geraGraficoDigitalizadoTipoProcesso() {

		List<Pizza> digitalizadoTipoProcesso = dashboardDiarioVO.getDigitalizadoTipoProcesso();
		JSONArray jsonArray = new JSONArray();
		long total = 0;

		for(Pizza pizza: digitalizadoTipoProcesso){
			String nome = pizza.getNome();
			String color = HighchartsColor.getColorForTipoProcesso(nome);
			long qtd = pizza.getQuantidade();
			total += qtd;

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", nome);
			jsonObject.put("y", qtd);
			jsonObject.put("color", color);

			jsonArray.put(jsonObject);
		}

		if(digitalizadoTipoProcesso.size() == 0){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", "vazio");
			jsonObject.put("y", 0);
			jsonArray.put(jsonObject);
		}

		Ajax.oncomplete("drawDigitalizadoTipoProcesso(" + jsonArray + ", " + total + ")");
	}

	public void geraGraficoConferidoTipoProcesso() {
		List<Pizza> conferidoTipoProcesso = dashboardDiarioVO.getConferidoTipoProcesso();
		JSONArray jsonArray = new JSONArray();
		long total = 0;

		for(Pizza pizza: conferidoTipoProcesso){
			String nome = pizza.getNome();
			String color = HighchartsColor.getColorForTipoProcesso(nome);
			long qtd = pizza.getQuantidade();
			total += qtd;

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", nome);
			jsonObject.put("y", qtd);
			jsonObject.put("color", color);

			jsonArray.put(jsonObject);
		}

		if(conferidoTipoProcesso.size() == 0){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", "vazio");
			jsonObject.put("y", 0);
			jsonArray.put(jsonObject);
		}

		Ajax.oncomplete("drawConferidoTipoProcesso(" + jsonArray + ", " + total + ")");
	}

	public void geraGraficoPendenciaPorDocumento() {
		List<Pizza> pendenciaPorDocumento = dashboardDiarioVO.getPendenciaPorDocumento();
		List<JSONObject> jsonList = new ArrayList<>();

		for(Pizza pizza : pendenciaPorDocumento){
			long qtd = pizza.getQuantidade();
			String nome = pizza.getNome();
			if(nome.length() > 40){
				nome = nome.substring(0, 39) + "...";
				pizza.setNome(nome);
			}

			String jsonString = DummyUtils.objectToJson(pizza);
			JSONObject jsonObject  = new JSONObject(jsonString);
			jsonObject.put("data", Arrays.asList(qtd));
			jsonList.add(jsonObject);
		}

		if(jsonList.size() == 0){
			Ajax.oncomplete("drawPendenciaPorDocumento([{name: 'vazio', data: [0]}])");
		}
		else {
			Ajax.oncomplete("drawPendenciaPorDocumento(" + jsonList + ")");
		}
	}

	public void geraGraficoPendenciaPorIrregularidade() {
		List<Pizza> pendenciaPorIrregularidade = dashboardDiarioVO.getPendenciaPorIrregularidade();
		List<JSONObject> jsonList = new ArrayList<>();

		for(Pizza pizza : pendenciaPorIrregularidade){
			long qtd = pizza.getQuantidade();

			String jsonString = DummyUtils.objectToJson(pizza);
			JSONObject jsonObject  = new JSONObject(jsonString);
			jsonObject.put("data", Arrays.asList(qtd));
			jsonList.add(jsonObject);
		}

		if(jsonList.size() == 0){
			Ajax.oncomplete("drawPendenciaPorIrregularidade([{name: 'vazio', data: [0]}])");
		}
		else{
			Ajax.oncomplete("drawPendenciaPorIrregularidade(" + jsonList + ")");
		}
	}

	public DashboardDiarioFiltro getFiltro() {
		return filtro;
	}

	public DashboardDiarioVO getDashboardDiarioVO() {
		return dashboardDiarioVO;
	}

	public void setDashboardDiarioVO(DashboardDiarioVO dashboardDiarioVO) {
		this.dashboardDiarioVO = dashboardDiarioVO;
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