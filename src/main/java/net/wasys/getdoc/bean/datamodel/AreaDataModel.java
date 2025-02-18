package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.service.AreaService;

public class AreaDataModel extends LazyDataModel<Area> {

	private AreaService areaService;
	private List<Area> list;

	@Override
	public List<Area> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = areaService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = areaService.findAll(first, pageSize);

		return list;
	}

	public void setService(AreaService areaService) {
		this.areaService = areaService;
	}
}
