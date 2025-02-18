package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.LogAtendimento;
import net.wasys.getdoc.domain.service.LogAtendimentoService;
import net.wasys.getdoc.domain.vo.filtro.LogAtendimentoFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class RelatorioStatusLaboralDataModel extends LazyDataModel<LogAtendimento> {

	private LogAtendimentoService logAtendimentoService;
	private List<LogAtendimento> list;
	private LogAtendimentoFiltro filtro;

	@Override
	public List<LogAtendimento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logAtendimentoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = logAtendimentoService.findByFiltro(filtro, first, pageSize);

		return list;
	}
	
	public void setService(LogAtendimentoService logAtendimentoService) {
		this.logAtendimentoService = logAtendimentoService;
	}

	public void setFiltro(LogAtendimentoFiltro filtro) {
		this.filtro = filtro;
	}
}
