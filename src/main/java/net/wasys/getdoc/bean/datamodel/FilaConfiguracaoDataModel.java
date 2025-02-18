package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.domain.service.FilaConfiguracaoService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class FilaConfiguracaoDataModel extends LazyDataModel<FilaConfiguracao> {

	private FilaConfiguracaoService service;
	private List<FilaConfiguracao> list;

	@Override
	public List<FilaConfiguracao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = service.findAll(first, pageSize);
		return list;
	}

	@Override
	public int getRowCount() {

		int count = service.count();
		return count;
	}

	public void setService(FilaConfiguracaoService service) {
		this.service = service;
	}
}
