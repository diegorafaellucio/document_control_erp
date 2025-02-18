package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.CategoriaDocumentoDataModel;
import net.wasys.getdoc.domain.entity.CategoriaDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.CategoriaDocumentoService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class CategoriaDocumentoCrudBean extends AbstractBean {

	@Autowired private CategoriaDocumentoService categoriaDocumentoService;

	private CategoriaDocumentoDataModel dataModel;
	private CategoriaDocumento categoriaDocumento;

	public void initBean() {
		dataModel = new CategoriaDocumentoDataModel();
		dataModel.setService(categoriaDocumentoService);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(categoriaDocumento);
			Usuario usuario = getUsuarioLogado();

			categoriaDocumentoService.saveOrUpdate(categoriaDocumento, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long categoriaDocumentoId = categoriaDocumento.getId();
		try {
			categoriaDocumentoService.excluir(categoriaDocumentoId, usuarioLogado);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public CategoriaDocumentoDataModel getDataModel() {
		return dataModel;
	}

	public CategoriaDocumento getCategoriaDocumento() {
		return categoriaDocumento;
	}

	public void setCategoriaDocumento(CategoriaDocumento categoriaDocumento) {

		if(categoriaDocumento == null) {
			categoriaDocumento = new CategoriaDocumento();
		}

		this.categoriaDocumento = categoriaDocumento;
	}
}
