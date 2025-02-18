package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.GrupoModeloDocumentoDataModel;
import net.wasys.getdoc.domain.entity.CategoriaGrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.GrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.CategoriaGrupoModeloDocumentoService;
import net.wasys.getdoc.domain.service.GrupoModeloDocumentoService;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class GrupoModeloDocumentoCrudBean extends AbstractBean {

	@Autowired private GrupoModeloDocumentoService grupoModeloDocumentoService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private CategoriaGrupoModeloDocumentoService categoriaGrupoModeloDocumentoService;

	private GrupoModeloDocumentoDataModel dataModel;
	private GrupoModeloDocumento grupoModeloDocumento;

	private CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento;

	private List<CategoriaGrupoModeloDocumento> categoriasGrupoModeloDocumento;

	private List<ModeloDocumento> modelosDocumento;
	private List<ModeloDocumento> modelosDocumentosSelecionados;

	public void initBean() {

		dataModel = new GrupoModeloDocumentoDataModel();
		dataModel.setService(grupoModeloDocumentoService);

		modelosDocumento = modeloDocumentoService.findAtivos();
		categoriasGrupoModeloDocumento = categoriaGrupoModeloDocumentoService.findAll();
	}

	public void salvar() {

		try {


			boolean insert = isInsert(grupoModeloDocumento);
			Usuario usuario = getUsuarioLogado();

			grupoModeloDocumento.setCategoriaGrupoModeloDocumento(categoriaGrupoModeloDocumento);

			grupoModeloDocumentoService.saveOrUpdate(grupoModeloDocumento, usuario, modelosDocumentosSelecionados);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}
	


	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long modeloDocumentoId = grupoModeloDocumento.getId();

		try {
			grupoModeloDocumentoService.excluir(modeloDocumentoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public GrupoModeloDocumentoDataModel getDataModel() {
		return dataModel;
	}

	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {

		if(grupoModeloDocumento == null) {
			grupoModeloDocumento = new GrupoModeloDocumento();
			this.grupoModeloDocumento = grupoModeloDocumento;
		} else {
			Long modeloDocumetnoGrupoId = grupoModeloDocumento.getId();

			this.grupoModeloDocumento = grupoModeloDocumento;
			this.categoriaGrupoModeloDocumento = this.grupoModeloDocumento.getCategoriaGrupoModeloDocumento();
			this.modelosDocumentosSelecionados = grupoModeloDocumentoService.findModelosDocumento(modeloDocumetnoGrupoId);
		}
	}


	public List<ModeloDocumento> getModelosDocumento() {
		return modelosDocumento;
	}

	public void setModelosDocumento(List<ModeloDocumento> modelosDocumento) {
		this.modelosDocumento = modelosDocumento;
	}

	public List<ModeloDocumento> getModelosDocumentosSelecionados() {
		return modelosDocumentosSelecionados;
	}

	public void setModelosDocumentosSelecionados(List<ModeloDocumento> modelosDocumentosSelecionados) {
		this.modelosDocumentosSelecionados = modelosDocumentosSelecionados;
	}

	public List<CategoriaGrupoModeloDocumento> getCategoriasGrupoModeloDocumento() {
		return categoriasGrupoModeloDocumento;
	}

	public void setCategoriasGrupoModeloDocumento(List<CategoriaGrupoModeloDocumento> categoriasGrupoModeloDocumento) {
		this.categoriasGrupoModeloDocumento = categoriasGrupoModeloDocumento;
	}

	public CategoriaGrupoModeloDocumento getCategoriaGrupoModeloDocumento() {
		return categoriaGrupoModeloDocumento;
	}

	public void setCategoriaGrupoModeloDocumento(CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento) {
		this.categoriaGrupoModeloDocumento = categoriaGrupoModeloDocumento;
	}
}
