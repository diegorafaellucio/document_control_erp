package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.BacalhauImagemPerdida;
import net.wasys.getdoc.domain.service.BacalhauImagemPerdidaService;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class BacalhauImagemPerdidaDataModel extends LazyDataModel<BacalhauImagemPerdida> {

	private BacalhauImagemPerdidaService bacalhauImagemPerdidaService;
	private List<BacalhauImagemPerdida> list;
	private BacalhauFiltro filtro;

	@Override
	public List<BacalhauImagemPerdida> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		BacalhauFiltro filtro = new BacalhauFiltro();
		filtro = this.filtro;
		filtro.setDataInicio(null);
		filtro.setDataFim(null);

		int count = bacalhauImagemPerdidaService.countByFiltro(this.filtro);
		setRowCount(count);

		if (count == 0) {
			return null;
		}

		list = bacalhauImagemPerdidaService.findByFiltro(this.filtro, first, pageSize);

		return list;
	}

	public void setService(BacalhauImagemPerdidaService bacalhauImagemPerdidaService) {
		this.bacalhauImagemPerdidaService = bacalhauImagemPerdidaService;
	}

	public void setFiltro(BacalhauFiltro filtro) {
		this.filtro = filtro;
	}
}
