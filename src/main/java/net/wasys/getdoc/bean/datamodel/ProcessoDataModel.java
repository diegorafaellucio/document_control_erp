package net.wasys.getdoc.bean.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.vo.ProcessoVO;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;

public class ProcessoDataModel extends LazyDataModel<ProcessoVO> {

	private ProcessoService processoService;
	private ProcessoFiltro filtro;
	private boolean buscar;
	private Map<String, Processo> map = new HashMap<>();

	@Override
	public List<ProcessoVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		if(!buscar) {
			setRowCount(0);
			return null;
		}

		int count = processoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		List<ProcessoVO> list = processoService.montarProcessoVO(filtro, first, pageSize);

		for (ProcessoVO processo : list) {
			Processo processo1 = processo.getProcesso();
			Long processoId = processo1.getId();
			String processoIdStr = processoId.toString();
			this.map.put(processoIdStr, processo1);
		}

		return list;
	}

	public void setService(ProcessoService processoService) {
		this.processoService = processoService;
	}

	public void setFiltro(ProcessoFiltro filtro) {
		this.filtro = filtro;
	}

	public void setBuscar(boolean buscar) {
		this.buscar = buscar;
	}

	@Override
	public ProcessoVO getRowData(String rowKey) {
		Processo processo = map.get(rowKey);
		ProcessoVO processoVO = new ProcessoVO(processo);

		return processoVO;
	}

	@Override
	public String getRowKey(ProcessoVO obj) {
		return String.valueOf(obj.getProcesso().getId());
	}
}
