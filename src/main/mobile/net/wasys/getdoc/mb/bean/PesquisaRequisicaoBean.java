package net.wasys.getdoc.mb.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.mb.Toast;
import net.wasys.getdoc.mb.faces.model.PaginDataModel;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.ViewModel;

@ManagedBean
@ViewScoped
public class PesquisaRequisicaoBean extends MobileBean {

	@Autowired private ProcessoService processoService;
	
	private ProcessoFiltro filtro;
	private PaginDataModel<ProcessoVO> dataModel;
	
	public void init() {
		
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			filtro = Faces.getFlashAttribute(ProcessoFiltro.class.getName());
			if (filtro == null) {
				filtro = new ProcessoFiltro();
				filtro.setAutor(usuario);
			}
			Toast toast = Faces.getFlashAttribute(Toast.class.getName());
			if (toast != null) {
				showToast(toast);
			}
			dataModel = new PaginDataModel<ProcessoVO>(30) {
				@Override
				protected int count() {
					return processoService.countByFiltro(filtro);
				}
				@Override
				protected List<ProcessoVO> load(String sort, boolean asc, int first, int limit) {
					List<Processo> processos = processoService.findByFiltro(filtro, first, limit);
					if (CollectionUtils.isNotEmpty(processos)) {
						List<ProcessoVO> rows = new ArrayList<>();
						for (Processo processo : processos) {
							rows.add(new ProcessoVO(processo));
						}
						return rows;
					}
					return null;
				}
			};
			dataModel.load();
		}
	}
	
	public void reload() {
		dataModel.load();
	}
	
	public void onFiltrarClick() throws IOException {
		Faces.setFlashAttribute(ProcessoFiltro.class.getName(), filtro);
		Faces.redirect("mobile/pesquisa/filtro.xhtml");
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("menu-pesquisa.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.FILTER, Position.RIGHT));
		viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.REFRESH, Position.RIGHT));
		return viewModel.parse();
	}
	
	public PaginDataModel<ProcessoVO> getDataModel() {
		return dataModel;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoVO> getRows() {
		return (List<ProcessoVO>) dataModel.getWrappedData();
	}
}