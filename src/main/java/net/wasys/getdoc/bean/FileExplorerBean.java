package net.wasys.getdoc.bean;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class FileExplorerBean extends AbstractBean {

	private static final String KEY = "file_explorer_item";
	private String path = "/admin/util/file-explorer.xhtml";

	protected void initBean() {

		HttpSession session = getSession();
		HttpServletRequest request = getRequest();

		try {
			String pasta = (String) getSession().getAttribute(KEY);
			if(StringUtils.isBlank(pasta)) {
				File file = new File("/");
				pasta = file.getCanonicalPath();
			}

			File pastaFile = new File(pasta);

			String goc = getRequest().getParameter("goc");
			String god = getRequest().getParameter("god");
			String goparent = getRequest().getParameter("goparent");
			String item = getRequest().getParameter("item");
			String download = getRequest().getParameter("download");
			String excluir = getRequest().getParameter("excluir");


			if(StringUtils.isNotBlank(goc)) {

				pastaFile = new File("C:\\");
				pasta = pastaFile.getCanonicalPath();
				session.setAttribute(KEY, pasta);
				redirect(path);
				return;
			}
			else if(StringUtils.isNotBlank(god)) {

				pastaFile = new File("D:\\");
				pasta = pastaFile.getCanonicalPath();
				session.setAttribute(KEY, pasta);
				redirect(path);
				return;
			}
			else if(StringUtils.isNotBlank(goparent)) {

				String parent = pastaFile.getParent();
				pastaFile = new File(parent);
				pasta = pastaFile.getCanonicalPath();
				session.setAttribute(KEY, pasta);

				redirect(path);
				return;
			}
			else if(StringUtils.isNotBlank(item)) {

				pasta = pasta + File.separator + item;
				pastaFile = new File(pasta);
				session.setAttribute(KEY, pasta);

				redirect(path);
				return;
			}
			else if(StringUtils.isNotBlank(download)) {

				File downloadFile = new File(pasta + File.separator + download);
				String name = downloadFile.getName();

				sendFile(downloadFile, name);

				return;
			}
			else if(StringUtils.isNotBlank(excluir)) {

				File excluirFile = new File(pasta + File.separator + excluir);
				DummyUtils.deleteFile(excluirFile);

				redirect(path);
				return;
			}


			request.setAttribute("diretorioAtual", pasta);

			String parent = pastaFile.getParent();
			if(parent != null) {
				request.setAttribute("parent", parent);
			}

			File[] list1 = pastaFile.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
			if(list1 != null) {
				List<FileVO> listVOs = buildVOs(request, list1);
				request.setAttribute("listagemDiretorios", listVOs);
			}
			File[] list2 = pastaFile.listFiles((FileFilter) FileFileFilter.FILE);
			if(list2 != null) {
				List<FileVO> listVOs = buildVOs(request, list2);
				request.setAttribute("listagemArquivos", listVOs);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<FileVO> buildVOs(HttpServletRequest request, File[] list2) {

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

			String pasta = (String) getRequest().getSession().getAttribute(KEY);
			byte[] bytes = file.getContent();
			String name = file.getFileName();

			try {
				FileUtils.writeByteArrayToFile(new File(pasta + File.separator + name), bytes);

				FacesMessage message = new FacesMessage("Sucesso", event.getFile().getFileName() + " foi carregado com sucesso.");
				FacesContext.getCurrentInstance().addMessage(null, message);

			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void downloadZip() {

		HttpServletRequest request = getRequest();
		String[] arquivos = request.getParameterValues("arquivos");
		if (arquivos != null) {
			downloadZip(arquivos);
		}
		else {
			redirect(path);
		}
	}

	private void downloadZip(String[] arquivos) {

		String pasta = (String) getRequest().getSession().getAttribute(KEY);

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;

		try {
			File arquivoZip = File.createTempFile("downloadZip", ".zip");
			DummyUtils.deleteOnExitFile(arquivoZip);

			fos = new FileOutputStream(arquivoZip);
			bos = new BufferedOutputStream(fos, 2048);
			zos = new ZipOutputStream(bos);

			for (String arquivo : arquivos) {

				File file = new File(pasta, arquivo);

				ZipEntry ze = new ZipEntry(arquivo);
				zos.putNextEntry(ze);
				zos.setMethod(ZipOutputStream.DEFLATED);

				FileInputStream fis = new FileInputStream(file);
				IOUtils.copy(fis, zos);

				fis.close();
				zos.flush();
			}

			zos.close();
			sendFile(arquivoZip, "arquivos.zip");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if(zos != null) {
				try {
					zos.close();
				} catch( Exception e ) {}
			}
			if(bos != null) {
				try {
					bos.close();
				} catch( Exception e ) {}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch( Exception e ) {}
			}
		}
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
