package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ConsultaExternaLog;
import net.wasys.getdoc.domain.service.ConsultaExternaLogService;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaLogFiltro;

public class ConsultaExternaLogDataModel extends LazyDataModel<ConsultaExternaLog> {

	private ConsultaExternaLogService consultaExternaLogService;
	private List<ConsultaExternaLog> list;
	private ConsultaExternaLogFiltro filtro;

	@Override
	public List<ConsultaExternaLog> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		setRowCount(consultaExternaLogService.countByFiltro(filtro));

		if (getRowCount() == 0) {
			return null;
		}

		setList(consultaExternaLogService.findByFiltro(getFiltro(), first, pageSize));

		return getList();
	}

	public List<ConsultaExternaLog> getList() {
		return list;
	}

	public void setList(List<ConsultaExternaLog> list) {
		this.list = list;
	}

	public ConsultaExternaLogFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(ConsultaExternaLogFiltro filtro) {
		this.filtro = filtro;
	}

	public ConsultaExternaLogService getConsultaExternaService() {
		return consultaExternaLogService;
	}

	public void setConsultaExternaLogService(ConsultaExternaLogService consultaExternaLogService) {
		this.consultaExternaLogService = consultaExternaLogService;
	}
}
