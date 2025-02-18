package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.CategoriaGrupoModeloDocumento;
import net.wasys.getdoc.domain.service.CategoriaGrupoModeloDocumentoService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CategoriaGrupoModeloDocumentoDataModel extends LazyDataModel<CategoriaGrupoModeloDocumento> {

	private CategoriaGrupoModeloDocumentoService categoriaGrupoModeloDocumentoService;
	private List<CategoriaGrupoModeloDocumento> list;

	@Override
	public List<CategoriaGrupoModeloDocumento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = categoriaGrupoModeloDocumentoService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = categoriaGrupoModeloDocumentoService.findAll();
		return list;
	}

	public void setService(CategoriaGrupoModeloDocumentoService categoriaGrupoModeloDocumentoService) {
		this.categoriaGrupoModeloDocumentoService = categoriaGrupoModeloDocumentoService;
	}
}
