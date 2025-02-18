package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.CategoriaDocumento;
import net.wasys.getdoc.domain.service.CategoriaDocumentoService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CategoriaDocumentoDataModel extends LazyDataModel<CategoriaDocumento> {

	private CategoriaDocumentoService categoriaDocumentoService;
	private List<CategoriaDocumento> list;

	@Override
	public List<CategoriaDocumento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = categoriaDocumentoService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = categoriaDocumentoService.findAll(first, pageSize);
		return list;
	}

	public void setService(CategoriaDocumentoService categoriaDocumentoService) {
		this.categoriaDocumentoService = categoriaDocumentoService;
	}
}
