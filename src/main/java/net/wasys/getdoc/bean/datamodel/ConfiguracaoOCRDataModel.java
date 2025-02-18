package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.service.ConfiguracaoOCRService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ConfiguracaoOCRDataModel extends LazyDataModel<ConfiguracaoOCR> {

	private ConfiguracaoOCRService configuracaoOCRService;
	private List<ConfiguracaoOCR> list;

	@Override
	public List<ConfiguracaoOCR> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = configuracaoOCRService.findAll();
		return list;
	}

	@Override
	public int getRowCount() {

		int count = configuracaoOCRService.countAll();
		return count;
	}

	public void setService(ConfiguracaoOCRService configuracaoOCRService) {
		this.configuracaoOCRService = configuracaoOCRService;
	}

}
