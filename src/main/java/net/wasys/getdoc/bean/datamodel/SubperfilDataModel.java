package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.service.SubperfilService;

public class SubperfilDataModel extends LazyDataModel<Subperfil> {

	private SubperfilService service;
	private List<Subperfil> list;

	@Override
	public List<Subperfil> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = service.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = service.findAll(first, pageSize);

		return list;
	}

	public void setService(SubperfilService service) {
		this.service = service;
	}
}
