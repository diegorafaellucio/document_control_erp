package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.LogAlteracao;
import net.wasys.getdoc.domain.service.LogAlteracaoService;
import net.wasys.getdoc.domain.vo.filtro.LogAlteracaoFiltro;

public class LogAlteracaoDataModel extends LazyDataModel<LogAlteracao> {

	private LogAlteracaoService logAlteracaoService;
	private List<LogAlteracao> list;
	private LogAlteracaoFiltro filtro;

	@Override
	public List<LogAlteracao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logAlteracaoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}


		list = logAlteracaoService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(LogAlteracaoService logAlteracaoService) {
		this.logAlteracaoService = logAlteracaoService;
	}

	public void setFiltro(LogAlteracaoFiltro filtro) {
		this.filtro = filtro;
	}
}
