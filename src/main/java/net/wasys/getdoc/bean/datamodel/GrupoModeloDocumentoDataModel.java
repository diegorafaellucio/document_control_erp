package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.GrupoModeloDocumento;
import net.wasys.getdoc.domain.service.GrupoModeloDocumentoService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class GrupoModeloDocumentoDataModel extends LazyDataModel<GrupoModeloDocumento> {

	private GrupoModeloDocumentoService grupoModeloDocumentoService;
	private List<GrupoModeloDocumento> list;

	@Override
	public List<GrupoModeloDocumento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = grupoModeloDocumentoService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = grupoModeloDocumentoService.findAll();
		return list;
	}

	public void setService(GrupoModeloDocumentoService grupoModeloDocumentoService) {
		this.grupoModeloDocumentoService = grupoModeloDocumentoService;
	}
}
