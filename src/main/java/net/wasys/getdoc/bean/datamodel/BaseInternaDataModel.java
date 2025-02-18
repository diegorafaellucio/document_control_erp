package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;

public class BaseInternaDataModel extends LazyDataModel<BaseInterna> {

	private BaseInternaService baseInternaService;
	private BaseInternaFiltro filtro;

	@Override
	public List<BaseInterna> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = baseInternaService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		return baseInternaService.findByFiltro(filtro, first, pageSize);
	}

	public void setService(BaseInternaService baseInternaService) {
		this.baseInternaService = baseInternaService;
	}

	public void setFiltro(BaseInternaFiltro filtro) {
		this.filtro = filtro;
	}
}
