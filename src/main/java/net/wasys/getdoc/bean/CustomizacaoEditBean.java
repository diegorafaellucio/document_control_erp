package net.wasys.getdoc.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class CustomizacaoEditBean extends AbstractBean {

	@Autowired private ParametroService parametroService;

	private Map<String, String> map;

	private String fileName;

	private File file;

	protected void initBean() {

		map = parametroService.getCustomizacao();
	}

	public void salvar() {

		try {
			parametroService.salvarCustomizacao(map, file);

			addMessage("registroAlterado.sucesso");
			redirect("/admin/customizacoes/");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void restaurarPadrao() {

		try {
			parametroService.restaurarPadrao();

			addMessage("registroAlterado.sucesso");
			redirect("/admin/customizacoes/");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void upload(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileName = fileName;
			this.file = File.createTempFile("logo-", "." + extensao);
			DummyUtils.deleteOnExitFile(this.file);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, file);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
