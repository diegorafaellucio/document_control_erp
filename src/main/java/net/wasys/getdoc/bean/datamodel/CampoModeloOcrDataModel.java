package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.getdoc.domain.service.CampoModeloOcrService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CampoModeloOcrDataModel extends LazyDataModel<CampoModeloOcr> {

	private CampoModeloOcrService campoModeloOcrService;
	private List<CampoModeloOcr> list;
	private Long modeloOcrId;


	@Override
	public List<CampoModeloOcr> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = campoModeloOcrService.findByModeloOcr(modeloOcrId);
		return list;
	}

	@Override
	public int getRowCount() {

		int count = campoModeloOcrService.countByModeloOcr(modeloOcrId);
		return count;
	}

	public void setService(CampoModeloOcrService campoModeloOcrService) {
		this.campoModeloOcrService = campoModeloOcrService;
	}

	public void setModeloOcrId(Long modeloOcrId) {
		this.modeloOcrId = modeloOcrId;
	}
}
