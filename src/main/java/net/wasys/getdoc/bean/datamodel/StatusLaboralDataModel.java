package net.wasys.getdoc.bean.datamodel;
import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.service.StatusLaboralService;
import net.wasys.getdoc.domain.vo.filtro.StatusLaboralFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class StatusLaboralDataModel extends LazyDataModel<StatusLaboral> {

	private StatusLaboralService statusLaboralService;
	private List<StatusLaboral> list;
	private StatusLaboralFiltro filtro;

	@Override
	public List<StatusLaboral> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = statusLaboralService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = statusLaboralService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	@Override
	public StatusLaboral getRowData(String rowKey) {
		for(StatusLaboral con : list) {
			if(con.getId().toString().equals(rowKey)) {
				return con;
			}
		}
		return null;
	}

	public void setService(StatusLaboralService statusLaboralService) {
		this.statusLaboralService = statusLaboralService;
	}

	public StatusLaboralFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(StatusLaboralFiltro filtro) {
		this.filtro = filtro;
	}
}
