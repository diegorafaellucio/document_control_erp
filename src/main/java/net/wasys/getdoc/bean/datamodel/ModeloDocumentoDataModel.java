package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;

public class ModeloDocumentoDataModel extends LazyDataModel<ModeloDocumento> {

	private ModeloDocumentoService modeloDocumentoService;
	private List<ModeloDocumento> list;

	@Override
	public List<ModeloDocumento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = modeloDocumentoService.count();
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = modeloDocumentoService.findAll(first, pageSize);
		return list;
	}

	public void setService(ModeloDocumentoService modeloDocumentoService) {
		this.modeloDocumentoService = modeloDocumentoService;
	}
}
