package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;

public class BaseRegistroDataModel extends LazyDataModel<RegistroValorVO> {

	private BaseRegistroService baseRegistroService;
	private BaseRegistroFiltro filtro;

	@Override
	public List<RegistroValorVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {
		int count = baseRegistroService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		return baseRegistroService.findByFiltro(filtro, first, pageSize);
	}

	public void setService(BaseRegistroService baseRegistroService) {
		this.baseRegistroService = baseRegistroService;
	}

	public void setFiltro(BaseRegistroFiltro filtro) {
		this.filtro = filtro;
	}
}
