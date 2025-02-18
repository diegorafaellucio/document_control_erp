package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.service.RelatorioEficienciaAreasService;
import net.wasys.getdoc.domain.vo.RelatorioEficienciaAreasVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class RelatorioEficienciaAreasBean extends AbstractBean {

	@Autowired private RelatorioEficienciaAreasService relatorioEficienciaAreasService;
	private static final SimpleDateFormat MES_DF = new SimpleDateFormat("MM/yyyy");

	private Date dataInicio;
	private Date dataFim;
	private Map<String, List<RelatorioEficienciaAreasVO>> map;

	protected void initBean() {
		dataInicio = new Date();
		dataFim = new Date();

		map = new LinkedHashMap<String, List<RelatorioEficienciaAreasVO>>();
	}

	public void buscar() {

		Map<Date, List<RelatorioEficienciaAreasVO>> relatorioEficienciaAreas;
		relatorioEficienciaAreas = relatorioEficienciaAreasService.getRelatorioEficienciaAreas(null, dataInicio, dataFim);

		for (Date mes : relatorioEficienciaAreas.keySet()) {
			List<RelatorioEficienciaAreasVO> list = relatorioEficienciaAreas.get(mes);
			Collections.sort(list, new Comparator<RelatorioEficienciaAreasVO>() {
				@Override
				public int compare(RelatorioEficienciaAreasVO o1, RelatorioEficienciaAreasVO o2) {
					Area area1 = o1.getArea();
					Area area2 = o2.getArea();
					String areaDescricao1 = area1 != null ? area1.getDescricao() : null;
					String areaDescricao2 = area2 != null ? area2.getDescricao() : null;
					return DummyUtils.compareTo(areaDescricao1, areaDescricao2, false);
				}
			});
			String mesStr = mes != null ? MES_DF.format(mes) : "";
			map.put(mesStr, list);
		}
	}

	public void detalhes(String mesAno, Area area) {

		setFlashAttribute("dataInicio", dataInicio);
		setFlashAttribute("dataFim", dataFim);
		setFlashAttribute("mesAno", mesAno);
		setFlashAttribute("area", area);
		setFlashAttribute("tipo", RelatorioProcessoListagemBean.Tipo.RELATORIO_EFICIENCIA_AREA);
		redirect("/relatorio/listagem/");
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Map<String, List<RelatorioEficienciaAreasVO>> getMap() {
		return map;
	}
}
