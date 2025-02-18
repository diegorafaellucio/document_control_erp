package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.enumeration.DashboardCampos;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.DashboardService;
import net.wasys.getdoc.domain.vo.DashboardCountVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.math.BigInteger;
import java.util.*;

@ManagedBean
@SessionScoped
public class DashboardBean extends AbstractBean {

	@Autowired private DashboardService dashboardService;
	@Autowired private BaseRegistroService baseRegistroService;

	private DashboardFiltro filtro = new DashboardFiltro();
	private List<RegistroValorVO> regionais;
	private List<RegistroValorVO> campus;
	private String jsonObjetos = "{}";
	private List<DashboardCampos.IntervaloEnum> intervals = Arrays.asList(DashboardCampos.IntervaloEnum.DIA, DashboardCampos.IntervaloEnum.SEMANA, DashboardCampos.IntervaloEnum.MES, DashboardCampos.IntervaloEnum.ANO);
	private List<DashboardCampos.SituacaoEnum> situacoes = Arrays.asList(DashboardCampos.SituacaoEnum.INSCRITO, DashboardCampos.SituacaoEnum.CONFERIDO, DashboardCampos.SituacaoEnum.DIGITALIZADO);
	private List<DashboardCampos.TipoAgrupamentoEnum> tiposAgrupamento = Arrays.asList(DashboardCampos.TipoAgrupamentoEnum.ACUMULADO, DashboardCampos.TipoAgrupamentoEnum.PERIODO);

	@Override
	protected void initBean() {

		BaseRegistroFiltro filtro2 = new BaseRegistroFiltro();
		filtro2.setBaseInterna(new BaseInterna(BaseInterna.REGIONAL_ID));
		this.regionais = baseRegistroService.findByFiltro(filtro2, null, null);

		filtro2 = new BaseRegistroFiltro();
		filtro2.setBaseInterna(new BaseInterna(BaseInterna.CAMPUS_ID));
		this.campus = baseRegistroService.findByFiltro(filtro2, null, null);

		Date dataInicio = DateUtils.addDays(new Date(), -3);
		Date dataFim = new Date();
		filtro.setDataInicio(dataInicio);
		filtro.setDataFim(dataFim);

		filtro.setSituacao(DashboardCampos.SituacaoEnum.CONFERIDO);
		filtro.setSituacaoCompara(DashboardCampos.SituacaoEnum.DIGITALIZADO);
		filtro.setInterval(DashboardCampos.IntervaloEnum.DIA);
		filtro.setTipoAgrupamento(DashboardCampos.TipoAgrupamentoEnum.ACUMULADO);
 	}

	public void countBySituacaoFormaIngresso() {
		this.jsonObjetos = mapToJson(filtro);
	}

	private String mapToJson(DashboardFiltro filtro) {

		List<DashboardCountVO> situacaoFormaIngresso = new ArrayList<>();

		dashboardService.countBySituacaoFormaIngressoCompara(filtro, situacaoFormaIngresso);
		Map<String, Map<Date, Map<String, DashboardCountVO>>> map = mapVo(situacaoFormaIngresso);
		List<Map<String, Object>> series = mapObject(map);
		String json = DummyUtils.objectToJson(series);

		return json;
	}

	private List<Map<String, Object>> mapObject(Map<String, Map<Date, Map<String, DashboardCountVO>>> map) {
		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		DashboardCampos.IntervaloEnum interval = filtro.getInterval();
		List<Date> periodos = new ArrayList<>();
		while (dataInicio.before(dataFim)) {
			Date dia = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
			periodos.add(dia);
			dataInicio = DateUtils.addDays(dataInicio, 1);
		}

		List<Map<String, Object>> series = new ArrayList<>();
		List<String> formasIngresso = new ArrayList<>();
		List<String> colors = Arrays.asList("#C42828", "#9B3794", "#7cb5ec", "#f7a35c", "#90ee7e", "#7798BF", "#aaeeee", "#ff0066",
				"#eeaaee", "#55BF3B", "#DF5353", "#7798BF", "#aaeeee", "#058DC7", "#50B432",
				"#ED561B", "#DDDF00", "#24CBE5", "#64E572", "#FF9655","#1E90FF", "#FF4500");
		Map<String, String> mapColors = new TreeMap<>();
		for (String key : map.keySet()) {
			String[] split = key.split("\\|");
			Map<Date, Map<String, DashboardCountVO>> map2 = map.get(key);
			Map<String, Object> serie = new LinkedHashMap<>();
			String formaIngresso = split[0];
			String tipo = split[1];
			tipo = Character.toString(tipo.charAt(0));
			if (!(findStringList(formasIngresso, formaIngresso))) {
				formasIngresso.add(formaIngresso);
				mapColors.put(formaIngresso, colors.get(formasIngresso.size()-1));
			}

			serie.put("linkedTo", formaIngresso);
			serie.put("name", formaIngresso);
			serie.put("stack", tipo);
			serie.put("color", mapColors.get(formaIngresso));
			List<List<Object>> dataArray = new ArrayList<>();
			for (Date data : periodos) {
				Map<String, DashboardCountVO> map3 = map2.get(data);
				DashboardCountVO voBacklog1 = map3 != null ? map3.get("Inscrito (I)") != null ? map3.get("Inscrito (I)") : map3.get("Digitalizado (D)") != null ? map3.get("Digitalizado (D)") : map3.get("Conferido (C)") : null;
				voBacklog1 = voBacklog1 != null ? voBacklog1 : new DashboardCountVO();
				boolean addData = true;
				List<Object> dataArray2 = new ArrayList<>();
				dataArray2.add(data.getTime());
				BigInteger count1 = voBacklog1.getProcessos();
				if(count1 == null){
					addData = false;
				}
				count1 = count1 != null ? count1 : new BigInteger("0");
				dataArray2.add(count1);
				if(addData) {
					dataArray.add(dataArray2);
				}
				DashboardCountVO voDigitalizado2 = map3 != null ? map3.get("Inscrito (I)") != null && !((voBacklog1.getTipo()).equals("Inscrito (I)")) ? map3.get("Inscrito (I)") : map3.get("Digitalizado (D)") != null && !((voBacklog1.getTipo()).equals("Digitalizado (D)")) ? map3.get("Digitalizado (D)") : map3.get("Conferido (C)") != null && !((voBacklog1.getTipo()).equals("Conferido (C)")) ? map3.get("Conferido (C)") : null : null;
				voDigitalizado2 = voDigitalizado2 != null ? voDigitalizado2 : new DashboardCountVO();
				dataArray2 = new ArrayList<>();
				dataArray2.add(data.getTime());
				BigInteger count2 = voDigitalizado2.getProcessos();
				if(count2 == null) {
					addData = false;
				}
				count2 = count2 != null ? count2 : new BigInteger("0");
				dataArray2.add(count2);
				if(addData == true) {
					dataArray.add(dataArray2);
				}
			}
			serie.put("data", dataArray);
			series.add(serie);
		}

		for (String formaIngresso : mapColors.keySet()) {
			Map<String, Object> serieB = new LinkedHashMap<>();
			Map<String, Object> serieC = new LinkedHashMap<>();
			Map<String, Object> serieD = new LinkedHashMap<>();

			serieD.put("stack", "D");
			serieD.put("name", formaIngresso);
			serieD.put("color", mapColors.get(formaIngresso));
			serieD.put("linkedTo", formaIngresso);
			series.add(0, serieD);

			serieC.put("stack", "C");
			serieC.put("name", formaIngresso);
			serieC.put("color", mapColors.get(formaIngresso));
			serieC.put("linkedTo", formaIngresso);
			series.add(0, serieC);

			serieB.put("stack", "B");
			serieB.put("name", formaIngresso);
			serieB.put("color", mapColors.get(formaIngresso));
			serieB.put("id", formaIngresso);
			series.add(0, serieB);
		}

		return series;
	}

	private Map<String, Map<Date, Map<String, DashboardCountVO>>> mapVo(List<DashboardCountVO> voList) {
		Map<String, Map<Date, Map<String, DashboardCountVO>>> map = new LinkedHashMap<>();
		for (DashboardCountVO vo : voList) {
			String formaIngresso = vo.getFormaIngresso();
			String tipo = vo.getTipo();
			Date periodo = vo.getPeriodo();
			String key = formaIngresso + "|" + tipo;
			Map<Date, Map<String, DashboardCountVO>> map2 = map.get(key);
			map2 = map2 != null ? map2 : new LinkedHashMap<>();
			map.put(key, map2);
			Map<String, DashboardCountVO> map3 = map2.get(periodo);
			map3 = map3 != null ? map3 : new LinkedHashMap<>();
			map2.put(periodo, map3);
			map3.put(tipo, vo);
		}

		return map;
	}

	public DashboardFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(DashboardFiltro filtro) {
		this.filtro = filtro;
	}

	public List<RegistroValorVO> getRegionais() {
		return regionais;
	}

	public List<RegistroValorVO> getCampus() {
		return campus;
	}

	public void setCampus() {
		RegistroValorVO regional = this.filtro.getRegional();
		this.campus = baseRegistroService.findByRelacionamento(BaseInterna.CAMPUS_ID, regional, BaseRegistro.COD_REGIONAL);
	}

	public String getJsonObjetos() {
		return jsonObjetos;
	}

	public List<DashboardCampos.SituacaoEnum> getSituacoes() {
		return situacoes;
	}

	public List<DashboardCampos.IntervaloEnum> getIntervals() {
		return intervals;
	}

	public List<DashboardCampos.TipoAgrupamentoEnum> getTiposAgrupamento() {
		return tiposAgrupamento;
	}

	private boolean findStringList(List<String> list, String find) {
		find = find.trim();
		for (String string : list) {
			string = string.trim();
			if (string.equals(find)) {
				return true;
			}
		}
		return false;
	}
}