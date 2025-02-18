package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.service.TextoPadraoService;
import net.wasys.getdoc.domain.vo.filtro.TextoPadraoFiltro;

public class TextoPadraoDataModel extends LazyDataModel<TextoPadrao> {

	private TextoPadraoService textoPadraoService;
	private List<TextoPadrao> list;
	private TextoPadraoFiltro filtro;

	@Override
	public List<TextoPadrao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = textoPadraoService.count(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = textoPadraoService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(TextoPadraoService textoPadraoService) {
		this.textoPadraoService = textoPadraoService;
	}

	public TextoPadraoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(TextoPadraoFiltro filtro) {
		this.filtro = filtro;
	}
}
