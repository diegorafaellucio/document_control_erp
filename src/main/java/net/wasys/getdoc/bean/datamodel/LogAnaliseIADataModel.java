package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.LogAnaliseIA;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.service.LogAnaliseIAService;
import net.wasys.getdoc.domain.service.RelatorioGeralService;
import net.wasys.getdoc.domain.vo.filtro.LogAnaliseIAFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class LogAnaliseIADataModel extends LazyDataModel<LogAnaliseIA> {

	private LogAnaliseIAService logAnaliseIAService;
	private List<LogAnaliseIA> list;
	private LogAnaliseIAFiltro filtro;

	@Override
	public List<LogAnaliseIA> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logAnaliseIAService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			list = null;
			return null;
		}

		list = logAnaliseIAService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(LogAnaliseIAService logAnaliseIAService) {
		this.logAnaliseIAService = logAnaliseIAService;
	}

	public void setFiltro(LogAnaliseIAFiltro filtro) {
		this.filtro = filtro;
	}


}
