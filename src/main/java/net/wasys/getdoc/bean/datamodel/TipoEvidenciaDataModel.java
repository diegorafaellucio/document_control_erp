package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.getdoc.domain.service.TipoEvidenciaService;

public class TipoEvidenciaDataModel extends LazyDataModel<TipoEvidencia> {

	private TipoEvidenciaService tipoEvidenciaService;
	private List<TipoEvidencia> list;

	@Override
	public List<TipoEvidencia> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = tipoEvidenciaService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = tipoEvidenciaService.findAll(first, pageSize);

		return list;
	}

	public void setService(TipoEvidenciaService tipoEvidenciaService) {
		this.tipoEvidenciaService = tipoEvidenciaService;
	}
}
