package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.filtro.CamposFiltro;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.service.RelatorioGeralService;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;

public class RelatorioGeralDataModel extends LazyDataModel<RelatorioGeral> {

	private RelatorioGeralService relatorioGeralService;
	private List<RelatorioGeral> list;
	private RelatorioGeralFiltro filtro;
	private boolean buscar;
	private boolean campoFiltro;

	@Override
	public List<RelatorioGeral> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		setCampoFiltro(filtro.isCampoFiltro());
		List <CampoDinamicoVO> listCamposFiltro = filtro.getCamposFiltro();
		if(campoFiltro == true && !listCamposFiltro.isEmpty()){
			List<Processo> processosByCampoFiltro = relatorioGeralService.verificaCampoFiltroByProcesso(filtro);

			relatorioGeralService.comparaProcessosCriaCasoNaoExista(processosByCampoFiltro, filtro);
		}

		int count = relatorioGeralService.countByFiltro(filtro);
		setRowCount(count);

		if(count == 0) {
			list = null;
			return null;
		}

		list = relatorioGeralService.findByFiltro(filtro, first, pageSize);

		return list;
	}

	public List<RelatorioGeral> getList() {
		return list;
	}

	public void setService(RelatorioGeralService relatorioGeralService) {
		this.relatorioGeralService = relatorioGeralService;
	}

	public void setFiltro(RelatorioGeralFiltro filtro) {
		this.filtro = filtro;
	}

	public void setBuscar(boolean buscar) {
		this.buscar = buscar;
	}

	public void setCampoFiltro(Boolean campoFiltro) {
		this.campoFiltro = campoFiltro;
	}
}
