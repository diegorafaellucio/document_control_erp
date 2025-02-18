package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.service.ModeloOcrService;

public class ModeloOcrDataModel extends LazyDataModel<ModeloOcr> {

	private ModeloOcrService modeloOcrService;
	private List<ModeloOcr> list;

	@Override
	public List<ModeloOcr> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = modeloOcrService.findAll(first, pageSize);
		return list;
	}

	@Override
	public int getRowCount() {

		int count = modeloOcrService.count();
		return count;
	}

	public void setService(ModeloOcrService logAlteracaoService) {
		this.modeloOcrService = logAlteracaoService;
	}
}
