package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Ajuda;
import net.wasys.getdoc.domain.service.AjudaService;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;

public class AjudaDataModel extends LazyDataModel<Ajuda> {

	private AjudaService service;
	private List<Ajuda> list;
	private AjudaFiltro filtro;

	@Override
	public List<Ajuda> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = getService().countByFiltro(getFiltro());
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = getService().findByFiltro(getFiltro(), first, pageSize);

		return list;
	}

	public AjudaService getService() {
		return service;
	}

	public void setService(AjudaService service) {
		this.service = service;
	}

	public AjudaFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(AjudaFiltro filtro) {
		this.filtro = filtro;
	}

}
