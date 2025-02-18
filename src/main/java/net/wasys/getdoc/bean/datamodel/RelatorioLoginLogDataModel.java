package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.LoginLog;
import net.wasys.getdoc.domain.service.LoginLogService;
import net.wasys.getdoc.domain.vo.filtro.LoginLogFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class RelatorioLoginLogDataModel extends LazyDataModel<LoginLog> {

	private LoginLogService loginLogService;
	private List<LoginLog> list;
	private LoginLogFiltro filtro;

	@Override
	public List<LoginLog> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = loginLogService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = loginLogService.findByFiltro(filtro, first, pageSize);

		return list;
	}
	
	public void setService(LoginLogService loginLogService) {
		this.loginLogService = loginLogService;
	}

	public void setFiltro(LoginLogFiltro filtro) {
		this.filtro = filtro;
	}
}
