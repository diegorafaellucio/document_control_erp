package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.DashboardMensalVO;
import net.wasys.getdoc.domain.vo.DashboardMensalVO.TempoOperacao;
import net.wasys.getdoc.domain.vo.DashboardMensalVO.ListaProcessoVO;
import net.wasys.getdoc.domain.vo.DashboardMensalVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardMensalFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.*;

@ManagedBean
@ViewScoped
public class DashboardMensalBean extends AbstractBean {

	@Autowired private DashboardMensalService dashboardMensalService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ApplicationContext applicationContext;

	private DashboardMensalFiltro filtro = new DashboardMensalFiltro();
	private DashboardMensalVO dashboardMensalVO;
	private DashboardMensalExporter exporter;

	private Boolean reloadAutomatico;
	private Boolean modoTelaCheia;

	private List<RegistroValorVO> regionais;
	private List<RegistroValorVO> campusList;
	private List<RegistroValorVO> cursos;
	private List<TipoProcesso> tiposProcesso;
	private Map<Integer, String> meses;
	private List<String> anos;

	protected void initBean(){
		reloadAutomatico = false;
		modoTelaCheia = false;

		initFiltros();

		Date hoje = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(hoje);
		filtro.setMes(String.valueOf(cal.get(Calendar.MONTH) + 1));
		filtro.setAno(String.valueOf(cal.get(Calendar.YEAR)));
	}

	public void verificar() {

		if(exporter == null) {
			return;
		}

		if(exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(DashboardMensalExporter.class);
			exporter.setFiltro(filtro);
			Usuario usuarioLogado = getUsuarioLogado();
			exporter.setUsuario(usuarioLogado);
			exporter.start();
		}
	}

	public void baixar() {

		Exception error = exporter.getError();
		if(error != null) {
			addMessageError(error);
		}
		else {
			File file = exporter.getFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				String fileName = "relatorio-geral.xlsx";
				String fileName2 = exporter.getFileName();
				if(StringUtils.isNotBlank(fileName2)) {
					fileName = fileName2;
				}
				Faces.sendFile(fis, fileName, true);
			}
			catch (Exception e1) {
				addMessageError(e1);
			}
		}

		exporter = null;
	}

	public void buscar() {
		filtro.setDataInicioDia(DummyUtils.parseDateTime("01/"+filtro.getMes()+"/"+filtro.getAno()+" 00:00"));
		Calendar datas = new GregorianCalendar();
		datas.setTime(filtro.getDataInicioDia());
		filtro.setDiasMes(datas.getActualMaximum (Calendar.DAY_OF_MONTH));
		filtro.setDataFimDia(DummyUtils.parseDateTime(filtro.getDiasMes() + "/"+filtro.getMes()+"/"+filtro.getAno()+" 00:00"));

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

		dashboardMensalVO = dashboardMensalService.buildVO(filtro);
		long totalPeriodo = dashboardMensalVO.getTipoProcessoPorDia().getTotalPeriodo();
		if(totalPeriodo == 0){
			addMessageWarn("erroConsultaIntraday.error");
			return;
		}
		geraGraficoSituacaoPorDia();
		geraGraficoSituacaoPorMes();
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
		Calendar datas = new GregorianCalendar();
		BaseInterna baseInterna;
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();

		baseInterna = baseInternaService.get(BaseInterna.REGIONAL_ID);
		filtro.setBaseInterna(baseInterna);
		regionais = baseRegistroService.findByFiltro(filtro, null, null);

		tiposProcesso = tipoProcessoService.findAtivos(null);

		setMeses( new LinkedHashMap<Integer, String>());
		meses.put(1, "Janeiro");
		meses.put(2, "Fevereiro");
		meses.put(3, "Março");
		meses.put(4, "Abril");
		meses.put(5, "Maio");
		meses.put(6, "Junho");
		meses.put(7, "Julho");
		meses.put(8, "Agosto");
		meses.put(9, "Setembro");
		meses.put(10, "Outubro");
		meses.put(11, "Novembro");
		meses.put(12, "Dezembro");

		setAnos(new ArrayList<>());
		getAnos().add(String.valueOf(datas.get(Calendar.YEAR)));
		getAnos().add(String.valueOf(datas.get(Calendar.YEAR) -1));
		getAnos().add(String.valueOf(datas.get(Calendar.YEAR) -2));
		getAnos().add(String.valueOf(datas.get(Calendar.YEAR) -3));
	}

	public void findCampusList(){
		String regional = filtro.getRegional();
		campusList = dashboardMensalService.findRegistroValorVOCombos(regional, BaseInterna.CAMPUS_ID, BaseRegistro.COD_REGIONAL);
	}

	public void findCursos(){
		String campus = filtro.getCampus();
		cursos = dashboardMensalService.findRegistroValorVOCombos(campus, BaseInterna.CURSO_ID, BaseRegistro.COD_CAMPUS);
	}

	//GERA GRAFICOS
	public void geraGraficoSituacaoPorDia() {

		ListaProcessoVO tipoProcessoPorDia = dashboardMensalVO.getTipoProcessoPorDia();
		//ListaProcessoVO tipoProcessoPorDiaTratado = dashboardMensalVO.getTipoProcessoPorDiaTratado();
		List<Long> conferidoPorDiaQtd = dashboardMensalService.calculaTotal(tipoProcessoPorDia, filtro.getDiasMes());
		//List<Long> tratadosPorDiaQtd = dashboardMensalService.calculaTotal(tipoProcessoPorDiaTratado);

		//tipoProcessoPorDiaTratado.setTotalPorDia(tratadosPorDiaQtd);
		tipoProcessoPorDia.setTotalPorDia(conferidoPorDiaQtd);
		long conferidoTotalDia = tipoProcessoPorDia.getTotalPeriodo();

		double conferidoQtdMax = (double) Collections.max(conferidoPorDiaQtd);

		List<Double> valoresMax = Arrays.asList(conferidoQtdMax);
		double yMax = Collections.max(valoresMax);
		yMax = Math.ceil(yMax);

		TempoOperacao tempoOperacao = dashboardMensalVO.getTempoOperacao();
		List<Double> conferido = tempoOperacao.getConferido();

		double y2Max = Collections.max(conferido);
		y2Max = Math.ceil(y2Max);

		List<String> diasMes = new ArrayList<>();

		for(int dia=1; dia <= filtro.getDiasMes(); dia++){
			diasMes.add(String.valueOf(dia));
		}

		Ajax.oncomplete("drawConferidoPorDia(" + conferidoPorDiaQtd + ", " + yMax + "," + y2Max + ", " + conferidoTotalDia +"," + conferido + ", "+diasMes+ ", '" + meses.get(Integer.parseInt(filtro.getMes())) +"')");
	}

	public void geraGraficoSituacaoPorMes() {

		ListaProcessoVO tipoProcessoPorMes = dashboardMensalVO.getTipoProcessoPorMes();
		//ListaProcessoVO tipoProcessoPorDiaTratado = dashboardMensalVO.getTipoProcessoPorDiaTratado();
		List<Long> conferidoPorMesQtd = dashboardMensalService.calculaTotalMes(tipoProcessoPorMes);
		//List<Long> tratadosPorDiaQtd = dashboardMensalService.calculaTotal(tipoProcessoPorDiaTratado);

		//tipoProcessoPorDiaTratado.setTotalPorDia(tratadosPorDiaQtd);
		tipoProcessoPorMes.setTotalPorDia(conferidoPorMesQtd);
		long conferidoTotalMes = tipoProcessoPorMes.getTotalPeriodo();

		double conferidoQtdMax = (double) Collections.max(conferidoPorMesQtd);

		List<Double> valoresMax = Arrays.asList(conferidoQtdMax);
		double yMax = Collections.max(valoresMax);
		yMax = Math.ceil(yMax);

		TempoOperacao tempoOperacao = dashboardMensalVO.getTempoOperacaoMes();
		List<Double> conferido = tempoOperacao.getConferido();

		double y2Max = Collections.max(conferido);
		y2Max = Math.ceil(y2Max);

		List<String> meses = new ArrayList<>();
		Integer mes = Integer.parseInt(filtro.getMes())-2;
		for(int index=0; index < 4; index++){

			if(mes > 12){
				mes = mes - 12;
			}
			else if(mes <= 0){
				mes = 12 + mes;
			}


			meses.add(this.meses.get(mes));
			mes++;
		}

		String textoMeses = "[";
		for(String temp : meses){
			textoMeses = textoMeses + "'" + temp + "',";
		}
		textoMeses = textoMeses.substring(0, textoMeses.length() -1) + "]";

		Ajax.oncomplete("drawConferidoPorMes(" + conferidoPorMesQtd + ", " + yMax + "," + y2Max + ", " + conferido + ", "+textoMeses+ ", 'Consolidado')");
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
		if(dashboardMensalVO == null){
			return "0.0";
		}
		TempoOperacao tempoOperacao = dashboardMensalVO.getTempoOperacao();
		List<Double> conferido = tempoOperacao.getConferido();
		Double agingProcesso = conferido.get(12);
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(agingProcesso).replace(",", ".");
	}

	public DashboardMensalFiltro getFiltro() {
		return filtro;
	}

	public DashboardMensalVO getDashboardMensalVO() {
		return dashboardMensalVO;
	}

	public void setDashboardIntradayVO(DashboardMensalVO dashboardMensalVO) {
		this.dashboardMensalVO = dashboardMensalVO;
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

	public Map<Integer, String> getMeses() {
		return meses;
	}

	public void setMeses(Map<Integer, String> meses) {
		this.meses = meses;
	}

	public List<String> getAnos() {
		return anos;
	}

	public void setAnos(List<String> anos) {
		this.anos = anos;
	}

	public DashboardMensalExporter getExporter() {
		return exporter;
	}
}