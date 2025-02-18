package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.enumeration.Resposta;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.service.TipoDocumentoService;

public class TipoDocumentoDataModel extends LazyDataModel<TipoDocumento> {

	private TipoDocumentoService tipoDocumentoService;
	private List<TipoDocumento> list;
	private TipoDocumentoFiltro filtro;

	@Override
	public List<TipoDocumento> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = tipoDocumentoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = tipoDocumentoService.findByFiltro(filtro, first, pageSize);
		return list;
	}

	public void setService(TipoDocumentoService logAlteracaoService) {
		this.tipoDocumentoService = logAlteracaoService;
	}

	public void setFiltro(TipoDocumentoFiltro filtro) {
		this.filtro = filtro;
	}

	public void buscar(){
		list = tipoDocumentoService.findByFiltro(filtro,null,null);
	}

	public void limpar(){
		filtro.setId(null);
		filtro.setCodOrigem(null);
		filtro.setNomeLike(null);
		filtro.setObrigatorio(Resposta.TODOS);
		filtro.setAtivo(Resposta.TODOS);
		buscar();
	}
}
