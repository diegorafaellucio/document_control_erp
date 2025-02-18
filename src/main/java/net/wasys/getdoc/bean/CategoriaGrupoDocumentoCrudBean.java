package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.CategoriaGrupoModeloDocumentoDataModel;
import net.wasys.getdoc.domain.entity.CategoriaGrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.CategoriaGrupoModeloDocumentoService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class CategoriaGrupoDocumentoCrudBean extends AbstractBean {

	@Autowired
    private CategoriaGrupoModeloDocumentoService categoriaGrupoModeloDocumentoService;

	private CategoriaGrupoModeloDocumentoDataModel dataModel;
	private CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento;

	public void initBean() {
		dataModel = new CategoriaGrupoModeloDocumentoDataModel();
		dataModel.setService(categoriaGrupoModeloDocumentoService);
	}

	public void salvar() {
		try {
			boolean insert = isInsert(categoriaGrupoModeloDocumento);
			Usuario usuario = getUsuarioLogado();

			categoriaGrupoModeloDocumentoService.saveOrUpdate(categoriaGrupoModeloDocumento, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {
		Usuario usuarioLogado = getUsuarioLogado();
		Long modeloDocumentoId = categoriaGrupoModeloDocumento.getId();
		try {
			categoriaGrupoModeloDocumentoService.excluir(modeloDocumentoId, usuarioLogado);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public CategoriaGrupoModeloDocumentoDataModel getDataModel() {
		return dataModel;
	}

	public CategoriaGrupoModeloDocumento getCategoriaGrupoModeloDocumento() {
		return categoriaGrupoModeloDocumento;
	}

	public void setCategoriaGrupoModeloDocumento(CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento) {
		if(categoriaGrupoModeloDocumento == null) {
			categoriaGrupoModeloDocumento = new CategoriaGrupoModeloDocumento();
			this.categoriaGrupoModeloDocumento = categoriaGrupoModeloDocumento;
		} else {
			this.categoriaGrupoModeloDocumento = categoriaGrupoModeloDocumento;
		}
	}
}
