package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;

public class UsuarioDataModel extends LazyDataModel<Usuario> {

	private UsuarioService usuarioService;
	private List<Usuario> list;
	private UsuarioFiltro filtro;

	@Override
	public List<Usuario> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = usuarioService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		list = usuarioService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public void setService(UsuarioService logAlteracaoService) {
		this.usuarioService = logAlteracaoService;
	}

	public void setFiltro(UsuarioFiltro filtro) {
		this.filtro = filtro;
	}
}
