package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.service.ExecucaoGeracaoRelatorioService;
import net.wasys.getdoc.domain.vo.ExecucaoGeracaoRelatorioVO;
import net.wasys.getdoc.domain.vo.filtro.ExecucaoGeracaoRelatorioFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ExecucaoGeracaoRelatorioVoDataModel extends LazyDataModel<ExecucaoGeracaoRelatorioVO> {

	private ExecucaoGeracaoRelatorioService configuracaoGeracaoRelatorioService;
	private List<ExecucaoGeracaoRelatorioVO> list;
	private ExecucaoGeracaoRelatorioFiltro filtro;

	@Override
	public List<ExecucaoGeracaoRelatorioVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = configuracaoGeracaoRelatorioService.countByFiltro(filtro);
		setRowCount(count);

		if (count == 0) {
			return null;
		}

		list = configuracaoGeracaoRelatorioService.findVOsByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(ExecucaoGeracaoRelatorioService configuracaoGeracaoRelatorioService) {
		this.configuracaoGeracaoRelatorioService = configuracaoGeracaoRelatorioService;
	}

	public void setFiltro(ExecucaoGeracaoRelatorioFiltro filtro) {
		this.filtro = filtro;
	}
}
