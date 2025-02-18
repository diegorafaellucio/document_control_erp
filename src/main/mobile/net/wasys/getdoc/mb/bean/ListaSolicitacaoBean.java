package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.service.ProcessoLogAnexoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.SolicitacaoService;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.ViewModel;
import net.wasys.getdoc.mb.regra.ProcessoAutorizacao;

@ManagedBean
@ViewScoped
public class ListaSolicitacaoBean extends MobileBean {

	@Autowired private ProcessoService processoService;
	@Autowired private SolicitacaoService solicitacaoService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	
	// Processo.id
	private Long id;

	private Tela origem;
	private Processo processo;
	private ProcessoAutorizacao regra;
	private List<SolicitacaoVO> rows;
	
	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			processo = processoService.get(id);
			regra = new ProcessoAutorizacao(usuario, processo, processoService);
			carregar();
		}
	}
	
	private void carregar() {
		rows = solicitacaoService.findVosByProcesso(id);
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("solicitacoes.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}
	
	public void download(ProcessoLogAnexo anexo) {
		File file = processoLogAnexoService.getFile(anexo);
		String nome = anexo.getNome();
		sendFile(file, nome);
	}
	
	public Long getId() {
		return id;
	}
	
	public Tela getOrigem() {
		return origem;
	}
	
	public ProcessoAutorizacao getRegra() {
		return regra;
	}
	
	public List<SolicitacaoVO> getRows() {
		return rows;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setOrigem(Tela origem) {
		this.origem = origem;
	}
}
