package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.Aluno;
import net.wasys.getdoc.domain.service.AlunoService;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class AlunoDataModel extends LazyDataModel<Aluno> {

	private AlunoService alunoService;
	private List<Aluno> list;
	private AlunoFiltro filtro;

	@Override
	public List<Aluno> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = alunoService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		List<Aluno> list = alunoService.findByFiltro(filtro, first, pageSize);
		return list;
	}

	public void buscar(){
		list = alunoService.findByFiltro(filtro,null,null);
	}

	public void limpar(){
		filtro.setCpf(null);
		filtro.setNome(null);
		buscar();
	}
	public void setService(AlunoService alunoService) {
		this.alunoService= alunoService;
	}

	public AlunoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(AlunoFiltro filtro) {
		this.filtro = filtro;
	}
}
