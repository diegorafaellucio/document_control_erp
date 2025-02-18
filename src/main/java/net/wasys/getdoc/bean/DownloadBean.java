package net.wasys.getdoc.bean;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.wasys.getdoc.domain.service.ParametroService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class DownloadBean extends AbstractBean {

	@Autowired private ResourceService resourceService;
	@Autowired private ParametroService parametroService;

	private boolean senhaOk = false;
	private String pasta;

	protected void initBean() {

		HttpServletRequest request = getRequest();

		String pasta = resourceService.getValue(ResourceService.DOWNLOAD_PATH);
		File pastaFile = new File(pasta);
		if(StringUtils.isNotBlank(this.pasta)) {
			pastaFile = new File(pastaFile, this.pasta);
		}

		String senha = request.getParameter("senha");
		senhaOk = new SimpleDateFormat("ddMM").format(new Date()).equals(senha);

		String download = request.getParameter("download");
		String excluir = request.getParameter("excluir");

		if(StringUtils.isNotBlank(download)) {

			File downloadFile = new File(pastaFile, download);
			String name = downloadFile.getName();

			sendFile(downloadFile, name);

			return;
		}
		else if(StringUtils.isNotBlank(excluir)) {

			File excluirFile = new File(pastaFile, excluir);
			DummyUtils.deleteFile(excluirFile);

			redirect("/download/" + (this.pasta != null ? this.pasta : ""));
			return;
		}

		request.setAttribute("diretorioAtual", pasta);

		String parent = pastaFile.getParent();
		if(parent != null) {
			request.setAttribute("parent", parent);
		}

		File[] list2 = pastaFile.listFiles((FileFilter) FileFileFilter.FILE);
		if(list2 != null) {
			List<FileVO> listVOs = buildVOs(request, list2);
			request.setAttribute("listagemArquivos", listVOs);
		}
	}

	private List<FileVO> buildVOs(HttpServletRequest request, File[] list2) {

		Arrays.sort(list2, (o1, o2) -> ((Long) o2.lastModified()).compareTo(o1.lastModified()));

		List<FileVO> listVOs = new ArrayList<>();
		for (File file : list2) {

			String name = file.getName();
			long length = file.length();
			String size = DummyUtils.toFileSize(length);
			long lastModified = file.lastModified();
			Date lastModifiedDate = new Date(lastModified);
			String lastModifiedDateStr = DummyUtils.formatDateTime(lastModifiedDate);

			FileVO vo = new FileVO();
			vo.setName(name);
			vo.setSize(size);
			vo.setLastModifiedDate(lastModifiedDateStr);
			listVOs.add(vo);
		}

		return listVOs;
	}

	public void uploadEvent(FileUploadEvent event) {

		UploadedFile file = event.getFile();
		if(file != null) {

			String pasta = resourceService.getValue(ResourceService.DOWNLOAD_PATH);
			byte[] bytes = file.getContent();
			String name = file.getFileName();

			try {
				parametroService.validarArquivoBloqueado(name);

				FileUtils.writeByteArrayToFile(new File(pasta + File.separator + name), bytes);

				FacesMessage message = new FacesMessage("Sucesso", event.getFile().getFileName() + " foi carregado com sucesso.");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
			catch (Exception e) {
				addMessageError(e);
			}
		}

		redirect("/download/" + (this.pasta != null ? this.pasta : ""));
	}

	public boolean getSenhaOk() {
		return senhaOk;
	}

	public String getPasta() {
		return pasta;
	}

	public void setPasta(String pasta) {
		this.pasta = pasta;
	}

	public static class FileVO {

		private String name;
		private String lastModifiedDate;
		private String size;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLastModifiedDate() {
			return lastModifiedDate;
		}

		public void setLastModifiedDate(String lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}
	}
}
