package net.wasys.getdoc.mb.bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.enumeration.StatusProcesso;
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
public class FilaRequisicaoBean extends MobileBean {

	@Autowired private ProcessoService processoService;
	
	private ProcessoFiltro filtro;
	private PaginDataModel<ProcessoVO> dataModel;
	
	public void init() {
		
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			
			Toast toast = Faces.getFlashAttribute(Toast.class.getName());
			if (toast != null) {
				showToast(toast);
			}
			
			filtro = new ProcessoFiltro();
			
			filtro.setAutor(usuario);
			
			List<StatusProcesso> statusList = StatusProcesso.getPendenciaRequisitante();
			filtro.setStatusList(statusList);
			
			dataModel = new PaginDataModel<ProcessoVO>(30) {
				@Override
				protected int count() {
					return processoService.countByFiltro(filtro);
				}
				@Override
				protected List<ProcessoVO> load(String sort, boolean asc, int first, int limit) {
					return processoService.findVOsByFiltro(filtro, first, limit);
				}
			};
			dataModel.load();
		}
	}
	
	public void reload() {
		dataModel.load();
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("filaTrabalho.titulo");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.PLUS, Position.RIGHT));
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