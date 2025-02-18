package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.AlunoDataModel;
import net.wasys.getdoc.domain.entity.Aluno;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.AlunoService;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class AlunoCrudBean extends AbstractBean {

	@Autowired private AlunoService alunoService;

	private AlunoDataModel dataModel;
	private Aluno aluno;

	private AlunoFiltro filtro;

	protected void initBean() {

		if(filtro == null) {
			filtro = new AlunoFiltro();
		}

		dataModel = new AlunoDataModel();
		dataModel.setService(alunoService);
		dataModel.setFiltro(filtro);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(aluno);
			Usuario usuario = getUsuarioLogado();

			alunoService.saveOrUpdate(aluno, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long alunoId = aluno.getId();

		try {
			alunoService.excluir(alunoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
			redirect("/cadastros/alunos/");
		}
		catch (Exception e) {
			aluno = alunoService.get(alunoId);
			addMessageError(e);
		}
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {

		if(aluno == null) {
			aluno = new Aluno();
		}
		else {
			Long alunoId = aluno.getId();
			aluno = alunoService.get(alunoId);
		}

		this.aluno = aluno;
	}

	public AlunoDataModel getDataModel() {
		return dataModel;
	}

	public AlunoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(AlunoFiltro filtro) {
		this.filtro = filtro;
	}
}
