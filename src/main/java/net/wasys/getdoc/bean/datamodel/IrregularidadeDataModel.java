package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.vo.filtro.IrregularidadeFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.service.IrregularidadeService;

public class IrregularidadeDataModel extends LazyDataModel<Irregularidade> {

	private IrregularidadeService irregularidadeService;
	private List<Irregularidade> list;
	private IrregularidadeFiltro filtro = new IrregularidadeFiltro();

	@Override
	public List<Irregularidade> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = irregularidadeService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		return irregularidadeService.findByFiltro(filtro,first, pageSize);
	}

	public void setService(IrregularidadeService logAlteracaoService) {
		this.irregularidadeService = logAlteracaoService;
	}


	public void setFiltro(IrregularidadeFiltro filtro) {
		this.filtro=filtro;
	}
}
