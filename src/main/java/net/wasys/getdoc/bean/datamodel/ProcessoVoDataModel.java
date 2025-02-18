package net.wasys.getdoc.bean.datamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;

public class ProcessoVoDataModel extends LazyDataModel<ProcessoVO> {

	private ProcessoService processoService;
	private ProcessoFiltro filtro;
	private Map<Long, ProcessoVO> map = new HashMap<>();

	@Override
	public List<ProcessoVO> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = processoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}


		List<ProcessoVO> list = processoService.findVOsByFiltro(filtro, first, pageSize);

		for (ProcessoVO vo : list) {

			Processo processo = vo.getProcesso();
			Long processoId = processo.getId();
			this.map.put(processoId, vo);
		}

		return list;
	}

	public void setService(ProcessoService logAlteracaoService) {
		this.processoService = logAlteracaoService;
	}

	public void setFiltro(ProcessoFiltro filtro) {
		this.filtro = filtro;
	}

	@Override
	public ProcessoVO getRowData(String rowKey) {
		Long processoId = StringUtils.isNotBlank(rowKey) ? new Long(rowKey) : null;
		ProcessoVO vo = map.get(processoId);
		return vo;
	}

	@Override
	public String getRowKey(ProcessoVO obj) {
		return String.valueOf(obj.getProcesso().getId());
	}

	public Collection<Long> getProcessosIds() {
		return map.keySet();
	}
}
