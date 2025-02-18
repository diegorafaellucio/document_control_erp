package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.service.ConfiguracaoGeracaoRelatorioService;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoGeracaoRelatorioFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ConfiguracaoGeracaoRelatorioDataModel extends LazyDataModel<ConfiguracaoGeracaoRelatorio> {

	private ConfiguracaoGeracaoRelatorioService configuracaoGeracaoRelatorioService;
	private List<ConfiguracaoGeracaoRelatorio> list;
	private ConfiguracaoGeracaoRelatorioFiltro filtro;

	@Override
	public List<ConfiguracaoGeracaoRelatorio> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = configuracaoGeracaoRelatorioService.countByFiltro(filtro);
		setRowCount(count);

		if (count == 0) {
			return null;
		}

		list = configuracaoGeracaoRelatorioService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(ConfiguracaoGeracaoRelatorioService textoPadraoService) {
		this.configuracaoGeracaoRelatorioService = textoPadraoService;
	}

	public void setFiltro(ConfiguracaoGeracaoRelatorioFiltro filtro) {
		this.filtro = filtro;
	}
}
