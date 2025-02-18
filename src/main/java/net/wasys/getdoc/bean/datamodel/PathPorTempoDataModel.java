package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.vo.LogPorTempoVO;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class PathPorTempoDataModel extends LazyDataModel<LogPorTempoVO> {

	private LogAcessoService logAcessoService;
	private List<LogPorTempoVO> list;
	private LogAcessoFiltro filtro;

	@Override
	public List<LogPorTempoVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = logAcessoService.countPathPorTime(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = logAcessoService.findPathPorTempo(filtro, first, pageSize);


		return list;
	}

	public void setService(LogAcessoService logAlteracaoService) {
		this.logAcessoService = logAlteracaoService;
	}

	public void setFiltro(LogAcessoFiltro filtro) {
		this.filtro = filtro;
	}
}