package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.service.EmailRecebidoService;
import net.wasys.getdoc.domain.service.ProcessoLogAnexoService;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.ViewModel;

@ManagedBean
@ViewScoped
public class DetalheEmailBean extends MobileBean {

	@Autowired private EmailRecebidoService emailRecebidoService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	
	private Long id;
	private Tela origem;
	private EmailRecebido email;
	
	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			email = emailRecebidoService.get(id);
		}
	}
	
	public void download(ProcessoLogAnexo anexo) {
		File file = processoLogAnexoService.getFile(anexo);
		String nome = anexo.getNome();
		sendFile(file, nome);
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("email.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}
	
	public Long getId() {
		return id;
	}
	
	public Tela getOrigem() {
		return origem;
	}
	
	public EmailRecebido getEmail() {
		return email;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setOrigem(Tela origem) {
		this.origem = origem;
	}
}