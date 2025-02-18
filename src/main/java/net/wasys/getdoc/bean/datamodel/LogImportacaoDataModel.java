package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.LogImportacao;
import net.wasys.getdoc.domain.service.LogImportacaoService;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;

public class LogImportacaoDataModel extends LazyDataModel<LogImportacao> {

	private LogImportacaoService logImportacaoService;
	private LogImportacaoFiltro filtro;

	@Override
	public List<LogImportacao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logImportacaoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}


		return logImportacaoService.findByFiltro(filtro, first, pageSize);
	}

	public void setService(LogImportacaoService logImportacaoService) {
		this.logImportacaoService = logImportacaoService;
	}

	public void setFiltro(LogImportacaoFiltro filtro) {
		this.filtro = filtro;
	}
}
