package net.wasys.getdoc.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.AjudaDataModel;
import net.wasys.getdoc.domain.entity.Ajuda;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.AjudaService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class AjudaListBean extends AbstractBean {

	@Autowired private AjudaService ajudaService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private AjudaFiltro filtro = new AjudaFiltro();
	private Map<TipoProcesso, AjudaDataModel> dataModelMap = new LinkedHashMap<TipoProcesso, AjudaDataModel>();

	private Ajuda ajuda;
	private List<TipoProcesso> tiposProcessos;

	protected void initBean() {

		getFiltro().setSomenteNoInicial(true);

		buscar();
	}

	public void buscar() {

		dataModelMap.clear();
		tiposProcessos = tipoProcessoService.findAll(null, null);
		for (TipoProcesso tp : tiposProcessos) {
			Long tipoProcessoId = tp.getId();
			AjudaFiltro filtro1 = filtro.clone();
			filtro1.setTipoProcessoId(tipoProcessoId);
			AjudaDataModel dataModel = new AjudaDataModel();
			dataModel.setFiltro(filtro1);
			dataModel.setService(ajudaService);
			dataModelMap.put(tp, dataModel);
		}
	}

	public void excluir() {

		try {
			ajudaService.excluirRecursivo(ajuda.getId());

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public Ajuda getAjuda() {
		return ajuda;
	}

	public void setAjuda(Ajuda ajuda) {
		this.ajuda = ajuda;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public AjudaFiltro getFiltro() {
		return filtro;
	}

	public Map<TipoProcesso, AjudaDataModel> getDataModelMap() {
		return dataModelMap;
	}

	public void subirOrdem(Ajuda ajuda) {
		Usuario usuario = getUsuarioLogado();
		ajudaService.subirOrdem(ajuda, usuario);
	}

	public void descerOrdem(Ajuda ajuda) {
		Usuario usuario = getUsuarioLogado();
		ajudaService.descerOrdem(ajuda, usuario);
	}
}
