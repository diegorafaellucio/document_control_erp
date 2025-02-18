package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.AdminAjudaDataModel;
import net.wasys.getdoc.domain.entity.AdminAjuda;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.AdminAjudaService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.DownloadVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.filtro.AdminAjudaFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.omnifaces.util.Faces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class AdminAjudaBean extends AbstractBean {

	@Autowired private AdminAjudaService adminAjudaService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ParametroService parametroService;
	@Autowired private ResourceService resourceService;

	private AdminAjudaFiltro filtro = new AdminAjudaFiltro();
	private AdminAjudaDataModel dataModel = new AdminAjudaDataModel();
	private FileVO fileVO;

	private AdminAjuda adminAjuda;
	private boolean podeCriar;
	private Long ajudaId;

	protected void initBean() {

		adminAjuda = new AdminAjuda();
		ajudaId = null;
		fileVO = null;

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();

		podeCriar = false;
		if(RoleGD.GD_GESTOR.equals(roleGD) || RoleGD.GD_ADMIN.equals(roleGD)) {
			podeCriar = true;
		}

		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem==null || campoOrdem.isEmpty()) {
			filtro.setCampoOrdem("admin_ajuda.dataCriacao");
			filtro.setOrdem(SortOrder.DESCENDING);
		}

		dataModel.setFiltro(filtro);
		dataModel.setService(adminAjudaService);
	}

	public void uploadAnexo(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		try{
			parametroService.validarArquivoPermitido(fileName);
		} catch (Exception e) {
			addMessageError(e);
			return;
		}

		File tmpFile = getFile(uploadedFile);
		if(tmpFile != null) {
			addMessage("arquivoCarregado.sucesso");
		}

		fileVO.setFile(tmpFile);
		fileVO.setName(fileName);
		fileVO.setLength(String.valueOf(tmpFile.getTotalSpace()));
	}

	public void removerAnexo() {
		File file = fileVO.getFile();
		DummyUtils.deleteFile(file);
		this.fileVO = null;
	}

	private File getFile(UploadedFile uploadedFile) {

		try {
			File tmpFile = File.createTempFile("anexo-adminAjuda-", ".tmp");
			DummyUtils.deleteOnExitFile(tmpFile);

			InputStream is = uploadedFile.getInputStream();
			FileUtils.copyInputStreamToFile(is, tmpFile);

			return tmpFile;
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}

		return null;
	}

	public void criarAjuda(){
       File tmpFile = fileVO.getFile();
       String name = fileVO.getName();
       int lenght = name.length();
       String extensao = name.substring(lenght-3, lenght);
       try {
            Usuario usuario = getUsuarioLogado();
            adminAjuda.setTamanho(tmpFile.length());
            adminAjuda.setExtensao(extensao);
            adminAjuda.setDataCriacao(new Date());
            adminAjuda.setAnalista(usuario);
            adminAjudaService.saveOrUpdate(adminAjuda, usuario);
            String dir = resourceService.getValue(ResourceService.ADMINAJUDA_PATH);
            String caminho = AdminAjuda.gerarCaminho(dir, adminAjuda);
            FileUtils.copyFile(tmpFile, new File(caminho));
            String hash = DummyUtils.getHashChecksum(new File(caminho));
            adminAjuda.setCaminho(caminho);
            adminAjuda.setHashChecksum(hash);
            adminAjudaService.saveOrUpdate(adminAjuda, usuario);
		    addMessage("registroCadastrado.sucesso");
        }
        catch (Exception e) {
       		String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
       		addMessageError("erroInesperado.error", rootCauseMessage);
        }

       initBean();
	}

	public void deletaAjuda() {
		String ajudaIdStr = Faces.getRequestParameter("ajudaId");
		Long adminAjudaId = new Long(ajudaIdStr);
		Usuario usuario = getUsuarioLogado();
		adminAjudaService.excluir(adminAjudaId, usuario);
		addMessage("registroExcluido.sucesso");
		initBean();

	}

	public void carregaCriarAjuda() {
		adminAjuda = new AdminAjuda();
		fileVO = new FileVO();
	}

    public void downloadDocumentoPrev() {
        String ajudaIdStr = Faces.getRequestParameter("ajudaId");
        this.ajudaId = new Long(ajudaIdStr);
    }

	public StreamedContent downloadDocumento() {

		try {
			DownloadVO downloadVO = adminAjudaService.getDownload(ajudaId);

			File file = downloadVO.getFile();
			DummyUtils.deleteOnExitFile(file);
			String fileName = downloadVO.getFileName();


			FileInputStream fis = new FileInputStream(file);
			DefaultStreamedContent dsc =
					DefaultStreamedContent.builder()
							.contentType("application/csv")
							.name(fileName)
							.stream(() -> fis)
							.build();
			return dsc;
		}
		catch (Exception e) {
			addMessageError(e);
			return null;
		}
	}

	public void buscar(){
		initBean();
	}

	public void limpar() {
		filtro = new AdminAjudaFiltro();
		initBean();
	}

	public AdminAjudaFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(AdminAjudaFiltro filtro) {
		this.filtro = filtro;
	}

	public AdminAjuda getAdminAjuda() {
		return adminAjuda;
	}

	public void setAdminAjuda(AdminAjuda adminAjuda) {
		this.adminAjuda = adminAjuda;
	}

	public boolean isPodeCriar() {
		return podeCriar;
	}

	public FileVO getFileVO() {
		return fileVO;
	}

	public void setFileVO(FileVO fileVO) {
		this.fileVO = fileVO;
	}

	public AdminAjudaDataModel getDataModel() {
		return dataModel;
	}
}
