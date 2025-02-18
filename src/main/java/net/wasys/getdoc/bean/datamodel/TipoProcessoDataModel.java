package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;

public class TipoProcessoDataModel extends LazyDataModel<TipoProcessoVO> {

	private TipoProcessoService tipoProcessoService;
	private List<TipoProcessoVO> list;

	@Override
	public List<TipoProcessoVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = tipoProcessoService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = tipoProcessoService.findAll(first, pageSize, null, null);
		return list;
	}

	public void setService(TipoProcessoService logAlteracaoService) {
		this.tipoProcessoService = logAlteracaoService;
	}
}
