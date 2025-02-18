package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.getdoc.domain.service.EtapaService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class EtapaDataModel extends LazyDataModel<Etapa> {

	private EtapaService etapaService;
	private List<Etapa> list;
	private Long tipoProcessoId;

	@Override
	public List<Etapa> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = etapaService.countByTipoProcesso(tipoProcessoId);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = etapaService.findByTipoProcesso(tipoProcessoId);
		return list;
	}

	public void setService(EtapaService etapaService) {
		this.etapaService = etapaService;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}
}
