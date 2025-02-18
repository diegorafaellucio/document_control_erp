package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;

public class SituacaoDataModel extends LazyDataModel<Situacao> {

	private SituacaoService situacaoService;
	private List<Situacao> list;
	private SituacaoFiltro filtro;

	@Override
	public List<Situacao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = situacaoService.count(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = situacaoService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(SituacaoService situacaoService) {
		this.situacaoService = situacaoService;
	}

	public SituacaoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(SituacaoFiltro filtro) {
		this.filtro = filtro;
	}
}
