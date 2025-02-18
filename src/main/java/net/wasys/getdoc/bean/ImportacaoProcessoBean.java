package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.LogImportacaoDataModel;
import net.wasys.getdoc.domain.entity.LogImportacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import net.wasys.getdoc.domain.service.ImportacaoProcessoService;
import net.wasys.getdoc.domain.service.LogImportacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.omnifaces.util.Faces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@ManagedBean
@ViewScoped
public class ImportacaoProcessoBean extends AbstractBean {

	@Autowired ImportacaoProcessoService importacaoProcessoService;
	@Autowired LogImportacaoService logImportacaoService;
	@Autowired TipoProcessoService tipoProcessoService;

	private LogImportacaoDataModel dataModel;
	private LogImportacaoFiltro filtro;
	private String fileNameImportacao;
	private File fileExcelImportacao;
	private String fileNameReimportacao;
	private File fileExcelReimportacao;
	private List<TipoProcesso> tipoProcessoList;
	private LogImportacao logSelecionado;
	private LogImportacao logModeloFies;
	private LogImportacao logModeloProuni;
	private boolean temImportacaoEmProcessamento = false;

	protected void initBean() {

		if(filtro == null) {
			filtro = new LogImportacaoFiltro();
		}

		filtro.setTipoImportacao(TipoImportacao.PROCESSO);

		dataModel = new LogImportacaoDataModel();
		dataModel.setService(logImportacaoService);
		dataModel.setFiltro(filtro);

		tipoProcessoList = tipoProcessoService.findByIds(Arrays.asList(TipoProcesso.SIS_FIES, TipoProcesso.SIS_PROUNI));

		verificarImportacaoEmProcessamento();
		carregaModelos();
	}

	public void importarArquivo() {
		try {

			importacaoProcessoService.iniciarProcessamentoDoArquivo(fileExcelImportacao, getUsuarioLogado(), fileNameImportacao, false);

			addMessage("arquivoCarregado.sucesso");

			HttpServletRequest request = getRequest();
			String requestURI = request.getRequestURI();
			redirect(requestURI);
		}
		catch(Exception e) {
			addMessageError(e);
		}
	}

	public void reimportarArquivo() {
		try {

			importacaoProcessoService.iniciarProcessamentoDoArquivo(fileExcelReimportacao, getUsuarioLogado(), fileNameReimportacao, true);

			addMessage("arquivoCarregado.sucesso");

			HttpServletRequest request = getRequest();
			String requestURI = request.getRequestURI();
			redirect(requestURI);
		}
		catch(Exception e) {
			addMessageError(e);
		}
	}

	public void limpar() {
		filtro = new LogImportacaoFiltro();
		initBean();
	}

	public void upload(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileNameImportacao = fileName;
			this.fileExcelImportacao = File.createTempFile(fileName, "." + extensao);
			DummyUtils.deleteOnExitFile(this.fileExcelImportacao);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, fileExcelImportacao);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void uploadReimportacao(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileNameReimportacao = fileName;
			this.fileExcelReimportacao = File.createTempFile(fileName, "." + extensao);
			DummyUtils.deleteOnExitFile(this.fileExcelReimportacao);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, fileExcelReimportacao);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void downloadExcel(LogImportacao logImportacao) {

		String pathArquivo = logImportacao.getPathArquivo();
		File file = new File(pathArquivo);

		if(file != null) {
			String nomeArquivo = logImportacao.getNomeArquivo();

			FileInputStream fis;
			try {
				File file1 = importacaoProcessoService.criaColunaProcessoId(file, nomeArquivo, logImportacao);
				fis = new FileInputStream(file1);
				Faces.sendFile(fis, nomeArquivo, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		addMessage("arquivoCarregado.sucesso");
	}

	private void carregaModelos() {
		LogImportacaoFiltro filtroModelo = new LogImportacaoFiltro();
		filtroModelo.setTipoImportacao(TipoImportacao.MODELO);

		carregaModeloFies(filtroModelo);
		carregaModeloProuni(filtroModelo);
	}

	private void carregaModeloProuni(LogImportacaoFiltro filtroModelo) {
		TipoProcesso tipoProcesso = new TipoProcesso(TipoProcesso.SIS_PROUNI);
		filtroModelo.setTipoProcesso(tipoProcesso);
		LogImportacao logSisProuni = logImportacaoService.getLastByFiltro(filtroModelo);
		this.logModeloProuni = logSisProuni;
	}

	private void carregaModeloFies(LogImportacaoFiltro filtroModelo) {
		TipoProcesso tipoProcesso = new TipoProcesso(TipoProcesso.SIS_FIES);
		filtroModelo.setTipoProcesso(tipoProcesso);
		LogImportacao logSisFies = logImportacaoService.getLastByFiltro(filtroModelo);
		this.logModeloFies = logSisFies;
	}

	public String getFileNameImportacao() {
		return fileNameImportacao;
	}

	public LogImportacao getLogSelecionado() {
		return logSelecionado;
	}

	public void setLogSelecionado(LogImportacao logSelecionado) {
		this.logSelecionado = logSelecionado;
	}

	public LogImportacaoDataModel getDataModel() {
		return dataModel;
	}

	public LogImportacaoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(LogImportacaoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoProcesso> getTipoProcessoList() {
		return tipoProcessoList;
	}

	public void verificarImportacaoEmProcessamento() {
		temImportacaoEmProcessamento =	 logImportacaoService.temImportacaoEmProcessamento();
	}

	public boolean isTemImportacaoEmProcessamento() {
		return temImportacaoEmProcessamento;
	}

	public LogImportacao getLogModeloFies() {
		return logModeloFies;
	}

	public void setLogModeloFies(LogImportacao logModeloFies) {

		this.logModeloFies = logModeloFies;
	}

	public LogImportacao getLogModeloProuni() {
		return logModeloProuni;
	}

	public void setLogModeloProuni(LogImportacao logModeloProuni) {
		this.logModeloProuni = logModeloProuni;
	}
}
