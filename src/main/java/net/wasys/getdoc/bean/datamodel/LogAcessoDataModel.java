package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;

public class LogAcessoDataModel extends LazyDataModel<LogAcesso> {

	private LogAcessoService logAcessoService;
	private List<LogAcesso> list;
	private LogAcessoFiltro filtro;

	@Override
	public List<LogAcesso> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logAcessoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}


		list = logAcessoService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(LogAcessoService logAlteracaoService) {
		this.logAcessoService = logAlteracaoService;
	}

	public void setFiltro(LogAcessoFiltro filtro) {
		this.filtro = filtro;
	}
}
