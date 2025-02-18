package net.wasys.getdoc.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.service.CampoModeloOcrService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.ModeloOcrDataModel;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class ModeloOcrCrudBean extends AbstractBean {

	@Autowired private ModeloOcrService modeloOcrService;
	@Autowired private CampoModeloOcrService campoModeloOcrService;

	private ModeloOcrDataModel dataModel;
	private ModeloOcr modeloOcr;
	private String fileName;
	private File file;

	public void initBean() {

		dataModel = new ModeloOcrDataModel();
		dataModel.setService(modeloOcrService);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(modeloOcr);
			Usuario usuario = getUsuarioLogado();

			if(fileName != null) {
				modeloOcr.setNomeArquivo(fileName);
			}

			modeloOcrService.saveOrUpdate(modeloOcr, usuario, file);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void upload(FileUploadEvent event) {

		try {
			UploadedFile uploadedFile = event.getFile();
			this.fileName = uploadedFile.getFileName();

			this.file = DummyUtils.getFile("modeloocr-", uploadedFile);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long modeloOcrId = modeloOcr.getId();

		try {
			modeloOcrService.excluir(modeloOcrId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public ModeloOcrDataModel getDataModel() {
		return dataModel;
	}

	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {

		if(modeloOcr == null) {
			modeloOcr = new ModeloOcr();
		}

		this.modeloOcr = modeloOcr;
		this.fileName= modeloOcr.getNomeArquivo();
	}

	public String getFileName() {
		return fileName;
	}

	public int getTotalDeCampos(Long id) {
		return campoModeloOcrService.countByModeloOcr(id);
	}
}
