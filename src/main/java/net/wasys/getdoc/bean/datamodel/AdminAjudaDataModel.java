package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.AdminAjuda;
import net.wasys.getdoc.domain.service.AdminAjudaService;
import net.wasys.getdoc.domain.vo.filtro.AdminAjudaFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAjudaDataModel extends LazyDataModel<AdminAjuda> {

	private AdminAjudaService adminAjudaService;
	private AdminAjudaFiltro filtro;
	private Map<Long, AdminAjuda> map = new HashMap<>();

	@Override
	public List<AdminAjuda> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = adminAjudaService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		List<AdminAjuda> list = adminAjudaService.findByFiltro(filtro, first, pageSize);

		for (AdminAjuda adminAjuda : list) {
			Long adminAjudaId = adminAjuda.getId();
			this.map.put(adminAjudaId, adminAjuda);
		}

		return list;
	}

	public void setService(AdminAjudaService logAlteracaoService) {
		this.adminAjudaService = logAlteracaoService;
	}

	public void setFiltro(AdminAjudaFiltro filtro) {
		this.filtro = filtro;
	}

	@Override
	public AdminAjuda getRowData(String rowKey) {
		AdminAjuda adminAjuda = map.get(rowKey);
		return adminAjuda;
	}

	public Collection<Long> getAjudasIds() {
		return map.keySet();
	}
}
