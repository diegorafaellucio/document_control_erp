package net.wasys.getdoc.bean.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.service.ProcessoRegraService;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;

public class RelatorioExecucaoRegrasDataModel extends LazyDataModel<ProcessoRegra> {

	private ProcessoRegraService processoRegraService;
	private List<ProcessoRegra> list;
	private ProcessoRegraFiltro filtro;

	@Override
	public List<ProcessoRegra> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		boolean ativo = filtro.isAtivo();
		if(!ativo) {
			return new ArrayList<>();
		}

		int count = processoRegraService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = processoRegraService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(ProcessoRegraService logAlteracaoService) {
		this.processoRegraService = logAlteracaoService;
	}

	public void setFiltro(ProcessoRegraFiltro filtro) {
		this.filtro = filtro;
	}
}
