package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.Calendario;
import net.wasys.getdoc.domain.service.CalendarioService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CalendarioDataModel extends LazyDataModel<Calendario> {

	private CalendarioService calendarioService;
	private List<Calendario> list;

	@Override
	public List<Calendario> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = calendarioService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = calendarioService.findAll(first, pageSize);
		return list;
	}

	public void setService(CalendarioService calendarioService) {
		this.calendarioService = calendarioService;
	}
}
