package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Regra;
import net.wasys.getdoc.domain.service.RegraService;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;

public class RegraDataModel extends LazyDataModel<Regra> {

	private RegraService regraService;
	private List<Regra> list;
	private RegraFiltro filtro;

	@Override
	public List<Regra> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = regraService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		String ordem = "";
		ordem += " tps.tipoProcesso.nome, s.nome, r.decisaoFluxo desc, r.nome ";

		filtro.setOrdem(ordem);
		list = regraService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(RegraService textoPadraoService) {
		this.regraService = textoPadraoService;
	}

	public RegraFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RegraFiltro filtro) {
		this.filtro = filtro;
	}
}
