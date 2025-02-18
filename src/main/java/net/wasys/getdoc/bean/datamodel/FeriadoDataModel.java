package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Feriado;
import net.wasys.getdoc.domain.service.FeriadoService;

public class FeriadoDataModel extends LazyDataModel<Feriado> {

	private FeriadoService feriadoService;
	private List<Feriado> list;
	private Boolean paralizacao;

	@Override
	public List<Feriado> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = feriadoService.count(paralizacao);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = feriadoService.findAll(paralizacao, first, pageSize);
		return list;
	}

	public void setService(FeriadoService logAlteracaoService) {
		this.feriadoService = logAlteracaoService;
	}

	public void setParalizacao(Boolean paralizacao) {
		this.paralizacao = paralizacao;
	}
}
