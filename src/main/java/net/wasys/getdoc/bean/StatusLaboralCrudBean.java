package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.StatusLaboralDataModel;
import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.service.StatusLaboralService;
import net.wasys.getdoc.domain.vo.filtro.StatusLaboralFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Arrays;
import java.util.List;

;

@ManagedBean
@ViewScoped
public class StatusLaboralCrudBean extends AbstractBean {

	@Autowired private StatusLaboralService statusLaboralService;

	private StatusLaboralFiltro filtro = new StatusLaboralFiltro();
	private StatusLaboral statusLaboral;
	private List<StatusAtendimento> statusAtendimentoList;

	private StatusLaboralDataModel dataModel;

	protected void initBean() {
		dataModel = new StatusLaboralDataModel();
		dataModel.setService(statusLaboralService);
		dataModel.setFiltro(filtro);

		statusAtendimentoList = Arrays.asList(StatusAtendimento.values());
	}

	public void salvar() {

		try {
			boolean insert = isInsert(statusLaboral);
			Usuario usuario = getUsuarioLogado();

			statusLaboralService.saveOrUpdate(statusLaboral, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long situacaoId = statusLaboral.getId();

		try {
			statusLaboralService.excluir(situacaoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
			redirect("/situacoes-atendimento/");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		statusLaboral = null;
	}

	public StatusLaboral getStatusLaboral() {
		return statusLaboral;
	}

	public void setStatusLaboral(StatusLaboral statusLaboral) {
		if (statusLaboral == null) {
			statusLaboral = new StatusLaboral();
		}
		this.statusLaboral = statusLaboral;
	}

	public List<StatusAtendimento> getStatusAtendimentoList() {
		return statusAtendimentoList;
	}

	public void setStatusAtendimentoList(List<StatusAtendimento> statusAtendimentoList) {
		this.statusAtendimentoList = statusAtendimentoList;
	}

	public StatusLaboralFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(StatusLaboralFiltro filtro) {
		this.filtro = filtro;
	}

	public StatusLaboralDataModel getDataModel() {
		return dataModel;
	}
}
