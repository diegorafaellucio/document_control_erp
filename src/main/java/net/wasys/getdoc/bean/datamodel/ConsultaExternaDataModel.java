package net.wasys.getdoc.bean.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaFiltro;

public class ConsultaExternaDataModel extends LazyDataModel<ConsultaExterna> {

	private ConsultaExternaService consultaExternaService;
	private List<ConsultaExterna> list;
	private ConsultaExternaFiltro filtro;

	@Override
	public List<ConsultaExterna> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		if(filtro.isValido()) {

			Integer count = consultaExternaService.countByFiltro(filtro);
			setRowCount(count);

			if (count == 0) {
				return null;
			}

			setList(consultaExternaService.findByFiltro(filtro, first, pageSize));
			return getList();
		}

		return new ArrayList<>();
	}

	public List<ConsultaExterna> getList() {
		return list;
	}

	public void setList(List<ConsultaExterna> list) {
		this.list = list;
	}

	public ConsultaExternaFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(ConsultaExternaFiltro filtro) {
		this.filtro = filtro;
	}

	public ConsultaExternaService getConsultaExternaService() {
		return consultaExternaService;
	}

	public void setConsultaExternaService(ConsultaExternaService consultaExternaService) {
		this.consultaExternaService = consultaExternaService;
	}
}
