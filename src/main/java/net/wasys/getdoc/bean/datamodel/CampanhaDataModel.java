package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.Campanha;
import net.wasys.getdoc.domain.service.CampanhaService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CampanhaDataModel extends LazyDataModel<Campanha> {

	private CampanhaService campanhaService;
	private List<Campanha> list;
	private Long tipoProcessoId;


	@Override
	public List<Campanha> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = campanhaService.findByTipoProcesso(tipoProcessoId, first, pageSize);
		return list;
	}

	@Override
	public int getRowCount() {

		int count = campanhaService.countByTipoProcesso(tipoProcessoId);
		return count;
	}

	public void setService(CampanhaService logAlteracaoService) {
		this.campanhaService = logAlteracaoService;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}
}
