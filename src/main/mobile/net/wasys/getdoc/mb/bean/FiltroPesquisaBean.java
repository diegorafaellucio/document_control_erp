package net.wasys.getdoc.mb.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.CheckboxModel;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.ViewModel;

@ManagedBean
@ViewScoped
public class FiltroPesquisaBean extends MobileBean {

	@Autowired private UsuarioService usuarioService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	
	private ProcessoFiltro filtro;
	
	private List<Usuario> analistas;
	private List<Situacao> situacoes;
	private List<CheckboxModel<TipoProcesso>> checkboxTiposProcessos;
	private List<CheckboxModel<StatusProcesso>> checkboxStatusProcessos;
	
	public void init() {
		
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			
			filtro = Faces.getFlashAttribute(ProcessoFiltro.class.getName());
			
			UsuarioFiltro f = new UsuarioFiltro();
			f.setRole(RoleGD.GD_ANALISTA);
			f.setStatus(StatusUsuario.ATIVO);
			analistas = usuarioService.findByFiltro(f);
			
			RoleGD roleGD = usuario.getRoleGD();
			List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
			List<TipoProcesso> tiposProcessos = tipoProcessoService.findAtivos(permissoes);
			if (CollectionUtils.isNotEmpty(tiposProcessos)) {
				List<TipoProcesso> list = filtro.getTiposProcesso();
				checkboxTiposProcessos = new ArrayList<>();
				for (TipoProcesso tipoProcesso : tiposProcessos) {
					CheckboxModel<TipoProcesso> checkbox = new CheckboxModel<TipoProcesso>(tipoProcesso);
					checkbox.setChecked(list != null && list.contains(tipoProcesso));
					checkboxTiposProcessos.add(checkbox);
				}
			}
			
			StatusProcesso[] statusProcessos = StatusProcesso.values();
			checkboxStatusProcessos = new ArrayList<>();
			List<StatusProcesso> list = filtro.getStatusList();
			for (StatusProcesso statusProcesso : statusProcessos) {
				CheckboxModel<StatusProcesso> checkbox = new CheckboxModel<StatusProcesso>(statusProcesso);
				checkbox.setChecked(list != null && list.contains(statusProcesso));
				checkboxStatusProcessos.add(checkbox);
			}
			
			situacoes = situacaoService.findAtivas(null);
		}
	}
	
	public void onVoltarClick() throws IOException {
		Faces.setFlashAttribute(ProcessoFiltro.class.getName(), filtro);
		Faces.redirect("mobile/pesquisa/lista.xhtml");
	}
	
	public void onBuscarClick() throws IOException {
		List<TipoProcesso> tiposProcesso = null;
		for (CheckboxModel<TipoProcesso> checkbox : checkboxTiposProcessos) {
			if (checkbox.isChecked()) {
				if (tiposProcesso == null) {
					tiposProcesso = new ArrayList<>();
				}
				tiposProcesso.add(checkbox.getValue());
			}
		}
		filtro.setTiposProcesso(tiposProcesso);
		List<StatusProcesso> statusProcessos = null;
		for (CheckboxModel<StatusProcesso> checkbox : checkboxStatusProcessos) {
			if (checkbox.isChecked()) {
				if (statusProcessos == null) {
					statusProcessos = new ArrayList<>();
				}
				statusProcessos.add(checkbox.getValue());
			}
		}
		filtro.setStatusList(statusProcessos);
		Faces.setFlashAttribute(ProcessoFiltro.class.getName(), filtro);
		Faces.redirect("mobile/pesquisa/lista.xhtml");
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("filtroForm.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}
	
	public ProcessoFiltro getFiltro() {
		return filtro;
	}
	
	public List<Usuario> getAnalistas() {
		return analistas;
	}
	
	public List<Situacao> getSituacoes() {
		return situacoes;
	}
	
	public List<CheckboxModel<TipoProcesso>> getCheckboxTiposProcessos() {
		return checkboxTiposProcessos;
	}
	
	public List<CheckboxModel<StatusProcesso>> getCheckboxStatusProcessos() {
		return checkboxStatusProcessos;
	}
}
