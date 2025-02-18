package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.ConfiguracaoLoginAzure;
import net.wasys.getdoc.domain.service.ConfiguracaoLoginAzureService;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoLoginAzureFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ConfiguracaoLoginAzureDataModel extends LazyDataModel<ConfiguracaoLoginAzure> {

	private ConfiguracaoLoginAzureService configuracaoLoginAzureService;
	private ConfiguracaoLoginAzureFiltro filtro;

	@Override
	public List<ConfiguracaoLoginAzure> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = configuracaoLoginAzureService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		return configuracaoLoginAzureService.findByFiltro(filtro, first, pageSize);
	}

	public void setService(ConfiguracaoLoginAzureService configuracaoLoginAzureService) {
		this.configuracaoLoginAzureService = configuracaoLoginAzureService;
	}

	public void setFiltro(ConfiguracaoLoginAzureFiltro filtro) {
		this.filtro = filtro;
	}
}
