package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.RelatorioGeralEtapa;
import net.wasys.getdoc.domain.service.RelatorioGeralEtapaService;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class RelatorioGeralEtapaDataModel extends LazyDataModel<RelatorioGeralEtapa> {

	private RelatorioGeralEtapaService relatorioGeralEtapaService;
	private List<RelatorioGeralEtapa> list;
	private RelatorioGeralFiltro filtro;

	@Override
	public List<RelatorioGeralEtapa> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = relatorioGeralEtapaService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			list = null;
			return null;
		}

		list = relatorioGeralEtapaService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public List<RelatorioGeralEtapa> getList() {
		return list;
	}

	public void setService(RelatorioGeralEtapaService relatorioGeralEtapaService) {
		this.relatorioGeralEtapaService = relatorioGeralEtapaService;
	}

	public void setFiltro(RelatorioGeralFiltro filtro) {
		this.filtro = filtro;
	}
}
