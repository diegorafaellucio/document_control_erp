package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.domain.vo.LogPorTempoVO;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class WSPorTempoDataModel extends LazyDataModel<LogPorTempoVO> {

	private ConsultaExternaService consultaExternaService;
	private List<LogPorTempoVO> list;
	private LogAcessoFiltro filtro;

	@Override
	public List<LogPorTempoVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = consultaExternaService.countWSPorTempo(filtro);
		setRowCount(count);

		list = consultaExternaService.findWSPorTempo(filtro, first, pageSize);

		if(count == 0) {
			return null;
		}

		return list;
	}

	public void setService(ConsultaExternaService consultaExternaService) {
		this.consultaExternaService = consultaExternaService;
	}

	public void setFiltro(LogAcessoFiltro filtro) {
		this.filtro = filtro;
	}
}