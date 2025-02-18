package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.Bacalhau;
import net.wasys.getdoc.domain.service.BacalhauService;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class BacalhauDataModel extends LazyDataModel<Bacalhau> {

	private BacalhauService bacalhauService;
	private List<Bacalhau> list;
	private BacalhauFiltro filtro;

	@Override
	public List<Bacalhau> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = bacalhauService.countByFiltro(filtro);
		setRowCount(count);

		if (count == 0) {
			return null;
		}

		list = bacalhauService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(BacalhauService bacalhauService) {
		this.bacalhauService = bacalhauService;
	}

	public void setFiltro(BacalhauFiltro filtro) {
		this.filtro = filtro;
	}
}
