package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;

public class ProcessoLogDataModel extends LazyDataModel<ProcessoLog> {

	private ProcessoLogService processoLogService;
	private List<ProcessoLog> list;
	private ProcessoLogFiltro filtro;

	@Override
	public List<ProcessoLog> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = processoLogService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = processoLogService.findByFiltro(filtro, first, pageSize);

		return list;
	}
	
	public void setService(ProcessoLogService processoLogService) {
		this.processoLogService = processoLogService;
	}

	public void setFiltro(ProcessoLogFiltro filtro) {
		this.filtro = filtro;
	}
}
